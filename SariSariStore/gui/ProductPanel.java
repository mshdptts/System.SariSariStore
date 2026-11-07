package SariSariStore.gui;

import SariSariStore.model.Product;
import SariSariStore.service.InventoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class ProductPanel extends JPanel {

    // Instance variables
    private InventoryService inventoryService;
    private DefaultTableModel tableModel;
    private JTable productTable;
    private JTextField txtSearch;
    private JTextField txtName;
    private JTextField txtPrice;
    private JTextField txtQuantity;
    private JButton btnSearch;
    private JButton btnAdd;
    private JButton btnDelete;

    // Constructor
    public ProductPanel(InventoryService inventoryService) {
        this.inventoryService = inventoryService;

        // Set default panel properties
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Initialize GUI sections using helper methods
        initNorthPanel();
        initCenterTable();
        initSouthPanel();

        // Load initial data
        refreshTable(this.inventoryService != null ? this.inventoryService.getProducts() : new ArrayList<>());
    }

    // Helper method for Search Section
    private void initNorthPanel() {
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(15);
        btnSearch = new JButton("Search");

        btnSearch.addActionListener((ActionEvent e) -> {
            searchProduct();
        });

        northPanel.add(new JLabel("Search Name:"));
        northPanel.add(txtSearch);
        northPanel.add(btnSearch);
        add(northPanel, BorderLayout.NORTH);
    }

    // Helper method for Table Display Section
    private void initCenterTable() {
        String[] columns = {"Name", "Price (₱)", "Quantity"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable = new JTable(tableModel);
        productTable.getSelectionModel().addListSelectionListener(e -> fillFormFromSelectedRow());
        add(new JScrollPane(productTable), BorderLayout.CENTER);
    }

    // Helper method for Input Form and Action Buttons
    private void initSouthPanel() {
        JPanel southContainer = new JPanel(new GridLayout(2, 1, 4, 4));

        // Form inputs
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        txtName = new JTextField(8);
        txtPrice = new JTextField(5);
        txtQuantity = new JTextField(5);

        formPanel.add(new JLabel("Name:"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("Price:"));
        formPanel.add(txtPrice);
        formPanel.add(new JLabel("Qty:"));
        formPanel.add(txtQuantity);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnAdd = new JButton("Add Product");
        btnDelete = new JButton("Delete");

        btnAdd.addActionListener((ActionEvent e) -> {
            addProduct();
        });

        btnDelete.addActionListener((ActionEvent e) -> {
            deleteSelectedProduct();
        });

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnDelete);

        southContainer.add(formPanel);
        southContainer.add(buttonPanel);
        add(southContainer, BorderLayout.SOUTH);
    }

    // Action: Refresh table rows
    private void refreshTable(ArrayList<Product> products) {
        tableModel.setRowCount(0);
        if (products != null) {
            for (Product p : products) {
                Object[] rowData = {p.getName(), String.format("%.2f", p.getSellPrice()), p.getQuantity()};
                tableModel.addRow(rowData);
            }
        }
    }

    // Action: Search Product by Name
    private void searchProduct() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            refreshTable(inventoryService.getProducts());
        } else {
            Product found = inventoryService.findProductByName(query);
            ArrayList<Product> searchResult = new ArrayList<>();
            if (found != null) {
                searchResult.add(found);
            }
            refreshTable(searchResult);
        }
    }

    // Action: Add new product
    private void addProduct() {
        try {
            String name = txtName.getText().trim();
            double price = Double.parseDouble(txtPrice.getText().trim());
            int quantity = Integer.parseInt(txtQuantity.getText().trim());

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Product name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (price < 0 || quantity < 0) {
                JOptionPane.showMessageDialog(this, "Price and quantity cannot be negative.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Product newProduct = new Product(name, price, price, quantity, true);
            inventoryService.addProduct(newProduct);

            refreshTable(inventoryService.getProducts());
            clearInputs();
            JOptionPane.showMessageDialog(this, "Product added successfully!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for price and quantity.", "Invalid Entry", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Action: Delete selected product
    private void deleteSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow != -1) {
            int confirm = JOptionPane.showConfirmDialog(this, "Delete selected product?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String name = (String) tableModel.getValueAt(selectedRow, 0);
                Product target = inventoryService.findProductByName(name);
                if (target != null) {
                    inventoryService.removeProduct(target);
                    refreshTable(inventoryService.getProducts());
                    clearInputs();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a product from the table first.", "Selection Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Selection listener helper
    private void fillFormFromSelectedRow() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow != -1) {
            txtName.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtPrice.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtQuantity.setText(tableModel.getValueAt(selectedRow, 2).toString());
        }
    }

    // Clear text field inputs
    private void clearInputs() {
        txtName.setText("");
        txtPrice.setText("");
        txtQuantity.setText("");
        productTable.clearSelection();
    }
}
