# E-Shop Developer's Guide

A guide for developers who want to extend, modify, or maintain the E-Shop application.

---

## 🚀 Getting Started as a Developer

### Prerequisites
- Read `README_COMPREHENSIVE.md` for overview
- Read `QUICK_START.md` to get app running
- Review `ARCHITECTURE.md` to understand design

### Development Environment Setup

```powershell
# 1. Install Tools
- JDK 25+ (with JAVA_HOME set)
- MySQL 8.0 Server
- Text Editor or IDE (VS Code, Eclipse, IntelliJ)
- Git (for version control)

# 2. Verify Installation
java -version
mysql --version

# 3. Setup Development Database
mysql -u root -p < database_setup.sql

# 4. Compile Project
javac -d build/classes -cp "lib\mysql-connector-j-8.0.33.jar" `
  src/com/eshop/models/*.java src/com/eshop/database/*.java `
  src/com/eshop/utils/*.java src/com/eshop/ui/*.java

# 5. Run Application
java -cp "build\classes;lib\mysql-connector-j-8.0.33.jar" com.eshop.ui.LoginFrame
```

---

## 📝 Code Style Guidelines

### Naming Conventions

```java
// Classes: PascalCase
public class ShoppingCart { }
public class UserDAO { }
public class MainFrame { }

// Methods & Variables: camelCase
private void addToCart() { }
private int itemCount = 0;
private String productName;

// Constants: UPPER_SNAKE_CASE
private static final int WINDOW_WIDTH = 1000;
private static final String DB_URL = "jdbc:mysql://localhost";

// Database columns: snake_case (in schema)
user_id
product_id
price_at_adding
created_date
```

### Code Formatting

```java
// 1. Indentation: 4 spaces (not tabs)
public void someMethod() {
    if (condition) {
        doSomething();
    }
}

// 2. Line Length: max 100 characters
String longString = "This is a very long string that should be " +
                    "broken into multiple lines for readability";

// 3. Spacing: blank lines between methods
public void methodOne() {
    // code
}

public void methodTwo() {
    // code
}

// 4. Comments: above declarations
// This variable stores the current user session
private User currentUser;
```

### Imports

```java
// 1. Standard Java imports first
import java.util.*;
import java.sql.*;
import javax.swing.*;

// 2. Project imports second
import com.eshop.models.*;
import com.eshop.database.*;

// 3. Use specific imports (not wildcard) for clarity
// ✓ Good:
import com.eshop.models.User;
import com.eshop.database.UserDAO;

// ✗ Avoid:
import com.eshop.models.*;
import com.eshop.database.*;
```

---

## 🏗️ Adding New Features

### Example 1: Add Search Functionality

**Step 1: Create Database Changes**
```sql
-- Add full-text index to products table
ALTER TABLE products ADD FULLTEXT INDEX ft_search (name, description);

-- Create search history table (optional)
CREATE TABLE search_history (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    search_query VARCHAR(255),
    search_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**Step 2: Create New DAO Method**
```java
// File: ProductDAO.java
public List<Product> searchProducts(String query) throws SQLException {
    String sql = "SELECT * FROM products WHERE " +
                 "MATCH(name, description) AGAINST(? IN BOOLEAN MODE)";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, query);
        
        List<Product> results = new ArrayList<>();
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                // ... set other fields
                results.add(p);
            }
        }
        return results;
    }
}
```

**Step 3: Update UI Frame**
```java
// In MainFrame.java, add search panel:
private JTextField searchField;
private JButton searchButton;

private void addSearchPanel() {
    JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    
    searchField = new JTextField(20);
    searchButton = new JButton("Search");
    
    searchButton.addActionListener(e -> performSearch());
    
    searchPanel.add(new JLabel("Search:"));
    searchPanel.add(searchField);
    searchPanel.add(searchButton);
    
    // Add to main panel
    add(searchPanel, BorderLayout.NORTH);
}

private void performSearch() {
    String query = searchField.getText();
    
    if (query.trim().isEmpty()) {
        showError("Please enter search terms");
        return;
    }
    
    try {
        ProductDAO dao = new ProductDAO();
        List<Product> results = dao.searchProducts(query);
        
        if (results.isEmpty()) {
            showInfo("No products found");
        } else {
            displaySearchResults(results);
        }
    } catch (SQLException ex) {
        showError("Search error: " + ex.getMessage());
    }
}
```

**Step 4: Test**
- Run application
- Try searching for "iPhone"
- Verify results display correctly
- Test edge cases (empty search, special characters)

---

### Example 2: Add User Reviews Feature

**Step 1: Database Schema**
```sql
CREATE TABLE reviews (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    rating INT (1-5),
    comment TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);
```

**Step 2: Create Model**
```java
// File: Review.java
package com.eshop.models;

public class Review {
    private int id;
    private int userId;
    private int productId;
    private int rating;
    private String comment;
    private String reviewDate;
    
    // Constructor
    public Review(int userId, int productId, int rating, String comment) {
        this.userId = userId;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public int getProductId() { return productId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getReviewDate() { return reviewDate; }
    
    // Validation
    public boolean isValid() {
        return rating >= 1 && rating <= 5 && 
               comment != null && !comment.trim().isEmpty();
    }
}
```

**Step 3: Create DAO**
```java
// File: ReviewDAO.java
package com.eshop.database;

import com.eshop.models.Review;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {
    
    public boolean addReview(Review review) throws SQLException {
        String sql = "INSERT INTO reviews (user_id, product_id, rating, comment) " +
                     "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, review.getUserId());
            pstmt.setInt(2, review.getProductId());
            pstmt.setInt(3, review.getRating());
            pstmt.setString(4, review.getComment());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public List<Review> getProductReviews(int productId) throws SQLException {
        String sql = "SELECT * FROM reviews WHERE product_id = ? " +
                     "ORDER BY review_date DESC";
        
        List<Review> reviews = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review(
                        rs.getInt("user_id"),
                        rs.getInt("product_id"),
                        rs.getInt("rating"),
                        rs.getString("comment")
                    );
                    r.setId(rs.getInt("id"));
                    reviews.add(r);
                }
            }
        }
        return reviews;
    }
    
    public double getAverageRating(int productId) throws SQLException {
        String sql = "SELECT AVG(rating) as avg_rating FROM reviews " +
                     "WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_rating");
                }
            }
        }
        return 0.0;
    }
}
```

**Step 4: Integrate into UI**
```java
// In MainFrame.java - add review section under product details
private void showProductDetails(Product product) throws SQLException {
    ReviewDAO reviewDAO = new ReviewDAO();
    List<Review> reviews = reviewDAO.getProductReviews(product.getId());
    double avgRating = reviewDAO.getAverageRating(product.getId());
    
    JPanel detailsPanel = new JPanel(new BorderLayout());
    
    // Product info
    JPanel infoPanel = new JPanel();
    infoPanel.add(new JLabel("Rating: " + String.format("%.1f", avgRating) + "/5"));
    infoPanel.add(new JLabel("(" + reviews.size() + " reviews)"));
    
    // Reviews list
    JPanel reviewsPanel = new JPanel();
    reviewsPanel.setLayout(new BoxLayout(reviewsPanel, BoxLayout.Y_AXIS));
    
    for (Review r : reviews) {
        JTextArea reviewText = new JTextArea(2, 40);
        reviewText.setText(r.getRating() + "/5 - " + r.getComment());
        reviewText.setEditable(false);
        reviewsPanel.add(reviewText);
    }
    
    detailsPanel.add(infoPanel, BorderLayout.NORTH);
    detailsPanel.add(new JScrollPane(reviewsPanel), BorderLayout.CENTER);
    
    // Show dialog
    JOptionPane.showMessageDialog(this, detailsPanel, 
        "Product Details", JOptionPane.INFORMATION_MESSAGE);
}
```

---

## 🐛 Debugging Guide

### Common Issues and Solutions

#### 1. Database Connection Issues

**Problem:** "No suitable driver found"
```
Solution:
1. Verify mysql-connector-j-8.0.33.jar exists in lib/
2. Check classpath includes: -cp "lib\mysql-connector-j-8.0.33.jar"
3. Ensure MySQL service is running: net start MySQL80
4. Test connection: mysql -u root -p
```

**Problem:** "Communications link failure"
```
Solution:
1. Verify MySQL is running on port 3306
   mysql -u root -p -h 127.0.0.1 -e "status"

2. Check firewall isn't blocking 3306

3. Verify credentials in DatabaseConnection.java:
   User: root
   Password: MySql#2026
   Host: localhost:3306
   Database: eshop_db
```

#### 2. Compilation Errors

**Problem:** "cannot find symbol"
```
Solution:
1. Compile in correct order:
   - Models first (no dependencies)
   - Database second (depends on models)
   - Utils third
   - UI last (depends on everything)

2. Verify all .java files are present

3. Check imports match file locations
```

**Problem:** "incompatible types"
```
Solution:
1. Check variable type matches assignment
2. Verify method return types
3. Ensure type casting where needed

Example:
// ✗ Wrong
int count = productDAO.getProducts(); // returns List

// ✓ Correct
List<Product> products = productDAO.getProducts();
```

#### 3. Runtime Issues

**Problem:** "NullPointerException"
```
Solution:
1. Always check for null before using objects:
   // ✗ Wrong
   String name = user.getName().toUpperCase();
   
   // ✓ Correct
   String name = user.getName();
   if (name != null) {
       name = name.toUpperCase();
   }

2. Initialize objects before use:
   // ✓ Good
   ProductDAO dao = new ProductDAO();
   List<Product> products = dao.getAllProducts();
```

**Problem:** "Out of memory"
```
Solution:
1. Increase heap size:
   java -Xmx512m -cp "..." com.eshop.ui.LoginFrame

2. Close resources properly:
   // ✓ Good
   try (Connection conn = getConnection()) {
       // use connection
   } // Auto-closed

3. Avoid storing large lists in memory
```

---

## ✅ Testing Strategy

### Unit Tests (if using JUnit)

```java
// Example: TestUserDAO.java
import org.junit.Test;
import static org.junit.Assert.*;

public class TestUserDAO {
    
    @Test
    public void testAuthenticateUser() throws SQLException {
        UserDAO dao = new UserDAO();
        
        // Test successful login
        User user = dao.authenticateUser("admin", "admin123");
        assertNotNull(user);
        assertEquals("admin", user.getUsername());
        
        // Test failed login
        User invalidUser = dao.authenticateUser("admin", "wrongpassword");
        assertNull(invalidUser);
    }
    
    @Test
    public void testRegisterUser() throws SQLException {
        UserDAO dao = new UserDAO();
        User newUser = new User();
        newUser.setUsername("testuser" + System.currentTimeMillis());
        newUser.setPassword("password123");
        newUser.setEmail("test@example.com");
        
        int userId = dao.registerUser(newUser);
        assertTrue(userId > 0);
    }
}
```

### Manual Testing Checklist

```
Functional Tests:
☐ User can register with valid data
☐ User cannot register with invalid username
☐ User can login with correct credentials
☐ User cannot login with wrong password
☐ User can browse products
☐ User can add product to cart
☐ User can remove product from cart
☐ User can adjust quantities
☐ User can place order
☐ Order appears in order history
☐ User can add product to wishlist
☐ User can remove from wishlist
☐ Price drops show in wishlist
☐ User can add to cart from wishlist ← NEW

Boundary Tests:
☐ Empty cart displays message
☐ Zero quantity handled correctly
☐ Negative price rejected
☐ Large cart (100 items) works
☐ Largest number (MAX_INT) handled

Error Handling:
☐ Invalid DB connection shows error
☐ Missing product shows error
☐ Duplicate username shows error
☐ Out of stock product shows error
```

---

## 📚 Code Documentation Standards

### JavaDoc Comments

```java
/**
 * Adds a product to the shopping cart.
 * If the product already exists in the cart,
 * increases its quantity instead of adding duplicate.
 *
 * @param product The product to add (must not be null)
 * @param quantity The quantity to add (must be positive)
 * @throws IllegalArgumentException if quantity is <= 0
 * @return true if item was added, false if updated existing
 * 
 * @see CartItem
 */
