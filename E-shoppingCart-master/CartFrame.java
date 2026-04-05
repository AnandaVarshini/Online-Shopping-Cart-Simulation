package com.eshop.database;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing product pricing and price updates
 * This helps track price changes and trigger alerts for price drops
 */
public class PricingManager {
    
    private static final ProductDAO productDAO = new ProductDAO();
    private static final PriceHistoryDAO priceHistoryDAO = new PriceHistoryDAO();
    
    /**
     * Update single product price with reason
     * Example: updatePrice(5, 49.99, "Flash Sale");
     */
    public static boolean updatePrice(int productId, double newPrice, String reason) {
        try {
            productDAO.updateProductPrice(productId, newPrice, reason);
            System.out.println("✓ Product " + productId + " price updated to $" + newPrice);
            return true;
        } catch (SQLException e) {
            System.err.println("✗ Error updating price: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Apply percentage discount to product
     * Example: applyDiscount(5, 15, "Christmas Sale");
     */
    public static boolean applyDiscount(int productId, double discountPercent, String reason) {
        try {
            productDAO.applyDiscount(productId, discountPercent, reason);
            System.out.println("✓ Applied " + discountPercent + "% discount to product " + productId);
            return true;
        } catch (SQLException e) {
            System.err.println("✗ Error applying discount: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Bulk update prices for clearance or promotion
     * Example: 
     *   Map<Integer, Double> prices = new HashMap<>();
     *   prices.put(1, 99.99);
     *   prices.put(2, 149.99);
     *   bulkUpdatePrices(prices, "Clearance Sale");
     */
    public static boolean bulkUpdatePrices(Map<Integer, Double> priceUpdates, String reason) {
        try {
            productDAO.bulkUpdatePrices(priceUpdates, reason);
            System.out.println("✓ Updated " + priceUpdates.size() + " products");
            return true;
        } catch (SQLException e) {
            System.err.println("✗ Error in bulk update: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get price trend for a product (last 30 days)
     */
    public static void showPriceTrend(int productId) {
        try {
            System.out.println("\n=== Price Trend for Product " + productId + " ===");
            priceHistoryDAO.getPriceHistory(productId).forEach(ph -> {
                System.out.println("Date: " + ph.getRecordedDate() + 
                                   " | Price: $" + ph.getPrice() + 
                                   " | Reason: " + ph.getPriceChangeReason());
            });
        } catch (SQLException e) {
            System.err.println("✗ Error fetching price trend: " + e.getMessage());
        }
    }
    
    /**
     * Get min/max price for a product in last 30 days
     */
    public static void showMinMaxPrice(int productId) {
        try {
            Object[] result = priceHistoryDAO.getMinMaxPrice(productId);
            System.out.println("\n=== Min/Max Price (Last 30 Days) ===");
            System.out.println("Minimum: $" + result[0]);
            System.out.println("Maximum: $" + result[1]);
        } catch (SQLException e) {
            System.err.println("✗ Error: " + e.getMessage());
        }
    }
    
    // ===== EXAMPLE USAGE =====
    public static void main(String[] args) {
        // Example 1: Update single product price
        updatePrice(1, 899.99, "Price Adjustment");
        
        // Example 2: Apply percentage discount
        applyDiscount(2, 20, "Summer Sale");
        
        // Example 3: Bulk update prices
        Map<Integer, Double> clearancePrices = new HashMap<>();
        clearancePrices.put(3, 199.99);
        clearancePrices.put(4, 299.99);
        clearancePrices.put(5, 49.99);
        bulkUpdatePrices(clearancePrices, "End of Season Clearance");
        
        // Example 4: View price history
        showPriceTrend(1);
        showMinMaxPrice(1);
    }
}
