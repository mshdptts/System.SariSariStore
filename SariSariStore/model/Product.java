package SariSariStore.model;

public class Product {

    
private int id;
private String name;
private double buyPrice;
private double sellPrice;
private int quantity;
private boolean isAvailable;

// Constructor
public Product(String name, double buyPrice, double sellPrice, int quantity, boolean isAvailable)
 {
    this.name = name;
    this.buyPrice = buyPrice;
    this.sellPrice = sellPrice;
    this.quantity = quantity;
    this.isAvailable = isAvailable;
}
public Product(int id, String name, double buyPrice, double sellPrice, int quantity, boolean isAvailable) {
    this(name, buyPrice, sellPrice, quantity, isAvailable);
    this.id = id;
}



// Getters and setters
public String getName() {
    return name; }
public void setName(String name) {
    this.name = name; }

    public double getBuyPrice() {
    return buyPrice; }
public void setBuyPrice(double buyPrice) {
    if(buyPrice < 0) {
        throw new IllegalArgumentException("Buy price cannot be negative.");
    }
    this.buyPrice = buyPrice; }

    public double getSellPrice() {
    return sellPrice; }
public void setSellPrice(double sellPrice) {
    if(sellPrice < 0) {
        throw new IllegalArgumentException("Sell price cannot be negative.");
    }
    this.sellPrice = sellPrice; }

    public int getQuantity() {
    return quantity; }
public void setQuantity(int quantity) {
    if(quantity < 0) {
        throw new IllegalArgumentException("Quantity cannot be negative.");
    }
    this.quantity = quantity; }


    public boolean isAvailable() {
    return isAvailable; }
public void setAvailable(boolean isAvailable) {
    this.isAvailable = isAvailable; }
    
public int getId() {
    return id; }
public void setId(int id) {
    this.id = id; }



















}