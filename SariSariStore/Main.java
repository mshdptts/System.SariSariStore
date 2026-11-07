package SariSariStore;

import SariSariStore.model.Product;
import SariSariStore.model.SaleItem;
import SariSariStore.model.SaleReport;
import SariSariStore.service.InventoryService;
import SariSariStore.service.ReportService;
import SariSariStore.service.SalesService;

import SariSariStore.util.DatabaseInitializer;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws SQLException {
        DatabaseInitializer.initialize();
        runSaleTest();
    }

    private static void runSaleTest() throws SQLException {
        String productName = "__main_sale_test__" + System.currentTimeMillis();
        Product product = new Product(productName, 10.0, 15.0, 5, true);
        InventoryService inventory = new InventoryService();
        inventory.addProduct(product);

        ArrayList<SaleItem> items = new ArrayList<>();
        items.add(new SaleItem(product.getId(), 2));
        int saleId = new SalesService().recordSale(items);

        Product updatedProduct = inventory.findProductByName(productName);
        check(updatedProduct != null && updatedProduct.getQuantity() == 3,
                "Sale should reduce stock from 5 to 3");

        List<SaleReport> sales = new ReportService()
                .getDailySalesReport(LocalDate.now().toString());
        SaleReport report = sales.stream()
                .filter(sale -> sale.getId() == saleId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Sale missing from daily report"));
        check(report.getTotalAmount() == 30.0, "Sale total should be 30.00");

        System.out.printf("Sale test passed and saved: id=%d, total=%.2f, remaining stock=%d%n",
                saleId, report.getTotalAmount(), updatedProduct.getQuantity());
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}