public boolean addItem(Product product, int quantity) {
    // Method body
}
```

### Method Comments

```java
public void checkout() {
    // 1. Validate cart is not empty
    if (cart.isEmpty()) {
        showError("Cart is empty");
        return;
    }
    
    // 2. Validate stock availability for all items
    try {
        cart.validateStock();
    } catch (IllegalArgumentException ex) {
        showError(ex.getMessage());
        return;
    }
    
    // 3. Create order in database
    try {
        OrderDAO dao = new OrderDAO();
        Order order = createOrderFromCart();
        int orderId = dao.createOrder(order);
        
        // 4. Clear cart after successful order
        cart.clear();
        
        // 5. Show confirmation
        showSuccess("Order placed successfully! Order #" + orderId);
    } catch (SQLException ex) {
        showError("Error placing order: " + ex.getMessage());
    }
}
```

---

## 🔒 Security Best Practices

### Input Validation

```java
/**
 * ✓ GOOD: Validates all inputs
 */
public boolean registerUser(String username, String password, String email) {
    // Validate username
    if (username == null || username.length() < 6) {
        throw new IllegalArgumentException("Username must be 6+ characters");
    }
    
    // Validate password
    if (password == null || password.length() < 8) {
        throw new IllegalArgumentException("Password must be 8+ characters");
    }
    
    // Validate email format
    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
        throw new IllegalArgumentException("Invalid email format");
    }
    
    // Check for alphanumeric username only
    if (!username.matches("^[a-zA-Z0-9_]+$")) {
        throw new IllegalArgumentException("Username can only contain letters, numbers, underscore");
    }
    
    return true;
}

