import { clearSession, getSession} from "./session.js";

const API_BASE = location.port === '5500' ? 'http://localhost:8080/api' : '/api';

export class ApiError extends Error {
    constructor(status, problem) {
        super(problem?.detail ?? `Request failed (${status}).`);
        this.status = status;
        this.problem = problem ?? {};
    }
}

async function request(method, path, { params = {}, body, auth = false } = {}) {
    const url = new URL(API_BASE + path, location.origin);
    for (const [key, value] of Object.entries(params)) {
        if (value !== undefined && value !== null && value !== '') {
            url.searchParams.set(key, value);
        }
    }

    const options = { method, headers: { Accept: 'application/json' } };
    if (body !== undefined) {
        options.headers['Content-Type'] = 'application/json';
        options.body = JSON.stringify(body);
    }
    if (auth) {
        const session = getSession();
        if (!session) {
            throw new ApiError(401, null, 'Please log in first.');
        }
        options.headers.Authorization = `Bearer ${session.token}`;
    }

    let response;
    try {
        response = await fetch(url, options);
    } catch {
        throw new Error('Cannot reach the server. Is the backend running?');
    }

    if (!response.ok) {
        let problem = null;
        try {
            problem = await response.json();
        } catch {

        }
        if (response.status === 401 && auth) {
            clearSession();
            throw new ApiError(401, 'Your session has expired. Please log in again.');
        }
        throw new ApiError(response.status, problem);
    }
    return response.status === 204 ? null : response.json();
}

export const fetchBrands = () => request('GET', '/brands');
export const fetchProducts = (params) => request('GET', '/products', { params });
export const fetchProduct = (slug) => request('GET', `/products/${encodeURIComponent(slug)}`);

export const register = (email, password) => request('POST', '/auth/register', { body: { email, password } });
export const login = (email, password) => request('POST', '/auth/login', { body: { email, password} });

export const fetchMyAccount = () => request('GET', '/me', {auth: true });