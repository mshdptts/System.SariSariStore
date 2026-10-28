package SariSariStore;

import SariSariStore.util.DBConnection;
import java.sql.Connection;
//test
public class Main {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Connected to MySQL!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}