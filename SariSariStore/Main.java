//small test for salesservice and inventoryservice
//ai generated test
package SariSariStore;

import SariSariStore.model.Product;
import SariSariStore.model.SaleItem;
import SariSariStore.service.InventoryService;
import SariSariStore.service.SalesService;

import java.util.ArrayList;

public class Main {

    // Adds the product only if it isn't in the database yet, then returns it
    private static Product ensureProduct(InventoryService inventory, String name,
                                         double buy, double sell, int qty) {
        Product p = inventory.findProductByName(name);
        if (p == null) {
            p = new Product(name, buy, sell, qty, true);
            inventory.addProduct(p);
        }
        return p;
    }

    public static void main(String[] args) {
        InventoryService inventory = new InventoryService();
        SalesService sales = new SalesService();

        // 1. Make sure the products exist
        Product noodles = ensureProduct(inventory, "Lucky Me Pancit Canton", 11.50, 15.00, 50);
        Product coffee  = ensureProduct(inventory, "Nescafe 3in1 Original", 6.00, 8.00, 100);
        Product canned  = ensureProduct(inventory, "Argentina Corned Beef", 22.00, 28.00, 30);
        Product candy   = ensureProduct(inventory, "Choco Mucho", 3.00, 5.00, 5);

        // 2. One customer buys several products in ONE receipt
        ArrayList<SaleItem> cart = new ArrayList<>();
        cart.add(new SaleItem(canned.getId(), 3));   // 3 x 28 = 84
        cart.add(new SaleItem(coffee.getId(), 2));   // 2 x 8  = 16
        cart.add(new SaleItem(noodles.getId(), 1));  // 1 x 15 = 15

        int receipt = sales.recordSale(cart);
        System.out.println("Sale OK, receipt #" + receipt + " (expected total: 115.00)");

        // 3. Show the stock of every test product now
        Product[] products = {noodles, coffee, canned, candy};
        for (Product product : products) {
            Product p = inventory.findProductByName(product.getName());
            System.out.println(p.getId() + " | " + p.getName() + " | stock: " + p.getQuantity());
        }

        // 4. Failure test: second item has too little stock, so the WHOLE sale rolls back
        try {
            ArrayList<SaleItem> badCart = new ArrayList<>();
            badCart.add(new SaleItem(coffee.getId(), 1));  // this one is fine...
            badCart.add(new SaleItem(candy.getId(), candy.getQuantity() + 1)); // one more than available
            sales.recordSale(badCart);
        } catch (Exception e) {
            System.out.println("Expected error: " + e.getMessage());
        }
    }
}