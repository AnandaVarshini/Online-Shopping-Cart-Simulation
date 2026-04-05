package com.eshop.database;

import com.eshop.models.Recommendation;
import com.eshop.models.Product;
import java.sql.*;
import java.util.*;

/**
 * Simple recommendation system - Shows top products by stock/popularity
 */
public class RecommendationDAO {
    
    /**
     * Get product recommendations - Returns top products by popularity
     */
    public List<Recommendation> getRecommendationsForUser(int userId, int limit) throws SQLException {
        return getPopularProducts(limit);
    }
    
    /**
     * Get most popular products (sorted by stock)
     */
    public List<Recommendation> getPopularProducts(int limit) throws SQLException {
        List<Recommendation> recommendations = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT * FROM products ORDER BY stock DESC LIMIT " + limit)) {
            
            while (rs.next()) {
                Product product = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDouble("price"),
                    rs.getString("imagePath"),
                    rs.getInt("stock")
                );
                
                double relevance = 0.8 + (rs.getInt("stock") / 100.0 * 0.2);
                String reason = "Popular - " + rs.getInt("stock") + " in stock";
                recommendations.add(new Recommendation(product, Math.min(relevance, 1.0), reason));
            }
        } catch (SQLException e) {
            System.err.println("⚠ Error getting recommendations: " + e.getMessage());
        }
        
        return recommendations;
    }
}

