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

function CrudPanel({ resource }) {
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [formData, setFormData] = useState(() => emptyForm(resource));
  const [editingId, setEditingId] = useState(null);

  const columns = useMemo(() => {
    const preferred = [resource.idField, ...resource.fields.map((f) => f.key)];
    return [...new Set(preferred)];
  }, [resource]);

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
    loadData();
  }, [resource.key]);

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
    } catch (e) {
      setError(e.message);
    }
  }

  async function onDelete(id) {
    try {
      await apiFetch(`${resource.endpoint}/${id}`, { method: "DELETE" });
      await loadData();
    } catch (e) {
      setError(e.message);
    }
  }

  return (
    <div className="panel">
      <div className="panel-header">
        <h2>{resource.label}</h2>
        <button onClick={loadData} className="secondary-btn" type="button">
          Refresh
        </button>
      </div>

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
              />
            ) : field.type === "select" ? (
              <select
                value={formData[field.key]}
                onChange={(e) => handleChange(field.key, e.target.value)}
                required={field.required}
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
              />
            )}
          </label>
        ))}

        <div className="form-actions">
          <button type="submit">{editingId == null ? "Create" : "Update"}</button>
          <button onClick={clearForm} type="button" className="secondary-btn">
            Clear
          </button>
        </div>
      </form>

      <div className="table-wrap">
        {loading ? <p>Loading...</p> : null}
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
            {rows.map((row) => (
              <tr key={row[resource.idField]}>
                {columns.map((column) => (
                  <td key={`${row[resource.idField]}-${column}`}>
                    {Array.isArray(row[column]) ? row[column].join(", ") : String(row[column] ?? "")}
                  </td>
                ))}
                <td className="actions-cell">
                  <button
                    type="button"
                    className="secondary-btn"
                    onClick={() => startEdit(row)}
                  >
                    Edit
                  </button>
                  <button type="button" className="danger-btn" onClick={() => onDelete(row[resource.idField])}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
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
      onSuccess();
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="center-box">
      <h1>Gym System Login</h1>
      <p>Demo conturi: admin/Admin123! si user/User123!</p>
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
          {loading ? "Signing in..." : "Sign in"}
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

  const selectedResource = resources.find((resource) => resource.key === selectedResourceKey) ?? resources[0];

  useEffect(() => {
    (async () => {
      setIsCheckingAuth(true);
      try {
        const session = await getCurrentSession();
        setIsAuthenticated(session.authenticated);
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
    }
  }

  if (isCheckingAuth) {
    return <div className="center-box">Checking session...</div>;
  }

  if (!isAuthenticated) {
    return <LoginView onSuccess={() => setIsAuthenticated(true)} />;
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <h1>Fitness Gym Admin UI</h1>
        <div className="topbar-actions">
          <a href="/swagger-ui/index.html" target="_blank" rel="noreferrer">
            Swagger
          </a>
          <button type="button" className="secondary-btn" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      {authError ? <p className="error-text">{authError}</p> : null}

      <nav className="tabs">
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

      <CrudPanel resource={selectedResource} />
    </div>
  );
}
