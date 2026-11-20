import { register } from './api.js';
import { logIn } from './auth.js';
import { safeNext } from './session.js';
import { showMessage } from './ui.js';

const next = safeNext(new URLSearchParams(location.search).get('next'));
const form = document.querySelector('#register-form');
const feedback = document.querySelector('#feedback');

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    feedback.replaceChildren();

    const email = form.elements.email.value.trim();
    const password = form.elements.password.value;
    if (password !== form.elements.repeat.value) {
        showMessage(feedback, 'The two passwords are different.', 'error');
        return;
    }

    const button = form.querySelector('button[type="submit"]');
    button.disabled = true;
    try {
        await register(email, password);
        await logIn(email, password);
        location.replace(next);
    } catch (error) {
        const text = error.status === 400
            ? 'Please use a valid email and a password of 8 to 72 characters.'
            : error.message; // 409: "An account with this email already exists"
        showMessage(feedback, text, 'error');
        button.disabled = false;
    }
});