/**
 * ✗ BAD: No validation
 */
public boolean registerUserBad(String username, String password) {
    // Directly use user input in query
    return true;
}
```

### SQL Injection Prevention

```java
/**
 * ✗ VULNERABLE: String concatenation
 */
String query = "SELECT * FROM users WHERE username='" + username + "'";
// If username = "' OR '1'='1", query becomes:
// SELECT * FROM users WHERE username='' OR '1'='1'
// This returns ALL users!

/**
 * ✓ SECURE: Parameterized query
 */
String query = "SELECT * FROM users WHERE username=?";
PreparedStatement pstmt = conn.prepareStatement(query);
pstmt.setString(1, username);  // Username safely bound as parameter
// Even if username = "' OR '1'='1'", it's treated as literal string
// No SQL injection possible!
```

### Error Handling

```java
/**
 * ✓ GOOD: Generic error message
 */
try {
    User user = userDAO.authenticateUser(username, password);
} catch (SQLException ex) {
    // Don't expose database details
    showError("Login failed. Please try again.");
    // Log actual error for debugging
    System.err.println("DB Error: " + ex.getMessage());
}

/**
 * ✗ BAD: Reveals sensitive information
 */
try {
    User user = userDAO.authenticateUser(username, password);
} catch (SQLException ex) {
    // Shows database structure to attacker!
    showError("Error: SQL exception in users table" + 
              ex.getSQLState() + ex.getMessage());
}
```

---

## 🚀 Performance Optimization Tips

### Database Query Optimization

```java
/**
 * ✗ BAD: N+1 query problem
 */
