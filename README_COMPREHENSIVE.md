# E-Shop Quick Start Guide

Get the E-Shop application running in 5 minutes!

---

## ✅ Prerequisites (5 min)

### What You Need
- **JDK 25+** - Java Development Kit
- **MySQL 8.0+** - Database server  
- **mysql-connector-j-8.0.33.jar** - Already included in `lib/` folder

### Check Your Setup
```powershell
# Check Java
java -version
# Expected output: openjdk 25 or higher

# Check MySQL
mysql --version
# Expected output: mysql Ver 8.0.x

# Start MySQL (Windows)
net start MySQL80
```

---

## 🗄️ Database Setup (2 min)

### 1. Create Database
```powershell
# Open MySQL command line
mysql -u root -p

# Paste these commands:
```

```sql
CREATE DATABASE eshop_db;
USE eshop_db;

-- Users Table
CREATE TABLE users (
  id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  full_name VARCHAR(100),
  created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Products Table
CREATE TABLE products (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  price DECIMAL(10, 2) NOT NULL,
  stock INT NOT NULL,
  imagePath VARCHAR(255),
  created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Orders Table
CREATE TABLE orders (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  total_amount DECIMAL(10, 2),
  status VARCHAR(50),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Order Items Table
CREATE TABLE order_items (
  id INT PRIMARY KEY AUTO_INCREMENT,
  order_id INT NOT NULL,
  product_id INT NOT NULL,
  quantity INT,
  price DECIMAL(10, 2),
  FOREIGN KEY (order_id) REFERENCES orders(id),
  FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Wishlist Table (NEW)
CREATE TABLE wishlist (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  product_id INT NOT NULL,
  price_at_adding DECIMAL(10, 2),
  added_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  notification_enabled BOOLEAN DEFAULT TRUE,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (product_id) REFERENCES products(id),
  UNIQUE KEY unique_wishlist (user_id, product_id)
);

-- Price History Table (NEW)
CREATE TABLE price_history (
  id INT PRIMARY KEY AUTO_INCREMENT,
  product_id INT NOT NULL,
  price DECIMAL(10, 2),
  recorded_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Recommendations Table
CREATE TABLE recommendations (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  product_id INT NOT NULL,
  score DECIMAL(3, 2),
  created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Insert Sample Data
INSERT INTO users (username, password, email, full_name) VALUES
('admin', 'admin123', 'admin@eshop.com', 'Admin User'),
('john', 'john123', 'john@eshop.com', 'John Doe'),
('jane', 'jane123', 'jane@eshop.com', 'Jane Smith');

INSERT INTO products (name, description, price, stock) VALUES
('iPhone 15', 'Latest Apple smartphone', 999.99, 50),
('MacBook Pro', '14-inch laptop', 1999.99, 30),
('iPad Air', 'Tablet device', 599.99, 40),
('AirPods Pro', 'Wireless earbuds', 249.99, 100),
('Apple Watch', 'Smartwatch', 399.99, 60);

SELECT 'Database setup complete!' as Status;
```

---

## 💾 Build Project (1 min)

### Navigate to Project
```powershell
cd path\to\E-shoppingCart-master
```

