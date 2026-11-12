
begin;

drop index if exists ix_product_variants_product_id;


insert into products (brand_id, name, slug, description, category)
select (select min(id) from brands), 'Bulk ' || g, 'bulk-' || g, 'Bulk test product', 'PHONE'
from generate_series(1, 20000) as g;

insert into product_variants (product_id, sku, colour, storage_gb, price_amount, price_currency, stock)
select p.id, 'BULK-' || p.id || '-' || v, 'Black', 128, 499.00, 'EUR', 5
from products p cross join generate_series(1, 5) as v
where p.slug like 'bulk-%';


analyze products;
analyze product_variants;


select id as pid from products where slug = 'bulk-12345' \gset


explain analyze select * from product_variants where product_id = :pid;


create index ix_lab_product_id on product_variants (product_id);
analyze product_variants;
explain analyze select * from product_variants where product_id = :pid;

rollback;