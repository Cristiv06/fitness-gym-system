import { getCsrfToken } from "./http";

async function readLoginCsrf() {
  const response = await fetch("/login", {
    method: "GET",
    credentials: "include"
  });
  if (!response.ok) {
    throw new Error("Nu pot incarca pagina de login pentru CSRF.");
  }

  const html = await response.text();
  const match = html.match(/<input[^>]*type="hidden"[^>]*name="([^"]+)"[^>]*value="([^"]+)"/i);
  if (!match) {
    return { parameterName: "_csrf", token: getCsrfToken() };
  }

  return { parameterName: match[1], token: match[2] };
}

export async function login({ username, password, rememberMe }) {
  const { parameterName, token } = await readLoginCsrf();
  const csrfToken = token || getCsrfToken();
  const form = new URLSearchParams();
  form.set("username", username);
  form.set("password", password);
  if (csrfToken) {
    form.set(parameterName, csrfToken);
  }
  if (rememberMe) {
    form.set("remember-me", "on");
  }

  const response = await fetch("/login", {
    method: "POST",
    credentials: "include",
    redirect: "manual",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded"
    },
    body: form
  });

  const loginSucceeded =
    response.type === "opaqueredirect" ||
    response.ok ||
    response.redirected ||
    (response.status >= 300 && response.status < 400);
  if (!loginSucceeded) {
    throw new Error("Login failed. Verify username/password.");
  }
}

export async function logout() {
  const { parameterName, token } = await readLoginCsrf();
  const csrfToken = token || getCsrfToken();
  const form = new URLSearchParams();
  if (csrfToken) {
    form.set(parameterName, csrfToken);
  }

  const response = await fetch("/logout", {
    method: "POST",
    credentials: "include",
    redirect: "manual",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded"
    },
    body: form
  });

  const logoutSucceeded =
    response.type === "opaqueredirect" ||
    response.ok ||
    response.redirected ||
    (response.status >= 300 && response.status < 400);
  if (!logoutSucceeded) {
    throw new Error("Logout failed.");
  }
}

export async function getCurrentSession() {
  const response = await fetch("/api/members", {
    method: "GET",
    credentials: "include"
  });
  if (response.status === 401) {
    return { authenticated: false, role: "guest" };
  }
  if (!response.ok) {
    throw new Error("Unable to validate session.");
  }

  // Role probe: USER gets 403 on write methods, ADMIN usually gets 400 for invalid payload.
  const probe = await fetch("/api/members", {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({})
  });

  const role = probe.status === 403 ? "user" : "admin";
  return { authenticated: true, role };
}
