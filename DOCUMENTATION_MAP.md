# Database Migration: Image Storage BLOB Migration

## Overview
This migration moves product image storage from the filesystem to the database using LONGBLOB (Binary Large Object) storage. This eliminates filesystem dependencies and makes the application more portable.

## Migration Steps

### Step 1: Backup Current Data
Before running this migration, **backup your database**:
```sql
-- Optional: Create backup
BACKUP DATABASE your_database_name TO DISK = 'C:\Backups\database_backup.bak';
```

### Step 2: Alter Products Table
Add a new `image_data` LONGBLOB column to store binary image data:

```sql
-- For MySQL
ALTER TABLE products 
ADD COLUMN image_data LONGBLOB;

-- Verify the column was added
DESCRIBE products;
```

### Step 3: Check Database Structure
Your `products` table should now have:
- `id` - INT (Primary Key)
- `name` - VARCHAR
- `description` - TEXT
- `price` - DOUBLE
- `imagePath` - VARCHAR (kept for reference, optional)
- `image_data` - LONGBLOB (NEW - stores binary image data)
- `stock` - INT

## What Changed

### Before Migration (Filesystem-Based)
```
User uploads image → AdminDashboard copies to /images/ folder → Stores filename in DB
Admin Dashboard → ProductDAO → products.image (VARCHAR filename)
MainFrame displays → ImageLoader (reads from filesystem)
```

### After Migration (Database BLOB)
```
User uploads image → AdminDashboard reads bytes → Stores BLOB in DB
Admin Dashboard → ProductDAO → products.image_data (LONGBLOB binary)
MainFrame displays → ImageLoader (queries database)
```

## Code Changes Summary

### 1. Product Model (`Product.java`)
- Added `private byte[] imageData` field
- Added getter/setter for `imageData`
- Added constructor with image data parameter

### 2. ProductDAO (`ProductDAO.java`)
- Added `saveProductWithImage()` method to insert image BLOBs
- Added `getProductImageData()` method to retrieve BLOBs from database
- Updated `saveProduct()` to support image data

### 3. AdminDashboard (`AdminDashboard.java`)
- Removed `copyImageToDirectory()` method (filesystem copy)
- Removed unused imports: `StandardCopyOption`, `SimpleDateFormat`, `Date`
- Updated `addProduct()` to:
  - Read image file as bytes using `Files.readAllBytes()`
  - Store bytes in database as BLOB
  - Display image size instead of filename

### 4. ImageLoader (`ImageLoader.java`)
- **Completely rewritten** to use database
- New method: `loadImage(int productId, int width, int height)` - queries database
- Old method: `loadImage(String imagePath, int width, int height)` - deprecated, returns fallback
- Removed filesystem directory lookup code

### 5. MainFrame (`MainFrame.java`)
- Updated all ImageLoader calls to use product ID:
  - `createProductCard()`: `ImageLoader.loadImage(product.getId(), 150, 150)`
  - `createRecommendationCard()`: `ImageLoader.loadImage(product.getId(), 200, 100)`
  - `createPopularProductCard()`: `ImageLoader.loadImage(product.getId(), 200, 100)`
- Removed `ImageGenerator.generateMissingImages()` call (no longer needed)
- Removed unused import: `ImageGenerator`

## Important Notes

### Database Compatibility
- **MySQL**: `LONGBLOB` supports up to 4GB per image
- **Size Limit**: Adjust server's `max_allowed_packet` if needed:
  ```sql
  SET GLOBAL max_allowed_packet=67108864;  -- 64MB
  ```

### Backwards Compatibility
1. **imagePath column is kept** for backwards compatibility
2. New images will store filename + binary data
3. Old apps using only `imagePath` will still work (but display "No Image" fallback)
4. New apps using `image_data` get actual images

### Troubleshooting

#### If images don't display after migration:
1. Verify `image_data` column exists: `DESCRIBE products;`
2. Check ProductDAO imports include `ProductDAO` in ImageLoader
3. Verify new products have data in `image_data` column:
   ```sql
   SELECT id, name, CHAR_LENGTH(image_data) as image_size FROM products WHERE image_data IS NOT NULL;
   ```

#### If database errors occur:
1. Check MySQL error log
2. Verify `max_allowed_packet` setting:
   ```sql
   SHOW VARIABLES LIKE 'max_allowed_packet';
   ```

## Rollback Instructions (If Needed)
If you need to revert to filesystem-based images:

### Option 1: Remove BLOB Column
```sql
-- Drop the new BLOB column (keeps old filename references)
ALTER TABLE products DROP COLUMN image_data;
```

### Option 2: Restore Full Database
```sql
-- Restore from backup
RESTORE DATABASE your_database_name FROM DISK = 'C:\Backups\database_backup.bak';
```

## Testing Checklist

- [ ] Database migration script runs without errors
- [ ] New `image_data` column exists in products table
- [ ] Application compiles successfully
- [ ] Admin Dashboard uploads image and stores in database
- [ ] MainFrame displays images correctly
- [ ] Fallback gradient appears when image_data is NULL
- [ ] Old products (with only imagePath) show fallback gradient
- [ ] New products display actual uploaded images

## Performance Considerations

### Advantages
- ✅ No filesystem dependency
- ✅ Easier deployment (no separate file management)
- ✅ Database transactions ensure data integrity
- ✅ Backup/restore includes images
- ✅ Works across multiple servers

### Disadvantages
- ❌ Larger database file size
- ❌ Slower image retrieval than filesystem (minimal impact for typical use)
- ❌ Database needs sufficient disk space

### Optimization Tips
1. Consider image compression before upload
2. Monitor database size: `SELECT * FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'your_database' AND TABLE_NAME = 'products';`
3. Use image resize in AdminDashboard if needed
4. Consider archiving old images if not needed

---

**Migration Date**: [Insert date when migration was performed]
**Backup Location**: [Insert backup file location]
**Notes**: [Insert any special migration notes]
