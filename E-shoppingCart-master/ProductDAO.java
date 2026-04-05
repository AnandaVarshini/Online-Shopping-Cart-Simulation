-- Check if image_data column exists
DESCRIBE eshop_db.products;

-- Check products and their image data
SELECT id, name, image, LENGTH(image_data) as image_bytes FROM eshop_db.products WHERE image_data IS NOT NULL LIMIT 5;

-- Check products with NULL image_data
SELECT id, name, image FROM eshop_db.products WHERE image_data IS NULL LIMIT 3;
