package SariSariStore.service;

import SariSariStore.model.Product;
import SariSariStore.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class InventoryService {
//database row to Prodauct object

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getDouble("buy_price"),
            rs.getDouble("sell_price"),
            rs.getInt("quantity"),
            rs.getBoolean("is_available")
        );

    }



    public void addProduct(Product product) {
        String sql = "INSERT INTO products (name, buy_price, sell_price, quantity, is_available) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getBuyPrice());
            pstmt.setDouble(3, product.getSellPrice());
            pstmt.setInt(4, product.getQuantity());
            pstmt.setBoolean(5, product.isAvailable());
            pstmt.executeUpdate();

            // Get the generated ID and set it to the product
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
           throw new RuntimeException("could not add product" + e.getMessage(), e);
        }
    }

    public boolean removeProduct(Product product){

        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, product.getId());

            return pstmt.executeUpdate() > 0; // Returns true if a row was deleted
        } catch (SQLException e) {
            throw new RuntimeException("Could not remove product: " + e.getMessage(), e);
        }

    }


    public void updateProduct(Product product){
        String sql = "UPDATE products SET name = ?, buy_price = ?, sell_price = ?" +
                ", quantity = ?, is_available = ? WHERE id = ?";

                try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

                
                    pstmt.setString(1, product.getName());
                    pstmt.setDouble(2, product.getBuyPrice());
                    pstmt.setDouble(3, product.getSellPrice());
                    pstmt.setInt(4, product.getQuantity());
                    pstmt.setBoolean(5, product.isAvailable());
                    pstmt.setInt(6, product.getId());
                    pstmt.executeUpdate();
                } catch (SQLException e){
                    throw new RuntimeException("Could not update product: " + e.getMessage(), e);
                }

                
    }

     public Product findProductByName(String name) {
        String sql = "SELECT * FROM products WHERE name = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not find product: " + e.getMessage(), e);
        }
        return null;
    }

    public Product restockProduct(String name, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }

        String sql = "UPDATE products SET quantity = quantity + ? WHERE name = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setString(2, name);
            if (ps.executeUpdate() == 0) {
                return null; // no product with that name
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not restock product: " + e.getMessage(), e);
        }
        return findProductByName(name);
    }











}