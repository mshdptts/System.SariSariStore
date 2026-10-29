package SariSariStore.service;

import SariSariStore.model.Product;
import SariSariStore.util.DBConnection;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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






}