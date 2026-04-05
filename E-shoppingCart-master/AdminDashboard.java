package com.eshop.database;

import com.eshop.models.PriceHistory;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PriceHistoryDAO {
    
    /**
     * Record a price change for a product
     */
    public boolean recordPriceChange(int productId, double newPrice, String reason) throws SQLException {
        String query = "INSERT INTO price_history (product_id, price, recorded_date, price_change_reason) " +
                       "VALUES (?, ?, NOW(), ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, productId);
            pstmt.setDouble(2, newPrice);
            pstmt.setString(3, reason);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Get price history for a product (last 30 days)
     */
    public List<PriceHistory> getPriceHistory(int productId) throws SQLException {
        String query = "SELECT id, product_id, price, recorded_date, price_change_reason " +
                       "FROM price_history " +
                       "WHERE product_id = ? AND recorded_date >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                       "ORDER BY recorded_date ASC";
        
        List<PriceHistory> history = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PriceHistory ph = new PriceHistory(
                        rs.getInt("id"),
                        rs.getInt("product_id"),
                        rs.getDouble("price"),
                        rs.getTimestamp("recorded_date").toLocalDateTime(),
                        rs.getString("price_change_reason")
                    );
                    history.add(ph);
                }
            }
        }
        return history;
    }
    
    /**
     * Get minimum and maximum price for a product in last 30 days
     */
    public Object[] getMinMaxPrice(int productId) throws SQLException {
        String query = "SELECT MIN(price) as min_price, MAX(price) as max_price " +
                       "FROM price_history " +
                       "WHERE product_id = ? AND recorded_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Object[] {
                        rs.getDouble("min_price"),
                        rs.getDouble("max_price")
                    };
                }
            }
        }
        return new Object[] {0.0, 0.0};
    }
    
    /**
     * Get lowest price ever recorded for a product
     */
    public double getLowestPrice(int productId) throws SQLException {
        String query = "SELECT MIN(price) as lowest_price FROM price_history WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("lowest_price");
                }
            }
        }
        return 0.0;
    }
    
    /**
     * Check if price has dropped since a specific value
     */
    public double getPriceDrop(int productId, double originalPrice) throws SQLException {
        String query = "SELECT price FROM products WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double currentPrice = rs.getDouble("price");
                    return originalPrice - currentPrice; // Positive value = price dropped
                }
            }
        }
        return 0.0;
    }
}
