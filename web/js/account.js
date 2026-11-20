import { fetchMyAccount } from './api.js';
import { loginUrl, requireLogin } from './session.js';
import { el, showMessage } from './ui.js';

const view = document.querySelector('#account-view');

if (requireLogin()) {
    load();
}

async function load() {
    try {
        const me = await fetchMyAccount(); // a protected call: proves the token works
        const list = el('dl', 'facts');
        list.append(
            el('dt', null, 'Email'), el('dd', null, me.email),
            el('dt', null, 'Role'), el('dd', null, me.roles.join(', ')),
        );
        view.replaceChildren(el('h1', null, 'My account'), list);
    } catch (error) {
        if (error.status === 401) {
            location.replace(loginUrl());
            return;
        }
        showMessage(view, error.message, 'error');
    }
}