package SariSariStore.model;

public class Product {

    

private String name;
private double buyPrice;
private double sellPrice;
private int quantity;
private boolean isAvailable;

//constructor
public Product(String name, double buyPrice, double sellPrice, int quantity, boolean isAvailable)
 {
    this.name = name;
    this.buyPrice = buyPrice;
    this.sellPrice = sellPrice;
    this.quantity = quantity;
    this.isAvailable = isAvailable;
}



//getters and setters
public String getName() {
    return name; }
public void setName(String name) {
    this.name = name; }

    public double getBuyPrice() {
    return buyPrice; }
public void setBuyPrice(double buyPrice) {
    this.buyPrice = buyPrice; }

    public double getSellPrice() {
    return sellPrice; }
public void setSellPrice(double sellPrice) {
    this.sellPrice = sellPrice; }

    public int getQuantity() {
    return quantity; }
public void setQuantity(int quantity) {
    this.quantity = quantity; }


    public boolean isAvailable() {
    return isAvailable; }
public void setAvailable(boolean isAvailable) {
    this.isAvailable = isAvailable; }




















}