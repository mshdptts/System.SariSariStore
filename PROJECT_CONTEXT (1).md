# Sari-Sari Store — Project Context for AI Models

> **Purpose:** a structured map of this Java project: what every file and class does, every public function, and how the parts connect (code dependencies, database tables, data flow). Written so an AI model can load it as context before reading or changing the code.
>
> **Snapshot described:** git branch `withDB`, last commit `abfed65` (2026-10-31, message: "finalized back end [sales report, feature tests], gui to be added]"). The working tree equals that commit except for CRLF line endings.
>
> **How it was checked:** every fact comes from reading the source. The project compiles cleanly on OpenJDK 21 (`javac -Xlint:all`, zero warnings). The DB-free parts of `tests/FeatureTests.java` (model validation, `MoneyFormatter`) were executed and pass. Everything that touches MySQL was **not executed** (no server available), so those claims are labelled **(code-derived)**, and the database schema is labelled **(inferred)**.
>
> **Read first:** §1 quick facts, §2 status, §9 known issues. Do not trust `README.md` on its own (see §9.3).

---

## 1. Quick facts

| Item | Value |
|---|---|
| Project | Sari-Sari Store. A "sari-sari store" is a small neighborhood convenience shop in the Philippines. |
| What it does | Desktop Swing application for managing products, stock (inventory), multi-item sales, and reports. |
| Language | Java. Uses lambdas (Java 8+). Verified compiling on OpenJDK 21. |
| Build tool | None (no Maven/Gradle). Plain `javac` with `lib/*` on the classpath. |
| Database | MySQL, database `sarisari_store`, accessed with raw JDBC (`PreparedStatement`). No ORM. |
| Libraries in `lib/` | `mysql-connector-j-26.10.0.jar` (JDBC driver, **used**, auto-registers `com.mysql.cj.jdbc.Driver`), `flatlaf-3.7.2.jar` (FlatLaf look-and-feel, used by `Main`) |
| Root package | `SariSariStore` (capital S). Source root = repo root (no `src/` folder). Tests are in package `tests` at the repo root. |
| Architecture | Layered: `gui` → `service` (business logic **and** all SQL) → `model` + `util` → MySQL |
| Entry points | `SariSariStore.Main#main` (FlatLaf Swing launcher) and `tests.FeatureTests#main` (test runner) |
| Tests | One framework-free runner (`tests/FeatureTests.java`, no JUnit). Needs a live MySQL. |
| Size | 11 app files (≈600 lines; 2 of them are empty files, 1 is an empty class) + 1 test file (370 lines) |
| Origin | GitHub `mshdptts/System.SariSariStore` (remote `origin`); 24 commits, 2026-10-19 to 2026-10-31 |
| Dev environment clues | Windows (CRLF line endings, PowerShell commands in README), VS Code (git config), Laragon MySQL (comment in `DBConnection`) |

---

## 2. Status at a glance

| Component | Status | Notes |
|---|---|---|
| `model.Product`, `model.SaleItem` | IMPLEMENTED | Plain data classes with some validation |
| `service.InventoryService` | IMPLEMENTED, tested | add / update / remove / find by name / restock |
| `service.SalesService` | IMPLEMENTED, tested | transactional multi-item sale with stock checks and rollback |
| `service.ReportService`: stock reports | IMPLEMENTED, tested | low stock, out of stock, full inventory |
| `service.ReportService`: sales reports | **BROKEN, untested** | `getDailySalesReport`, `getSalesReport` (§9, issue 1) |
| `service.ReportService`: best-selling products | NOT STARTED | only a commented-out idea at `ReportService.java:127-129` |
| `util.DBConnection` | IMPLEMENTED | hard-coded local MySQL settings |
| `util.MoneyFormatter` | IMPLEMENTED | called only by tests so far |
| `gui.MainFrame` | STUB | `public class MainFrame { }`: not even a `JFrame` |
| `gui.ProductPanel`, `gui.SalesDialog` | EMPTY FILE | 0 bytes |
| `Main` | DEMO SCRIPT | writes to the real database on every run |
| Database schema script | NOT IN REPO (optional) | tables already exist in the author's local MySQL; `DBConnection` holds connection settings only; a minimal copy of the DDL is in `FeatureTests` |

---

## 3. Repository layout

```text
sarisari_store_ni_ralph/              <- repo root = Java source root = classpath root
├── README.md                         human-written overview (partly outdated, see §9.3)
├── .gitignore                        ignores out/ and *.class
├── lib/
│   ├── mysql-connector-j-26.10.0.jar   MySQL Connector/J 26.10.0 (JDBC driver)
│   └── flatlaf-3.7.2.jar               FlatLaf 3.7.2 (Swing look-and-feel; unused so far)
├── SariSariStore/                    root package `SariSariStore`
│   ├── Main.java                     console demo / smoke test
│   ├── model/
│   │   ├── Product.java              store item entity (mutable)
│   │   └── SaleItem.java             one cart line: productId + quantity (immutable)
│   ├── service/
│   │   ├── InventoryService.java     product CRUD + restock       (table: products)
│   │   ├── SalesService.java         transactional sale recording (tables: sales, sale_items, products)
│   │   └── ReportService.java        read-only reports            (tables: products, sales)
│   ├── util/
│   │   ├── DBConnection.java         JDBC connection factory
│   │   └── MoneyFormatter.java       Philippine peso formatting
│   └── gui/
│       ├── MainFrame.java            empty class
│       ├── ProductPanel.java         EMPTY FILE (0 bytes)
│       └── SalesDialog.java          EMPTY FILE (0 bytes)
└── tests/
    └── FeatureTests.java             package `tests`; framework-free test runner (370 lines)
```