List<Order> orders = orderDAO.getOrdersByUser(userId);
for (Order order : orders) {
    List<OrderItem> items = orderDAO.getOrderItems(order.getId());
    // Called N times!
}

/**
 * ✓ GOOD: Single query with JOIN
 */
public List<Object[]> getOrdersWithItems(int userId) {
    String sql = "SELECT o.*, oi.* FROM orders o " +
                 "LEFT JOIN order_items oi ON o.id = oi.order_id " +
                 "WHERE o.user_id = ?";
    // Single query! More efficient.
}
```

### Image Loading Optimization

```java
/**
 * ✗ BAD: Load full resolution always
 */
ImageIcon icon = new ImageIcon("images/product.jpg");
// Large memory footprint

/**
 * ✓ GOOD: Lazy load & scale
 */
ImageIcon icon = ImageLoader.loadImageScaled("images/product.jpg", 300, 400);
// Loads only when needed
// Scales to exact size needed
// Better memory usage
```

### List Pagination

```java
/**
 * ✗ BAD: Load all products
 */
List<Product> allProducts = productDAO.getAllProducts();
// If 100,000 products, loads all at once!

/**
 * ✓ GOOD: Paginate results
 */
int pageSize = 20;
int pageNum = 1;
String sql = "SELECT * FROM products LIMIT ? OFFSET ?";
pstmt.setInt(1, pageSize);       // 20 items per page
pstmt.setInt(2, (pageNum-1)*20); // Skip to correct page
// Loads only 20 items at a time
```

---

## 🔄 Version Control Workflow

### Git Workflow

```bash
# 1. Create feature branch
git checkout -b feature/add-search

# 2. Make changes
# ... edit files ...

# 3. Check status
git status

# 4. Stage changes
git add src/com/eshop/database/ProductDAO.java
git add src/com/eshop/ui/MainFrame.java

# 5. Commit with clear message
git commit -m "Add product search functionality

- Added searchProducts() method to ProductDAO
- Added search UI panel to MainFrame
- Implements full-text search on name and description"

# 6. Push to repo
git push origin feature/add-search

# 7. Create Pull Request
# ... on GitHub/GitLab ...

# 8. Merge after review
git checkout main
git merge feature/add-search
git push origin main
```

### Commit Message Guidelines

```
✓ Good commit messages:
- Add support for product search
- Fix NullPointerException in CartFrame
- Improve database query performance
- Remove unused imports

