import { fetchBrands, fetchProducts, searchByMeaning } from './api.js';
import { CATEGORIES, categoryIcon, categoryLabel, el, formatPrice, showMessage } from './ui.js';
import { heartButton, wishlistSlugs } from './wishlist.js';
import { compareButton, showCompareTray } from './compare.js';

const PAGE_SIZE = 12;
const FILTER_KEYS = ['category', 'brand', 'minPrice', 'maxPrice', 'q'];


const params = new URLSearchParams(location.search);
const page = Math.max(0, Number.parseInt(params.get('page') ?? '0', 10) || 0);

const form = document.querySelector('#filters');
const grid = document.querySelector('#grid');
const status = document.querySelector('#status');
const pager = document.querySelector('#pager');

showCompareTray();
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
    form.elements.meaning.checked = params.get('meaning') === '1';
}

async function loadBrands() {
    const select = form.elements.brand;
    try {
        for (const brand of await fetchBrands()) {
            select.append(new Option(brand.name, brand.slug));
        }
        select.value = params.get('brand') ?? ''; n
    } catch {
        select.disabled = true;
    }
}

function applyFilters(event) {
    event.preventDefault();
    const next = new URLSearchParams();
    for (const key of FILTER_KEYS) {
        const value = form.elements[key]?.value.trim();
        if (value) {
            next.set(key, value);
        }
    }
    if (form.elements.meaning.checked) {
        next.set('meaning', '1');
    }
    location.search = next.toString();
}

async function loadProducts() {
    if (params.get('meaning') === '1' && params.get('q')) {
        loadByMeaning();
        return;
    }
    status.textContent = 'Loading products…';
    const query = { page, size: PAGE_SIZE };
    for (const key of FILTER_KEYS) {
        query[key] = params.get(key);
    }

    try {
        const [result, saved] = await Promise.all([fetchProducts(query), wishlistSlugs()]);
        renderGrid(result.content, saved);
        renderStatus(result);
        renderPager(result);
    } catch (error) {
        status.textContent = '';
        showMessage(grid, error.message, 'error');
    }
}

async function loadByMeaning() {
    status.textContent = 'Searching by meaning…';
    pager.hidden = true; // semantic search returns the best matches, not pages
    try {
        const [result, saved] = await Promise.all([
            searchByMeaning({
                q: params.get('q'),
                category: params.get('category'),
                maxPrice: params.get('maxPrice'),
                limit: 10,
            }),
            wishlistSlugs(),
        ]);
        const products = result.hits.map(fromHit);
        renderGrid(products, saved);
        if (products.length === 0) {
            status.textContent = '';
            showMessage(grid, 'Nothing found by meaning. Try other words, or untick "Search by meaning".');
            return;
        }
        status.textContent = `Best ${products.length} matches for "${result.query}", most similar first`;
    } catch (error) {
        status.textContent = '';
        showMessage(grid, error.message, 'error');
    }
}


function fromHit(hit) {
    return {
        slug: hit.slug,
        name: hit.name,
        category: hit.category,
        brand: { name: hit.brand },
        startingPrice: hit.startingPrice === null ? null : { amount: String(hit.startingPrice), currency: 'EUR' },
        available: hit.available,
        summary: `${Math.round(hit.similarity * 100)}% match`,
    };
}

function renderGrid(products, saved) {
    grid.replaceChildren(...products.map((product) => productCard(product, saved.has(product.slug))));
}

function productCard(product, isSaved) {

    const wrapper = el('article', 'card-wrap');

    const card = el('a', 'card');
    card.href = `product.html?slug=${encodeURIComponent(product.slug)}`;

    const thumb = el('div', 'card-thumb');
    thumb.append(categoryIcon(product.category));
    if (!product.available) {
        thumb.append(el('span', 'badge badge-soldout', 'Sold out'));
    }

    const body = el('div', 'card-body');
    body.append(
        el('p', 'card-meta', `${product.brand.name} · ${categoryLabel(product.category)}`),
        el('h3', 'card-title', product.name),
        el('p', 'card-summary', product.summary),
        el('p', 'card-price', product.startingPrice ? `from ${formatPrice(product.startingPrice)}` : 'Price on request'),
    );

    card.append(thumb, body);
    wrapper.append(card, heartButton(product.slug, isSaved), compareButton(product));
    return wrapper;
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