---

## 4. Architecture and connections

### 4.1 Layers

```text
gui/*  (planned, empty)  ──calls──►  service/*  ──uses──►  model/*  (Product, SaleItem)
                                        │
                                        └──uses──►  util/DBConnection ──JDBC──► MySQL `sarisari_store`

util/MoneyFormatter: display helper with no dependencies; currently called only by tests.
```

The three services are **independent**: none calls another. Each opens its own JDBC connection and runs its own SQL. In particular `SalesService` does **not** use `InventoryService` or `Product`; it updates `products.quantity` with its own SQL inside its transaction.

### 4.2 Code dependencies (verified from `import` statements)

| Class | Uses (project classes) | Uses (external) |
|---|---|---|
| `Main` | `Product`, `SaleItem`, `InventoryService`, `SalesService` | `java.util.ArrayList` |
| `InventoryService` | `Product`, `DBConnection` | `java.sql.*` |
| `SalesService` | `SaleItem`, `DBConnection` | `java.sql.*` |
| `ReportService` | `Product`, `DBConnection` | `java.sql.*` |
| `DBConnection` | none | `java.sql.*`; MySQL driver loaded at runtime via `DriverManager` |
| `MoneyFormatter` | none | `java.text.NumberFormat`, `java.util.Locale` |
| `Product`, `SaleItem` | none | none |
| `MainFrame`, `ProductPanel`, `SalesDialog` | none | none (no `javax.swing`/FlatLaf imports anywhere) |
| `tests.FeatureTests` | all of `Product`, `SaleItem`, the 3 services, `DBConnection`, `MoneyFormatter` | `java.sql.*`, `java.util.*` |

Reverse view (who uses whom):

- `Product` ← `Main`, `InventoryService`, `ReportService`, `FeatureTests`
- `SaleItem` ← `Main`, `SalesService`, `FeatureTests`
- `InventoryService` ← `Main`, `FeatureTests`
- `SalesService` ← `Main`, `FeatureTests`
- `ReportService` ← `FeatureTests`
- `DBConnection` ← `InventoryService`, `SalesService`, `ReportService`, `FeatureTests`
- `MoneyFormatter` ← `FeatureTests` only
- `MainFrame`, `ProductPanel`, `SalesDialog` ← nothing

### 4.3 Database access by class

| Table | `InventoryService` | `SalesService` | `ReportService` | `FeatureTests` |
|---|---|---|---|---|
| `products` | INSERT; UPDATE all fields by id; UPDATE `quantity = quantity + ?` by name; DELETE by id; SELECT `*` by name | SELECT `name, sell_price, quantity, is_available` ... `FOR UPDATE` by id; UPDATE `quantity = quantity - ?` by id | SELECT `*` (all rows, `quantity < ?`, or `quantity = 0`) | CREATE IF NOT EXISTS; SELECT ids by name prefix; DELETE |
| `sales` | none | INSERT `(total)` = 0, then UPDATE `total` | SELECT `*` by `DATE(sale_date)` (single day or range). **Broken**, see §9 issue 1 | CREATE IF NOT EXISTS; SELECT `total`; DELETE |
| `sale_items` | none | INSERT one row per cart line | none | CREATE IF NOT EXISTS; SELECT COUNT; SELECT DISTINCT `sale_id`; DELETE |

Logical relationships between tables (none are declared as foreign keys in the repo):

```text
sales (1) ───< sale_items (many) >─── (1) products
sale_items.sale_id    → sales.id
sale_items.product_id → products.id
sale_items.unit_price = products.sell_price copied at the moment of sale (price snapshot)
sales.total           = sum of (unit_price × quantity) over the sale's sale_items
```

### 4.4 Main call chains

- **Add a product:** `new Product(...)` → `InventoryService.addProduct(p)` → INSERT into `products` → the generated id is written back into the same `Product` object (`p.setId(...)`).
- **Record a sale:** `new SaleItem(productId, qty)` (one per cart line) → `SalesService.recordSale(ArrayList<SaleItem>)` → one DB transaction over `sales`, `products`, `sale_items` → returns the receipt id (`sales.id`).
- **Stock report:** `ReportService.getLowStockProducts / getOutofStockProducts / getInventorySummary` → SELECT on `products` → `ArrayList<Product>`.
- **Sales report:** `ReportService.getDailySalesReport / getSalesReport` → SELECT on `sales` → **fails or returns nothing useful** (§9 issue 1).
- **Format money for display:** `MoneyFormatter.format(double)` → string like `₱1,250.00`.

---

## 5. Data model

### 5.1 Java classes

