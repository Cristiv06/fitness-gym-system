import { useEffect, useMemo, useRef, useState } from "react";
import {
  createAdminAccount,
  createTrainerClass,
  enrollToClass,
  getCurrentSession,
  getTrainerClassesForMember,
  login,
  logout,
  registerNormalAccount
} from "./api/auth";
import { apiFetch } from "./api/http";
import { resources } from "./config/resources";

const USER_SECTIONS = [
  {
    key: "my-classes",
    label: "Clasele mele",
    endpoint: "/api/auth/me/classes",
    idField: "classId",
    columns: [
      { key: "title", label: "Titlu" },
      { key: "startTime", label: "Inceput" },
      { key: "endTime", label: "Sfarsit" },
      { key: "maxParticipants", label: "Locuri max" }
    ]
  },
  {
    key: "my-subscriptions",
    label: "Abonamentele mele",
    endpoint: "/api/auth/me/subscriptions",
    idField: "subscriptionId",
    columns: [
      { key: "planName", label: "Plan" },
      { key: "startDate", label: "Data start" },
      { key: "endDate", label: "Data sfarsit" },
      { key: "status", label: "Status" }
    ]
  },
  {
    key: "trainer-classes",
    label: "Clase antrenori",
    endpoint: "/api/auth/me/trainer-classes",
    idField: "classId",
    columns: [
      { key: "classId", label: "ID Clasa" },
      { key: "title", label: "Titlu" },
      { key: "trainerId", label: "Trainer ID" },
      { key: "startTime", label: "Inceput" },
      { key: "endTime", label: "Sfarsit" }
    ]
  }
];

function emptyForm(resource) {
  return resource.fields.reduce((acc, field) => {
    acc[field.key] = "";
    return acc;
  }, {});
}

function formatForInput(value, type) {
  if (value == null) return "";
  if (type === "csv-number-array" && Array.isArray(value)) return value.join(", ");
  if (type === "datetime-local") return String(value).slice(0, 16);
  return String(value);
}

function toPayload(value, type) {
  if (value === "") return null;
  if (type === "number" || type === "plan-select") return Number(value);
  if (type === "csv-number-array") {
    return value.split(",").map((v) => v.trim()).filter(Boolean).map(Number);
  }
  return value;
}

function formatCell(value) {
  if (value == null) return "";
  if (Array.isArray(value)) return value.join(", ");
  return String(value);
}

function validateForm(resource, formData) {
  for (const field of resource.fields) {
    const value = formData[field.key];
    if (field.required && !String(value || "").trim()) return `${field.label} este obligatoriu.`;
    if ((field.type === "number" || field.type === "plan-select") && value !== "" && Number.isNaN(Number(value))) {
      return `${field.label} trebuie sa fie un numar valid.`;
    }
    if (field.type === "email" && value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
      return `${field.label} nu este o adresa de email valida.`;
    }
  }
  const startDate = formData.startDate || formData.startTime;
  const endDate = formData.endDate || formData.endTime;
  if (startDate && endDate && endDate < startDate) {
    return "Data/ora de sfarsit trebuie sa fie dupa data/ora de start.";
  }
  return "";
}

function clientSortAndFilter(rows, columns, searchTerm, sortField, sortDir) {
  let result = rows;
  const term = searchTerm.trim().toLowerCase();
  if (term) {
    result = result.filter((row) =>
      columns.some((col) => formatCell(row[col]).toLowerCase().includes(term))
    );
  }
  if (sortField) {
    result = [...result].sort((a, b) => {
      const av = a[sortField];
      const bv = b[sortField];
      if (av == null && bv == null) return 0;
      if (av == null) return 1;
      if (bv == null) return -1;
      const cmp =
        typeof av === "number" && typeof bv === "number"
          ? av - bv
          : String(av).localeCompare(String(bv));
      return sortDir === "desc" ? -cmp : cmp;
    });
  }
  return result;
}

function SortableHeader({ column, label, sortField, sortDir, onSort }) {
  const active = sortField === column;
  return (
    <th className="sortable-th" onClick={() => onSort(column)}>
      {label ?? column}
      <span className="sort-indicator">{active ? (sortDir === "asc" ? " ▲" : " ▼") : " ↕"}</span>
    </th>
  );
}

