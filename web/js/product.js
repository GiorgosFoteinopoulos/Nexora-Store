import { fetchProduct } from './api.js';
import { categoryIcon, categoryLabel, el, formatPrice, showMessage } from './ui.js';
import { heartButton, wishlistSlugs } from './wishlist.js';

const view = document.querySelector('#product');
const slug = new URLSearchParams(location.search).get('slug');

const yesNo = (value) => (value ? 'Yes' : 'No');


const SPEC_LABELS = {
    screenInches: ['Screen', (value) => `${value}″`],
    batteryMah: ['Battery', (value) => `${value.toLocaleString('en')} mAh`],
    fiveG: ['5G', yesNo],
    processor: ['Processor', String],
    ramGb: ['Memory', (value) => `${value} GB`],
    weightKg: ['Weight', (value) => `${value} kg`],
    ultraportable: ['Ultraportable', yesNo],
    stylusSupport: ['Stylus support', yesNo],
    cellular: ['Mobile data', yesNo],
    batteryLifeDays: ['Battery life', (value) => `${value} days`],
    heartRateMonitor: ['Heart-rate monitor', yesNo],
    gps: ['GPS', yesNo],
    fits: ['Works with', (value) => value.map(categoryLabel).join(', ')],
};

if (!slug) {
    showMessage(view, 'No product chosen. Go back to the catalog and pick one.', 'error');
} else {
    load();
}

async function load() {
    try {
        const [product, saved] = await Promise.all([fetchProduct(slug), wishlistSlugs()]);
        render(product, saved.has(product.slug));
    } catch (error) {
        const text = error.status === 404 ? 'This product does not exist (any more).' : error.message;
        showMessage(view, text, 'error');
    }
}

function render(product, isSaved) {
    document.title = `${product.name} · Nexora Store`;

    const crumbs = el('nav', 'crumbs');
    const home = el('a', null, 'Catalog');
    home.href = 'index.html';
    const category = el('a', null, categoryLabel(product.category));
    category.href = `index.html?category=${encodeURIComponent(product.category)}`;
    crumbs.append(home, el('span', null, ' › '), category);

    const thumb = el('div', 'detail-thumb');
    thumb.append(categoryIcon(product.category));

    const info = el('div', 'detail-info');
    info.append(
        el('p', 'card-meta', product.brand.name),
        el('h1', null, product.name),
        el('p', 'detail-price', product.startingPrice ? `from ${formatPrice(product.startingPrice)}` : ''),
        el('p', product.available ? 'stock stock-in' : 'stock stock-out',
            product.available ? 'In stock' : 'Sold out'),
        el('p', 'detail-description', product.description),
        heartButton(product.slug, isSaved, { withText: true }),
    );

    const top = el('div', 'detail');
    top.append(thumb, info);

    view.replaceChildren(crumbs, top, specsSection(product.specs), variantsSection(product.variants));
}

function specsSection(specs) {
    const section = el('section', 'panel');
    section.append(el('h2', null, 'Specifications'));
    const table = el('table', 'table table-specs');
    for (const [key, value] of Object.entries(specs ?? {})) {
        const [label, show] = SPEC_LABELS[key] ?? [key, String];
        const row = table.insertRow();
        row.append(el('th', null, label), el('td', null, show(value)));
    }
    section.append(table);
    return section;
}

function variantsSection(variants) {
    const section = el('section', 'panel');
    section.append(el('h2', null, 'Versions and prices'));
    const table = el('table', 'table');
    const head = table.createTHead().insertRow();
    for (const title of ['Colour', 'Storage', 'Price', 'Availability']) {
        head.append(el('th', null, title));
    }
    const body = table.createTBody();
    for (const variant of variants) {
        const row = body.insertRow();
        row.append(
            el('td', null, variant.colour),
            el('td', null, storage(variant.storageGb)),
            el('td', null, formatPrice(variant.price)),
            el('td', variant.inStock ? 'stock-in' : 'stock-out', variant.inStock ? 'In stock' : 'Sold out'),
        );
    }
    section.append(table);
    return section;
}

function storage(gigabytes) {
    if (gigabytes === null || gigabytes === undefined) {
        return '—';
    }
    return gigabytes >= 1024 ? `${gigabytes / 1024} TB` : `${gigabytes} GB`;
}