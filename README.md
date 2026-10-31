# Sari-Sari Store

A Java desktop application for managing products, inventory, sales, and reports.

## Project structure

```text
SariSariStore/
├── Main.java
├── model/
│   └── Product.java
├── service/
│   ├── InventoryService.java
│   ├── SalesService.java
│   └── ReportService.java
├── gui/
│   ├── MainFrame.java
│   ├── ProductPanel.java
│   └── SalesDialog.java
└── util/
    └── MoneyFormatter.java [optional]
```
### Main
- **`Main.java`**: Starts the app and opens the main window.

### model/
- **`Product.java`**: Represents one store item (name, price, stock).

### service/
- **`InventoryService.java`**: Add, edit, delete, and search products, and keep track of stock.
- **`SalesService.java`**: Handles sales: computes totals, reduces stock after each sale and records sales.
- **`SalesItem.java`**: Breaks down individual sales into its components and records it
- **`ReportService.java`**: Creates reports like daily sales and low-stock alerts.

### gui/
- **`MainFrame.java`**: The main window that holds everything together.
- **`ProductPanel.java`**: Screen for viewing and managing products.
- **`SalesDialog.java`**: Pop-up for recording a sale.

### util/
- **`MoneyFormatter.java`** *(optional)*: Formats prices as pesos (e.g., ₱1,250.00).
