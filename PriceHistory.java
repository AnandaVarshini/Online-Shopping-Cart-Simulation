-- =====================================================
-- Database Migration Script: Image BLOB Storage
-- =====================================================
-- This script migrates the E-Shop database from 
-- filesystem-based image storage to database BLOB storage
-- 
-- IMPORTANT: Backup your database before running this!
-- =====================================================

-- Step 1: Add the image_data LONGBLOB column to products table
ALTER TABLE products 
ADD COLUMN image_data LONGBLOB COMMENT 'Binary image data stored as BLOB';

-- Step 2: Verify the column was added successfully
DESCRIBE products;

-- Step 3: Optional - Check current data structure
SELECT 
    COLUMN_NAME, 
    COLUMN_TYPE, 
    IS_NULLABLE, 
    COLUMN_KEY, 
    EXTRA 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'products' 
ORDER BY ORDINAL_POSITION;

-- Step 4: Verify no errors
-- If all steps execute successfully, you're ready to use the new image storage system!

-- To check image sizes after adding images (Optional):
-- SELECT id, name, 
--     IFNULL(CHAR_LENGTH(image_data), 0) as image_bytes,
--     ROUND(CHAR_LENGTH(image_data)/1024/1024, 2) as image_mb
-- FROM products 
-- WHERE image_data IS NOT NULL
-- ORDER BY image_bytes DESC;

-- To verify products table size (Optional):
-- SELECT 
--     table_name,
--     ROUND(((data_length + index_length) / 1024 / 1024), 2) as size_mb
-- FROM INFORMATION_SCHEMA.TABLES
-- WHERE table_schema = DATABASE()
-- AND table_name = 'products';