### Compile All Classes
```powershell
javac -d build/classes -cp "lib\mysql-connector-j-8.0.33.jar" `
  src/com/eshop/models/*.java `
  src/com/eshop/database/*.java `
  src/com/eshop/utils/*.java `
  src/com/eshop/ui/*.java
```

**Expected:** No errors, clean compilation ✓

---

## 🚀 Run Application (1 min)

### Start the App
```powershell
java -cp "build\classes;lib\mysql-connector-j-8.0.33.jar" com.eshop.ui.LoginFrame
```

### Login
- **Username:** `admin`
- **Password:** `admin123`

Or register a new account!

---

## ✨ Key Features

### 1. Shopping
- Browse products
- Add to cart
- Adjust quantities
- Checkout & place orders

### 2. Wishlist (NEW)
- ❤️ Add products to wishlist
- 📊 Track price changes
- 💰 Get price drop alerts
- 🛒 **Add directly to cart from wishlist**

### 3. Price Tracking (NEW)
- Monitor price changes
- Automatic alerts when prices drop (5%)
- See savings amount
- Compare original vs current price

---

## 📍 Main Screens

### LoginFrame
- Login with username/password
- Register new account
- Password validation (8+ chars)

### MainFrame
- Browse all products
- Add to cart (green button)
- Add to wishlist (❤️ button)
- View recommendations
- Access cart, wishlist, account

### CartFrame
- Review cart items
- Adjust quantities
- Remove items
- See total price
- Checkout

### WishlistFrame (NEW FEATURE)
- View all wishlist items
- See price comparison (original → current)
- View savings (green ✅) or price increase (red ⚠️)
- **Add item directly to cart** ← NEW
- Remove items
- Toggle price alerts

---

## 🔧 Troubleshooting

### "MySQL Connection Failed"
```powershell
# Start MySQL service
net start MySQL80

# Verify connection
mysql -u root -p -h localhost -e "USE eshop_db; SHOW TABLES;"
```

### "Driver not found" Error
- Ensure `lib/mysql-connector-j-8.0.33.jar` exists
- Check classpath includes it: `-cp "lib\mysql-connector-j-8.0.33.jar"`

### "Compilation errors"
- Compile in order: models → database → utils → ui
- Check syntax with: `javac -Xlint:all`

### "Database not created"
- Verify all tables: `SELECT * FROM information_schema.TABLES WHERE TABLE_SCHEMA='eshop_db';`
- Rerun the CREATE TABLE statements above

### "Login fails"
- Verify user exists: `SELECT * FROM users WHERE username='admin';`
- Check password correct in database

---

## 📁 Project Structure

```
E-shoppingCart-master/
├── src/
│   └── com/eshop/
│       ├── ui/              [Login, Shop, Cart, Wishlist pages]
│       ├── database/        [Database connection & operations]
│       ├── models/          [User, Product, Order, etc.]
│       └── utils/           [Images utility]
├── build/classes/           [Compiled .class files]
├── lib/
│   └── mysql-connector-j-8.0.33.jar
├── images/                  [Product images]
├── README.md                [Original readme]
└── README_COMPREHENSIVE.md  [Full documentation]
```

---

## Next Steps

### To Learn More
- Read `README_COMPREHENSIVE.md` for detailed documentation
- Review source code with inline comments
- Check database schema explanations

### To Extend
- Add new features to MainFrame
- Create additional Frames
- Modify product filtering
- Add payment integration

---

## ⏱️ What's Happening During Startup

1. **JVM starts** (0.8s)
2. **MySQL connects** (0.3s)
3. **LoginFrame loads** (0.2s)
4. **Ready** (Total: ~1.3s)

---

## 🎯 Try These First

1. **Register Account**
   - Click "Register" in LoginFrame
   - Fill details and create account
   - Login with new account

2. **Browse Products**
   - See product grid
   - Check auto-generated images
   - View product details

3. **Add to Cart**
   - Click green "Add to Cart" button
   - See cart total update
   - Click "View Cart"

4. **Use Wishlist** (NEW)
   - Click ❤️ on product
   - Click "View Wishlist" in header
   - Check price tracking
   - Click "Add to Cart" from wishlist

5. **Place Order**
   - Add items to cart
   - Click "Checkout"
   - Order placed ✓

---

## 💡 Tips

- **Images:** Auto-generated if missing (gray gradient fallback)
- **Database:** Table structure is created automatically with SQL above
- **Products:** Sample data included in INSERT statements
- **Performance:** First run may take few seconds to generate images
- **Price Alerts:** Now showing price drops in wishlist ✅

---

## 📝 Common Commands

```powershell
# One-line compile command (copy-paste friendly)
javac -d build/classes -cp "lib\mysql-connector-j-8.0.33.jar" src/com/eshop/models/*.java src/com/eshop/database/*.java src/com/eshop/utils/*.java src/com/eshop/ui/*.java

# Run application
java -cp "build\classes;lib\mysql-connector-j-8.0.33.jar" com.eshop.ui.LoginFrame

# Check MySQL
mysql -u root -p -e "USE eshop_db; SELECT COUNT(*) as products FROM products;"

# View table structure
mysql -u root -p eshop_db -e "DESCRIBE products;"
```

---

## ❓ FAQ

**Q: Can I use a different database?**  
A: Yes, edit `DatabaseConnection.java` with your DB details

**Q: How do I add my own products?**  
A: Insert directly in MySQL or add through application (if feature available)

**Q: Can I change the MySQL password?**  
A: Yes, update credentials in `DatabaseConnection.java` and recompile

**Q: Where are images stored?**  
A: `images/` folder - auto-created on startup

**Q: How do price alerts work?**  
A: Wishlist items tracked - alerts show when price drops 5% or more

---

## 🎉 You're All Set!

Your E-Shop is now running with:
- ✅ Shopping cart
- ✅ Wishlist system  
- ✅ Price tracking
- ✅ Click-to-buy from wishlist

**Happy Shopping!** 🛒

---

**Questions?** Check `README_COMPREHENSIVE.md` for detailed docs!
