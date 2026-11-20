import { logOut } from "./auth.js";
import { getSession, isAdmin, loginUrl } from "./session.js";
import { el } from './ui.js';

const nav = document.querySelector('#account');
if (nav) {
    render();
}

function render() {
    const session = getSession();
    if (!session) {
        const link = el('a', 'button button-secondary', 'Log in');
        link.href = loginUrl();
        nav.replaceChildren(link);
        return;
    }

    const me = el('a', 'account-name', session.email);
    me.href = 'account.html';
    me.title = 'My Account';
    const parts = [me];
    if (isAdmin(session)) {
        parts.push(el('span', 'badge badge-admin', 'Admin'));
    }

    const out = el('button', 'button-secondary', 'Log out');
    out.type = 'button';
    out.addEventListener('click', () => {
        logOut();
        location.href = 'index.html';
    });
    parts.push(out);
    nav.replaceChildren(...parts);
}