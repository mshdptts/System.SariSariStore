package SariSariStore.service;

import SariSariStore.model.Product;
import java.util.ArrayList;

public class InventoryService {

    private ArrayList<Product> products;

    public InventoryService() {
        this.products = new ArrayList<>();
    }

    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }

        products.add(product);
    }

    public boolean removeProduct(Product product) {
        return products.remove(product);
    }

    public ArrayList<Product> getProducts() {
        return new ArrayList<>(products);
    }

    public Product findProductByName(String name) {
        for (Product product : products) {
            if (product.getName().equalsIgnoreCase(name)) {
                return product;
            }
        }

        return null;
    }

    public Product restockProduct(String name, int quantity) {
        Product product = findProductByName(name);
        if (product != null) {
            if (quantity < 0) {
                throw new IllegalArgumentException("Quantity cannot be negative.");
            }
            product.setQuantity(product.getQuantity() + quantity);
            return product;
        }
        return null;
    }

    public Product getLowStockProduct(int threshold) {
        for (Product product : products) {
            if (product.getQuantity() < threshold) {
                return product;
            }
        }
        return null;
    }
}