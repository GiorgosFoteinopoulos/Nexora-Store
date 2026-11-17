
INSERT INTO brands (name, slug) VALUES
    ('Nexora', 'nexora'),
    ('Voltix', 'voltix'),
    ('Lumio',  'lumio')
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (category, name, slug, description, brand_id)
SELECT v.category, v.name, v.slug, v.description, b.id
FROM (VALUES
    ('PHONE', 'Nexora Phone 15', 'nexora-phone-15',
     'Everyday phone with a bright 6.1-inch display that is easy to use with one hand. The 4,500 mAh battery lasts a full day of messaging, maps and photos. A safe choice for most people who want a reliable phone without paying flagship prices.', 'nexora'),
    ('PHONE', 'Nexora Phone 15 Pro', 'nexora-phone-15-pro',
     'Flagship phone with a 6.7-inch display, a titanium frame and the best Nexora camera, with optical zoom and excellent night photos. Made for photography, video recording and demanding apps. 5G and fast charging.', 'nexora'),
    ('PHONE', 'Voltix V9', 'voltix-v9',
     'Mid-range phone with a large 5,200 mAh battery that comfortably lasts two days of normal use. Great value for heavy users, commuters and anyone who often forgets to charge.', 'voltix'),
    ('PHONE', 'Lumio Lite 2', 'lumio-lite-2',
     'Simple, affordable phone for calls, messages, maps and social apps. A good first phone for teenagers or a dependable phone for older users. No 5G.', 'lumio'),
    ('PHONE', 'Nexora Phone 15 Mini', 'nexora-phone-15-mini',
     'Compact phone with a 5.8-inch display that fits small hands and pockets. Flagship-level speed in a light body. The battery lasts a normal day, heavy users may need to charge in the evening.', 'nexora'),
    ('PHONE', 'Nexora Phone 14', 'nexora-phone-14',
     'The previous Nexora generation at a lower price. Still fast, with a good camera and years of software updates. A smart pick for students and anyone on a budget who wants a premium feel.', 'nexora'),
    ('PHONE', 'Voltix V9 Pro', 'voltix-v9-pro',
     'Large-screen phone with a 6.7-inch 120 Hz display and a 5,500 mAh battery, tuned for mobile gaming and streaming. Stays cool during long gaming sessions and charges fully in about 40 minutes.', 'voltix'),
    ('PHONE', 'Voltix V7', 'voltix-v7',
     'Budget 5G phone with a 6.5-inch screen and a 5,000 mAh battery. Covers calls, social media, video and maps at a very low price.', 'voltix'),
    ('PHONE', 'Voltix Rugged R2', 'voltix-rugged-r2',
     'Tough phone with a reinforced shell, water and dust resistance and a 6,000 mAh battery. Built for outdoor work, hiking, camping and construction sites. Survives drops that would break most phones.', 'voltix'),
    ('PHONE', 'Lumio Lite 3', 'lumio-lite-3',
     'The newest affordable Lumio phone, now with 5G and a brighter 6.3-inch screen. Easy menus and a long battery make it a great first smartphone.', 'lumio'),
    ('PHONE', 'Lumio Easy', 'lumio-easy',
     'Phone designed for older users: large text and icons, a loud speaker, an emergency button and a very simple home screen. Long battery life and an affordable price.', 'lumio'),
    ('PHONE', 'Lumio Note 6', 'lumio-note-6',
     'Big-screen phone with a 6.9-inch display, ideal for reading, watching video and anyone who prefers larger text, at a mid-range price.', 'lumio'),
    ('LAPTOP', 'Nexora Book 14', 'nexora-book-14',
     'Light 14-inch laptop that weighs only 1.2 kg and lasts a full working day on battery. Perfect for work, travel and university, it fits in any bag. Quiet and fast for office apps, browsing and video calls.', 'nexora'),
    ('LAPTOP', 'Nexora Book Pro 16', 'nexora-book-pro-16',
     'Powerful 16-inch laptop for video editing, 3D work, software development and other creative work. 32 GB of memory and a colour-accurate display. Heavier at 2.1 kg, best used at a desk.', 'nexora'),
    ('LAPTOP', 'Voltix Air 13', 'voltix-air-13',
     'Compact, very light 13-inch laptop for students. Handles note-taking, essays, browsing and streaming without trouble. Affordable and easy to carry between classes.', 'voltix'),
    ('LAPTOP', 'Nexora Book 13', 'nexora-book-13',
     'Ultra-light 13-inch laptop under 1 kg for frequent travellers. Up to 18 hours of battery and a premium aluminium body. Ideal for business trips and working from anywhere.', 'nexora'),
    ('LAPTOP', 'Nexora Book Pro 14', 'nexora-book-pro-14',
     'Portable professional laptop for developers and designers who travel: 32 GB of memory in a 14-inch body that weighs 1.6 kg. Excellent screen and long battery life.', 'nexora'),
    ('LAPTOP', 'Voltix Air 15', 'voltix-air-15',
     'Larger-screen student laptop with a 15.6-inch display and a full keyboard with number pad. Good for spreadsheets, programming courses and multitasking, at a fair price.', 'voltix'),
    ('LAPTOP', 'Voltix Air 13 Plus', 'voltix-air-13-plus',
     'Upgraded Voltix Air with 16 GB of memory and a faster processor, for students who also code or edit photos. Still light at 1.2 kg.', 'voltix'),
    ('LAPTOP', 'Voltix Gamer G7', 'voltix-gamer-g7',
     'Gaming laptop with a dedicated graphics card and a fast 165 Hz 16-inch display. Runs modern games at high settings and handles video editing easily. Heavy and power-hungry, best on a desk and plugged in.', 'voltix'),
    ('LAPTOP', 'Voltix Gamer G5', 'voltix-gamer-g5',
     'Entry-level gaming laptop for popular online games and esports titles. Also capable of photo and video editing on a budget.', 'voltix'),
    ('LAPTOP', 'Lumio Book 14', 'lumio-book-14',
     'Budget 14-inch laptop for homework, browsing, email and video calls. Simple and dependable for families and schools.', 'lumio'),
    ('LAPTOP', 'Lumio Flip 14', 'lumio-flip-14',
     '2-in-1 laptop with a touchscreen that folds back into a tablet. Handy for handwritten notes, drawing, presentations and watching videos in bed.', 'lumio'),
    ('TABLET', 'Nexora Tab 11', 'nexora-tab-11',
     '11-inch tablet with stylus support for handwritten notes, sketching and reading. A great companion for students and creatives, and it works with a keyboard cover.', 'nexora'),
    ('TABLET', 'Lumio Pad 10', 'lumio-pad-10',
     'Affordable 10-inch tablet with 4G, so it works anywhere without Wi-Fi. Made for reading, streaming, video calls and travel.', 'lumio'),
    ('TABLET', 'Nexora Tab 13 Pro', 'nexora-tab-13-pro',
     'Large 13-inch professional tablet for illustrators and designers, with precise stylus support and a laptop-class chip. Mobile data built in, so it works on the go.', 'nexora'),
    ('TABLET', 'Nexora Tab Mini 8', 'nexora-tab-mini-8',
     'Small 8-inch tablet that fits in a jacket pocket. Ideal for reading e-books, maps and quick notes on the go, with stylus support.', 'nexora'),
    ('TABLET', 'Voltix Tab 10', 'voltix-tab-10',
     'Budget 10-inch tablet for kids and family entertainment: videos, games and e-books. Durable and easy to use.', 'voltix'),
    ('TABLET', 'Voltix Tab 12', 'voltix-tab-12',
     'Large-screen tablet with stylus support at a mid-range price. Good for films, split-screen work and drawing.', 'voltix'),
    ('TABLET', 'Lumio Pad Kids 8', 'lumio-pad-kids-8',
     'Kid-friendly 8-inch tablet with a shock-absorbing case and parental controls. A safe first tablet for young children.', 'lumio'),
    ('WEARABLE', 'Nexora Watch S3', 'nexora-watch-s3',
     'Smartwatch with GPS, heart-rate monitor and notifications from your phone. Tracks runs and bike rides without carrying the phone. About a week of battery.', 'nexora'),
    ('WEARABLE', 'Voltix Band 4', 'voltix-band-4',
     'Light fitness band that counts steps, sleep and heart rate, with two weeks of battery life. Simple and affordable.', 'voltix'),
    ('WEARABLE', 'Nexora Watch Ultra', 'nexora-watch-ultra',
     'Rugged sports watch with precise GPS, a titanium case and two weeks of battery. Built for marathon training, hiking and outdoor adventures.', 'nexora'),
    ('WEARABLE', 'Nexora Watch SE', 'nexora-watch-se',
     'Entry-level smartwatch for notifications, workouts and heart-rate tracking at a lower price. Pairs with your phone for GPS.', 'nexora'),
    ('WEARABLE', 'Voltix Band 5', 'voltix-band-5',
     'The newest Voltix fitness band, now with built-in GPS for runs and three weeks of battery.', 'voltix'),
    ('WEARABLE', 'Lumio Fit', 'lumio-fit',
     'Budget fitness tracker with heart-rate and sleep monitoring. Simple, light and comfortable for everyday wear.', 'lumio'),
    ('WEARABLE', 'Voltix Kids Watch', 'voltix-kids-watch',
     'Watch for children with GPS location, so parents can see where their child is, and calls to a few trusted contacts.', 'voltix'),
    ('ACCESSORY', 'Nexora 30W Charger', 'nexora-30w-charger',
     'Small 30 W USB-C fast charger for phones and tablets. Light enough to keep in any bag.', 'nexora'),
    ('ACCESSORY', 'Nexora Phone 15 Case', 'nexora-phone-15-case',
     'Slim protective case for the Nexora Phone 15, with raised edges that protect the screen and camera.', 'nexora'),
    ('ACCESSORY', 'Voltix USB-C Hub', 'voltix-usb-c-hub',
     '7-in-1 USB-C hub that adds HDMI, USB-A, SD card and Ethernet ports to laptops and tablets.', 'voltix'),
    ('ACCESSORY', 'Nexora 65W Charger', 'nexora-65w-charger',
     'Powerful 65 W USB-C charger that charges a laptop, a tablet or a phone. One charger for all your devices when travelling.', 'nexora'),
    ('ACCESSORY', 'Nexora Pencil 2', 'nexora-pencil-2',
     'Precise stylus for Nexora tablets with pressure and tilt sensitivity, for handwritten notes and drawing.', 'nexora'),
    ('ACCESSORY', 'Nexora Tab 11 Keyboard Cover', 'nexora-tab-11-keyboard-cover',
     'Keyboard cover that turns the Nexora Tab 11 into a small laptop for writing on the go.', 'nexora'),
    ('ACCESSORY', 'Voltix Laptop Sleeve 14', 'voltix-laptop-sleeve-14',
     'Padded, water-resistant sleeve for 13 and 14 inch laptops. Slim enough to slide into a backpack.', 'voltix'),
    ('ACCESSORY', 'Lumio Power Bank 20000', 'lumio-power-bank-20000',
     '20,000 mAh power bank that charges a phone about four times. Ideal for travel, festivals and long days out.', 'lumio'),
    ('ACCESSORY', 'Voltix Sport Strap', 'voltix-sport-strap',
     'Breathable sport strap for Voltix and Nexora watches, comfortable during workouts.', 'voltix')
) AS v(category, name, slug, description, brand_slug)
JOIN brands b ON b.slug = v.brand_slug
ON CONFLICT (slug) DO UPDATE SET description = EXCLUDED.description;

