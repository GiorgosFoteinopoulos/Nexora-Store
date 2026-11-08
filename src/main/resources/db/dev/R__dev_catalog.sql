
INSERT INTO brands (name, slug) VALUES
    ('Nexora', 'nexora'),
    ('Voltix', 'voltix'),
    ('Lumio',  'lumio')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (category, name, slug, description, brand_id)
SELECT v.category, v.name, v.slug, v.description, b.id
FROM (VALUES
    ('PHONE',     'Nexora Phone 15',      'nexora-phone-15',      'Everyday phone with a 6.1-inch display and all-day battery.',  'nexora'),
    ('PHONE',     'Nexora Phone 15 Pro',  'nexora-phone-15-pro',  'Flagship phone with a 6.7-inch display and titanium frame.',  'nexora'),
    ('PHONE',     'Voltix V9',            'voltix-v9',            'Mid-range phone with a large battery.',                         'voltix'),
    ('PHONE',     'Lumio Lite 2',         'lumio-lite-2',         'Affordable phone for calls, messages and maps.',                'lumio'),
    ('LAPTOP',    'Nexora Book 14',       'nexora-book-14',       'Light 14-inch laptop for work and travel.',                     'nexora'),
    ('LAPTOP',    'Nexora Book Pro 16',   'nexora-book-pro-16',   'Powerful 16-inch laptop for creative work.',                    'nexora'),
    ('LAPTOP',    'Voltix Air 13',        'voltix-air-13',        'Compact 13-inch laptop for students.',                          'voltix'),
    ('TABLET',    'Nexora Tab 11',        'nexora-tab-11',        '11-inch tablet with stylus support.',                           'nexora'),
    ('TABLET',    'Lumio Pad 10',         'lumio-pad-10',         '10-inch tablet with 4G for reading and streaming.',             'lumio'),
    ('WEARABLE',  'Nexora Watch S3',      'nexora-watch-s3',      'Smartwatch with GPS and heart-rate monitor.',                   'nexora'),
    ('WEARABLE',  'Voltix Band 4',        'voltix-band-4',        'Fitness band with two weeks of battery life.',                  'voltix'),
    ('ACCESSORY', 'Nexora 30W Charger',   'nexora-30w-charger',   'USB-C fast charger for phones and tablets.',                    'nexora'),
    ('ACCESSORY', 'Nexora Phone 15 Case', 'nexora-phone-15-case', 'Slim protective case for the Nexora Phone 15.',                 'nexora'),
    ('ACCESSORY', 'Voltix USB-C Hub',     'voltix-usb-c-hub',     '7-in-1 hub for laptops and tablets.',                           'voltix')
) AS v(category, name, slug, description, brand_slug)
JOIN brands b ON b.slug = v.brand_slug
ON CONFLICT (slug) DO NOTHING;

INSERT INTO phones (id, screen_inches, battery_mah, five_g)
SELECT p.id, v.screen_inches, v.battery_mah, v.five_g
FROM (VALUES
    ('nexora-phone-15',     6.1, 4500, true),
    ('nexora-phone-15-pro', 6.7, 5000, true),
    ('voltix-v9',           6.5, 5200, true),
    ('lumio-lite-2',        6.0, 4000, false)
) AS v(slug, screen_inches, battery_mah, five_g)
JOIN products p ON p.slug = v.slug
ON CONFLICT (id) DO NOTHING;

INSERT INTO laptops (id, screen_inches, processor, ram_gb, weight_kg)
SELECT p.id, v.screen_inches, v.processor, v.ram_gb, v.weight_kg
FROM (VALUES
    ('nexora-book-14',     14.0, 'Nexora N3',     16, 1.2),
    ('nexora-book-pro-16', 16.0, 'Nexora N3 Max', 32, 2.1),
    ('voltix-air-13',      13.3, 'Voltix V2',      8, 1.1)
) AS v(slug, screen_inches, processor, ram_gb, weight_kg)
JOIN products p ON p.slug = v.slug
ON CONFLICT (id) DO NOTHING;

INSERT INTO tablets (id, screen_inches, stylus_support, cellular)
SELECT p.id, v.screen_inches, v.stylus_support, v.cellular
FROM (VALUES
    ('nexora-tab-11', 11.0, true,  false),
    ('lumio-pad-10',  10.1, false, true)
) AS v(slug, screen_inches, stylus_support, cellular)
JOIN products p ON p.slug = v.slug
ON CONFLICT (id) DO NOTHING;

