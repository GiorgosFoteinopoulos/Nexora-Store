import { compareProducts } from './api.js';
import { clearCompare } from './compare.js';
import { el, showMessage } from './ui.js';

const view = document.querySelector('#comparison');


const slugs = (new URLSearchParams(location.search).get('slugs') ?? '')
    .split(',')
    .map((slug) => slug.trim())
    .filter(Boolean);

document.querySelector('#clear-compare').addEventListener('click', () => {
    clearCompare();
    location.href = 'index.html';
});

if (slugs.length < 2) {
    showMessage(view, 'Pick 2 to 4 products of the same category in the catalog, then press Compare.');
} else {
    load();
}

async function load() {
    try {
        render(await compareProducts(slugs));
    } catch (error) {
        showMessage(view, error.message, 'error');
    }
}

function render(comparison) {
    document.title = `Compare ${comparison.category} · Nexora Store`;
    const cheapestColumn = comparison.products.indexOf(comparison.cheapest);
    const { Product: names, ...rows } = comparison.table;

    const table = el('table', 'table compare-table');
    const head = table.createTHead().insertRow();
    head.append(el('th'));
    comparison.products.forEach((slug, column) => {
        const cell = el('th', column === cheapestColumn ? 'col-cheapest' : null);
        const link = el('a', 'list-title', names[column]);
        link.href = `product.html?slug=${encodeURIComponent(slug)}`;
        cell.append(link);
        if (column === cheapestColumn) {
            cell.append(el('span', 'badge badge-cheapest', 'Cheapest'));
        }
        head.append(cell);
    });

    const body = table.createTBody();
    for (const [label, values] of Object.entries(rows)) {

        const allSame = values.every((value) => value === values[0]);
        const row = body.insertRow();
        row.className = allSame ? 'row-same' : '';
        row.append(el('th', null, label));
        values.forEach((value, column) => {
            row.append(el('td', column === cheapestColumn ? 'col-cheapest' : null, value));
        });
    }

    const scroller = el('div', 'table-scroll');
    scroller.append(table);
    view.replaceChildren(el('h1', null, `Compare ${comparison.category}`), scroller);
}