INSERT INTO phones (id, screen_inches, battery_mah, five_g)
SELECT p.id, v.screen_inches, v.battery_mah, v.five_g
FROM (VALUES
    ('nexora-phone-15',      6.1, 4500, true),
    ('nexora-phone-15-pro',  6.7, 5000, true),
    ('voltix-v9',            6.5, 5200, true),
    ('lumio-lite-2',         6.0, 4000, false),
    ('nexora-phone-15-mini', 5.8, 3800, true),
    ('nexora-phone-14',      6.1, 4300, true),
    ('voltix-v9-pro',        6.7, 5500, true),
    ('voltix-v7',            6.5, 5000, true),
    ('voltix-rugged-r2',     6.3, 6000, false),
    ('lumio-lite-3',         6.3, 4500, true),
    ('lumio-easy',           6.1, 4200, false),
    ('lumio-note-6',         6.9, 5000, true)
) AS v(slug, screen_inches, battery_mah, five_g)
JOIN products p ON p.slug = v.slug
ON CONFLICT (id) DO NOTHING;

INSERT INTO laptops (id, screen_inches, processor, ram_gb, weight_kg)
SELECT p.id, v.screen_inches, v.processor, v.ram_gb, v.weight_kg
FROM (VALUES
    ('nexora-book-14',     14.0, 'Nexora N3',     16, 1.2),
    ('nexora-book-pro-16', 16.0, 'Nexora N3 Max', 32, 2.1),
    ('voltix-air-13',      13.3, 'Voltix V2',      8, 1.1),
    ('nexora-book-13',     13.3, 'Nexora N3',     16, 0.9),
    ('nexora-book-pro-14', 14.2, 'Nexora N3 Pro', 32, 1.6),
    ('voltix-air-15',      15.6, 'Voltix V2',     16, 1.7),
    ('voltix-air-13-plus', 13.3, 'Voltix V3',     16, 1.2),
    ('voltix-gamer-g7',    16.0, 'Voltix V5 RTX', 32, 2.6),
    ('voltix-gamer-g5',    15.6, 'Voltix V4 GTX', 16, 2.3),
    ('lumio-book-14',      14.0, 'Lumio L1',       8, 1.5),
    ('lumio-flip-14',      14.0, 'Lumio L2',       8, 1.4)
) AS v(slug, screen_inches, processor, ram_gb, weight_kg)
JOIN products p ON p.slug = v.slug
ON CONFLICT (id) DO NOTHING;

