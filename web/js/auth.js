import { fetchMyAccount, login } from './api.js';
import { clearSession, saveSession } from './session.js';


export async function logIn(email, password) {
    const token = await login(email, password);
    const session = {
        token: token.accessToken,
        expiresInSeconds: token.expiresInSeconds,
        email,
        roles: [],
    };
    saveSession(session);

    const me = await fetchMyAccount();
    return saveSession({ ...session, email: me.email, roles: me.roles });
}


export function logOut() {
    clearSession();
}