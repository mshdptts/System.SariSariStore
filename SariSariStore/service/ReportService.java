package SariSariStore.service;

import SariSariStore.model.Product;
import SariSariStore.model.SaleReport;
import SariSariStore.util.DBConnection;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;




public class ReportService {
    public ArrayList<Product> getLowStockProducts(int treshold){
        ArrayList<Product> products = new ArrayList<>();

        String sql = " SELECT * FROM products WHERE quantity < ?";

        try( Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setInt(1, treshold);
            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve low stock products: " + e.getMessage(), e);
        }

        return products;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
    return new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getDouble("buy_price"),
            rs.getDouble("sell_price"),
            rs.getInt("quantity"),
            rs.getBoolean("is_available"));
}



    public ArrayList<Product> getOutofStockProducts() {
        ArrayList<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products WHERE quantity = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve out of stock products: " + e.getMessage(), e);
        }

        return products;
    } 
    public ArrayList<Product> getInventorySummary() {
        ArrayList<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve inventory summary: " + e.getMessage(), e);
        }
        return products;
    }

    private SaleReport mapSaleRow(ResultSet rs) throws SQLException {
        java.sql.Timestamp soldAt = rs.getTimestamp("sold_at");
        LocalDateTime reportSoldAt = soldAt == null ? null : soldAt.toLocalDateTime();
        return new SaleReport(
                rs.getInt("id"),
                reportSoldAt,
                rs.getDouble("total"));
    }

    public ArrayList<SaleReport> getDailySalesReport(String date) {
        ArrayList<SaleReport> sales = new ArrayList<>();

        String sql = "SELECT * FROM sales WHERE DATE(sold_at) = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sales.add(mapSaleRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve daily sales report: " + e.getMessage(), e);
        }

        return sales;
    }

    public ArrayList<SaleReport> getSalesReport(String startDate, String endDate){
        ArrayList<SaleReport> sales = new ArrayList<>();

        String sql = "SELECT * FROM sales WHERE DATE(sold_at) BETWEEN ? AND ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, startDate);
            ps.setString(2, endDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sales.add(mapSaleRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve sales report: " + e.getMessage(), e);
        }
        return sales;
    }


/* )

getBestSellingProducts(startDate, endDate) */ //wag na to kahirap next time nalang

}