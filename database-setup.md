# Database Setup Guide - Admin Console

## Quick Setup for Admin Users

### Option 1: Update Existing User to Admin

```sql
-- Make an existing user an admin
UPDATE users 
SET role = 'admin' 
WHERE username = 'your_username';

-- Verify the change
SELECT id, username, fullName, role FROM users WHERE username = 'your_username';
```

### Option 2: Create New Admin User

```sql
-- Create a new admin user (adjust values as needed)
INSERT INTO users (username, password, fullName, email, address, role) 
VALUES (
    'admin',
    'admin_password',  -- Use hashed password in production!
    'Administrator',
    'admin@example.com',
    'Admin Office',
    'admin'
);

-- Verify creation
SELECT * FROM users WHERE role = 'admin';
```

### Option 3: Enable Admin for Multiple Users

```sql
-- Grant admin role to specific users
UPDATE users 
SET role = 'admin' 
WHERE username IN ('user1', 'user2', 'user3');

-- View all admins
SELECT id, username, fullName, role FROM users WHERE role = 'admin';
```

---

## Verification Queries

### Check Current Admin Users
```sql
SELECT id, username, fullName, email, role 
FROM users 
WHERE role = 'admin' 
ORDER BY username;
```

### Check User Roles Distribution
```sql
SELECT role, COUNT(*) as count 
FROM users 
GROUP BY role;
```

### View All Users with Role
```sql
SELECT id, username, fullName, email, role 
FROM users 
ORDER BY role, username;
```

### Find Users Without Admin Role
```sql
SELECT id, username, fullName, email, role 
FROM users 
WHERE role IS NULL OR role != 'admin';
```

---

## Database Schema Reference

### Users Table Structure
```sql
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

### Products Table Structure
```sql
CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DOUBLE NOT NULL,
    stock INT NOT NULL,
    image VARCHAR(255)  -- Stores only the filename (e.g., "product_20260301_143050.jpg")
);
```

---

## Troubleshooting

### Admin Button Not Showing
1. Login to your account
2. Run this query to check your role:
   ```sql
   SELECT username, role FROM users WHERE username = 'your_username';
   ```
3. If role is not 'admin', update it:
   ```sql
   UPDATE users SET role = 'admin' WHERE username = 'your_username';
   ```
4. Logout and login again

### Test Admin Access
1. Create a test admin account:
   ```sql
   INSERT INTO users (username, password, fullName, email, address, role) 
   VALUES ('testadmin', 'test123', 'Test Admin', 'test@admin.com', 'Test Address', 'admin');
   ```
2. Login with username: `testadmin` and password: `test123`
3. Check if "⚙️ Admin Console" button appears

### Database Connection Issues
```sql
-- Test database connection
SELECT * FROM users LIMIT 1;

-- If error, check:
-- 1. Database is running
-- 2. Correct credentials in DatabaseConnection.java
-- 3. Network/firewall not blocking connection
```

---

## Role-Based Access Control

### Current Implementation
```
┌─────────────┐
│   Login     │
└──────┬──────┘
       │
       ├─→ Admin User (role = 'admin')
       │   ├─ View Store
       │   ├─ View Cart
       │   ├─ Make Purchases
       │   └─ Access Admin Console ← NEW!
       │
       └─→ Regular User (role = 'customer' or NULL)
           ├─ View Store
           ├─ View Cart
           └─ Make Purchases
```

### Future Role Enhancement Ideas
```sql
-- Add more roles as needed
UPDATE users SET role = 'moderator' WHERE username = 'moderator_name';
UPDATE users SET role = 'support' WHERE username = 'support_name';
```

---

## Example: Complete Setup

### Step 1: Backup Current Database
```sql
-- Export your database before making changes
-- Use your database tool's export feature
```

### Step 2: Create Admin User
```sql
INSERT INTO users (username, password, fullName, email, address, role) 
VALUES (
    'admin',
    'securepassword123',
    'Shop Administrator',
    'admin@myshop.com',
    '123 Admin Street, Admin City',
    'admin'
);
```

### Step 3: Verify Setup
```sql
SELECT * FROM users WHERE role = 'admin';

-- Expected output:
-- id | username | password | fullName | email | address | role
-- 1  | admin    | ...      | Shop ... | admin | 123 ... | admin
```

### Step 4: Test in Application
1. Rebuild the project
2. Run the application
3. Login with admin account
4. Verify "⚙️ Admin Console" button appears
5. Click button to open admin dashboard
6. Add a test product with image

### Step 5: Verify Product Added
```sql
SELECT * FROM products ORDER BY id DESC LIMIT 1;

-- Expected columns:
-- id | name | description | price | stock | image
```

### Step 6: Check Image File
```
Check that the image file exists at:
E-shoppingCart-master/images/product_[timestamp].[extension]
```

---

## Common SQL Patterns

### List All Admins
```sql
SELECT * FROM users WHERE LOWER(role) = 'admin';
```

### Count Admins
```sql
SELECT COUNT(*) as admin_count FROM users WHERE role = 'admin';
```

### Remove Admin Privileges
```sql
UPDATE users SET role = 'customer' WHERE username = 'username';
```

### Reset All Roles to Customer
```sql
UPDATE users SET role = 'customer';
```

### Find Non-Admin Users
```sql
SELECT * FROM users WHERE role != 'admin' OR role IS NULL;
```

---

## Security Recommendations

1. **Use Strong Passwords**: Don't use simple passwords like "admin" or "password"
2. **Hash Passwords**: Use proper password hashing (BCrypt, SHA-256, etc.)
3. **Limit Admin Accounts**: Only grant admin role to trusted users
4. **Audit Logs**: Consider adding logs of admin actions
5. **Backup Database**: Regular backups before any changes
6. **Update Periodically**: Change admin passwords periodically

---

## Production Checklist

- [ ] Created at least one admin user
- [ ] Verified admin user can login
- [ ] Verified "⚙️ Admin Console" button appears for admins
- [ ] Tested adding a product with image
- [ ] Verified product appears on main store page
- [ ] Verified image displays correctly
- [ ] Regular users cannot see admin button
- [ ] Database properly backed up
- [ ] Images directory has read/write permissions

---

## Quick Reference SQL

```sql
-- Quick Admin Setup
INSERT INTO users (username, password, fullName, email, address, role) 
VALUES ('admin', 'password', 'Admin', 'admin@test.com', 'Admin Address', 'admin');

-- Quick Verification
SELECT * FROM users WHERE role = 'admin';

-- View Products with Images
SELECT name, price, stock, image FROM products;

-- Count Products with Images
SELECT COUNT(*) FROM products WHERE image IS NOT NULL;
```

---

**Your database is now ready for admin product management!** 🎉
