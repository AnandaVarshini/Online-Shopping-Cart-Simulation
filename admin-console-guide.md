# Admin Console - Product Management Guide

## Overview
Your e-shopping cart application now has a fully functional **Admin Console** with proper image handling. This guide explains how to use the new admin features.

---

## 🎯 Key Features

### 1. **Secure Admin Access**
- Only users with `role = "admin"` can access the Admin Console
- Admin button appears in the top navigation bar (⚙️ Admin Console)
- Prevents unauthorized access with permission checks

### 2. **Image Upload Support**
- No more hardcoded image names
- File chooser dialog to select images from your computer
- Supported formats: JPG, JPEG, PNG, GIF
- Images are automatically copied to the `images` directory with unique timestamps

### 3. **Product Management**
- Add products with complete details:
  - Product Name
  - Description
  - Price
  - Stock Quantity
  - Product Image (with file upload)

### 4. **Image Handling**
- Images are stored in the `images` folder
- Filenames are auto-generated with timestamps: `product_YYYYMMDD_HHmmss.extension`
- Original image quality is preserved
- Image preview displayed before adding product

---

## 📝 How to Use the Admin Console

### Step 1: Login with Admin Account
1. Launch the application
2. Login with an admin account (must have `role = 'admin'` in database)
3. You'll see the **⚙️ Admin Console** button in the top-right corner

### Step 2: Open Admin Dashboard
Click the **⚙️ Admin Console** button to open the product management interface

### Step 3: Add a New Product

#### Fill in Product Details:
1. **Product Name**: Enter the product name (e.g., "Dell Laptop")
2. **Description**: Enter detailed product description
3. **Price**: Enter product price (e.g., 999.99)
4. **Stock Quantity**: Enter available stock quantity (e.g., 50)

#### Upload Product Image:
1. Click the **📁 Choose Image File** button
2. Select an image from your computer (JPG, PNG, or GIF)
3. The image preview will display in the left panel
4. Selected filename shows below the button

#### Save Product:
1. Click the **✅ Add Product** button
2. System validates all fields
3. Image is copied to `images` directory
4. Product is automatically saved to database
5. Success message displays product details

### Step 4: Clear Form
Click **🔄 Clear** to reset the form for adding another product

---

## 🗂️ Image Directory Structure

```
E-shoppingCart-master/
├── images/               (Auto-created if doesn't exist)
│   ├── product_20260301_120530.jpg
│   ├── product_20260301_145200.png
│   └── ...
├── src/
├── build/
└── ...
```

**Image Naming Convention:**
- Format: `product_YYYYMMDD_HHmmss.extension`
- Example: `product_20260301_143050.jpg` (March 1, 2026 - 14:30:50)
- Ensures unique filenames for all uploaded images

---

## 🔐 Database Schema

The product images are stored as filenames in the database:

```sql
-- Products table
CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DOUBLE NOT NULL,
    stock INT NOT NULL,
    image VARCHAR(255)  -- Stores only the filename (e.g., "product_20260301_143050.jpg")
);

-- Users table (for role-based access)
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    fullName VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    address TEXT,
    role VARCHAR(50) DEFAULT 'customer'  -- 'admin' or 'customer'
);
```

---

## ✨ Technical Implementation

### AdminDashboard.java Enhancements
- **User Authentication**: Validates admin role before opening
- **File Upload**: JFileChooser for image selection
- **Image Copy**: Automatically copies selected images to `images` directory
- **Timestamp Naming**: Prevents filename conflicts with timestamp-based naming
- **Input Validation**: Checks all fields before database insertion
- **Image Preview**: Shows selected image before adding product
- **Error Handling**: Comprehensive error messages for user guidance

### MainFrame.java Updates
- **Admin Button**: Conditionally displays admin console button for admin users
- **Role Check**: `if ("admin".equalsIgnoreCase(currentUser.getRole()))`
- **Navigation**: Opens AdminDashboard when admin button is clicked

### Image Handling Sync
- **ImageLoader.java**: Searches for images in `images` directory
- **ImageGenerator.java**: Auto-generates placeholder images on first run
- **Dynamic Loading**: Products now properly display uploaded images
- **Fallback**: Gradient backgrounds if image is not found

---

## 🚀 Getting Started

### For Admins:
1. Ensure your user account has `role = 'admin'` set in the database
2. Restart the application and login
3. You'll see the "⚙️ Admin Console" button
4. Click it to start adding products with images

### For Database Administrators:
1. Update user roles in the database:
   ```sql
   UPDATE users SET role = 'admin' WHERE username = 'admin_username';
   ```

### For Regular Users:
- No changes needed
- Admin console is automatically hidden for non-admin users
- Users can still browse and purchase products with their images

---

## 🎨 UI/UX Improvements

### Enhanced Admin Dashboard:
- Clean, organized interface with sections
- Color-coded buttons (Green for Add, Orange for Admin, Red for Clear)
- Real-time image preview
- Descriptive labels and informative messages
- Full error handling with helpful error messages

### Visual Feedback:
- ✓ Green checkmark icons for success
- 🚫 Red warning icons for errors
- 📁 File icon for image selection
- ⚙️ Settings icon for admin console
- 🖼️ Image preview with fallback

---

## 🔍 Troubleshooting

### Issue: Admin Console button doesn't appear
**Solution**: Check if your user account has `role = 'admin'` in the database

### Issue: Image upload fails
**Solution**: 
- Ensure file format is JPG, PNG, or GIF
- Check that the `images` directory has write permissions
- Verify file is not empty or corrupted

### Issue: Product added but image doesn't show
**Solution**:
- Check if image file was successfully copied to `images` folder
- Verify database entry shows correct filename
- Refresh the application to reload image cache

### Issue: Can't find the Admin Console button
**Solution**: 
- Log out and log back in
- Make sure you're using an admin account
- Restart the application

---

## 📊 Database Verification

To verify admin setup and products:

```sql
-- Check admin users
SELECT id, username, fullName, role FROM users WHERE role = 'admin';

-- Check products with images
SELECT id, name, price, image FROM products;

-- Verify image files exist
SELECT DISTINCT image FROM products WHERE image IS NOT NULL;
```

---

## 🎓 Key Improvements Made

1. **Dynamic Image Management**: No more hardcoded image names
2. **File Upload Support**: Web-style image selection for products
3. **Admin Role-Based Access**: Secure admin console visible only to admins
4. **Automatic Image Organization**: Timestamped filenames prevent conflicts
5. **Better UI/UX**: Professional admin panel with validation
6. **Sync with Existing System**: Works seamlessly with ImageLoader and ImageGenerator

---

## 📞 Support

For issues or questions:
1. Check the error messages in the application (they are descriptive)
2. Verify database roles are correctly set
3. Ensure `images` directory has proper permissions
4. Check Java console output for detailed logs

---

**Your e-shopping cart is now ready for proper product management! 🎉**