INSERT INTO tablets (id, screen_inches, stylus_support, cellular)
SELECT p.id, v.screen_inches, v.stylus_support, v.cellular
FROM (VALUES
    ('nexora-tab-11',     11.0, true,  false),
    ('lumio-pad-10',      10.1, false, true),
    ('nexora-tab-13-pro', 12.9, true,  true),
    ('nexora-tab-mini-8',  8.3, true,  false),
    ('voltix-tab-10',     10.4, false, false),
    ('voltix-tab-12',     12.4, true,  false),
    ('lumio-pad-kids-8',   8.0, false, false)
) AS v(slug, screen_inches, stylus_support, cellular)
JOIN products p ON p.slug = v.slug
ON CONFLICT (id) DO NOTHING;

INSERT INTO wearables (id, battery_life_days, heart_rate_monitor, gps)
SELECT p.id, v.battery_life_days, v.heart_rate_monitor, v.gps
FROM (VALUES
    ('nexora-watch-s3',     7, true,  true),
    ('voltix-band-4',      14, true,  false),
    ('nexora-watch-ultra', 14, true,  true),
    ('nexora-watch-se',     5, true,  false),
    ('voltix-band-5',      21, true,  true),
    ('lumio-fit',          10, true,  false),
    ('voltix-kids-watch',   3, false, true)
) AS v(slug, battery_life_days, heart_rate_monitor, gps)
JOIN products p ON p.slug = v.slug
ON CONFLICT (id) DO NOTHING;

