package com.eshop.database;

import com.eshop.models.Wishlist;
import com.eshop.models.Product;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WishlistDAO {
    
    /**
     * Add a product to user's wishlist
     */
    public int addToWishlist(int userId, int productId, double priceAtAdding) throws SQLException {
        String query = "INSERT INTO wishlist (user_id, product_id, price_at_adding, added_date, notification_enabled) " +
                       "VALUES (?, ?, ?, NOW(), true)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            pstmt.setDouble(3, priceAtAdding);
            
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        return -1;
    }
    
    /**
     * Remove a product from wishlist
     */
    public boolean removeFromWishlist(int wishlistId) throws SQLException {
        String query = "DELETE FROM wishlist WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, wishlistId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Check if product is in user's wishlist
     */
    public boolean isProductInWishlist(int userId, int productId) throws SQLException {
        String query = "SELECT id FROM wishlist WHERE user_id = ? AND product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    /**
     * Get all wishlist items for a user with product details
     */
    public List<Object[]> getUserWishlistWithProducts(int userId) throws SQLException {
        String query = "SELECT w.id, w.product_id, w.price_at_adding, w.added_date, " +
                       "p.name, p.price, p.description, p.imagePath, p.stock, " +
                       "(p.price - w.price_at_adding) as price_diff, w.notification_enabled " +
                       "FROM wishlist w " +
                       "JOIN products p ON w.product_id = p.id " +
                       "WHERE w.user_id = ? " +
                       "ORDER BY w.added_date DESC";
        
        List<Object[]> results = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("id"),                    // 0: wishlist_id
                        rs.getInt("product_id"),            // 1: product_id
                        rs.getDouble("price_at_adding"),    // 2: price when added
                        rs.getString("added_date"),         // 3: added_date
                        rs.getString("name"),               // 4: product_name
                        rs.getDouble("price"),              // 5: current_price
                        rs.getString("description"),        // 6: description
                        rs.getString("imagePath"),         // 7: imagePath
                        rs.getInt("stock"),                 // 8: stock
                        rs.getDouble("price_diff"),         // 9: price_diff
                        rs.getBoolean("notification_enabled") // 10: notification_enabled
                    };
                    results.add(row);
                }
            }
        }
        return results;
    }
    
    /**
     * Get count of items in wishlist
     */
    public int getWishlistCount(int userId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM wishlist WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }
    
    /**
     * Toggle notification for a wishlist item
     */
    public boolean toggleNotification(int wishlistId, boolean enabled) throws SQLException {
        String query = "UPDATE wishlist SET notification_enabled = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setBoolean(1, enabled);
            pstmt.setInt(2, wishlistId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Get price drop alerts - items with significant price reduction
     */
    public List<Object[]> getPriceDropAlerts(int userId, double discountPercentage) throws SQLException {
        String query = "SELECT w.id, w.product_id, w.price_at_adding, p.name, p.price, " +
                       "((w.price_at_adding - p.price) / w.price_at_adding * 100) as discount_percent " +
                       "FROM wishlist w " +
                       "JOIN products p ON w.product_id = p.id " +
                       "WHERE w.user_id = ? AND w.notification_enabled = true " +
                       "AND p.price < (w.price_at_adding * (100 - ?) / 100) " +
                       "ORDER BY discount_percent DESC";
        
        List<Object[]> alerts = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, userId);
            pstmt.setDouble(2, discountPercentage);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] alert = {
                        rs.getInt("id"),                    // 0: wishlist_id
                        rs.getInt("product_id"),            // 1: product_id
                        rs.getString("name"),               // 2: product_name
                        rs.getDouble("price_at_adding"),    // 3: original_price
                        rs.getDouble("price"),              // 4: current_price
                        rs.getDouble("discount_percent")    // 5: discount_percent
                    };
                    alerts.add(alert);
                }
            }
        }
        return alerts;
    }
}
