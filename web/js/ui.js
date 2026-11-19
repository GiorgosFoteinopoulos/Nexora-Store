export const CATEGORIES = [
    { value: 'PHONE', label: 'Phones' },
    { value: 'LAPTOP', label: 'Laptops' },
    { value: 'TABLET', label: 'Tablets' },
    { value: 'WEARABLE', label: 'Wearables' },
    { value: 'ACCESSORY', label: 'Accessories' },
];

export function categoryLabel(value) {
    return CATEGORIES.find((category) => category.value === value)?.label ?? value;
}

const money = new Map();

export function formatPrice(price) {
    if (!price) {
        return '';
    }
    if (!price) {
        return '';
    }
    if (!money.has(price.currency)) {
        money.set(price.currency, new Intl.NumberFormat('en-IE', { style: 'currency', currency: price.currency}));
    }
    return money.get(price.currency).format(Number(price.amount));
}

export function el(tag, className, text) {
    const node = document.createElement(tag);
    if (className) {
        node.className = className;
    }
    if (text !== undefined && text !== null) {
        node.textContent = text;
    }
    return node;
}

const ICONS = {
    PHONE: '<rect x="7" y="2" width="10" height="20" rx="2"/><path d="M11 18h2"/>',
    LAPTOP: '<rect x="4" y="4" width="16" height="11" rx="1"/><path d="M2 19h20"/>',
    TABLET: '<rect x="4" y="2" width="16" height="20" rx="2"/><path d="M11 18h2"/>',
    WEARABLE: '<rect x="7" y="6" width="10" height="12" rx="3"/><path d="M9 6V2h6v4M9 18v4h6v-4"/>',
    ACCESSORY: '<path d="M9 2v6M15 2v6"/><path d="M6 8h12v4a6 6 0 0 1-12 0z"/><path d="M12 18v4"/>',
};

export function categoryIcon(category) {
    const wrapper = el('span', 'icon');
    wrapper.setAttribute('aria-hidden', 'true');
    wrapper.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"
    stroke-linecap = "round" stroke-linejoin="round">${ICONS[category] ?? ''}</svg>`;
    return wrapper;
}

export function showMessage(container, text, kind = 'info') {
    container.replaceChildren(el('p', `message message-${kind}`, text));
}