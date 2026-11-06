package SariSariStore.model;

import java.time.LocalDateTime;

public class SaleReport {
    private final int id;
    private final LocalDateTime soldAt;
    private final double totalAmount;

    public SaleReport(int id, LocalDateTime soldAt, double totalAmount) {
        this.id = id;
        this.soldAt = soldAt;
        this.totalAmount = totalAmount;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getSoldAt() {
        return soldAt;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}
