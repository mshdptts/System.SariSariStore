package SariSariStore.gui;

import SariSariStore.model.Product;
import SariSariStore.model.SaleItem;
import SariSariStore.service.InventoryService;
import SariSariStore.service.SalesService;
import SariSariStore.util.MoneyFormatter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class SalesPanel extends JPanel {
    private final InventoryService inventory;
    private final SalesService sales;
    private final JTextField search = new JTextField();
    private final JPanel productGrid = new JPanel(new GridLayout(0, 3, 12, 12));
    private final DefaultListModel<String> cartModel = new DefaultListModel<>();
    private final JList<String> cartList = new JList<>(cartModel);
    private final JLabel total = new JLabel();
    private final Map<Integer, Integer> cart = new LinkedHashMap<>();
    private ArrayList<Product> products = new ArrayList<>();

    public SalesPanel(InventoryService inventory, SalesService sales, Runnable onChange) {
        this.inventory = inventory;
        this.sales = sales;
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.PAGE);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));
        build(onChange);
        search.getDocument().addDocumentListener((DocumentListener) e -> renderProducts());
        refresh();
    }

    private void build(Runnable onChange) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(Theme.title("Sales"));
        heading.add(Theme.muted("Process a customer purchase"));
        header.add(heading, BorderLayout.WEST);
        search.putClientProperty("JTextField.placeholderText", "Search product...");
        search.setPreferredSize(new Dimension(240, 36));
        header.add(search, BorderLayout.EAST);

        productGrid.setOpaque(false);
        JScrollPane productScroll = new JScrollPane(productGrid);
        productScroll.setBorder(BorderFactory.createEmptyBorder());
        JPanel productsCard = Theme.card(new BorderLayout(8, 8));
        productsCard.add(new JLabel("Products"), BorderLayout.NORTH);
        productsCard.add(productScroll, BorderLayout.CENTER);

        JPanel cartCard = Theme.card(new BorderLayout(8, 8));
        cartCard.add(new JLabel("Current Sale"), BorderLayout.NORTH);
        cartCard.add(new JScrollPane(cartList), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.setOpaque(false);
        total.setFont(total.getFont().deriveFont(Font.BOLD, 20f));
        footer.add(total, BorderLayout.WEST);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton clear = Theme.secondary("Clear Cart");
        clear.addActionListener(e -> clearCart());
        JButton charge = Theme.primary("Charge (F9)");
        charge.addActionListener(e -> completeSale(onChange));
        buttons.add(clear);
        buttons.add(charge);
        footer.add(buttons, BorderLayout.EAST);
        cartCard.add(footer, BorderLayout.SOUTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 16, 16));
        center.setOpaque(false);
        center.add(productsCard);
        center.add(cartCard);
        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private void renderProducts() {
        String query = search.getText().trim().toLowerCase();
        productGrid.removeAll();
        for (Product product : products) {
            if (!query.isEmpty() && !product.getName().toLowerCase().contains(query)) continue;
            JButton card = new JButton("<html><b>" + product.getName() + "</b><br>"
                    + MoneyFormatter.format(product.getSellPrice()) + " | "
                    + product.getQuantity() + " pcs</html>");
            card.setHorizontalAlignment(SwingConstants.LEFT);
            card.setPreferredSize(new Dimension(150, 72));
            card.setEnabled(product.isAvailable() && product.getQuantity() > 0);
            card.addActionListener(e -> addToCart(product));
            productGrid.add(card);
        }
        productGrid.revalidate();
        productGrid.repaint();
    }

    private void addToCart(Product product) {
        int quantity = cart.getOrDefault(product.getId(), 0);
        if (quantity < product.getQuantity()) {
            cart.put(product.getId(), quantity + 1);
            renderCart();
        }
    }

    private void renderCart() {
        cartModel.clear();
        double amount = 0;
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Product product = findProduct(entry.getKey());
            if (product == null) continue;
            int quantity = entry.getValue();
            double line = product.getSellPrice() * quantity;
            amount += line;
            cartModel.addElement(product.getName() + "  x" + quantity + "  "
                    + MoneyFormatter.format(line));
        }
        total.setText("Total  " + MoneyFormatter.format(amount));
    }

    private Product findProduct(int id) {
        return products.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    private void completeSale(Runnable onChange) {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add at least one product to the sale.",
                    "Empty Sale", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ArrayList<SaleItem> items = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            items.add(new SaleItem(entry.getKey(), entry.getValue()));
        }
        try {
            int saleId = sales.recordSale(items);
            JOptionPane.showMessageDialog(this, "Sale #" + saleId + " recorded.");
            clearCart();
            onChange.run();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Sale Not Recorded",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearCart() {
        cart.clear();
        renderCart();
    }

    public void refresh() {
        products = inventory.getProducts();
        renderProducts();
        renderCart();
    }

    @FunctionalInterface
    private interface DocumentListener extends javax.swing.event.DocumentListener {
        void update(javax.swing.event.DocumentEvent event);
        default void insertUpdate(javax.swing.event.DocumentEvent event) { update(event); }
        default void removeUpdate(javax.swing.event.DocumentEvent event) { update(event); }
        default void changedUpdate(javax.swing.event.DocumentEvent event) { update(event); }
    }
}
