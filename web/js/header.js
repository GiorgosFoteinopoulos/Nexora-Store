import { fetchNotifications } from './api.js';
import { logOut } from './auth.js';
import { getSession, isAdmin, loginUrl } from './session.js';
import { el } from './ui.js';

// Our own constant drawing (safe for innerHTML): a bell
const BELL = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
    stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
    <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/></svg>`;

let badge = null;


const nav = document.querySelector('#account');
if (nav) {
    render();
}


document.addEventListener('notifications-read', () => setUnread(0));

function render() {
    const session = getSession();
    if (!session) {
        badge = null;
        const link = el('a', 'button button-secondary', 'Log in');
        link.href = loginUrl();
        nav.replaceChildren(link);
        return;
    }

    const wishlist = el('a', 'header-link', '♥ Wishlist');
    wishlist.href = 'wishlist.html';

    const bell = el('a', 'bell');
    bell.href = 'notifications.html';
    bell.title = 'Notifications';
    bell.setAttribute('aria-label', 'Notifications');
    bell.innerHTML = BELL;
    badge = el('span', 'bell-count');
    badge.hidden = true;
    bell.append(badge);

    const me = el('a', 'account-name', session.email);
    me.href = 'account.html';
    me.title = 'My account';

    const parts = [wishlist, bell, me];
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

    loadUnreadCount();
}

async function loadUnreadCount() {
    try {
        const notifications = await fetchNotifications();
        setUnread(notifications.filter((notification) => !notification.read).length);
    } catch (error) {
        if (error.status === 401) {
            render();
        }

    }
}

function setUnread(count) {
    if (!badge) {
        return;
    }
    badge.textContent = count > 9 ? '9+' : String(count);
    badge.hidden = count === 0;
}