| Class | Mutability | Fields | Validation |
|---|---|---|---|
| `Product` | mutable | `int id` (0 until saved), `String name`, `double buyPrice`, `double sellPrice`, `int quantity`, `boolean isAvailable` | **Setters** reject negative `buyPrice`, `sellPrice`, `quantity` with `IllegalArgumentException`. **Constructors, `setName`, `setId`, `setAvailable` do not validate.** |
| `SaleItem` | immutable (no setters) | `int productId`, `int quantity` | Constructor rejects `quantity <= 0` with `IllegalArgumentException`. `productId` is not checked. |

Mapping to the database: `Product` ↔ one row of `products`; `SaleItem` ↔ one row of `sale_items` **without** a price (the price is read from `products.sell_price` at sale time). There is **no `Sale` class**: a sale exists only as database rows, and `recordSale` returns just its id.

Field ↔ column naming (camelCase ↔ snake_case): `buyPrice` ↔ `buy_price`, `sellPrice` ↔ `sell_price`, `isAvailable` ↔ `is_available`; `id`, `name`, `quantity` are identical.

### 5.2 Database schema **(inferred)**

There is no schema script in the repo (the database was created outside it, and the author does not need one because the tables already exist locally). `DBConnection` holds only connection settings and **no table definitions**. The only DDL in the repo is in `FeatureTests.prepareDatabase()` (`CREATE TABLE IF NOT EXISTS`):

```sql
CREATE TABLE IF NOT EXISTS products (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  name         VARCHAR(255) NOT NULL,
  buy_price    DOUBLE NOT NULL,
  sell_price   DOUBLE NOT NULL,
  quantity     INT NOT NULL,
  is_available BOOLEAN NOT NULL
);
CREATE TABLE IF NOT EXISTS sales (
  id    INT AUTO_INCREMENT PRIMARY KEY,
  total DOUBLE NOT NULL
);
CREATE TABLE IF NOT EXISTS sale_items (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  sale_id    INT NOT NULL,
  product_id INT NOT NULL,
  quantity   INT NOT NULL,
  unit_price DOUBLE NOT NULL
);
```

Notes:

- `ReportService` reads **`sales.sale_date`**, which is **not** in this DDL. The real database presumably has it (probably `DATETIME` or `TIMESTAMP DEFAULT CURRENT_TIMESTAMP`, because `SalesService` inserts only `total`). A database created only by `FeatureTests.prepareDatabase()` lacks it.
- No foreign keys, no `UNIQUE` on `products.name` (even though names are used as lookup keys), no indexes are declared.
- The database `sarisari_store` itself must already exist: no code issues `CREATE DATABASE`.
- Connection settings are in `util/DBConnection.java`: `jdbc:mysql://localhost:3306/sarisari_store?useSSL=false&serverTimezone=Asia/Manila`, user `root`, empty password.

---

## 6. Public API index (every function at a glance)

Legend: **IAE** = `IllegalArgumentException`, **ISE** = `IllegalStateException`, **RTE** = `RuntimeException`. Every JDBC `SQLException` inside a service is wrapped in an RTE (original attached as cause), so service methods declare no `throws`. All throws listed are unchecked.

### `SariSariStore.model.Product`

