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
  const redirectTarget = response.headers.get("location") || "";
  if (redirectTarget.includes("error")) {
    throw new Error("Email/username sau parola gresita.");
  }
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
  const response = await fetch("/api/auth/me", {
    method: "GET",
    credentials: "include"
  });
  if (response.status === 401) {
    return { authenticated: false, role: "guest", roles: [] };
  }
  if (!response.ok) {
    throw new Error("Unable to validate session.");
  }
  const me = await response.json();
  const roles = Array.isArray(me.roles) ? me.roles : [];
  const role = roles.includes("ROLE_ADMIN") ? "admin" : "user";
  return { authenticated: true, role, ...me, roles };
}

export async function registerNormalAccount(payload) {
  const response = await fetch("/api/auth/register", {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || "Account creation failed.");
  }
  return response.json();
}

export async function createAdminAccount(payload) {
  const response = await fetch("/api/auth/admin/create-account", {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || "Admin account creation failed.");
  }
  return response.json();
}

export async function getMySubscriptions() {
  const response = await fetch("/api/auth/me/subscriptions", {
    method: "GET",
    credentials: "include"
  });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || "Cannot load subscriptions.");
  }
  return response.json();
}

export async function getMyClasses() {
  const response = await fetch("/api/auth/me/classes", {
    method: "GET",
    credentials: "include"
  });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || "Cannot load classes.");
  }
  return response.json();
}

export async function getTrainerClassesForMember() {
  const response = await fetch("/api/auth/me/trainer-classes", {
    method: "GET",
    credentials: "include"
  });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || "Cannot load trainer classes.");
  }
  return response.json();
}

export async function createTrainerClass(payload) {
  const response = await fetch("/api/auth/me/classes", {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(payload)
  });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || "Cannot create class.");
  }
  return response.json();
}

export async function enrollToClass(classId) {
  const response = await fetch("/api/auth/me/enrollments", {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ classId })
  });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || "Cannot enroll to class.");
  }
  return response.json();
}
