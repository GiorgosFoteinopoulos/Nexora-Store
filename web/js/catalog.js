import { fetchBrands, fetchProducts }  from './api.js';
import { CATEGORIES, categoryIcon, categoryLabel, el, formatPrice, showMessage } from "./ui.js";

const PAGE_SIZE = 12;
const FILTER_KEYS = ['category', 'brand', 'minPrice', 'maxPrice', 'q'];

const params = new URLSearchParams(location.search);
const page = Math.max(0, Number.parseInt(params.get('page') ?? '0', 10) || 0);

const form = document.querySelector('#filters');
const grid = document.querySelector('#grid');
const status = document.querySelector('#status');
const pager = document.querySelector('#pager');

fillCategories();
fillFormFromUrl();
form.addEventListener('submit', applyFilters);
loadBrands();
loadProducts();

function fillCategories() {
    const select = form.elements.category;
    for (const category of CATEGORIES) {
        select.append(new Option(category.label, category.value));
    }
}

function fillFormFromUrl() {
    for (const key of FILTER_KEYS) {
        if (form.elements[key]) {
            form.elements[key].value = params.get(key) ?? '';
        }
    }
    document.querySelector('#q').value = params.get('q') ?? '';
}

async function loadBrands() {
    const select = form.elements.brand;
    try {
        for (const brand of await fetchBrands()) {
            select.append(new Option(brand.name, brand.slug));
        }
        select.value = params.get('brand') ?? '';
    } catch {
        select.disabled = true;
    }
}

function applyFilters() {
    event.preventDefault();
    const next = new URLSearchParams();
    for (const key of FILTER_KEYS) {
        const value = form.elements[key]?.value.trim();
        if (value) {
            next.set(key, value);
        }
    }
    location.search = next.toString();
}

async function loadProducts() {
    status.textContent = 'Loading products...';
    const query = { page, size: PAGE_SIZE };
    for (const key of FILTER_KEYS) {
        query[key] = params.get(key);
    }

    try {
        const result = await fetchProducts(query);
        renderGrid(result.content);
        renderStatus(result);
        renderPager(result);
    } catch (error) {
        status.textContent = '';
        showMessage(grid, error.message, 'error');
    }
}

function renderGrid(products) {
    grid.replaceChildren(...products.map(productCard));
}

function productCard(product) {
    const card = el('a', 'card');
    card.href = `product.html?slug=${encodeURIComponent(product.slug)}`;

    const thumb = el('div', 'card-thumb')
    thumb.append(categoryIcon(product.category));
    if (!product.available) {
        thumb.append(el('span', 'badge badge-soldout', 'Sold out'));
    }
    const body = el('div', 'card-body');
    body.append(
        el('p', 'card-meta', `${product.brand.name} · ${categoryLabel(product.category)}`),
        el('h3', 'card-title', product.name),
        el('p', 'card-summary', product.summary),
        el('p', 'card-price', product.startingPrice ? `from ${formatPrice(product.startingPrice)}` : 'Price on request')
    );

    card.append(thumb, body);
    return card;
}

function renderStatus(result) {
    if (result.totalElements === 0) {
        status.textContent = '';
        showMessage(grid, 'No products match these filters.');
        return;
    }
    const noun = result.totalElements === 1 ? 'product' : 'products';
    status.textContent = `${result.totalElements} ${noun}`;
}

function renderPager(result) {
    pager.hidden = result.totalPages <= 1;
    if (pager.hidden) {
        return;
    }
    document.querySelector('#page-info').textContent = `Page ${result.page + 1} of ${result.totalPages}`;
    setPageLink(document.querySelector('#prev'), result.page - 1, result.page > 0);
    setPageLink(document.querySelector('#next'), result.page + 1, result.page + 1 < result.totalPages);
}

function setPageLink(link, targetPage, enabled) {
    if (!enabled) {
        link.removeAttribute('href');
        link.setAttribute('aria-disabled', 'true');
        return;
    }
    const next = new URLSearchParams(params);
    next.set('page', targetPage);
    link.href = `?${next}`;
    link.removeAttribute('aria-disabled');
}