| Signature | Returns | Throws | Purpose |
|---|---|---|---|
| `Product(String name, double buyPrice, double sellPrice, int quantity, boolean isAvailable)` | n/a | none | New, not-yet-saved product (`id` = 0). No validation. |
| `Product(int id, String name, double buyPrice, double sellPrice, int quantity, boolean isAvailable)` | n/a | none | Product loaded from the DB (used by the services' `mapRow`). Delegates to the constructor above, then sets `id`. |
| `getId()` / `setId(int id)` | `int` / `void` | none | DB primary key; set by `InventoryService.addProduct` after INSERT. |
| `getName()` / `setName(String name)` | `String` / `void` | none | No validation (null/empty accepted). |
| `getBuyPrice()` / `setBuyPrice(double buyPrice)` | `double` / `void` | IAE if `< 0` | Cost per unit. |
| `getSellPrice()` / `setSellPrice(double sellPrice)` | `double` / `void` | IAE if `< 0` | Selling price per unit. |
| `getQuantity()` / `setQuantity(int quantity)` | `int` / `void` | IAE if `< 0` | Units in stock. |
| `isAvailable()` / `setAvailable(boolean isAvailable)` | `boolean` / `void` | none | Whether the product may be sold. |

### `SariSariStore.model.SaleItem`

| Signature | Returns | Throws | Purpose |
|---|---|---|---|
| `SaleItem(int productId, int quantity)` | n/a | IAE if `quantity <= 0` ("Quantity must be atleast 1") | One cart line. |
| `getProductId()` | `int` | none | Id of the product being bought. |
| `getQuantity()` | `int` | none | Units being bought. |

### `SariSariStore.service.InventoryService` (table `products`)

| Signature | Returns | Throws | Purpose |
|---|---|---|---|
| `void addProduct(Product product)` | `void` | RTE | INSERT; writes the generated id back into `product`. |
| `boolean removeProduct(Product product)` | `boolean` | RTE | DELETE by `product.getId()`. `true` if a row was deleted, `false` if the id did not exist. |
| `void updateProduct(Product product)` | `void` | RTE | UPDATE all editable fields by `product.getId()`. Does not report whether the id existed. |
| `Product findProductByName(String name)` | `Product` or `null` | RTE | Exact-name lookup; returns the first match or `null`. |
| `Product restockProduct(String name, int quantity)` | `Product` or `null` | IAE if `quantity < 0`; RTE | Adds `quantity` to stock by name. Returns the refreshed product, or `null` if no product has that name. |

### `SariSariStore.service.SalesService` (tables `sales`, `sale_items`, `products`)

| Signature | Returns | Throws | Purpose |
|---|---|---|---|
| `int recordSale(ArrayList<SaleItem> items)` | receipt id (`sales.id`) | IAE (null/empty list, unknown product id); ISE (product unavailable, not enough stock); RTE (SQL error) | Records one customer purchase atomically and reduces stock. |

### `SariSariStore.service.ReportService` (all return `ArrayList<Product>`)

| Signature | Status | Purpose |
|---|---|---|
| `getLowStockProducts(int treshold)` | works, tested | Products with `quantity < treshold` (strictly less; includes quantity 0). Parameter name is misspelled in the source. |
| `getOutofStockProducts()` | works, tested | Products with `quantity = 0`. Note the lowercase "of" in the method name. |
| `getInventorySummary()` | works, tested | Every product. |
| `getDailySalesReport(String date)` | **broken**, untested | Intended: sales on one day. Queries `sales` but maps rows as `Product` (§9 issue 1). |
| `getSalesReport(String startDate, String endDate)` | **broken**, untested | Intended: sales between two dates, inclusive. Same defect. |

Dates are `String`s bound with `setString` (format presumably `YYYY-MM-DD`, inferred from `DATE(sale_date) = ?`). All five methods throw RTE on SQL errors.

### `SariSariStore.util`

| Signature | Returns | Throws | Purpose |
|---|---|---|---|
| `static Connection DBConnection.getConnection()` | new JDBC `Connection` | `SQLException` (checked, the **only** checked exception in the API) | Opens a fresh MySQL connection on every call. Caller must close it. |
| `static String MoneyFormatter.format(double amount)` | `String` | none | Peso format via `NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH"))`. `format(1250)` → `₱1,250.00` (verified on JDK 21). |

### Entry points

| Signature | Purpose |
|---|---|
| `static void SariSariStore.Main.main(String[] args)` | Console demo (§7.1). |
| `static void tests.FeatureTests.main(String[] args) throws Exception` | Runs all feature tests (§7.10). |

---

## 7. Class reference

### 7.1 `SariSariStore.Main`  `[DEMO SCRIPT]`

- **File:** `SariSariStore/Main.java`
- **Role:** console smoke test for `SalesService` and `InventoryService`. The file's own header comments say "small test for salesservice and inventoryservice" and "ai generated test". It does **not** start a GUI, despite what the README claims.
- **Depends on:** `Product`, `SaleItem`, `InventoryService`, `SalesService`.
- **Requires:** MySQL running and the `products`, `sales`, `sale_items` tables already created (`Main` does not create them).
- **Members:**
  - `private static Product ensureProduct(InventoryService inventory, String name, double buy, double sell, int qty)`: calls `findProductByName`; if `null`, creates `new Product(name, buy, sell, qty, true)` and `addProduct`; returns the product with its id.
  - `public static void main(String[] args)` does this, in order:
    1. Ensures four products exist: "Lucky Me Pancit Canton" (buy 11.50 / sell 15.00 / qty 50), "Nescafe 3in1 Original" (6.00 / 8.00 / 100), "Argentina Corned Beef" (22.00 / 28.00 / 30), "Choco Mucho" (3.00 / 5.00 / 5).
    2. Records one sale with three lines: corned beef ×3, coffee ×2, noodles ×1. Expected total 115.00 (3×28 + 2×8 + 1×15). Prints the receipt id.
    3. Prints `id | name | stock` for the four products.
    4. Failure test: a cart with coffee ×1 plus Choco Mucho × (its stock + 1). Expects an exception ("Not enough stock ..."), prints it as "Expected error: ...", and the whole sale (including the coffee line) is rolled back.
- **Side effects on the real DB:** inserts the four products if missing; **every run permanently records another sale** and lowers stock (corned beef −3, coffee −2, noodles −1).

### 7.2 `SariSariStore.model.Product`  `[IMPLEMENTED]`

- **File:** `SariSariStore/model/Product.java`
- **Role:** entity for one store item; mirrors one row of `products`.
- **Depends on:** nothing. **Used by:** `Main`, `InventoryService`, `ReportService`, `FeatureTests`.
- **Fields, constructors, methods:** see §5.1 and §6.
- **Behavior notes:** only the setters validate (negative `buyPrice`, `sellPrice`, `quantity` → `IllegalArgumentException` with messages "Buy price cannot be negative.", "Sell price cannot be negative.", "Quantity cannot be negative."). The constructors assign directly, so `new Product("x", -1, -1, -1, true)` is accepted. There is no `equals`, `hashCode` or `toString`.

### 7.3 `SariSariStore.model.SaleItem`  `[IMPLEMENTED]`

- **File:** `SariSariStore/model/SaleItem.java`
- **Role:** one line of a customer's cart (which product, how many). Input to `SalesService.recordSale`.
- **Depends on:** nothing. **Used by:** `Main`, `SalesService`, `FeatureTests`.
- **Behavior notes:** immutable. Carries no price: the unit price is read from the database at sale time. Constructor throws `IllegalArgumentException("Quantity must be atleast 1")` for `quantity <= 0`; `productId` is not validated here (an unknown id is rejected later by `SalesService`).

### 7.4 `SariSariStore.service.InventoryService`  `[IMPLEMENTED, tested]`

- **File:** `SariSariStore/service/InventoryService.java`
- **Role:** product management (add, update, remove, find, restock) against table `products`.
- **Depends on:** `Product`, `DBConnection`. **Used by:** `Main`, `FeatureTests`. Stateless (no fields).
- **SQL used:**
  - `addProduct`: `INSERT INTO products (name, buy_price, sell_price, quantity, is_available) VALUES (?, ?, ?, ?, ?)` with `RETURN_GENERATED_KEYS`. Error message: "could not add product" + SQL message.
  - `removeProduct`: `DELETE FROM products WHERE id = ?`.
  - `updateProduct`: `UPDATE products SET name = ?, buy_price = ?, sell_price = ?, quantity = ?, is_available = ? WHERE id = ?`.
  - `findProductByName`: `SELECT * FROM products WHERE name = ?`.
  - `restockProduct`: `UPDATE products SET quantity = quantity + ? WHERE name = ?`; if zero rows changed returns `null`, otherwise calls `findProductByName(name)` for the fresh state. A quantity of 0 is allowed; negative throws "Quantity cannot be negative.".
- **Private helper:** `Product mapRow(ResultSet rs)` builds a `Product` through the 6-argument constructor from columns `id, name, buy_price, sell_price, quantity, is_available`.
- **Test-confirmed behavior:** after `addProduct`, `getId() > 0`; update persists name, quantity and availability; `restockProduct(name, 3)` on stock 8 returns stock 11; restocking a missing name returns `null`; `removeProduct` returns `true` the first time and `false` the second.
- **Caveats:** `name` is the lookup key but is not unique in the schema: `findProductByName` returns the first match and `restockProduct` updates every row with that name. `removeProduct` does not touch `sale_items`.

### 7.5 `SariSariStore.service.SalesService`  `[IMPLEMENTED, tested]`

- **File:** `SariSariStore/service/SalesService.java`
- **Role:** records a customer's purchase as one atomic transaction and reduces stock.
- **Depends on:** `SaleItem`, `DBConnection`. **Used by:** `Main`, `FeatureTests`. Stateless.
- **Public method:** `int recordSale(ArrayList<SaleItem> items)`. Takes a concrete `ArrayList` (not `List`).
- **Algorithm:**
  1. If `items` is `null` or empty → `IllegalArgumentException("A sale needs at least one item.")`.
  2. Open a connection, `setAutoCommit(false)`.
  3. `insertSale`: `INSERT INTO sales (total) VALUES (0)`; read the generated key → `saleId`.
  4. For every item, in order, `processItem(conn, saleId, item)`:
     1. `SELECT name, sell_price, quantity, is_available FROM products WHERE id = ? FOR UPDATE` (row lock).
     2. No row → `IllegalArgumentException("Product id N not found.")`.
     3. `is_available` false → `IllegalStateException("<name> is not available.")`.
     4. `quantity` < requested → `IllegalStateException("Not enough stock for <name> (only N left).")`.
     5. `INSERT INTO sale_items (sale_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)` with `unit_price` = the current `sell_price`.
     6. `UPDATE products SET quantity = quantity - ? WHERE id = ?`.
     7. Return `sell_price × quantity` (the line subtotal).
  5. `updateSaleTotal`: `UPDATE sales SET total = ? WHERE id = ?` with the sum of subtotals.
  6. `commit()`; return `saleId` (the receipt number).
- **Failure handling:** any `SQLException` or `RuntimeException` inside steps 3-5 triggers `rollback()` of the **entire** sale (earlier lines included) and is rethrown. `IllegalArgumentException` and `IllegalStateException` reach the caller unchanged; SQL errors reach the caller as `RuntimeException("Could not record sale: ...")`.
- **Private helpers:** `int insertSale(Connection)`, `double processItem(Connection, int saleId, SaleItem)`, `void updateSaleTotal(Connection, int saleId, double total)`.
- **Test-confirmed behavior:** selling 2 units at 15.0 gives `sales.total` = 30.0, one `sale_items` row, stock 10 → 8; over-stock and unavailable products throw `IllegalStateException` and leave stock unchanged; unknown product id, `null` list and empty list throw `IllegalArgumentException`.
- **Note:** the commit message of `e2b3cc1` says this class was "still to be refactored".

### 7.6 `SariSariStore.service.ReportService`  `[stock reports work; sales reports BROKEN]`

- **File:** `SariSariStore/service/ReportService.java`
- **Role:** read-only reports. Every method returns `ArrayList<Product>`.
- **Depends on:** `Product`, `DBConnection`. **Used by:** `FeatureTests`. Stateless.
- **Methods and SQL:**
  - `getLowStockProducts(int treshold)`: `SELECT * FROM products WHERE quantity < ?`.
  - `getOutofStockProducts()`: `SELECT * FROM products WHERE quantity = 0`.
  - `getInventorySummary()`: `SELECT * FROM products`.
  - `getDailySalesReport(String date)`: `SELECT * FROM sales WHERE DATE(sale_date) = ?`.
  - `getSalesReport(String startDate, String endDate)`: `SELECT * FROM sales WHERE DATE(sale_date) BETWEEN ? AND ?`.
- **Private helper:** `Product mapRow(ResultSet rs)`: a copy of the one in `InventoryService`. It is applied to `products` rows in the first three methods (correct) and to `sales` rows in the last two (wrong, see §9 issue 1).
- **Commented-out idea (lines 127-129):** `getBestSellingProducts(startDate, endDate)`, with the Tagalog note "wag na to kahirap next time nalang" ("skip this, it's too hard, next time").
- **Test coverage:** `FeatureTests.testReportFeatures` covers only the three stock reports (threshold 5: a product with stock 3 is included, stock 20 is excluded).

### 7.7 `SariSariStore.util.DBConnection`  `[IMPLEMENTED]`

- **File:** `SariSariStore/util/DBConnection.java`
- **Role:** the single place that knows how to reach MySQL. Static utility; private constructor.
- **Constants:** `URL = "jdbc:mysql://localhost:3306/sarisari_store?useSSL=false&serverTimezone=Asia/Manila"`, `USER = "root"`, `PASSWORD = ""` (comment: "Laragon default").
- **Method:** `public static Connection getConnection() throws SQLException` returns `DriverManager.getConnection(URL, USER, PASSWORD)`: a **new connection per call**, no pooling. There is no `Class.forName`; the MySQL jar registers its driver automatically.
- **Used by:** `InventoryService`, `SalesService`, `ReportService`, `FeatureTests`.

### 7.8 `SariSariStore.util.MoneyFormatter`  `[IMPLEMENTED]`

- **File:** `SariSariStore/util/MoneyFormatter.java`
- **Role:** turns a `double` into a Philippine-peso string. Static utility; private constructor; holds one shared `NumberFormat` built with `Locale.forLanguageTag("en-PH")`.
- **Method:** `public static String format(double amount)`. Example: `format(1234.5)` → `₱1,234.50`.
- **Used by:** only `FeatureTests` at the moment. Intended for the future GUI. The README marks it "[optional]".

### 7.9 `SariSariStore.gui.*`  `[STUBS, nothing implemented]`

- `gui/MainFrame.java`: `public class MainFrame { }`. It does not extend `JFrame` and has no members.
- `gui/ProductPanel.java`: 0-byte file (planned: screen for viewing and managing products).
- `gui/SalesDialog.java`: 0-byte file (planned: pop-up for recording a sale).
- No `javax.swing`, `java.awt` or `com.formdev.flatlaf` reference exists anywhere in the source. FlatLaf (`com.formdev.flatlaf.FlatLightLaf` / `FlatDarkLaf`) is present in `lib/` only.
- Intended wiring when built (from README and git history): `MainFrame` hosts `ProductPanel`; `SalesDialog` records sales; all of them call the services.

### 7.10 `tests.FeatureTests`  `[IMPLEMENTED]`

- **File:** `tests/FeatureTests.java` (package `tests`, `public final class`, private constructor).
- **Role:** framework-free test runner (no JUnit). A failed check throws `AssertionError`. Success ends with the line `RESULT: All feature tests passed.`
- **Depends on:** all model, service and util classes (see §4.2).
- **Requires:** a live MySQL matching `DBConnection`. It uses the **real** `sarisari_store` database and isolates its data with the product-name prefix `__feature_test__`.
- **Execution order in `main`:**
  1. `testProductModel()`, `testSaleItemModel()`, `testMoneyFormatter()` (no DB needed).
  2. `prepareDatabase()` (creates the three tables if missing; see §5.2) and `cleanupTestData()`.
  3. In a `try`: `testInventoryFeatures()`, `testSalesFeatures()`, `testReportFeatures()`. In `finally`: `cleanupTestData()`.
- **What each test verifies:**

| Test method | Verifies |
|---|---|
| `testProductModel` | constructor/getters/setters; negative buy price, sell price and quantity rejected |
| `testSaleItemModel` | getters; quantity 0 and -1 rejected |
| `testMoneyFormatter` | `format(1234.5)` ends with `1,234.50` (symbol not asserted) |
| `testInventoryFeatures` | add (id generated), find, update, restock (missing → `null`, negative → IAE), remove (`true` then `false`) |
| `testSalesFeatures` | sale total 30.0, 1 sale item, stock 10 → 8; over-stock → ISE and stock unchanged; unavailable → ISE; unknown id, `null`, empty list → IAE |
| `testReportFeatures` | low-stock, out-of-stock and inventory-summary membership |

- **Not covered by any test:** `getDailySalesReport`, `getSalesReport`, `Main`, the GUI.
- **Helpers:** `check`, `checkEquals`, `expectThrows` (with private functional interface `ThrowingRunnable`), `itemsFor`, `hasProduct`, `readSaleTotal`, `countSaleItems`, `printReport`, `cleanupTestData` (deletes `sale_items` → `sales` → `products` for prefixed products), `placeholders`, `setIds`.

---

## 8. Conventions in the code (follow these when adding code)

1. **SQL lives in the service classes** as `String sql = "..."` with `?` placeholders run through `PreparedStatement`. User values are never concatenated into SQL. There is no DAO or repository layer.
2. **Resources:** every `Connection`, `PreparedStatement` and `ResultSet` is opened in try-with-resources. Each service call opens its own connection via `DBConnection.getConnection()`.
3. **Errors:** `SQLException` → `throw new RuntimeException("Could not <verb> ...: " + e.getMessage(), e)`. Bad input → `IllegalArgumentException`. Business-rule violation in a sale (unavailable, not enough stock) → `IllegalStateException`.
4. **Transactions:** only `SalesService.recordSale` uses one (`setAutoCommit(false)`, `commit`, `rollback`, row locks with `SELECT ... FOR UPDATE`). Everything else is a single auto-committed statement.
5. **Services are stateless plain classes** created with `new` (`new InventoryService()`). No interfaces, singletons or dependency injection.
6. **Concrete collection types:** public methods take and return `ArrayList<...>`, not `List<...>`.
7. **Not found:** finders return `null` (`findProductByName`; `restockProduct` for an unknown name). `removeProduct` returns `false`. A sale referencing an unknown product id throws.
8. **Money** is `double` in Java and `DOUBLE` in MySQL. Display with `MoneyFormatter.format`.
9. **Validation lives in the model setters and in `SaleItem`'s constructor**, not in the services (the services only add the sale-specific checks).
10. **Naming:** Java camelCase fields ↔ MySQL snake_case columns. The exact spelling of some identifiers matters, see §9 issue 10.
11. **Tests:** add checks to `FeatureTests` using its helpers; name test products with the `__feature_test__` prefix and extend `cleanupTestData()` if a new table is written.
12. **Line endings are CRLF.** Keep them to avoid whole-file diffs.

---

## 9. Known issues, gaps and gotchas

### 9.1 Issues

| # | Severity | Where | Problem |
|---|---|---|---|
| 1 | **High** | `ReportService.getDailySalesReport`, `getSalesReport` (`ReportService.java:84-124`) | Both query the `sales` table but convert rows with the **product** `mapRow` (it reads `name`, `buy_price`, `sell_price`, `quantity`, `is_available`). A `sales` row has none of those columns, so any call that matches at least one sale should throw `SQLException: Column 'name' not found`, surfacing as `RuntimeException("Could not retrieve ... report")`; with zero matches it returns an empty list. The return type `ArrayList<Product>` cannot hold sale data (total, date, line items) anyway. The queries also need `sales.sale_date`, which is absent from the only DDL in the repo, so on a database created by `FeatureTests.prepareDatabase()` they fail with "Unknown column 'sale_date'". No test covers them. **(code-derived, not executed)** Fix direction: a sale/report result class, its own row mapper, and a guaranteed `sale_date` column. |
| 2 | Medium | `SariSariStore/gui/*` | GUI not implemented: `MainFrame` is an empty class, `ProductPanel.java` and `SalesDialog.java` are 0 bytes, FlatLaf is unused. |
| 3 | Medium | `Main.java` | It is a demo script with database side effects (inserts products, records a new sale on every run), but the README says it "starts the app and opens the main window". |
| 4 | Low | database | No schema script in the repo. Not needed on the author's machine (the tables already exist), and `DBConnection` holds connection settings only, not table definitions. It matters only when setting up a fresh database: the real DDL (notably `sales.sale_date`) is unknown, no foreign keys or unique constraints are declared, and `sarisari_store` must be created by hand. |
| 5 | Low | `Product` | Constructors skip validation (only setters validate), so invalid objects can be created. `setName` accepts null or empty. |
| 6 | Low | `InventoryService.findProductByName`, `restockProduct` | Name is used as a key but is not unique in the schema: `find` returns the first match, `restock` updates all rows with that name. |
| 7 | Low | `DBConnection` | URL, user (`root`) and empty password are hard-coded; a new connection is opened per call (no pooling). Fine for local development only. |
| 8 | Low | money handling | `double` / `DOUBLE` for prices and totals can accumulate rounding error; `BigDecimal` / `DECIMAL` is the usual remedy. |
| 9 | Low | duplication | `mapRow` is copy-pasted in `InventoryService` and `ReportService`. |
| 10 | Low | naming | Exact spellings: parameter `treshold`; method `getOutofStockProducts` (lowercase "of"); message "Quantity must be atleast 1". Java is case-sensitive, so use these exactly when calling. |
| 11 | Info | `InventoryService.removeProduct` | Deletes only the `products` row. Related `sale_items` rows are neither checked nor removed (dangling, or the delete fails if the real database has a foreign key). **(code-derived)** |

### 9.2 Not built yet / missing

- The GUI: `MainFrame` (window), `ProductPanel` (view and manage products), `SalesDialog` (record a sale). Evidence: README and the last commit message "gui to be added".
- `ReportService.getBestSellingProducts(startDate, endDate)`: only a commented-out idea.
- A working sales report (issue 1).
- A refactor of `SalesService` (commit `e2b3cc1`: "[still to be refactored]").
- No build file, no external configuration for DB credentials, no unit-test framework, and no schema script (optional: the author's database already exists).

### 9.3 README vs. reality

| README says | Actual code |
|---|---|
| `Main.java`: "Starts the app and opens the main window." | Console demo; no GUI. |
| Structure lists `model/Product.java` only | `model/SaleItem.java` also exists (required by `SalesService`). |
| Structure lists `util/MoneyFormatter.java [optional]` only | `util/DBConnection.java` also exists and is required by every service. |
| `ReportService`: "reports like daily sales and low-stock alerts" | Low-stock works; the daily/range sales reports are broken (issue 1). |
| `gui/` files listed as part of the structure | Stub / empty files. |
| "ADDED DATA BASE!!!! … MYSQL" | True; the tables live in the author's local MySQL rather than in the repo. |

The README's test instructions are accurate (see §10).

---

## 10. Build, run and test

**Prerequisites:** JDK 8+ (verified on 21); MySQL on `localhost:3306` with an existing database `sarisari_store`, user `root`, empty password (or edit `DBConnection`); run commands from the repo root so `lib/*` resolves.

Compile (bash/macOS/Linux; **verified**):

```bash
javac -cp "lib/*" -d out $(find SariSariStore tests -name '*.java')
```

Run (classpath separator is `:` on Linux/macOS and `;` on Windows; **not executed here**, needs MySQL):

```bash
java -cp "out:lib/*" tests.FeatureTests      # tests (creates missing tables itself)
java -cp "out:lib/*" SariSariStore.Main      # demo (tables must already exist)
```

Windows PowerShell (from the README):

```powershell
$out = Join-Path $env:TEMP "sarisari-tests"
New-Item -ItemType Directory -Force $out
javac -cp "lib/*" -d $out (Get-ChildItem -Recurse -Filter *.java SariSariStore, tests | Select-Object -ExpandProperty FullName)
java -cp "$out;lib/*" tests.FeatureTests
```

Notes:

- `.gitignore` ignores `out/` and `*.class`, so `out/` is the conventional build folder.
- `FeatureTests` is not hermetic: it runs against the real `sarisari_store` database and relies on the `__feature_test__` name prefix plus `cleanupTestData()` to leave no trace. It never drops tables.
- `Main` leaves permanent data behind (§7.1). Do not run it against data you care about.

---

## 11. Usage example and change guidance

Example using the public API (compiles against the current code):

```java
InventoryService inventory = new InventoryService();
SalesService sales = new SalesService();
ReportService reports = new ReportService();

Product p = new Product("Lucky Me Pancit Canton", 11.50, 15.00, 50, true);
inventory.addProduct(p);                       // p.getId() is now set

ArrayList<SaleItem> cart = new ArrayList<>();
cart.add(new SaleItem(p.getId(), 3));
int receiptId = sales.recordSale(cart);        // transactional; decrements stock

Product restocked = inventory.restockProduct("Lucky Me Pancit Canton", 20); // null if name not found
ArrayList<Product> low = reports.getLowStockProducts(10);                   // quantity < 10
String price = MoneyFormatter.format(45.0);                                 // "₱45.00"
```

Where to make common changes:

| Task | Where and how |
|---|---|
| Add a database feature | Add a method to the matching service following §8 (PreparedStatement, try-with-resources, wrap `SQLException`). Add tests to `FeatureTests` and extend `cleanupTestData()` if it writes new data. |
| Build the GUI | Create the Swing classes in `SariSariStore/gui/`. Call only the services (never JDBC directly). Format prices with `MoneyFormatter.format`. FlatLaf is already in `lib/` (`com.formdev.flatlaf.FlatLightLaf`). Service calls are blocking JDBC calls. Replace or supplement the demo `Main` with a real launcher. |
| Fix the sales reports | Add a result type for sales (the current `ArrayList<Product>` cannot carry sale data), write a separate row mapper, and make sure `sales.sale_date` exists (§5.2, §9 issue 1). Add a test. |
| Implement "best-selling products" | Would join `sale_items` with `products` and group by product within a date range (idea left as a comment in `ReportService`). |
| Change DB settings | Constants in `util/DBConnection.java`. |

---

## 12. Glossary

- **Sari-sari store:** small neighborhood retail store in the Philippines.
- **`ni` in `sarisari_store_ni_ralph`:** Tagalog for "of / belonging to", so the folder name means "Ralph's sari-sari store".
- **`wag na to kahirap next time nalang`** (comment in `ReportService.java`): "skip this, it's too hard, maybe next time".
- **Stock / quantity:** units on hand (`products.quantity`).
- **Available:** the `is_available` flag. An unavailable product cannot be sold even if its quantity is above 0.
- **Low stock / out of stock:** `quantity < threshold` / `quantity = 0`.
- **Buy price / sell price:** cost per unit / retail price per unit.
- **Receipt / sale id:** `sales.id`, returned by `SalesService.recordSale`.
- **Peso (₱, PHP):** Philippine currency.
- **Laragon:** Windows local development stack that bundles MySQL (default user `root`, empty password).
- **FlatLaf:** a modern look-and-feel library for Java Swing.
