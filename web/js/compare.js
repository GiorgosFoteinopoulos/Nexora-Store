import { categoryLabel, el, toast } from "./ui.js";

const KEY = 'nexora.compare';
const MAX = 4;

export function compareSelection() {
    try {
        return JSON.parse(sessionStorage.getItem(KEY)) ?? [];
    } catch {
        return [];
    }
}

export function clearCompare() {
    save([]);
}

function save(items) {
    try {
        sessionStorage.setItem(KEY, JSON.stringify(items));
    } catch {

    }
    renderTray();
    syncButtons();
}

const isSelected = (slug) => compareSelection().some((item) => item.slug === slug);

export function toggleCompare({ slug, name, category }) {
    const items = compareSelection();
    if (items.some((item) => item.slug === slug)) {
        save(items.filter((item) => item.slug !== slug));
        return;
    }
    if (items.length > 0 && items[0].category !== category) {
        toast(`Compare one category at a time. You are comparing ${categoryLabel(items[0].category)}`, 'error');
        return;
    }
    if (items.length >= MAX) {
        toast(`You can compare up to ${MAX} products.`, 'error');
        return;
    }
    save([...items, {slug, name, category}]);
}

export function compareButton(product, { withText = false } = {}) {
    const button = el('button', withText ? 'compare-toggle compare-toggle-text' : 'compare-toggle');
    button.type = 'button';
    button.dataset.compareSlug = product.slug;
    paint(button, isSelected(product.slug));
    button.addEventListener('click', (event) => {
        event.preventDefault();
        toggleCompare(product);
    });
    return button;
}

function paint(button, selected) {
    button.setAttribute('aria-pressed', String(selected));
    button.textContent = selected ? '✓ Comparing' : '⇄ Compare';
    button.title = selected ? 'Remove from comparison' : 'Add to comparison';
}

function syncButtons() {
    for (const button of document.querySelectorAll('[data-compare-slug]')) {
        paint(button, isSelected(button.dataset.compareSlug));
    }
}

let tray = null;

export function showCompareTray() {
    tray = el('div', 'compare-tray');
    tray.setAttribute('aria-label', 'Products to compare');
    document.body.append(tray);
    renderTray();
}

function renderTray() {
    if (!tray) {
        return;
    }

    const items = compareSelection();
    tray.hidden = items.length === 0;
    document.body.classList.toggle('has-tray', items.length > 0);
    if (items.length === 0) {
        tray.replaceChildren();
        return;
    }
    const inner = el('div', 'container tray-inner');
    inner.append(el('span', 'tray-label', `Compare ${categoryLabel(items[0].category)}:`));

    const list = el('ul', 'tray-items');
    for (const item of items) {
        const chip = el('li', 'tray-chip', item.name);
        const remove = el('button', 'tray-remove', '×');
        remove.type = 'button';
        remove.setAttribute('aria-label', `Remove ${item.name}`);
        remove.addEventListener('click', () => toggleCompare(item));
        chip.append(remove);
        list.append(chip);
    }

    const clear = el('button', 'button-secondary', 'Clear');
    clear.type = 'button';
    clear.addEventListener('click', clearCompare);

    const go = el('a', 'button', `Compare (${items.length})`);
    if (items.length >= 2) {
        go.href = `compare.html?slugs=${items.map((item) => encodeURIComponent(item.slug)).join(',')}`;
    } else {
        go.setAttribute('aria-disabled', 'true');
        go.title = 'Pick at least 2 products';
    }

    inner.append(list, clear, go);
    tray.replaceChildren(inner);
}

