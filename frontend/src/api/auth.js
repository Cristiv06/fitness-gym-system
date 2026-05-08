import { getCsrfToken } from "./http";

export async function primeCsrfCookie() {
  await fetch("/login", { method: "GET", credentials: "include" });
}

export async function login({ username, password, rememberMe }) {
  await primeCsrfCookie();
  const csrfToken = getCsrfToken();
  const form = new URLSearchParams();
  form.set("username", username);
  form.set("password", password);
  if (rememberMe) {
    form.set("remember-me", "on");
  }

  const response = await fetch("/login", {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
      ...(csrfToken ? { "X-XSRF-TOKEN": csrfToken } : {})
    },
    body: form
  });

  if (!response.ok) {
    throw new Error("Login failed. Verify username/password.");
  }
}

export async function logout() {
  await primeCsrfCookie();
  const csrfToken = getCsrfToken();
  const form = new URLSearchParams();
  form.set("_csrf", csrfToken);

  const response = await fetch("/logout", {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded",
      ...(csrfToken ? { "X-XSRF-TOKEN": csrfToken } : {})
    },
    body: form
  });

  if (!response.ok) {
    throw new Error("Logout failed.");
  }
}

export async function getCurrentSession() {
  const response = await fetch("/api/members", {
    method: "GET",
    credentials: "include"
  });
  if (response.status === 401) {
    return { authenticated: false };
  }
  if (!response.ok) {
    throw new Error("Unable to validate session.");
  }
  return { authenticated: true };
}
