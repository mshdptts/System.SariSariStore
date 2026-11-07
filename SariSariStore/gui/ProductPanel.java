package SariSariStore.gui;

import SariSariStore.model.Product;
import SariSariStore.service.InventoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class ProductPanel extends JPanel {
    private final InventoryService inventory;
    private final Runnable onChange;
    private final JTextField search = new JTextField();
    private final JComboBox<String> status = new JComboBox<>(
            new String[]{"All", "In stock", "Low", "Out of stock"});
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Price", "Stock", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JLabel showing = Theme.muted("");
    private final JLabel formTitle = new JLabel("Add Product");
    private final JTextField name = new JTextField();
    private final JTextField price = new JTextField();
    private final JTextField quantity = new JTextField();
    private Product selected;
    private ArrayList<Product> allProducts = new ArrayList<>();

    public ProductPanel(InventoryService inventory, Runnable onChange) {
        this.inventory = inventory;
        this.onChange = onChange;
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.PAGE);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));
        build();
        search.getDocument().addDocumentListener((SimpleDocumentListener) e -> filter());
        status.addActionListener(e -> filter());
        table.getSelectionModel().addListSelectionListener(e -> selectRow());
        refreshProducts();
    }

    private void build() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(Theme.title("Products"));
        title.add(Theme.muted("Manage your inventory"));
        header.add(title, BorderLayout.WEST);
        JButton restock = Theme.secondary("Restock");
        restock.addActionListener(e -> restock());
        JButton add = Theme.primary("Add Product");
        add.addActionListener(e -> clearForm());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(restock);
        actions.add(add);
        header.add(actions, BorderLayout.EAST);

        search.putClientProperty("JTextField.placeholderText", "Search by name");
        search.setPreferredSize(new Dimension(220, 34));
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filters.setOpaque(false);
        filters.add(search);
        filters.add(status);

        table.setRowHeight(36);
        table.setAutoCreateRowSorter(true);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        JPanel left = Theme.card(new BorderLayout(8, 8));
        left.add(filters, BorderLayout.NORTH);
        left.add(tableScroll, BorderLayout.CENTER);
        left.add(showing, BorderLayout.SOUTH);

        JPanel form = Theme.card(new GridBagLayout());
        formTitle.setFont(formTitle.getFont().deriveFont(Font.BOLD, 16f));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        form.add(formTitle, c);
        addFormRow(form, c, 1, "Name", name);
        addFormRow(form, c, 2, "Price", price);
        addFormRow(form, c, 3, "Quantity", quantity);
        JButton save = Theme.primary("Save");
        save.addActionListener(e -> save());
        JButton delete = Theme.secondary("Delete");
        delete.addActionListener(e -> delete());
        JButton clear = Theme.secondary("Clear");
        clear.addActionListener(e -> clearForm());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        buttons.add(clear);
        buttons.add(delete);
        buttons.add(save);
        c.gridx = 0;
        c.gridy = 4;
        form.add(buttons, c);

        JPanel center = new JPanel(new GridLayout(1, 2, 16, 16));
        center.setOpaque(false);
        center.add(left);
        center.add(form);
        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private void addFormRow(JPanel panel, GridBagConstraints c, int row,
                            String label, JComponent field) {
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = row;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        panel.add(field, c);
    }

    private void filter() {
        String query = search.getText().trim().toLowerCase();
        String selectedStatus = (String) status.getSelectedItem();
        model.setRowCount(0);
        int shown = 0;
        for (Product product : allProducts) {
            String productStatus = product.getQuantity() == 0 ? "Out of stock"
                    : product.getQuantity() < 5 ? "Low" : "In stock";
            if ((!query.isEmpty() && !product.getName().toLowerCase().contains(query))
                    || (!"All".equals(selectedStatus) && !selectedStatus.equals(productStatus))) {
                continue;
            }
            model.addRow(new Object[]{product.getId(), product.getName(),
                    String.format("%.2f", product.getSellPrice()), product.getQuantity(),
                    productStatus});
            shown++;
        }
        showing.setText("Showing " + shown + " of " + allProducts.size() + " products");
    }

    private void selectRow() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);
        int id = (Integer) model.getValueAt(modelRow, 0);
        selected = allProducts.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
        if (selected != null) {
            formTitle.setText("Edit Product");
            name.setText(selected.getName());
            price.setText(String.valueOf(selected.getSellPrice()));
            quantity.setText(String.valueOf(selected.getQuantity()));
        }
    }

    private void save() {
        try {
            String productName = name.getText().trim();
            double productPrice = Double.parseDouble(price.getText().trim());
            int productQuantity = Integer.parseInt(quantity.getText().trim());
            if (productName.isEmpty() || productPrice < 0 || productQuantity < 0) {
                throw new IllegalArgumentException("Enter a name and non-negative values.");
            }
            if (selected == null) {
                inventory.addProduct(new Product(productName, productPrice, productPrice,
                        productQuantity, true));
            } else {
                selected.setName(productName);
                selected.setBuyPrice(productPrice);
                selected.setSellPrice(productPrice);
                selected.setQuantity(productQuantity);
                inventory.updateProduct(selected);
            }
            clearForm();
            onChange.run();
        } catch (NumberFormatException ex) {
            showError(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void delete() {
        if (selected == null) return;
        if (JOptionPane.showConfirmDialog(this, "Delete selected product?", "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            inventory.removeProduct(selected);
            clearForm();
            onChange.run();
        }
    }

    private void restock() {
        if (allProducts.isEmpty()) return;
        Product product = (Product) JOptionPane.showInputDialog(this, "Select product",
                "Restock", JOptionPane.PLAIN_MESSAGE, null, allProducts.toArray(), allProducts.get(0));
        if (product == null) return;
        String value = JOptionPane.showInputDialog(this, "Quantity to add:", "Restock",
                JOptionPane.PLAIN_MESSAGE);
        if (value == null) return;
        try {
            inventory.restockProduct(product.getName(), Integer.parseInt(value.trim()));
            onChange.run();
        } catch (NumberFormatException ex) {
            showError(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid product",
                JOptionPane.ERROR_MESSAGE);
    }

    public void refreshProducts() {
        allProducts = inventory.getProducts();
        filter();
    }

    private void clearForm() {
        selected = null;
        formTitle.setText("Add Product");
        name.setText("");
        price.setText("");
        quantity.setText("");
        table.clearSelection();
    }

    @FunctionalInterface
    private interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update(javax.swing.event.DocumentEvent event);
        default void insertUpdate(javax.swing.event.DocumentEvent event) { update(event); }
        default void removeUpdate(javax.swing.event.DocumentEvent event) { update(event); }
        default void changedUpdate(javax.swing.event.DocumentEvent event) { update(event); }
    }
}