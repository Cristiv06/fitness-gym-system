import { useEffect, useMemo, useRef, useState } from "react";
import { getCurrentSession, login, logout } from "./api/auth";
import { apiFetch } from "./api/http";
import { resources } from "./config/resources";

const USER_SECTIONS = [
  {
    key: "membership-plans",
    label: "Planuri abonament",
    endpoint: "/api/membership-plans",
    idField: "planId",
    columns: [
      { key: "name", label: "Nume" },
      { key: "durationMonths", label: "Durata (luni)" },
      { key: "price", label: "Pret (RON)" },
      { key: "description", label: "Descriere" }
    ]
  },
  {
    key: "trainers",
    label: "Antrenori",
    endpoint: "/api/trainers",
    idField: "trainerId",
    columns: [
      { key: "fullName", label: "Nume complet" },
      { key: "specialization", label: "Specializare" },
      { key: "phone", label: "Telefon" },
      { key: "email", label: "Email" }
    ]
  },
  {
    key: "gym-classes",
    label: "Clase disponibile",
    endpoint: "/api/gym-classes",
    idField: "classId",
    columns: [
      { key: "title", label: "Titlu" },
      { key: "startTime", label: "Inceput" },
      { key: "endTime", label: "Sfarsit" },
      { key: "maxParticipants", label: "Locuri max" }
    ]
  },
  {
    key: "subscriptions",
    label: "Abonamentele mele",
    endpoint: "/api/subscriptions",
    idField: "subscriptionId",
    columns: [
      { key: "memberId", label: "Membru ID" },
      { key: "planId", label: "Plan ID" },
      { key: "startDate", label: "Data start" },
      { key: "endDate", label: "Data sfarsit" },
      { key: "status", label: "Status" }
    ]
  },
  {
    key: "check-ins",
    label: "Check-in-urile mele",
    endpoint: "/api/check-ins",
    idField: "checkinId",
    columns: [
      { key: "checkinId", label: "ID" },
      { key: "memberId", label: "Membru ID" },
      { key: "checkinTime", label: "Data / Ora" }
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
  if (type === "number") return Number(value);
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
    if (field.type === "number" && value !== "" && Number.isNaN(Number(value))) {
      return `${field.label} trebuie sa fie numar valid.`;
    }
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
    setFormData((prev) => ({ ...prev, [key]: value }));
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
            ) : (
              <input
                type={
                  field.type === "csv-number-array" ? "text"
                  : field.type === "datetime-local" ? "datetime-local"
                  : field.type
                }
                step={field.step}
                value={formData[field.key]}
                onChange={(e) => handleChange(field.key, e.target.value)}
                required={field.required}
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

function UserPortal({ onLogout }) {
  const [activeSectionKey, setActiveSectionKey] = useState(USER_SECTIONS[0].key);
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [sortField, setSortField] = useState(null);
  const [sortDir, setSortDir] = useState("asc");
  const [page, setPage] = useState(1);
  const loadIdRef = useRef(0);
  const pageSize = 8;

  const section = USER_SECTIONS.find((s) => s.key === activeSectionKey);
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
      const data = await apiFetch(section.endpoint);
      if (thisId !== loadIdRef.current) return;
      setRows(Array.isArray(data) ? data : []);
    } catch (e) {
      if (thisId !== loadIdRef.current) return;
      setError(e.message);
    } finally {
      if (thisId !== loadIdRef.current) return;
      setLoading(false);
    }
  }

  useEffect(() => {
    setSearchTerm("");
    setSortField(null);
    setSortDir("asc");
    setPage(1);
    loadData();
  }, [activeSectionKey]);

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
          {USER_SECTIONS.map((s) => (
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

        {error ? <p className="error-text">{error}</p> : null}

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
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(true);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      await login({ username, password, rememberMe });
      await onSuccess();
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

  const selectedResource =
    resources.find((resource) => resource.key === selectedResourceKey) ?? resources[0];
  const canWrite = role === "admin";
  const roleLabel = role.toUpperCase();

  async function refreshSession() {
    const session = await getCurrentSession();
    setIsAuthenticated(session.authenticated);
    setRole(session.role || "user");
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
    }
  }

  if (isCheckingAuth) return <div className="center-box">Se verifica sesiunea...</div>;
  if (!isAuthenticated) return <LoginView onSuccess={refreshSession} />;
  if (role === "user") return <UserPortal onLogout={handleLogout} />;

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
        <CrudPanel resource={selectedResource} canWrite={canWrite} roleLabel={roleLabel} />
      </main>
    </div>
  );
}
