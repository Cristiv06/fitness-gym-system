import { useEffect, useMemo, useState } from "react";
import { getCurrentSession, login, logout } from "./api/auth";
import { apiFetch } from "./api/http";
import { resources } from "./config/resources";

function emptyForm(resource) {
  return resource.fields.reduce((acc, field) => {
    acc[field.key] = "";
    return acc;
  }, {});
}

function formatForInput(value, type) {
  if (value == null) return "";
  if (type === "csv-number-array" && Array.isArray(value)) {
    return value.join(", ");
  }
  if (type === "datetime-local") {
    return String(value).slice(0, 16);
  }
  return String(value);
}

function toPayload(value, type) {
  if (value === "") return null;
  if (type === "number") return Number(value);
  if (type === "csv-number-array") {
    return value
      .split(",")
      .map((v) => v.trim())
      .filter(Boolean)
      .map(Number);
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
    if (field.required && !String(value || "").trim()) {
      return `${field.label} este obligatoriu.`;
    }
    if (field.type === "number" && value !== "" && Number.isNaN(Number(value))) {
      return `${field.label} trebuie sa fie numar valid.`;
    }
  }
  return "";
}

function CrudPanel({ resource, canWrite, roleLabel }) {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [formData, setFormData] = useState(() => emptyForm(resource));
  const [editingId, setEditingId] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [page, setPage] = useState(1);
  const [toast, setToast] = useState("");
  const pageSize = 7;

  const columns = useMemo(() => {
    const preferred = [resource.idField, ...resource.fields.map((f) => f.key)];
    return [...new Set(preferred)];
  }, [resource]);

  const filteredRows = useMemo(() => {
    const normalized = searchTerm.trim().toLowerCase();
    if (!normalized) return rows;
    return rows.filter((row) =>
      columns.some((column) => formatCell(row[column]).toLowerCase().includes(normalized))
    );
  }, [rows, columns, searchTerm]);

  const totalPages = Math.max(1, Math.ceil(filteredRows.length / pageSize));
  const pageRows = useMemo(() => {
    const start = (page - 1) * pageSize;
    return filteredRows.slice(start, start + pageSize);
  }, [filteredRows, page]);

  async function loadData() {
    setLoading(true);
    setError("");
    try {
      const data = await apiFetch(resource.endpoint);
      setRows(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    setFormData(emptyForm(resource));
    setEditingId(null);
    setSearchTerm("");
    setPage(1);
    loadData();
  }, [resource.key]);

  useEffect(() => {
    if (page > totalPages) {
      setPage(totalPages);
    }
  }, [page, totalPages]);

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
    if (!canWrite) {
      setError(`Rolul ${roleLabel} poate doar citi (GET).`);
      return;
    }
    const validationMessage = validateForm(resource, formData);
    if (validationMessage) {
      setError(validationMessage);
      return;
    }

    const payload = {};
    for (const field of resource.fields) {
      const converted = toPayload(formData[field.key], field.type);
      if (converted !== null) payload[field.key] = converted;
    }

    try {
      if (editingId == null) {
        await apiFetch(resource.endpoint, {
          method: "POST",
          body: JSON.stringify(payload)
        });
      } else {
        await apiFetch(`${resource.endpoint}/${editingId}`, {
          method: "PUT",
          body: JSON.stringify(payload)
        });
      }
      clearForm();
      await loadData();
      setToast(editingId == null ? "Element creat cu succes." : "Element actualizat cu succes.");
      setTimeout(() => setToast(""), 2500);
    } catch (e) {
      setError(e.message);
    }
  }

  async function onDelete(id) {
    if (!canWrite) {
      setError(`Rolul ${roleLabel} poate doar citi (GET).`);
      return;
    }
    try {
      await apiFetch(`${resource.endpoint}/${id}`, { method: "DELETE" });
      await loadData();
      setToast("Element sters cu succes.");
      setTimeout(() => setToast(""), 2500);
    } catch (e) {
      setError(e.message);
    }
  }

  return (
    <div className="panel">
      <div className="panel-header">
        <div>
          <h2>{resource.label}</h2>
          <p className="subtle-text">Gestionare completa pentru resursa selectata.</p>
        </div>
        <div className="panel-header-actions">
          <input
            className="search-input"
            placeholder="Cauta in tabel..."
            value={searchTerm}
            onChange={(e) => {
              setSearchTerm(e.target.value);
              setPage(1);
            }}
          />
          <button onClick={loadData} className="secondary-btn" type="button">
            Refresh
          </button>
        </div>
      </div>

      <div className="stats-row">
        <span className="stat-chip">Rol curent: {roleLabel}</span>
        <span className="stat-chip">Total: {rows.length}</span>
        <span className="stat-chip">Filtrate: {filteredRows.length}</span>
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
                  <option key={opt} value={opt}>
                    {opt}
                  </option>
                ))}
              </select>
            ) : (
              <input
                type={
                  field.type === "csv-number-array"
                    ? "text"
                    : field.type === "datetime-local"
                      ? "datetime-local"
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
              {columns.map((column) => (
                <th key={column}>{column}</th>
              ))}
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {pageRows.map((row) => (
              <tr key={row[resource.idField]}>
                {columns.map((column) => (
                  <td key={`${row[resource.idField]}-${column}`}>
                    {formatCell(row[column])}
                  </td>
                ))}
                <td className="actions-cell">
                  <button
                    type="button"
                    className="secondary-btn"
                    onClick={() => startEdit(row)}
                    disabled={!canWrite}
                  >
                    Edit
                  </button>
                  <button
                    type="button"
                    className="danger-btn"
                    onClick={() => onDelete(row[resource.idField])}
                    disabled={!canWrite}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {!loading && pageRows.length === 0 ? (
          <p className="empty-text">Nu exista rezultate pentru filtrele curente.</p>
        ) : null}
        <div className="pagination">
          <button
            type="button"
            className="secondary-btn"
            onClick={() => setPage((p) => Math.max(1, p - 1))}
            disabled={page === 1}
          >
            Prev
          </button>
          <span>
            Page {page} / {totalPages} ({filteredRows.length} rows)
          </span>
          <button
            type="button"
            className="secondary-btn"
            onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
            disabled={page === totalPages}
          >
            Next
          </button>
        </div>
      </div>
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
      <h1>Gym System Login</h1>
      <p>Conturi demo: admin/Admin123! si user/User123!</p>
      <form onSubmit={handleSubmit} className="login-form">
        <label className="field">
          <span>Username</span>
          <input value={username} onChange={(e) => setUsername(e.target.value)} required />
        </label>
        <label className="field">
          <span>Password</span>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </label>
        <label className="checkbox-field">
          <input
            type="checkbox"
            checked={rememberMe}
            onChange={(e) => setRememberMe(e.target.checked)}
          />
          Remember me
        </label>
        <button type="submit" disabled={loading}>
          {loading ? "Signing in..." : "Login"}
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

  const selectedResource = resources.find((resource) => resource.key === selectedResourceKey) ?? resources[0];
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

  if (isCheckingAuth) {
    return <div className="center-box">Checking session...</div>;
  }

  if (!isAuthenticated) {
    return <LoginView onSuccess={refreshSession} />;
  }

  return (
    <div className="layout">
      <aside className="sidebar">
        <h2>Gym Frontend</h2>
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
            <a href="/swagger-ui/index.html" target="_blank" rel="noreferrer">
              Swagger
            </a>
            <button type="button" className="secondary-btn" onClick={handleLogout}>
              Logout
            </button>
          </div>
        </header>

        {!canWrite ? (
          <p className="notice-text">
            Esti logat cu rol USER. Poti vedea datele, dar operatiile Create/Update/Delete sunt dezactivate.
          </p>
        ) : null}

        {authError ? <p className="error-text">{authError}</p> : null}

        <CrudPanel resource={selectedResource} canWrite={canWrite} roleLabel={roleLabel} />
      </main>
    </div>
  );
}
