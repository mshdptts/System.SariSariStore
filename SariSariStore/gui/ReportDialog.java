package SariSariStore.service;

import SariSariStore.model.Product;
import SariSariStore.model.SaleRecord;
import SariSariStore.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ReportService {

    public ArrayList<Product> getLowStockProducts(int treshold) {
        ArrayList<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products WHERE quantity < ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, treshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProductRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve low stock products: " + e.getMessage(), e);
        }

        return products;
    }

    public ArrayList<Product> getOutofStockProducts() {
        ArrayList<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products WHERE quantity = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(mapProductRow(rs));
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
                products.add(mapProductRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve inventory summary: " + e.getMessage(), e);
        }
        return products;
    }
    public ArrayList<SaleRecord> getDailySalesReport(String date) {
        String sql = "SELECT id, sale_date, total FROM sales "
                   + "WHERE DATE(sale_date) = ? ORDER BY sale_date";
        return querySales(sql, date);
    }
    
    public ArrayList<SaleRecord> getSalesReport(String startDate, String endDate) {
        String sql = "SELECT id, sale_date, total FROM sales "
                   + "WHERE DATE(sale_date) BETWEEN ? AND ? ORDER BY sale_date";
        return querySales(sql, startDate, endDate);
    }

    private ArrayList<SaleRecord> querySales(String sql, String... dates) {
        ArrayList<SaleRecord> sales = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < dates.length; i++) {
                ps.setString(i + 1, dates[i]);
            }
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

    private Product mapProductRow(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("buy_price"),
                rs.getDouble("sell_price"),
                rs.getInt("quantity"),
                rs.getBoolean("is_available"));
    }

    private SaleRecord mapSaleRow(ResultSet rs) throws SQLException {
        return new SaleRecord(
                rs.getInt("id"),
                rs.getTimestamp("sale_date"),
                rs.getDouble("total"));
    }
}
