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
    └── MoneyFormatter.java
```

## Run locally

From the repository root, with JDK 11 or newer:

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java)
java -cp out SariSariStore.Main
```

## Put the project on GitHub

1. Create a new empty repository on GitHub.
2. From this project folder, run:

```powershell
git add .
git commit -m "Organize Sari-Sari Store application"
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
git push -u origin main
```

Replace `YOUR_USERNAME` and `YOUR_REPOSITORY` with your GitHub details. If your
branch is not named `main`, use the branch name shown by `git branch --show-current`.