INSERT INTO accessories (id)
SELECT id FROM products
WHERE slug IN ('nexora-30w-charger', 'nexora-phone-15-case', 'voltix-usb-c-hub',
               'nexora-65w-charger', 'nexora-pencil-2', 'nexora-tab-11-keyboard-cover',
               'voltix-laptop-sleeve-14', 'lumio-power-bank-20000', 'voltix-sport-strap')
ON CONFLICT (id) DO NOTHING;

INSERT INTO accessory_categories (accessory_id, category)
SELECT p.id, v.category
FROM (VALUES
    ('nexora-30w-charger',           'PHONE'),
    ('nexora-30w-charger',           'TABLET'),
    ('nexora-phone-15-case',         'PHONE'),
    ('voltix-usb-c-hub',             'LAPTOP'),
    ('voltix-usb-c-hub',             'TABLET'),
    ('nexora-65w-charger',           'PHONE'),
    ('nexora-65w-charger',           'TABLET'),
    ('nexora-65w-charger',           'LAPTOP'),
    ('nexora-pencil-2',              'TABLET'),
    ('nexora-tab-11-keyboard-cover', 'TABLET'),
    ('voltix-laptop-sleeve-14',      'LAPTOP'),
    ('lumio-power-bank-20000',       'PHONE'),
    ('lumio-power-bank-20000',       'TABLET'),
    ('voltix-sport-strap',           'WEARABLE')
) AS v(slug, category)
JOIN products p ON p.slug = v.slug
ON CONFLICT (accessory_id, category) DO NOTHING;

