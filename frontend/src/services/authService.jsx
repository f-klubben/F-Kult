import { API, BACKEND } from './api.jsx'

const API_URL = `${API}/auth`;

// Client registration id from application.properties on the backend
const SSO_REGISTRATION_ID = "fklub";

/** URL that starts the F-Klub SSO (OIDC) login on the backend. */
export function ssoLoginUrl() {
    return `${BACKEND}/oauth2/authorization/${SSO_REGISTRATION_ID}`;
}

/** Who the backend has logged in, based on the session cookie. */
export async function getSession() {
    try {
        const response = await fetch(`${API_URL}/session`, { credentials: "include" });
        if (!response.ok) return { authenticated: false };
        return await response.json();
    } catch (error) {
        console.error("Could not read the session:", error);
        return { authenticated: false };
    }
}

/** Login as admin with a username and password, without F-Klub SSO. */
export async function adminLogin(username, password) {
    const response = await fetch(`${API_URL}/admin-login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ username, password }),
    });

    if (!response.ok) {
        throw new Error(await response.text());
    }

    return await response.json();
}

/** Ends the session on the backend. Never throws, logging out must always work. */
export async function logout() {
    try {
        await fetch(`${API_URL}/logout`, { method: "POST", credentials: "include" });
    } catch (error) {
        console.error("Could not end the session on the backend:", error);
    }
}
