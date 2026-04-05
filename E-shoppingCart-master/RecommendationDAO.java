-- ============================================================================
-- E-Shopping Cart Database Migration: Image Storage (Filesystem to BLOB)
-- ============================================================================
-- This migration script adds support for storing product images directly in 
-- the database as LONGBLOB data instead of referencing filesystem files.
--
-- IMPORTANT: Backup your database before running this migration!
-- ============================================================================

-- ============================================================================
-- Step 1: Add image_data column to products table
-- ============================================================================
-- This column stores the binary image data (BLOB) for each product
-- The existing 'image' column stores the image filename reference
ALTER TABLE products 
ADD COLUMN image_data LONGBLOB NULL 
COMMENT 'Binary image data (JPEG/PNG/GIF) stored as BLOB';

-- ============================================================================
-- Step 2: Create index on products table for better query performance
-- ============================================================================
-- This helps speed up queries that fetch image data
CREATE INDEX idx_products_image_data ON products(id);

-- ============================================================================
-- Step 3: Verify the schema change
-- ============================================================================
-- Run this query to verify the new column was added successfully
-- SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_COMMENT 
-- FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_NAME = 'products' AND COLUMN_NAME IN ('image_data', 'imagePath');

-- ============================================================================
-- MIGRATION NOTES
-- ============================================================================
-- 
-- What Changed:
-- - imagePath column: Now stores just the filename as a reference
-- - image_data column (NEW): Stores the actual binary image data
--
-- Old Behavior:
-- - Images stored in: images/ directory (filesystem)
-- - Database stored: Filename only
--
-- New Behavior:
-- - Images stored in: Database (LONGBLOB column)
-- - Filesystem access: No longer needed
--
-- Migration Steps:
-- 1. Run this SQL script against your database
-- 2. Rebuild the Java application (build/classes folder is already updated)
-- 3. When adding products in AdminDashboard, images are now stored as BLOB
-- 4. Product images are loaded from database in MainFrame
-- 5. The images/ directory is no longer needed (optional to delete)
--
-- ============================================================================
-- ROLLING BACK (if needed)
-- ============================================================================
-- If you need to revert this change, run:
-- ALTER TABLE products DROP COLUMN image_data;
-- DROP INDEX idx_products_image_data ON products;
--
-- ============================================================================
-- DATA MIGRATION (Optional - if you have existing products with images)
-- ============================================================================
-- If you need to migrate existing filesystem images to the database, 
-- you would need to:
-- 1. Read image files from the images/ directory
-- 2. Convert them to binary data
-- 3. Insert the binary data into the image_data column
--
-- This can be done programmatically or by updating the relevant rows:
-- UPDATE products SET image_data = LOAD_FILE('/path/to/image.jpg') 
-- WHERE imagePath = 'filename.jpg';
--
-- Note: LOAD_FILE() requires MySQL server file permissions
-- ============================================================================

-- Display migration status
SELECT 'Migration completed successfully!' AS status;
