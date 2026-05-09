function getCookie(name) {
  const target = `${name}=`;
  const segments = document.cookie.split(";");
  for (const segment of segments) {
    const value = segment.trim();
    if (value.startsWith(target)) {
      return decodeURIComponent(value.substring(target.length));
    }
  }
  return "";
}

export function getCsrfToken() {
  return getCookie("XSRF-TOKEN");
}

export async function apiFetch(path, options = {}) {
  const response = await fetch(path, {
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    ...options
  });

  if (!response.ok) {
    const contentType = response.headers.get("content-type") || "";
    let message;
    if (contentType.includes("application/json")) {
      try {
        const json = await response.json();
        message = json.message || json.error || `Eroare ${response.status}`;
      } catch {
        message = `Eroare ${response.status}`;
      }
    } else {
      const text = await response.text();
      message = text || `Eroare ${response.status}`;
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}
