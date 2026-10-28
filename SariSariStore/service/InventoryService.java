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



    public void addProduct











}