✗ Bad commit messages:
- fixed stuff
- updated code
- changes
- asdfasdfasdfasdf
```

---

## 📖 Documentation Updates

When making changes, update documentation:

```
Files to update:
├─ README_COMPREHENSIVE.md  (if major feature)
├─ ARCHITECTURE.md          (if design changes)
├─ QUICK_START.md           (if setup changes)
├─ Code comments            (always)
├─ JavaDoc                  (for public methods)
└─ This file (DEVELOPER.md) (for process changes)
```

Example update for new feature:

```markdown
## New Feature: Product Search

### Database
- Added FULLTEXT index on products table
- Query: Full-text search on name and description

### Code Changes
- ProductDAO.searchProducts() method
- MainFrame.showSearchPanel() for UI

### How To Use
1. Enter search query in search box
2. Click Search button
3. Results display in product grid

### Files Modified
- src/com/eshop/database/ProductDAO.java
- src/com/eshop/ui/MainFrame.java

### Testing
- ✓ Search "iPhone" returns iPhone products
- ✓ Empty search shows error
- ✓ Special characters handled safely
```

---

## 🎓 Learning Resources

### Java Resources
- [Java Docs](https://docs.oracle.com/javase/tutorial/)
- [Swing Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/)
- [JDBC Guide](https://docs.oracle.com/javase/tutorial/jdbc/)

### Design Patterns
- [DAO Pattern](https://refactoring.guru/design-patterns/dao)
- [MVC Pattern](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93controller)
- [Singleton Pattern](https://refactoring.guru/design-patterns/singleton)

### Best Practices
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Code Complete by Steve McConnell](https://www.cc2e.com/)

---

## 🆘 Getting Help

### Troubleshooting Steps

1. **Check Error Message**
   - Read full stack trace
   - Identify line number with issue

2. **Search Documentation**
   - Check README files
   - Review Architecture.md
   - Look at similar code patterns

3. **Consult Code Comments**
   - Review JavaDoc
   - Check method documentation
   - Look for examples

4. **Test Isolation**
   - Create simple test case
   - Isolate problem to single method
   - Run with debug output

5. **Ask for Help**
   - Share full error message
   - Describe what you're trying to do
   - Show relevant code snippet

---

## 📋 Checklist Before Shipping Code

- [ ] Code compiles without errors
- [ ] Code compiles with no warnings
- [ ] All tests pass
- [ ] Manual testing complete
- [ ] Code follows style guidelines
- [ ] Comments added for complex logic
- [ ] JavaDoc updated for public methods
- [ ] Database changes documented
- [ ] No hardcoded values (use constants)
- [ ] Input validation added
- [ ] Error handling implemented
- [ ] SQL queries parameterized (no injection)
- [ ] Database connection properly closed
- [ ] Performance acceptable
- [ ] Documentation updated
- [ ] No debugging code left (System.out.println)
- [ ] No unnecessary imports
- [ ] No commented-out code

---

## 🚀 Advanced Topics

### Implementing Connection Pooling

Current implementation uses single connection. For production:

```java
// Use HikariCP or similar
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseConnection {
    private static HikariDataSource dataSource;
    
    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost/eshop_db");
        config.setUsername("root");
        config.setPassword("MySql#2026");
        config.setMaximumPoolSize(10);
        dataSource = new HikariDataSource(config);
    }
    
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
```

### Implementing Caching

```java
public class CachedProductDAO extends ProductDAO {
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
    private List<Product> cachedProducts;
    private long lastCachTime = 0;
    
    public List<Product> getAllProducts() throws SQLException {
        long now = System.currentTimeMillis();
        
        if (cachedProducts == null || (now - lastCacheTime) > CACHE_DURATION) {
            // Refresh cache
            cachedProducts = super.getAllProducts();
            lastCacheTime = now;
        }
        
        return cachedProducts;
    }
}
```

### Async Operations

```java
// Load data without freezing UI
SwingWorker<List<Product>, Void> worker = new SwingWorker<>() {
    protected List<Product> doInBackground() throws Exception {
        return productDAO.getAllProducts();
    }
    
    protected void done() {
        try {
            List<Product> products = get();
            displayProducts(products);
        } catch (Exception ex) {
            showError("Error loading products");
        }
    }
};

worker.execute();
```

---

## 📞 Support

For questions or issues:
1. Check README_COMPREHENSIVE.md
2. Review ARCHITECTURE.md
3. Look at source code comments
4. Review example implementations
5. Test in isolation

---

**Last Updated:** February 28, 2026
**Version:** 2.0
**Status:** Production Ready

Happy coding! 🚀
