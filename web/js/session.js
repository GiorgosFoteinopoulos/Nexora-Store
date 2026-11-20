const KEY = 'nexora.session';

export function getSession() {
    let session = null;
    try {
        session = JSON.parse(sessionStorage.getItem(KEY));
    } catch {

    }
    if (!session?.token || Date.now() >= session.expiresAt) {
        clearSession();
        return null;
    }
    return session;
}

export function saveSession({ token, expiresInSeconds, email, roles }) {
    const session = {
        token,
        email,
        roles: roles ?? [],
        expiresAt: Date.now() + (expiresInSeconds - 30) * 1000,
    };

    try {
        sessionStorage.setItem(KEY, JSON.stringify(session));
    } catch {

    }
    return session;
}

export function clearSession() {
    try {
        sessionStorage.removeItem(KEY);
    } catch {

    }
}

export const isAdmin = (session) => session?.roles.includes('ADMIN') ?? false;

export function requireLogin() {
    const session = getSession();
    if (!session) {
        location.replace(loginUrl());
    }
    return session;
}

export function loginUrl() {
    return `login.html?next=${encodeURIComponent(location.pathname + location.search)}`;
}

export function safeNext(raw) {
    return raw && raw.startsWith('/') && !raw.startsWith('//') && !raw.includes('\\') ? raw : 'index.html';
}