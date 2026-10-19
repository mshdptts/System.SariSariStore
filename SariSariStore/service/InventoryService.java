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
}