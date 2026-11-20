import { chatWithNexa, fetchConversation, forgetConversation } from './api.js';
import { getSession, loginUrl } from './session.js';
import { el, formatPrice } from './ui.js';


const MAX_LENGTH = 1000;

let panel = null;
let messages = null;
let form = null;
let historyLoaded = false;

const launcher = el('button', 'nexa-launcher', 'Ask Nexa');
launcher.type = 'button';
launcher.setAttribute('aria-expanded', 'false');
launcher.addEventListener('click', () => (panel && !panel.hidden ? close() : open()));
document.body.append(launcher);

function open() {
    if (!panel) {
        build();
    }
    panel.hidden = false;
    launcher.setAttribute('aria-expanded', 'true');
    if (getSession() && !historyLoaded) {
        loadHistory();
    }
    form?.elements.message.focus();
}

function close() {
    panel.hidden = true;
    launcher.setAttribute('aria-expanded', 'false');
    launcher.focus();
}

function build() {
    panel = el('aside', 'nexa-panel');
    panel.setAttribute('aria-label', 'Chat with Nexa');

    const head = el('div', 'nexa-head');
    const title = el('div');
    title.append(el('p', 'nexa-title', 'Nexa'), el('p', 'nexa-subtitle', 'Your shopping advisor'));
    const startOver = el('button', 'button-secondary nexa-small', 'Start over');
    startOver.type = 'button';
    startOver.addEventListener('click', startAgain);
    const shut = el('button', 'nexa-close', '×');
    shut.type = 'button';
    shut.setAttribute('aria-label', 'Close the chat');
    shut.addEventListener('click', close);
    head.append(title, startOver, shut);

    messages = el('div', 'nexa-messages');
    messages.setAttribute('aria-live', 'polite');

    panel.append(head, messages);

    if (!getSession()) {
        startOver.hidden = true;
        const box = el('div', 'nexa-login');
        const link = el('a', 'button', 'Log in to chat');
        link.href = loginUrl();
        box.append(el('p', null, 'Nexa remembers your conversation, so you need to be logged in.'), link);
        messages.append(box);
    } else {
        panel.append(buildForm());
        say('nexa', 'Hi! Tell me what you need, for example "a light laptop for travelling, under 900 euros".');
    }
    document.body.append(panel);
}

function buildForm() {
    form = el('form', 'nexa-form');
    const input = el('textarea');
    input.name = 'message';
    input.rows = 2;
    input.maxLength = MAX_LENGTH;
    input.placeholder = 'Ask Nexa…';
    input.setAttribute('aria-label', 'Your message to Nexa');
    input.addEventListener('keydown', (event) => {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            form.requestSubmit();
        }
    });
    const send = el('button', null, 'Send');
    send.type = 'submit';
    form.append(input, send);
    form.addEventListener('submit', (event) => {
        event.preventDefault();
        sendMessage(input.value.trim());
    });
    return form;
}

async function loadHistory() {
    historyLoaded = true;
    try {
        const turns = await fetchConversation();
        if (turns.length > 0) {
            messages.replaceChildren();
            for (const turn of turns) {
                say(turn.role === 'USER' ? 'user' : 'nexa', turn.text);
            }
        }
    } catch {
    }
}

async function sendMessage(text) {
    if (!text) {
        return;
    }
    const input = form.elements.message;
    const button = form.querySelector('button');
    input.value = '';
    say('user', text);
    const thinking = say('nexa thinking', 'Nexa is thinking…');
    input.disabled = true;
    button.disabled = true;

    try {
        const answer = await chatWithNexa(text);
        thinking.remove();
        say('nexa', answer.reply, answer.products);
    } catch (error) {
        thinking.remove();
        say('error', error.status === 401 ? 'Your session has expired. Please log in again.' : error.message);
    } finally {
        input.disabled = false;
        button.disabled = false;
        input.focus();
    }
}

async function startAgain() {
    try {
        await forgetConversation();
        messages.replaceChildren();
        say('nexa', 'Fresh start. What are you looking for?');
    } catch (error) {
        say('error', error.message);
    }
}


function say(kind, text, products = []) {
    const bubble = el('div', `nexa-msg nexa-${kind.replace(' ', ' nexa-')}`);
    bubble.append(el('p', null, text));
    if (products.length > 0) {
        const cards = el('div', 'nexa-cards');
        for (const product of products) {
            const card = el('a', 'nexa-card');
            card.href = `product.html?slug=${encodeURIComponent(product.slug)}`;
            card.append(
                el('span', 'nexa-card-name', product.name),
                el('span', 'nexa-card-price', product.startingPrice ? `from ${formatPrice(product.startingPrice)}` : ''),
                el('span', product.available ? 'stock-in' : 'stock-out', product.available ? 'In stock' : 'Sold out'),
            );
            cards.append(card);
        }
        bubble.append(cards);
    }
    messages.append(bubble);
    messages.scrollTop = messages.scrollHeight;
    return bubble;
}