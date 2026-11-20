import { fetchWishlist, removeFromWishlist } from './api.js';
import { loginUrl, requireLogin } from './session.js';
import { el, formatDateTime, showMessage, toast } from './ui.js';

const list = document.querySelector('#wishlist');
const count = document.querySelector('#wishlist-count');

if (requireLogin()) {
    load();
}

async function load() {
    try {
        render(await fetchWishlist());
    } catch (error) {
        if (error.status === 401) {
            location.replace(loginUrl());
            return;
        }
        showMessage(list, error.message, 'error');
    }
}

function render(items) {
    if (items.length === 0) {
        count.textContent = '';
        const empty = el('div', 'message');
        const link = el('a', null, 'Browse the catalog');
        link.href = 'index.html';
        empty.append(el('p', null, 'Your wishlist is empty. Tap the ♡ on any product to save it here.'), link);
        list.replaceChildren(empty);
        return;
    }
    count.textContent = `${items.length} saved ${items.length === 1 ? 'product' : 'products'}`;
    list.replaceChildren(...items.map(row));
}

function row(item) {
    const line = el('li', 'list-row');

    const text = el('div');
    const name = el('a', 'list-title', item.name);
    name.href = `product.html?slug=${encodeURIComponent(item.slug)}`;
    text.append(name, el('p', 'list-meta', `Saved ${formatDateTime(item.addedAt)}`));

    const remove = el('button', 'button-secondary', 'Remove');
    remove.type = 'button';
    remove.addEventListener('click', async () => {
        remove.disabled = true;
        try {
            await removeFromWishlist(item.slug);
            line.remove();
            toast('Removed from your wishlist');
            if (list.children.length === 0) {
                render([]);
            } else {
                count.textContent = `${list.children.length} saved ${list.children.length === 1 ? 'product' : 'products'}`;
            }
        } catch (error) {
            toast(error.message, 'error');
            remove.disabled = false;
        }
    });

    line.append(text, remove);
    return line;
}