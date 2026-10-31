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
- **`SalesService.java`**: Handles sales: computes totals and reduces stock after each sale.
- **`ReportService.java`**: Creates reports like daily sales and low-stock alerts.

### gui/
- **`MainFrame.java`**: The main window that holds everything together.
- **`ProductPanel.java`**: Screen for viewing and managing products.
- **`SalesDialog.java`**: Pop-up for recording a sale.

### util/
- **`MoneyFormatter.java`** *(optional)*: Formats prices as pesos (e.g., ₱1,250.00).

### ADDED DATA BASE!!!!
- This project now utilises MYSQL to manage multiple data base for sales and product items 

## Testing

The feature test runner covers product and sale-item validation, money formatting,
inventory operations, sales transactions, rollback behavior, and inventory reports.
It uses the MySQL connection configured in `DBConnection`.

From the repository root, compile and run it with:

```powershell
$out = Join-Path $env:TEMP "sarisari-tests"
New-Item -ItemType Directory -Force $out
javac -cp "lib/*" -d $out (Get-ChildItem -Recurse -Filter *.java SariSariStore, tests | Select-Object -ExpandProperty FullName)
java -cp "$out;lib/*" tests.FeatureTests
```
