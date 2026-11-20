import { logIn } from './auth.js';
import { getSession, safeNext } from './session.js';
import { showMessage } from './ui.js';

const next = safeNext(new URLSearchParams(location.search).get('next'));
const form = document.querySelector('#login-form');
const feedback = document.querySelector('#feedback');

if (getSession()) {
    location.replace(next);
}

document.querySelector('#register-link').href = `register.html?next=${encodeURIComponent(next)}`;

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const button = form.querySelector('button[type="submit"]');
    button.disabled = true;
    feedback.replaceChildren();
    try {
        await logIn(form.elements.email.value.trim(), form.elements.password.value);
        location.replace(next);
    } catch (error) {
        showMessage(feedback, error.message, 'error'); // the server's own words: "Wrong email or password"
        form.elements.password.value = '';
        button.disabled = false;
    }
});