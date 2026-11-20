import { addToWishlist, fetchWishlist, removeFromWishlist } from './api.js';
import { getSession, loginUrl } from './session.js';
import { el, toast } from './ui.js';

let saved = null;


export function wishlistSlugs() {
    if (!getSession()) {
        return Promise.resolve(new Set());
    }
    saved ??= fetchWishlist()
        .then((items) => new Set(items.map((item) => item.slug)))
        .catch(() => new Set());
    return saved;
}


export function heartButton(slug, isSaved, { withText = false } = {}) {
    const button = el('button', withText ? 'heart heart-text' : 'heart');
    button.type = 'button';
    paint(button, isSaved, withText);

    button.addEventListener('click', async (event) => {
        event.preventDefault();
        if (!getSession()) {
            location.href = loginUrl();
            return;
        }
        const wasSaved = button.getAttribute('aria-pressed') === 'true';
        paint(button, !wasSaved, withText);
        button.disabled = true;
        try {
            await (wasSaved ? removeFromWishlist(slug) : addToWishlist(slug));
            toast(wasSaved ? 'Removed from your wishlist' : 'Saved to your wishlist');
        } catch (error) {
            paint(button, wasSaved, withText);
            if (error.status === 401) {
                location.href = loginUrl();
                return;
            }
            toast(error.message, 'error');
        } finally {
            button.disabled = false;
        }
    });
    return button;
}

function paint(button, isSaved, withText) {
    button.setAttribute('aria-pressed', String(isSaved));
    button.setAttribute('aria-label', isSaved ? 'Remove from wishlist' : 'Save to wishlist');
    button.title = isSaved ? 'Remove from wishlist' : 'Save to wishlist';
    const heart = isSaved ? '♥' : '♡';
    button.textContent = withText ? `${heart} ${isSaved ? 'Saved' : 'Save to wishlist'}` : heart;
}