INSERT INTO wearables (id, battery_life_days, heart_rate_monitor, gps)
SELECT p.id, v.battery_life_days, v.heart_rate_monitor, v.gps
FROM (VALUES
    ('nexora-watch-s3',  7, true, true),
    ('voltix-band-4',   14, true, false)
) AS v(slug, battery_life_days, heart_rate_monitor, gps)
JOIN products p ON p.slug = v.slug
ON CONFLICT (id) DO NOTHING;

INSERT INTO accessories (id)
SELECT id FROM products
WHERE slug IN ('nexora-30w-charger', 'nexora-phone-15-case', 'voltix-usb-c-hub')
ON CONFLICT (id) DO NOTHING;

INSERT INTO accessory_categories (accessory_id, category)
SELECT p.id, v.category
FROM (VALUES
    ('nexora-30w-charger',   'PHONE'),
    ('nexora-30w-charger',   'TABLET'),
    ('nexora-phone-15-case', 'PHONE'),
    ('voltix-usb-c-hub',     'LAPTOP'),
    ('voltix-usb-c-hub',     'TABLET')
) AS v(slug, category)
JOIN products p ON p.slug = v.slug
ON CONFLICT (accessory_id, category) DO NOTHING;

INSERT INTO product_variants (product_id, sku, colour, storage_gb, price_amount, price_currency, stock)
SELECT p.id, v.sku, v.colour, v.storage_gb, v.price, 'EUR', v.stock
FROM (VALUES
    ('nexora-phone-15',      'NX-PH15-128-BLK',  'Black',      128,   799.00,  25),
    ('nexora-phone-15',      'NX-PH15-256-BLK',  'Black',      256,   899.00,  12),
    ('nexora-phone-15',      'NX-PH15-256-WHT',  'White',      256,   899.00,   0),
    ('nexora-phone-15-pro',  'NX-PH15P-256-TIT', 'Titanium',   256,  1199.00,   8),
    ('nexora-phone-15-pro',  'NX-PH15P-512-TIT', 'Titanium',   512,  1399.00,   3),
    ('voltix-v9',            'VX-V9-128-BLU',    'Blue',       128,   549.00,  40),
    ('voltix-v9',            'VX-V9-256-BLU',    'Blue',       256,   629.00,  18),
    ('lumio-lite-2',         'LM-LITE2-64-GRN',  'Green',       64,   249.00,  60),
    ('nexora-book-14',       'NX-BK14-512-SLV',  'Silver',     512,  1299.00,  10),
    ('nexora-book-14',       'NX-BK14-1TB-SLV',  'Silver',    1024,  1499.00,   4),
    ('nexora-book-pro-16',   'NX-BKP16-1TB-GRY', 'Space Grey', 1024, 2499.00,   2),
    ('voltix-air-13',        'VX-AIR13-256-SLV', 'Silver',     256,   799.00,  15),
    ('nexora-tab-11',        'NX-TAB11-128-GRY', 'Grey',       128,   649.00,  20),
    ('nexora-tab-11',        'NX-TAB11-256-GRY', 'Grey',       256,   749.00,   9),
    ('lumio-pad-10',         'LM-PAD10-64-BLU',  'Blue',        64,   299.00,  30),
    ('nexora-watch-s3',      'NX-WS3-45-BLK',    'Black',      NULL,  399.00,  14),
    ('voltix-band-4',        'VX-BAND4-BLK',     'Black',      NULL,   59.00, 100),
    ('nexora-30w-charger',   'NX-CHG30-WHT',     'White',      NULL,   39.00,  80),
    ('nexora-phone-15-case', 'NX-CASE15-CLR',    'Clear',      NULL,   29.00,  50),
    ('nexora-phone-15-case', 'NX-CASE15-BLK',    'Black',      NULL,   29.00,   0),
    ('voltix-usb-c-hub',     'VX-HUB7-GRY',      'Grey',       NULL,   49.00,  35)
) AS v(slug, sku, colour, storage_gb, price, stock)
JOIN products p ON p.slug = v.slug
ON CONFLICT (sku) DO NOTHING;