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
    const message = await response.text();
    throw new Error(message || `HTTP ${response.status}`);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}
