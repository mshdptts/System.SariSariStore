package SariSariStore.gui;

import SariSariStore.model.Product;
import SariSariStore.service.InventoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
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
    private final JLabel message = Theme.muted("");
    private final JLabel formTitle = new JLabel("Add Product");
    private final JTextField name = new JTextField();
    private final JTextField price = new JTextField();
    private final JTextField quantity = new JTextField();
    private Product selected;
    private ArrayList<Product> allProducts = new ArrayList<>();
    private boolean easterEggShown;

    public ProductPanel(InventoryService inventory, Runnable onChange) {
        this.inventory = inventory;
        this.onChange = onChange;
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.PAGE);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));
        build();
        search.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            filter();
            checkEasterEgg();
        });
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
        table.setDefaultRenderer(Object.class, new ProductCellRenderer());
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        JPanel left = Theme.card(new BorderLayout(8, 8));
        left.add(filters, BorderLayout.NORTH);
        left.add(tableScroll, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(showing, BorderLayout.WEST);
        footer.add(message, BorderLayout.EAST);
        left.add(footer, BorderLayout.SOUTH);

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

    private void checkEasterEgg() {
        boolean jerryTyped = "jerry".equalsIgnoreCase(search.getText().trim());
        if (!jerryTyped) {
            easterEggShown = false;
        } else if (!easterEggShown) {
            easterEggShown = true;
            SwingUtilities.invokeLater(this::showJerryPhoto);
        }
    }

    private void showJerryPhoto() {
        File imageFile = new File("SariSariStore" + File.separator + "img"
                + File.separator + "jerryface.jpg");
        if (!imageFile.isFile()) {
            imageFile = new File("img" + File.separator + "jerryface.jpg");
        }
        if (!imageFile.isFile()) {
            JOptionPane.showMessageDialog(this,
                    "The Jerry photo could not be found.",
                    "Easter Egg Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ImageIcon source = new ImageIcon(imageFile.getAbsolutePath());
        Image image = source.getImage();
        int maxSize = 520;
        double scale = Math.min(1.0, Math.min(
                (double) maxSize / source.getIconWidth(),
                (double) maxSize / source.getIconHeight()));
        ImageIcon icon = scale < 1.0
                ? new ImageIcon(image.getScaledInstance(
                        (int) (source.getIconWidth() * scale),
                        (int) (source.getIconHeight() * scale),
                        Image.SCALE_SMOOTH))
                : source;
        JLabel photo = new JLabel(icon);
        photo.setHorizontalAlignment(SwingConstants.CENTER);
        photo.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this), "Jerry!", Dialog.ModalityType.MODELESS);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(8, 8));
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 12));
        JLabel title = new JLabel("You found Jerry!");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        header.add(title, BorderLayout.WEST);
        JButton close = Theme.secondary("Close");
        close.setForeground(Color.WHITE);
        close.setOpaque(false);
        close.addActionListener(e -> dialog.dispose());
        header.add(close, BorderLayout.EAST);

        JLabel caption = Theme.muted("A special surprise from the product search.");
        caption.setBorder(BorderFactory.createEmptyBorder(8, 16, 0, 16));
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(caption, BorderLayout.NORTH);
        content.add(photo, BorderLayout.CENTER);
        dialog.add(header, BorderLayout.NORTH);
        dialog.add(content, BorderLayout.CENTER);
        dialog.getRootPane().setDefaultButton(close);
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ESCAPE"), "closeJerry");
        dialog.getRootPane().getActionMap().put("closeJerry",
                new AbstractAction() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent event) {
                        dialog.dispose();
                    }
                });
        dialog.pack();
        dialog.setMinimumSize(new Dimension(340, 260));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private static final class ProductCellRenderer extends
            javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                        boolean selected, boolean focused,
                                                        int row, int column) {
            Component cell = super.getTableCellRendererComponent(
                    table, value, selected, focused, row, column);
            if (!selected) {
                Object status = table.getValueAt(row, 4);
                if ("Out of stock".equals(status)) {
                    cell.setBackground(Theme.DANGER);
                    cell.setForeground(Color.WHITE);
                } else if ("Low".equals(status)) {
                    cell.setBackground(Theme.LOW_STOCK);
                    cell.setForeground(Theme.TEXT);
                } else {
                    cell.setBackground(Color.WHITE);
                    cell.setForeground(Theme.TEXT);
                }
            }
            return cell;
        }
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
            showMessage("Product saved.", Theme.MUTED);
            onChange.run();
        } catch (NumberFormatException ex) {
            showError("Price and quantity must be valid numbers.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void delete() {
        if (selected == null) return;
        if (JOptionPane.showConfirmDialog(this,
                "Delete selected product and its sale items?", "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                if (!inventory.removeProduct(selected)) {
                    showError("The selected product was not found.");
                    return;
                }
                clearForm();
                refreshProducts();
                showMessage("Product deleted.", Theme.MUTED);
                onChange.run();
            } catch (RuntimeException ex) {
                showError("Could not delete product: " + ex.getMessage());
            }
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
            int quantity = Integer.parseInt(value.trim());
            if (quantity <= 0) {
                showError("Restock quantity must be greater than zero.");
                return;
            }
            Product restocked = inventory.restockProduct(product.getName(), quantity);
            if (restocked == null) {
                showError("The selected product no longer exists.");
                return;
            }
            showMessage(product.getName() + " restocked.", Theme.MUTED);
            onChange.run();
        } catch (NumberFormatException ex) {
            showError("Restock quantity must be a whole number.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (RuntimeException ex) {
            showError("Could not restock product: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        showMessage(message, Theme.DANGER);
    }

    private void showMessage(String text, Color color) {
        message.setForeground(color);
        message.setText(text);
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
        message.setText("");
    }

    @FunctionalInterface
    private interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update(javax.swing.event.DocumentEvent event);
        default void insertUpdate(javax.swing.event.DocumentEvent event) { update(event); }
        default void removeUpdate(javax.swing.event.DocumentEvent event) { update(event); }
        default void changedUpdate(javax.swing.event.DocumentEvent event) { update(event); }
    }
}