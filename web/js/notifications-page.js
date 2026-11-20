import { fetchNotifications, markNotificationsRead } from './api.js';
import { loginUrl, requireLogin } from './session.js';
import { el, formatDateTime, showMessage } from './ui.js';

const list = document.querySelector('#notifications');

if (requireLogin()) {
    load();
}

async function load() {
    try {
        const notifications = await fetchNotifications();
        render(notifications);
        if (notifications.some((notification) => !notification.read)) {
            await markNotificationsRead();
            document.dispatchEvent(new CustomEvent('notifications-read'));
        }
    } catch (error) {
        if (error.status === 401) {
            location.replace(loginUrl());
            return;
        }
        showMessage(list, error.message, 'error');
    }
}

function render(notifications) {
    if (notifications.length === 0) {
        showMessage(list, 'No notifications yet. Save products to your wishlist and we will tell you when their price drops.');
        return;
    }
    list.replaceChildren(...notifications.map(row));
}

function row(notification) {
    const line = el('li', notification.read ? 'list-row' : 'list-row list-row-new');

    const text = el('div');
    const message = el('a', 'list-title', notification.message);
    message.href = `product.html?slug=${encodeURIComponent(notification.productSlug)}`;
    text.append(message, el('p', 'list-meta', formatDateTime(notification.createdAt)));

    line.append(text);
    if (!notification.read) {
        line.append(el('span', 'badge badge-new', 'New'));
    }
    return line;
}