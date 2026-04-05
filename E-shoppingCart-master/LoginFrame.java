package com.eshop.database;

import com.eshop.models.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    
    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String query = "SELECT id, name, description, price, image, stock FROM products";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getString("image"),
                    rs.getInt("stock")
                );
                products.add(product);
            }
        }
        
        return products;
    }
    
    public Product getProductById(int id) throws SQLException {
        String query = "SELECT id, name, description, price, image, stock FROM products WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDouble("price"),
                        rs.getString("image"),
                        rs.getInt("stock")
                    );
                }
            }
        }
        
        return null;
    }
    
    public void saveProduct(Product product) throws SQLException {
        String query = "INSERT INTO products (name, description, price, image, stock) " +
                       "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setString(4, product.getImagePath());
            pstmt.setInt(5, product.getStock());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    public void updateProduct(Product product) throws SQLException {
        String query = "UPDATE products SET name = ?, description = ?, price = ?, " +
                       "image = ?, stock = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getDescription());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setString(4, product.getImagePath());
            pstmt.setInt(5, product.getStock());
            pstmt.setInt(6, product.getId());
            
            pstmt.executeUpdate();
        }
    }
    
    public void deleteProduct(int id) throws SQLException {
        String query = "DELETE FROM products WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
    
    public void updateStock(int productId, int quantity) throws SQLException {
        String query = "UPDATE products SET stock = stock - ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Update product price and record in price history
     * @param productId Product ID to update
     * @param newPrice New price value
     * @param reason Reason for price change (e.g., "Seasonal Discount", "Promotion")
     * @return true if update successful
     */
    public boolean updateProductPrice(int productId, double newPrice, String reason) throws SQLException {
        String updateQuery = "UPDATE products SET price = ? WHERE id = ?";
        String historyQuery = "INSERT INTO price_history (product_id, price, price_change_reason) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Update product price
            try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                pstmt.setDouble(1, newPrice);
                pstmt.setInt(2, productId);
                pstmt.executeUpdate();
            }
            
            // Record price change in history
            try (PreparedStatement pstmt = conn.prepareStatement(historyQuery)) {
                pstmt.setInt(1, productId);
                pstmt.setDouble(2, newPrice);
                pstmt.setString(3, reason != null ? reason : "Price Update");
                pstmt.executeUpdate();
            }
            
            return true;
        }
    }
    
    /**
     * Bulk update prices for multiple products
     */
    public void bulkUpdatePrices(java.util.Map<Integer, Double> priceUpdates, String reason) throws SQLException {
        for (java.util.Map.Entry<Integer, Double> entry : priceUpdates.entrySet()) {
            updateProductPrice(entry.getKey(), entry.getValue(), reason);
        }
    }
    
    /**
     * Apply percentage discount to a product
     * @param productId Product ID
     * @param discountPercent Discount percentage (e.g., 10 for 10% off)
     * @param reason Reason for discount
     */
    public void applyDiscount(int productId, double discountPercent, String reason) throws SQLException {
        Product product = getProductById(productId);
        if (product != null) {
            double newPrice = product.getPrice() * (1 - (discountPercent / 100));
            updateProductPrice(productId, newPrice, reason + " (" + discountPercent + "% off)");
        }
    }
    
    /**
     * Get image data (BLOB) for a product
     * @param productId Product ID
     * @return Byte array of image data, or null if no image exists
     */
    public byte[] getProductImageData(int productId) throws SQLException {
        String query = "SELECT image_data FROM products WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("image_data");
                }
            }
        }
        
        return null;
    }
}


