package SariSariStore.model;

public class SaleItem {
    private int productId;
    private int quantity;


    public SaleItem(int productId, int quantity) {

        if (quantity <= 0){
            throw new IllegalArgumentException("Quantity must be atleast 1");
        }
        this.productId = productId;
        this.quantity = quantity;


    }
    public int getProductId() {
        return productId;
    }
    public int getQuantity(){
        return quantity;
    }





}