function PaginationBar({ page, totalPages, total, onPrev, onNext }) {
  return (
    <div className="pagination">
      <button type="button" className="secondary-btn" onClick={onPrev} disabled={page === 1}>
        Prev
      </button>
      <span>
        Pagina {page} / {totalPages} &middot; {total} inregistrari
      </span>
      <button
        type="button"
        className="secondary-btn"
        onClick={onNext}
        disabled={page === totalPages}
      >
        Next
      </button>
    </div>
  );
}

function CrudPanel({ resource, canWrite, roleLabel }) {
  const pageSize = 7;
  const loadIdRef = useRef(0);

  const [rows, setRows] = useState([]);
  const [serverTotalPages, setServerTotalPages] = useState(1);
  const [serverTotalRows, setServerTotalRows] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [formData, setFormData] = useState(() => emptyForm(resource));
  const [editingId, setEditingId] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [sortField, setSortField] = useState(null);
  const [sortDir, setSortDir] = useState("asc");
  const [page, setPage] = useState(1);
  const [toast, setToast] = useState("");
  const [plans, setPlans] = useState([]);

  const useServerPaging = Boolean(resource.pageEndpoint);

  const columns = useMemo(() => {
    const preferred = [resource.idField, ...resource.fields.map((f) => f.key)];
    return [...new Set(preferred)];
  }, [resource]);

  async function loadDataWith(sf, sd, pg) {
    const thisId = ++loadIdRef.current;
    setLoading(true);
    setError("");
    try {
      if (useServerPaging) {
        const qs = new URLSearchParams({ page: pg - 1, size: pageSize });
        if (sf) qs.set("sort", `${sf},${sd}`);
        const json = await apiFetch(`${resource.pageEndpoint}?${qs}`);
        if (thisId !== loadIdRef.current) return;
        setRows(json.content ?? []);
        setServerTotalPages(json.totalPages || 1);
        setServerTotalRows(json.totalElements || 0);
      } else {
        const data = await apiFetch(resource.endpoint);
        if (thisId !== loadIdRef.current) return;
        setRows(Array.isArray(data) ? data : []);
      }
    } catch (e) {
      if (thisId !== loadIdRef.current) return;
      setError(e.message);
    } finally {
      if (thisId !== loadIdRef.current) return;
      setLoading(false);
    }
  }

  function loadData() {
    return loadDataWith(sortField, sortDir, page);
  }

  useEffect(() => {
    setFormData(emptyForm(resource));
    setEditingId(null);
    setSearchTerm("");
    setSortField(null);
    setSortDir("asc");
    setPage(1);
    setRows([]);
    setServerTotalPages(1);
    setServerTotalRows(0);
    loadDataWith(null, "asc", 1);
    if (resource.key === "subscriptions") {
      apiFetch("/api/membership-plans").then((data) => setPlans(Array.isArray(data) ? data : [])).catch(() => {});
    } else {
      setPlans([]);
    }
  }, [resource.key]);

  function toggleSort(col) {
    const newField = col;
    const newDir = col === sortField ? (sortDir === "asc" ? "desc" : "asc") : "asc";
    setSortField(newField);
    setSortDir(newDir);
    setPage(1);
    if (useServerPaging) {
      loadDataWith(newField, newDir, 1);
    }
  }

  function goToPage(newPage) {
    setPage(newPage);
    if (useServerPaging) {
      loadDataWith(sortField, sortDir, newPage);
    }
  }

  useEffect(() => {
    if (page > displayTotalPages) setPage(displayTotalPages);
  });

  function handleChange(key, value) {
    setFormData((prev) => {
      const next = { ...prev, [key]: value };
      if (resource.key === "subscriptions" && (key === "planId" || key === "startDate")) {
        const planId = key === "planId" ? value : prev.planId;
        const startDate = key === "startDate" ? value : prev.startDate;
        if (planId && startDate) {
          const plan = plans.find((p) => String(p.planId) === String(planId));
          if (plan) {
            const d = new Date(startDate);
            d.setMonth(d.getMonth() + plan.durationMonths);
            next.endDate = d.toISOString().slice(0, 10);
          }
        }
      }
      return next;
    });
  }

  function startEdit(row) {
    const next = emptyForm(resource);
    for (const field of resource.fields) {
      next[field.key] = formatForInput(row[field.key], field.type);
    }
    setFormData(next);
    setEditingId(row[resource.idField]);
  }

  function clearForm() {
    setFormData(emptyForm(resource));
    setEditingId(null);
  }

  async function onSubmit(event) {
    event.preventDefault();
    if (!canWrite) { setError(`Rolul ${roleLabel} poate doar citi (GET).`); return; }
    const msg = validateForm(resource, formData);
    if (msg) { setError(msg); return; }
    const payload = {};
    for (const field of resource.fields) {
      const converted = toPayload(formData[field.key], field.type);
      if (converted !== null) payload[field.key] = converted;
    }
    try {
      if (editingId == null) {
        await apiFetch(resource.endpoint, { method: "POST", body: JSON.stringify(payload) });
      } else {
        await apiFetch(`${resource.endpoint}/${editingId}`, { method: "PUT", body: JSON.stringify(payload) });
      }
      clearForm();
      await loadDataWith(sortField, sortDir, page);
      setToast(editingId == null ? "Element creat cu succes." : "Element actualizat cu succes.");
      setTimeout(() => setToast(""), 2500);
    } catch (e) {
      setError(e.message);
    }
  }

  async function onDelete(id) {
    if (!canWrite) { setError(`Rolul ${roleLabel} poate doar citi (GET).`); return; }
    try {
      await apiFetch(`${resource.endpoint}/${id}`, { method: "DELETE" });
      await loadDataWith(sortField, sortDir, page);
      setToast("Element sters cu succes.");
      setTimeout(() => setToast(""), 2500);
    } catch (e) {
      setError(e.message);
    }
  }

  const processedRows = useMemo(() => {
    if (useServerPaging) {
      const term = searchTerm.trim().toLowerCase();
      if (!term) return rows;
      return rows.filter((row) =>
        columns.some((col) => formatCell(row[col]).toLowerCase().includes(term))
      );
    }
    return clientSortAndFilter(rows, columns, searchTerm, sortField, sortDir);
  }, [rows, columns, searchTerm, sortField, sortDir, useServerPaging]);

  const displayTotalPages = useServerPaging
    ? serverTotalPages
    : Math.max(1, Math.ceil(processedRows.length / pageSize));

  const displayTotalRows = useServerPaging ? serverTotalRows : processedRows.length;

  const pageRows = useServerPaging
    ? processedRows
    : processedRows.slice((page - 1) * pageSize, page * pageSize);

  return (
    <div className="panel">
      <div className="panel-header">
        <div>
          <h2>{resource.label}</h2>
          <p className="subtle-text">
            {useServerPaging ? "Sortare si paginare server-side." : "Gestionare completa pentru resursa selectata."}
          </p>
        </div>
        <div className="panel-header-actions">
          <input
            className="search-input"
            placeholder="Cauta in tabel..."
            value={searchTerm}
            onChange={(e) => { setSearchTerm(e.target.value); }}
          />
          <button onClick={loadData} className="secondary-btn" type="button">Refresh</button>
        </div>
      </div>

      <div className="stats-row">
        <span className="stat-chip">Rol: {roleLabel}</span>
        <span className="stat-chip">
          {useServerPaging ? `Total DB: ${serverTotalRows}` : `Total: ${rows.length}`}
        </span>
        {useServerPaging && <span className="stat-chip server-badge">Backend sort ✓</span>}
      </div>

      {toast ? <p className="success-text">{toast}</p> : null}
      {error ? <p className="error-text">{error}</p> : null}

      <form className="form-grid" onSubmit={onSubmit}>
        {resource.fields.map((field) => (
          <label key={field.key} className="field">
            <span>{field.label}</span>
            {field.type === "textarea" ? (
              <textarea
                value={formData[field.key]}
                onChange={(e) => handleChange(field.key, e.target.value)}
                required={field.required}
                disabled={!canWrite}
              />
            ) : field.type === "select" ? (
              <select
                value={formData[field.key]}
                onChange={(e) => handleChange(field.key, e.target.value)}
                required={field.required}
                disabled={!canWrite}
              >
                <option value="">Select...</option>
                {field.options.map((opt) => (
                  <option key={opt} value={opt}>{opt}</option>
                ))}
              </select>
            ) : field.type === "plan-select" ? (
              <select
                value={formData[field.key]}
                onChange={(e) => handleChange(field.key, e.target.value)}
                required={field.required}
                disabled={!canWrite}
              >
                <option value="">Selecteaza plan...</option>
                {plans.map((p) => (
                  <option key={p.planId} value={p.planId}>
                    {p.name} — {p.durationMonths} luni — {p.price} RON
                  </option>
                ))}
              </select>
            ) : (
              <input
                type={
                  field.type === "csv-number-array" ? "text"
                  : field.type === "datetime-local" ? "datetime-local"
                  : field.type === "email" ? "email"
                  : field.type === "date" ? "date"
                  : "text"
                }
                step={field.step}
                value={formData[field.key]}
                onChange={(e) => handleChange(field.key, e.target.value)}
                required={field.required}
                readOnly={field.key === "endDate" && resource.key === "subscriptions" && Boolean(formData.planId)}
                disabled={!canWrite}
              />
            )}
          </label>
        ))}
        <div className="form-actions">
          <button type="submit" disabled={!canWrite}>
            {editingId == null ? "Create" : "Update"}
          </button>
          <button onClick={clearForm} type="button" className="secondary-btn" disabled={!canWrite}>
            Reset
          </button>
        </div>
      </form>

      <div className="table-wrap">
        {loading ? <p className="subtle-text">Se incarca datele...</p> : null}
        <table>
          <thead>
            <tr>
              {columns.map((col) => (
                <SortableHeader
                  key={col}
                  column={col}
                  sortField={sortField}
                  sortDir={sortDir}
                  onSort={toggleSort}
                />
              ))}
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {pageRows.map((row) => (
              <tr key={row[resource.idField]}>
                {columns.map((col) => (
                  <td key={`${row[resource.idField]}-${col}`}>{formatCell(row[col])}</td>
                ))}
                <td className="actions-cell">
                  <button type="button" className="secondary-btn" onClick={() => startEdit(row)} disabled={!canWrite}>Edit</button>
                  <button type="button" className="danger-btn" onClick={() => onDelete(row[resource.idField])} disabled={!canWrite}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {!loading && pageRows.length === 0 ? (
          <p className="empty-text">Nu exista rezultate pentru filtrele curente.</p>
        ) : null}
        <PaginationBar
          page={page}
          totalPages={displayTotalPages}
          total={displayTotalRows}
          onPrev={() => goToPage(Math.max(1, page - 1))}
          onNext={() => goToPage(Math.min(displayTotalPages, page + 1))}
        />
      </div>
    </div>
  );
}

function UserPortal({ onLogout, me }) {
  const isTrainerOnly = Boolean(me?.trainerId) && !me?.memberId;
  const availableSections = useMemo(() => {
    if (isTrainerOnly) {
      return USER_SECTIONS.filter((section) => section.key === "my-classes");
    }
    return USER_SECTIONS;
  }, [isTrainerOnly]);

  const [activeSectionKey, setActiveSectionKey] = useState(availableSections[0].key);
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [toast, setToast] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [sortField, setSortField] = useState(null);
  const [sortDir, setSortDir] = useState("asc");
  const [page, setPage] = useState(1);
  const [trainerClassForm, setTrainerClassForm] = useState({
    roomId: "",
    title: "",
    startTime: "",
    endTime: "",
    maxParticipants: ""
  });
  const [rooms, setRooms] = useState([]);
  const [enrolledClassIds, setEnrolledClassIds] = useState(new Set());
  const loadIdRef = useRef(0);
  const pageSize = 8;

  const section = availableSections.find((s) => s.key === activeSectionKey) ?? availableSections[0];
  const colKeys = section.columns.map((c) => c.key);

  function toggleSort(col) {
    const newDir = col === sortField ? (sortDir === "asc" ? "desc" : "asc") : "asc";
    setSortField(col);
    setSortDir(newDir);
    setPage(1);
  }

  async function loadData() {
    const thisId = ++loadIdRef.current;
    setLoading(true);
    setError("");
    setRows([]);
    try {
      if (section.key === "trainer-classes") {
        const [classes, enrollments] = await Promise.all([
          getTrainerClassesForMember(),
          apiFetch("/api/auth/me/enrollments")
        ]);
        if (thisId !== loadIdRef.current) return;
        setEnrolledClassIds(new Set((enrollments || []).map((e) => e.classId)));
        setRows(Array.isArray(classes) ? classes : []);
      } else {
        const data = await apiFetch(section.endpoint);
        if (thisId !== loadIdRef.current) return;
        setRows(Array.isArray(data) ? data : []);
      }
    } catch (e) {
      if (thisId !== loadIdRef.current) return;
      setError(e.message);
    } finally {
      if (thisId !== loadIdRef.current) return;
      setLoading(false);
    }
  }

  async function handleEnroll(classId) {
    setError("");
    setToast("");
    try {
      await enrollToClass(classId);
      setToast("Inscriere realizata cu succes.");
      await loadData();
    } catch (e) {
      setError(e.message);
    }
  }

  async function handleTrainerCreateClass(event) {
    event.preventDefault();
    setError("");
    setToast("");
    if (trainerClassForm.startTime && trainerClassForm.endTime && trainerClassForm.endTime <= trainerClassForm.startTime) {
      setError("Ora de sfarsit trebuie sa fie dupa ora de start.");
      return;
    }
    if (!trainerClassForm.roomId) {
      setError("Selecteaza o sala.");
      return;
    }
    try {
      await createTrainerClass({
        roomId: Number(trainerClassForm.roomId),
        title: trainerClassForm.title,
        startTime: trainerClassForm.startTime,
        endTime: trainerClassForm.endTime,
        maxParticipants: Number(trainerClassForm.maxParticipants)
      });
      setToast("Clasa creata cu succes.");
      setTrainerClassForm({
        roomId: "",
        title: "",
        startTime: "",
        endTime: "",
        maxParticipants: ""
      });
      await loadData();
    } catch (e) {
      setError(e.message);
    }
  }

  useEffect(() => {
    if (isTrainerOnly && rooms.length === 0) {
      apiFetch("/api/rooms").then((data) => setRooms(Array.isArray(data) ? data : [])).catch(() => {});
    }
  }, [isTrainerOnly]);

  useEffect(() => {
    setSearchTerm("");
    setSortField(null);
    setSortDir("asc");
    setPage(1);
    loadData();
  }, [activeSectionKey]);

  useEffect(() => {
    setActiveSectionKey(availableSections[0].key);
  }, [availableSections]);

  const processedRows = useMemo(
    () => clientSortAndFilter(rows, colKeys, searchTerm, sortField, sortDir),
    [rows, colKeys, searchTerm, sortField, sortDir]
  );

  const totalPages = Math.max(1, Math.ceil(processedRows.length / pageSize));
  const pageRows = useMemo(() => {
    const start = (page - 1) * pageSize;
    return processedRows.slice(start, start + pageSize);
  }, [processedRows, page]);

  useEffect(() => {
    if (page > totalPages) setPage(totalPages);
  }, [page, totalPages]);

  return (
    <div className="portal-layout">
      <header className="portal-header">
        <span className="portal-brand">Fitness Gym</span>
        <nav className="portal-nav">
            {availableSections.map((s) => (
            <button
              key={s.key}
              type="button"
              className={s.key === activeSectionKey ? "portal-nav-btn active" : "portal-nav-btn"}
              onClick={() => setActiveSectionKey(s.key)}
            >
              {s.label}
            </button>
          ))}
        </nav>
        <button type="button" className="portal-logout-btn" onClick={onLogout}>Logout</button>
      </header>

      <main className="portal-main">
        <div className="portal-section-header">
          <div>
            <h2>{section.label}</h2>
            <p className="subtle-text">
              {processedRows.length} din {rows.length} inregistrari
            </p>
          </div>
          <div className="portal-section-actions">
            <input
              className="search-input"
              placeholder="Cauta..."
              value={searchTerm}
              onChange={(e) => { setSearchTerm(e.target.value); setPage(1); }}
            />
            <button type="button" className="secondary-btn" onClick={loadData}>Refresh</button>
          </div>
        </div>

        {toast ? <p className="success-text">{toast}</p> : null}
        {error ? <p className="error-text">{error}</p> : null}

        {isTrainerOnly ? (
          <div className="panel" style={{ marginBottom: 12 }}>
            <h3>Adauga clasa noua</h3>
            <form className="form-grid" onSubmit={handleTrainerCreateClass}>
              <label className="field">
                <span>Sala</span>
                <select
                  value={trainerClassForm.roomId}
                  onChange={(e) =>
                    setTrainerClassForm((prev) => ({ ...prev, roomId: e.target.value }))
                  }
                  required
                >
                  <option value="">Selecteaza sala...</option>
                  {rooms.map((r) => (
                    <option key={r.roomId} value={r.roomId}>
                      {r.name} (max {r.maxCapacity} persoane)
                    </option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>Titlu</span>
                <input
                  value={trainerClassForm.title}
                  onChange={(e) =>
                    setTrainerClassForm((prev) => ({ ...prev, title: e.target.value }))
                  }
                  required
                />
              </label>
              <label className="field">
                <span>Start</span>
                <input
                  type="datetime-local"
                  value={trainerClassForm.startTime}
                  onChange={(e) =>
                    setTrainerClassForm((prev) => ({ ...prev, startTime: e.target.value }))
                  }
                  required
                />
              </label>
              <label className="field">
                <span>End</span>
                <input
                  type="datetime-local"
                  value={trainerClassForm.endTime}
                  onChange={(e) =>
                    setTrainerClassForm((prev) => ({ ...prev, endTime: e.target.value }))
                  }
                  required
                />
              </label>
              <label className="field">
                <span>Locuri maxime</span>
                <input
                  type="number"
                  value={trainerClassForm.maxParticipants}
                  onChange={(e) =>
                    setTrainerClassForm((prev) => ({ ...prev, maxParticipants: e.target.value }))
                  }
                  required
                />
              </label>
              <div className="form-actions">
                <button type="submit">Creeaza clasa</button>
              </div>
            </form>
          </div>
        ) : null}

        <div className="portal-card">
          <div className="table-wrap">
            {loading ? <p className="subtle-text" style={{ padding: "12px 16px" }}>Se incarca datele...</p> : null}
            <table>
              <thead>
                <tr>
                  {section.columns.map((col) => (
                    <SortableHeader
                      key={col.key}
                      column={col.key}
                      label={col.label}
                      sortField={sortField}
                      sortDir={sortDir}
                      onSort={toggleSort}
                    />
                  ))}
                  {section.key === "trainer-classes" ? <th>Actiuni</th> : null}
                </tr>
              </thead>
              <tbody>
                {pageRows.map((row) => (
                  <tr key={row[section.idField]}>
                    {section.columns.map((col) => (
                      <td
                        key={col.key}
                        className={col.key === "status" ? `status-cell status-${formatCell(row[col.key]).toLowerCase()}` : ""}
                      >
                        {formatCell(row[col.key])}
                      </td>
                    ))}
                    {section.key === "trainer-classes" ? (
                      <td className="actions-cell">
                        {enrolledClassIds.has(row.classId) ? (
                          <span className="enrolled-badge">✓ Inscris</span>
                        ) : (
                          <button type="button" onClick={() => handleEnroll(row.classId)}>
                            Inscrie-ma
                          </button>
                        )}
                      </td>
                    ) : null}
                  </tr>
                ))}
              </tbody>
            </table>
            {!loading && pageRows.length === 0 ? (
              <p className="empty-text" style={{ padding: "12px 16px" }}>Nu exista inregistrari.</p>
            ) : null}
            <PaginationBar
              page={page}
              totalPages={totalPages}
              total={processedRows.length}
              onPrev={() => setPage((p) => Math.max(1, p - 1))}
              onNext={() => setPage((p) => Math.min(totalPages, p + 1))}
            />
          </div>
        </div>
      </main>
    </div>
  );
}

function LoginView({ onSuccess }) {
  const [activeTab, setActiveTab] = useState("login");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);
  const [registerData, setRegisterData] = useState({
    username: "",
    password: "",
    accountType: "MEMBER",
    email: "",
    fullName: "",
    phone: "",
    dateOfBirth: "",
    specialization: ""
  });

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");
    try {
      await login({ username, password, rememberMe });
      await onSuccess();
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleRegister(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");
    try {
      await registerNormalAccount({
        ...registerData,
        dateOfBirth: registerData.accountType === "MEMBER" ? registerData.dateOfBirth || null : null,
        specialization:
          registerData.accountType === "TRAINER" ? registerData.specialization || null : null
      });
      setSuccess("Cont USER creat cu succes. Te poti autentifica acum.");
      setActiveTab("login");
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="center-box">
      <h1>Gym System</h1>
      <p className="subtle-text">Conturi demo: admin / Admin123! &nbsp;&middot;&nbsp; user / User123!</p>
      <div className="auth-tabs">
        <button
          type="button"
          className={activeTab === "login" ? "tab active" : "tab"}
          onClick={() => setActiveTab("login")}
        >
          Login
        </button>
        <button
          type="button"
          className={activeTab === "register" ? "tab active" : "tab"}
          onClick={() => setActiveTab("register")}
        >
          Creeaza cont USER
        </button>
      </div>
      {activeTab === "login" ? (
        <form onSubmit={handleSubmit} className="login-form">
          <label className="field">
            <span>Username</span>
            <input value={username} onChange={(e) => setUsername(e.target.value)} required />
          </label>
          <label className="field">
            <span>Password</span>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </label>
          <label className="checkbox-field">
            <input type="checkbox" checked={rememberMe} onChange={(e) => setRememberMe(e.target.checked)} />
            Remember me
          </label>
          <button type="submit" disabled={loading}>
            {loading ? "Se conecteaza..." : "Login"}
          </button>
        </form>
      ) : (
        <form onSubmit={handleRegister} className="login-form">
          <label className="field">
            <span>Tip cont</span>
            <select
              value={registerData.accountType}
              onChange={(e) => setRegisterData((prev) => ({ ...prev, accountType: e.target.value }))}
            >
              <option value="MEMBER">Client sala</option>
              <option value="TRAINER">Antrenor</option>
            </select>
          </label>
          <label className="field">
            <span>Username</span>
            <input
              value={registerData.username}
              onChange={(e) => setRegisterData((prev) => ({ ...prev, username: e.target.value }))}
              required
            />
          </label>
          <label className="field">
            <span>Password</span>
            <input
              type="password"
              value={registerData.password}
              onChange={(e) => setRegisterData((prev) => ({ ...prev, password: e.target.value }))}
              required
            />
          </label>
          <label className="field">
            <span>Nume complet</span>
            <input
              value={registerData.fullName}
              onChange={(e) => setRegisterData((prev) => ({ ...prev, fullName: e.target.value }))}
              required
            />
          </label>
          <label className="field">
            <span>Email</span>
            <input
              type="email"
              value={registerData.email}
              onChange={(e) => setRegisterData((prev) => ({ ...prev, email: e.target.value }))}
              required
            />
          </label>
          <label className="field">
            <span>Telefon</span>
            <input
              value={registerData.phone}
              onChange={(e) => setRegisterData((prev) => ({ ...prev, phone: e.target.value }))}
            />
          </label>
          {registerData.accountType === "MEMBER" ? (
            <label className="field">
              <span>Data nasterii</span>
              <input
                type="date"
                value={registerData.dateOfBirth}
                onChange={(e) => setRegisterData((prev) => ({ ...prev, dateOfBirth: e.target.value }))}
                required
              />
            </label>
          ) : (
            <label className="field">
              <span>Specializare</span>
              <input
                value={registerData.specialization}
                onChange={(e) =>
                  setRegisterData((prev) => ({ ...prev, specialization: e.target.value }))
                }
              />
            </label>
          )}
          <button type="submit" disabled={loading}>
            {loading ? "Se creeaza..." : "Creeaza cont"}
          </button>
        </form>
      )}
      {success ? <p className="success-text">{success}</p> : null}
      {error ? <p className="error-text">{error}</p> : null}
    </div>
  );
}

function AdminAccountPanel() {
  const [formData, setFormData] = useState({
    username: "",
    password: "",
    email: "",
    fullName: "",
    phone: "",
    specialization: ""
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");
    try {
      await createAdminAccount(formData);
      setSuccess("Cont ADMIN creat cu succes.");
      setFormData({
        username: "",
        password: "",
        email: "",
        fullName: "",
        phone: "",
        specialization: ""
      });
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="panel">
      <div className="panel-header">
        <div>
          <h2>Creare cont ADMIN</h2>
          <p className="subtle-text">Pentru angajatii salii. Endpoint disponibil doar pentru ADMIN.</p>
        </div>
      </div>
      <form className="form-grid" onSubmit={handleSubmit}>
        <label className="field">
          <span>Username</span>
          <input
            value={formData.username}
            onChange={(e) => setFormData((prev) => ({ ...prev, username: e.target.value }))}
            required
          />
        </label>
        <label className="field">
          <span>Password</span>
          <input
            type="password"
            value={formData.password}
            onChange={(e) => setFormData((prev) => ({ ...prev, password: e.target.value }))}
            required
          />
        </label>
        <label className="field">
          <span>Email</span>
          <input
            type="email"
            value={formData.email}
            onChange={(e) => setFormData((prev) => ({ ...prev, email: e.target.value }))}
            required
          />
        </label>
        <label className="field">
          <span>Nume complet</span>
          <input
            value={formData.fullName}
            onChange={(e) => setFormData((prev) => ({ ...prev, fullName: e.target.value }))}
            required
          />
        </label>
        <label className="field">
          <span>Telefon</span>
          <input
            value={formData.phone}
            onChange={(e) => setFormData((prev) => ({ ...prev, phone: e.target.value }))}
          />
        </label>
        <label className="field">
          <span>Specializare</span>
          <input
            value={formData.specialization}
            onChange={(e) =>
              setFormData((prev) => ({ ...prev, specialization: e.target.value }))
            }
          />
        </label>
        <div className="form-actions">
          <button type="submit" disabled={loading}>
            {loading ? "Se creeaza..." : "Creeaza cont ADMIN"}
          </button>
        </div>
      </form>
      {success ? <p className="success-text">{success}</p> : null}
      {error ? <p className="error-text">{error}</p> : null}
    </div>
  );
}

export default function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isCheckingAuth, setIsCheckingAuth] = useState(true);
  const [selectedResourceKey, setSelectedResourceKey] = useState(resources[0].key);
  const [authError, setAuthError] = useState("");
  const [role, setRole] = useState("guest");
  const [me, setMe] = useState(null);

  const selectedResource =
    resources.find((resource) => resource.key === selectedResourceKey) ?? resources[0];
  const canWrite = role === "admin";
  const roleLabel = role.toUpperCase();

  async function refreshSession() {
    const session = await getCurrentSession();
    setIsAuthenticated(session.authenticated);
    setRole(session.role || "user");
    setMe(session);
  }

  useEffect(() => {
    (async () => {
      setIsCheckingAuth(true);
      try {
        await refreshSession();
      } catch (e) {
        setAuthError(e.message);
      } finally {
        setIsCheckingAuth(false);
      }
    })();
  }, []);

  async function handleLogout() {
    try {
      await logout();
    } catch (e) {
      setAuthError(e.message);
    } finally {
      setIsAuthenticated(false);
      setRole("guest");
      setMe(null);
    }
  }

  if (isCheckingAuth) return <div className="center-box">Se verifica sesiunea...</div>;
  if (!isAuthenticated) return <LoginView onSuccess={refreshSession} />;
  if (role === "user") return <UserPortal onLogout={handleLogout} me={me} />;

  return (
    <div className="layout">
      <aside className="sidebar">
        <h2>Gym Admin</h2>
        <p className="role-badge">Rol: {roleLabel}</p>
        <p className="sidebar-subtitle">Selecteaza o resursa pentru operatii CRUD.</p>
        <nav className="tabs tabs-vertical">
          {resources.map((resource) => (
            <button
              key={resource.key}
              type="button"
              className={resource.key === selectedResourceKey ? "tab active" : "tab"}
              onClick={() => setSelectedResourceKey(resource.key)}
            >
              {resource.label}
            </button>
          ))}
        </nav>
      </aside>
      <main className="app-shell">
        <header className="topbar">
          <h1>Fitness Gym Dashboard</h1>
          <div className="topbar-actions">
            <a href="/swagger-ui/index.html" target="_blank" rel="noreferrer">Swagger</a>
            <button type="button" className="secondary-btn" onClick={handleLogout}>Logout</button>
          </div>
        </header>
        {authError ? <p className="error-text">{authError}</p> : null}
        <AdminAccountPanel />
        <CrudPanel resource={selectedResource} canWrite={canWrite} roleLabel={roleLabel} />
      </main>
    </div>
  );
}
