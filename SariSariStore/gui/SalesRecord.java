package SariSariStore.model;

import java.sql.Timestamp;

/** one row of the sales table: a single receipt. used by the sales reports. */
public class SaleRecord {

    private final int id;
    private final Timestamp saleDate;
    private final double total;

    public SaleRecord(int id, Timestamp saleDate, double total) {
        this.id = id;
        this.saleDate = saleDate;
        this.total = total;
    }

    public int getId() {
        return id;
    }

    public Timestamp getSaleDate() {
        return saleDate;
    }

    public double getTotal() {
        return total;
    }
}