INSERT INTO product_variants (product_id, sku, colour, storage_gb, price_amount, price_currency, stock)
SELECT p.id, v.sku, v.colour, v.storage_gb, v.price, 'EUR', v.stock
FROM (VALUES

    ('nexora-phone-15',      'NX-PH15-128-BLK',   'Black',      128,   799.00,  25),
    ('nexora-phone-15',      'NX-PH15-256-BLK',   'Black',      256,   899.00,  12),
    ('nexora-phone-15',      'NX-PH15-256-WHT',   'White',      256,   899.00,   0),
    ('nexora-phone-15-pro',  'NX-PH15P-256-TIT',  'Titanium',   256,  1199.00,   8),
    ('nexora-phone-15-pro',  'NX-PH15P-512-TIT',  'Titanium',   512,  1399.00,   3),
    ('voltix-v9',            'VX-V9-128-BLU',     'Blue',       128,   549.00,  40),
    ('voltix-v9',            'VX-V9-256-BLU',     'Blue',       256,   629.00,  18),
    ('lumio-lite-2',         'LM-LITE2-64-GRN',   'Green',       64,   249.00,  60),
    ('nexora-phone-15-mini', 'NX-PH15M-128-BLK',  'Black',      128,   699.00,  20),
    ('nexora-phone-15-mini', 'NX-PH15M-128-PNK',  'Pink',       128,   699.00,   6),
    ('nexora-phone-14',      'NX-PH14-128-BLK',   'Black',      128,   649.00,  15),
    ('voltix-v9-pro',        'VX-V9P-256-BLK',    'Black',      256,   799.00,  22),
    ('voltix-v9-pro',        'VX-V9P-512-BLK',    'Black',      512,   899.00,   7),
    ('voltix-v7',            'VX-V7-128-GRY',     'Grey',       128,   299.00,  45),
    ('voltix-rugged-r2',     'VX-R2-128-ORG',     'Orange',     128,   449.00,  12),
    ('lumio-lite-3',         'LM-LITE3-128-GRN',  'Green',      128,   279.00,  50),
    ('lumio-lite-3',         'LM-LITE3-128-BLU',  'Blue',       128,   279.00,  35),
    ('lumio-easy',           'LM-EASY-32-BLK',    'Black',       32,   149.00,  25),
    ('lumio-note-6',         'LM-NOTE6-256-BLK',  'Black',      256,   449.00,  18),
    ('nexora-book-14',       'NX-BK14-512-SLV',   'Silver',     512,  1299.00,  10),
    ('nexora-book-14',       'NX-BK14-1TB-SLV',   'Silver',    1024,  1499.00,   4),
    ('nexora-book-pro-16',   'NX-BKP16-1TB-GRY',  'Space Grey', 1024, 2499.00,   2),
    ('voltix-air-13',        'VX-AIR13-256-SLV',  'Silver',     256,   799.00,  15),
    ('nexora-book-13',       'NX-BK13-512-GLD',   'Gold',       512,  1399.00,   8),
    ('nexora-book-pro-14',   'NX-BKP14-1TB-GRY',  'Space Grey', 1024, 1999.00,   5),
    ('nexora-book-pro-14',   'NX-BKP14-2TB-GRY',  'Space Grey', 2048, 2399.00,   2),
    ('voltix-air-15',        'VX-AIR15-512-SLV',  'Silver',     512,   899.00,  14),
    ('voltix-air-13-plus',   'VX-AIR13P-512-SLV', 'Silver',     512,  1049.00,  10),
    ('voltix-gamer-g7',      'VX-G7-1TB-BLK',     'Black',     1024,  1899.00,   6),
    ('voltix-gamer-g5',      'VX-G5-512-BLK',     'Black',      512,  1099.00,   9),
    ('lumio-book-14',        'LM-BK14-256-GRY',   'Grey',       256,   449.00,  20),
    ('lumio-flip-14',        'LM-FLIP14-256-SLV', 'Silver',     256,   599.00,  11),
    ('nexora-tab-11',        'NX-TAB11-128-GRY',  'Grey',       128,   649.00,  20),
    ('nexora-tab-11',        'NX-TAB11-256-GRY',  'Grey',       256,   749.00,   9),
    ('lumio-pad-10',         'LM-PAD10-64-BLU',   'Blue',        64,   299.00,  30),
    ('nexora-tab-13-pro',    'NX-TAB13P-256-GRY', 'Grey',       256,  1099.00,   6),
    ('nexora-tab-13-pro',    'NX-TAB13P-512-GRY', 'Grey',       512,  1299.00,   3),
    ('nexora-tab-mini-8',    'NX-TABM8-128-PRP',  'Purple',     128,   499.00,  12),
    ('voltix-tab-10',        'VX-TAB10-64-BLU',   'Blue',        64,   179.00,  40),
    ('voltix-tab-12',        'VX-TAB12-128-GRY',  'Grey',       128,   449.00,  16),
    ('lumio-pad-kids-8',     'LM-KIDS8-32-RED',   'Red',         32,   129.00,  30),
    ('nexora-watch-s3',      'NX-WS3-45-BLK',     'Black',      NULL,  399.00,  14),
    ('voltix-band-4',        'VX-BAND4-BLK',      'Black',      NULL,   59.00, 100),
    ('nexora-watch-ultra',   'NX-WU-49-TIT',      'Titanium',   NULL,  899.00,   0),
    ('nexora-watch-se',      'NX-WSE-40-SLV',     'Silver',     NULL,  249.00,  20),
    ('nexora-watch-se',      'NX-WSE-44-BLK',     'Black',      NULL,  279.00,  18),
    ('voltix-band-5',        'VX-BAND5-BLK',      'Black',      NULL,   89.00,  70),
    ('lumio-fit',            'LM-FIT-BLK',        'Black',      NULL,   39.00,  90),
    ('voltix-kids-watch',    'VX-KIDS-BLU',       'Blue',       NULL,   99.00,  25),
    ('nexora-30w-charger',   'NX-CHG30-WHT',      'White',      NULL,   39.00,  80),
    ('nexora-phone-15-case', 'NX-CASE15-CLR',     'Clear',      NULL,   29.00,  50),
    ('nexora-phone-15-case', 'NX-CASE15-BLK',     'Black',      NULL,   29.00,   0),
    ('voltix-usb-c-hub',     'VX-HUB7-GRY',       'Grey',       NULL,   49.00,  35),
    ('nexora-65w-charger',           'NX-CHG65-WHT',  'White', NULL,  59.00,  40),
    ('nexora-pencil-2',              'NX-PEN2-WHT',   'White', NULL, 119.00,  25),
    ('nexora-tab-11-keyboard-cover', 'NX-KB11-BLK',   'Black', NULL, 179.00,  10),
    ('voltix-laptop-sleeve-14',      'VX-SLV14-GRY',  'Grey',  NULL,  29.00,  60),
    ('lumio-power-bank-20000',       'LM-PB20K-BLK',  'Black', NULL,  45.00,  55),
    ('voltix-sport-strap',           'VX-STRAP-BLK',  'Black', NULL,  19.00, 120),
    ('voltix-sport-strap',           'VX-STRAP-GRN',  'Green', NULL,  19.00,   0)
) AS v(slug, sku, colour, storage_gb, price, stock)
JOIN products p ON p.slug = v.slug
ON CONFLICT (sku) DO NOTHING;