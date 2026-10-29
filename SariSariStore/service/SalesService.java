package SariSariStore.service;

import SariSariStore.model.SaleItem;
import SariSariStore.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SalesService {

    // Records one customer's purchase. Returns the new sale id.
    public int recordSale(ArrayList<SaleItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("A sale needs at least one item.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // start transaction

            try {
                int saleId = insertSale(conn);
                double total = 0;

                for (SaleItem item : items) {
                    total += processItem(conn, saleId, item);
                }

                updateSaleTotal(conn, saleId, total);
                conn.commit(); // everything worked, save it
                return saleId;

            } catch (SQLException | RuntimeException e) {
                conn.rollback(); // something failed, undo everything
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not record sale: " + e.getMessage(), e);
        }
    }

    // Creates the receipt row (total is filled in at the end)
    private int insertSale(Connection conn) throws SQLException {
        String sql = "INSERT INTO sales (total) VALUES (0)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    // Checks stock, saves the line item, reduces stock. Returns the line's subtotal.
    private double processItem(Connection conn, int saleId, SaleItem item) throws SQLException {
        String selectSql = "SELECT name, sell_price, quantity, is_available "
                         + "FROM products WHERE id = ? FOR UPDATE";

        String name;
        double price;

        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setInt(1, item.getProductId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Product id " + item.getProductId() + " not found.");
                }
                name = rs.getString("name");
                price = rs.getDouble("sell_price");

                if (!rs.getBoolean("is_available")) {
                    throw new IllegalStateException(name + " is not available.");
                }
                if (rs.getInt("quantity") < item.getQuantity()) {
                    throw new IllegalStateException("Not enough stock for " + name
                            + " (only " + rs.getInt("quantity") + " left).");
                }
            }
        }

        String insertSql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price) "
                         + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, saleId);
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, price); // price at the time of sale
            ps.executeUpdate();
        }

        String stockSql = "UPDATE products SET quantity = quantity - ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(stockSql)) {
            ps.setInt(1, item.getQuantity());
            ps.setInt(2, item.getProductId());
            ps.executeUpdate();
        }

        return price * item.getQuantity();
    }

    private void updateSaleTotal(Connection conn, int saleId, double total) throws SQLException {
        String sql = "UPDATE sales SET total = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, total);
            ps.setInt(2, saleId);
            ps.executeUpdate();
        }
    }
}