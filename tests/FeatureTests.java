package tests;

import SariSariStore.model.Product;
import SariSariStore.model.SaleItem;
import SariSariStore.service.InventoryService;
import SariSariStore.service.ReportService;
import SariSariStore.service.SalesService;
import SariSariStore.util.DBConnection;
import SariSariStore.util.MoneyFormatter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Framework-free feature tests.
 *
 * Run with the MySQL database configured by DBConnection after compiling the
 * application and this file with the dependencies in lib/.
 */
public final class FeatureTests {
    private static final String TEST_PREFIX = "__feature_test__";

    private FeatureTests() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Sari-Sari Store Feature Tests ===");
        testProductModel();
        testSaleItemModel();
        testMoneyFormatter();
        prepareDatabase();
        cleanupTestData();
        try {
            testInventoryFeatures();
            testSalesFeatures();
            testReportFeatures();
        } finally {
            cleanupTestData();
        }
        System.out.println("\nRESULT: All feature tests passed.");
    }

    private static void testProductModel() {
        System.out.println("\n[Product model]");
        Product product = new Product("Rice", 40.0, 50.0, 10, true);
        checkEquals("Rice", product.getName(), "Product name");
        checkEquals(40.0, product.getBuyPrice(), "Product buy price");
        checkEquals(50.0, product.getSellPrice(), "Product sell price");
        checkEquals(10, product.getQuantity(), "Product quantity");
        check(product.isAvailable(), "Product availability");

        product.setName("Premium Rice");
        product.setBuyPrice(42.0);
        product.setSellPrice(55.0);
        product.setQuantity(12);
        product.setAvailable(false);
        product.setId(7);
        checkEquals("Premium Rice", product.getName(), "Updated product name");
        checkEquals(42.0, product.getBuyPrice(), "Updated buy price");
        checkEquals(55.0, product.getSellPrice(), "Updated sell price");
        checkEquals(12, product.getQuantity(), "Updated quantity");
        check(!product.isAvailable(), "Updated availability");
        checkEquals(7, product.getId(), "Product id");

        expectThrows(IllegalArgumentException.class, () -> product.setBuyPrice(-1),
                "Negative buy price");
        expectThrows(IllegalArgumentException.class, () -> product.setSellPrice(-1),
                "Negative sell price");
        expectThrows(IllegalArgumentException.class, () -> product.setQuantity(-1),
                "Negative quantity");
        System.out.printf("Created and updated product: id=%d, name=%s, buy=%.2f, sell=%.2f, stock=%d, available=%s%n",
                product.getId(), product.getName(), product.getBuyPrice(), product.getSellPrice(),
                product.getQuantity(), product.isAvailable());
    }

    private static void testSaleItemModel() {
        System.out.println("\n[Sale item model]");
        SaleItem item = new SaleItem(3, 2);
        checkEquals(3, item.getProductId(), "Sale item product id");
        checkEquals(2, item.getQuantity(), "Sale item quantity");
        expectThrows(IllegalArgumentException.class, () -> new SaleItem(3, 0),
                "Zero sale quantity");
        expectThrows(IllegalArgumentException.class, () -> new SaleItem(3, -1),
                "Negative sale quantity");
        System.out.printf("Created sale item: productId=%d, quantity=%d%n",
                item.getProductId(), item.getQuantity());
    }

    private static void testMoneyFormatter() {
        System.out.println("\n[Money formatter]");
        String formatted = MoneyFormatter.format(1234.5);
        check(formatted.endsWith("1,234.50"), "Money formatter amount: " + formatted);
        System.out.printf("Formatted PHP amount: PHP %.2f (formatter output: %s)%n", 1234.5, formatted);
    }

    private static void testInventoryFeatures() {
        System.out.println("\n[Inventory features]");
        InventoryService inventory = new InventoryService();
        Product product = new Product(TEST_PREFIX + "inventory", 10.0, 15.0, 5, true);

        inventory.addProduct(product);
        check(product.getId() > 0, "Adding a product sets its generated id");

        Product found = inventory.findProductByName(product.getName());
        check(found != null, "Finding an existing product");
        checkEquals(5, found.getQuantity(), "Found product quantity");
        System.out.printf("Added product: id=%d, name=%s, stock=%d%n",
                found.getId(), found.getName(), found.getQuantity());

        product.setName(TEST_PREFIX + "updated");
        product.setQuantity(8);
        product.setAvailable(false);
        inventory.updateProduct(product);
        found = inventory.findProductByName(product.getName());
        check(found != null, "Finding an updated product");
        checkEquals(8, found.getQuantity(), "Updated product quantity");
        check(!found.isAvailable(), "Updated product availability");
        System.out.printf("Updated product: name=%s, stock=%d, available=%s%n",
                found.getName(), found.getQuantity(), found.isAvailable());

        Product restocked = inventory.restockProduct(product.getName(), 3);
        checkEquals(11, restocked.getQuantity(), "Restocked quantity");
        System.out.println("Restocked product: stock=" + restocked.getQuantity());
        check(inventory.restockProduct(TEST_PREFIX + "missing", 1) == null,
                "Restocking a missing product returns null");
        expectThrows(IllegalArgumentException.class,
                () -> inventory.restockProduct(product.getName(), -1),
                "Negative restock quantity");

        check(inventory.removeProduct(product), "Removing an existing product");
        check(inventory.findProductByName(product.getName()) == null,
                "Removed product is no longer found");
        check(!inventory.removeProduct(product), "Removing a missing product");
        System.out.println("Removed product successfully.");
    }

    private static void testSalesFeatures() throws SQLException {
        System.out.println("\n[Sales features]");
        InventoryService inventory = new InventoryService();
        SalesService sales = new SalesService();
        Product available = new Product(TEST_PREFIX + "sale", 10.0, 15.0, 10, true);
        Product unavailable = new Product(TEST_PREFIX + "unavailable", 5.0, 8.0, 10, false);
        inventory.addProduct(available);
        inventory.addProduct(unavailable);

        ArrayList<SaleItem> items = new ArrayList<>();
        items.add(new SaleItem(available.getId(), 2));
        int saleId = sales.recordSale(items);
        check(saleId > 0, "Recording a sale returns an id");
        checkEquals(8, inventory.findProductByName(available.getName()).getQuantity(),
                "Sale reduces stock");
        checkEquals(30.0, readSaleTotal(saleId), "Sale total");
        checkEquals(1, countSaleItems(saleId), "Sale item count");
        System.out.printf("Recorded sale: receipt=%d, items=%d, total=PHP %.2f, remaining stock=%d%n",
                saleId, countSaleItems(saleId), readSaleTotal(saleId),
                inventory.findProductByName(available.getName()).getQuantity());

        expectThrows(IllegalStateException.class,
                () -> sales.recordSale(itemsFor(available.getId(), 99)),
                "Insufficient stock");
        checkEquals(8, inventory.findProductByName(available.getName()).getQuantity(),
                "Failed sale rolls back stock");
        expectThrows(IllegalStateException.class,
                () -> sales.recordSale(itemsFor(unavailable.getId(), 1)),
                "Unavailable product");
        expectThrows(IllegalArgumentException.class,
                () -> sales.recordSale(itemsFor(999999999, 1)),
                "Missing product");
        expectThrows(IllegalArgumentException.class, () -> sales.recordSale(null),
                "Null sale");
        expectThrows(IllegalArgumentException.class, () -> sales.recordSale(new ArrayList<SaleItem>()),
                "Empty sale");
        System.out.println("Verified rejected unavailable, missing, empty, and over-stock sales; stock was rolled back.");
    }

    private static void testReportFeatures() {
        System.out.println("\n[Report features]");
        InventoryService inventory = new InventoryService();
        ReportService reports = new ReportService();
        inventory.addProduct(new Product(TEST_PREFIX + "low", 1.0, 2.0, 3, true));
        inventory.addProduct(new Product(TEST_PREFIX + "out", 1.0, 2.0, 0, false));
        inventory.addProduct(new Product(TEST_PREFIX + "normal", 1.0, 2.0, 20, true));

        List<Product> lowStock = reports.getLowStockProducts(5);
        List<Product> outOfStock = reports.getOutofStockProducts();
        List<Product> inventorySummary = reports.getInventorySummary();
        check(hasProduct(lowStock, TEST_PREFIX + "low"),
                "Low-stock report includes low-stock product");
        check(!hasProduct(lowStock, TEST_PREFIX + "normal"),
                "Low-stock report excludes normal stock");
        check(hasProduct(outOfStock, TEST_PREFIX + "out"),
                "Out-of-stock report includes zero-stock product");
        check(!hasProduct(outOfStock, TEST_PREFIX + "low"),
                "Out-of-stock report excludes positive stock");
        check(hasProduct(inventorySummary, TEST_PREFIX + "low")
                        && hasProduct(inventorySummary, TEST_PREFIX + "normal"),
                "Inventory summary includes products");
        printReport("Low stock", lowStock);
        printReport("Out of stock", outOfStock);
        printReport("Inventory summary", inventorySummary);
    }

    private static void printReport(String title, List<Product> products) {
        System.out.println(title + " (" + products.size() + " product(s)):");
        for (Product product : products) {
            if (product.getName().startsWith(TEST_PREFIX)) {
                System.out.printf("  - %s: stock=%d, available=%s%n",
                        product.getName(), product.getQuantity(), product.isAvailable());
            }
        }
    }

    private static ArrayList<SaleItem> itemsFor(int productId, int quantity) {
        ArrayList<SaleItem> items = new ArrayList<>();
        items.add(new SaleItem(productId, quantity));
        return items;
    }

    private static boolean hasProduct(List<Product> products, String name) {
        for (Product product : products) {
            if (name.equals(product.getName())) {
                return true;
            }
        }
        return false;
    }

    private static double readSaleTotal(int saleId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT total FROM sales WHERE id = ?")) {
            statement.setInt(1, saleId);
            try (ResultSet results = statement.executeQuery()) {
                check(results.next(), "Sale row exists");
                return results.getDouble(1);
            }
        }
    }

    private static int countSaleItems(int saleId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM sale_items WHERE sale_id = ?")) {
            statement.setInt(1, saleId);
            try (ResultSet results = statement.executeQuery()) {
                check(results.next(), "Sale item count exists");
                return results.getInt(1);
            }
        }
    }

    private static void prepareDatabase() throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS products ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "name VARCHAR(255) NOT NULL, "
                    + "buy_price DOUBLE NOT NULL, "
                    + "sell_price DOUBLE NOT NULL, "
                    + "quantity INT NOT NULL, "
                    + "is_available BOOLEAN NOT NULL)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS sales ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "total DOUBLE NOT NULL)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS sale_items ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "sale_id INT NOT NULL, "
                    + "product_id INT NOT NULL, "
                    + "quantity INT NOT NULL, "
                    + "unit_price DOUBLE NOT NULL)");
        }
    }

    private static void cleanupTestData() throws SQLException {
        List<Integer> productIds = new ArrayList<>();
        List<Integer> saleIds = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT id FROM products WHERE name LIKE ?")) {
                statement.setString(1, TEST_PREFIX + "%");
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        productIds.add(results.getInt(1));
                    }
                }
            }
            if (productIds.isEmpty()) {
                return;
            }
            String placeholders = placeholders(productIds.size());
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT DISTINCT sale_id FROM sale_items WHERE product_id IN (" + placeholders + ")")) {
                setIds(statement, productIds);
                try (ResultSet results = statement.executeQuery()) {
                    while (results.next()) {
                        saleIds.add(results.getInt(1));
                    }
                }
            }
            if (!saleIds.isEmpty()) {
                String salePlaceholders = placeholders(saleIds.size());
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM sale_items WHERE sale_id IN (" + salePlaceholders + ")")) {
                    setIds(statement, saleIds);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM sales WHERE id IN (" + salePlaceholders + ")")) {
                    setIds(statement, saleIds);
                    statement.executeUpdate();
                }
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM products WHERE id IN (" + placeholders + ")")) {
                setIds(statement, productIds);
                statement.executeUpdate();
            }
        }
    }

    private static String placeholders(int count) {
        StringBuilder result = new StringBuilder("?");
        for (int i = 1; i < count; i++) {
            result.append(", ?");
        }
        return result.toString();
    }

    private static void setIds(PreparedStatement statement, List<Integer> ids) throws SQLException {
        for (int i = 0; i < ids.size(); i++) {
            statement.setInt(i + 1, ids.get(i));
        }
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
    }

    private static void checkEquals(Object expected, Object actual, String description) {
        if (!expected.equals(actual)) {
            throw new AssertionError(description + ": expected " + expected + ", got " + actual);
        }
    }

    private static void expectThrows(Class<? extends Throwable> type,
                                     ThrowingRunnable action,
                                     String description) {
        try {
            action.run();
        } catch (Throwable error) {
            if (type.isInstance(error)) {
                return;
            }
            throw new AssertionError(description + ": expected " + type.getSimpleName()
                    + ", got " + error.getClass().getSimpleName(), error);
        }
        throw new AssertionError(description + ": expected " + type.getSimpleName());
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
