package SariSariStore.gui;

import SariSariStore.model.Product;
import SariSariStore.service.InventoryService;
import SariSariStore.service.ReportService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DashboardPanel extends JPanel {
    private final InventoryService inventory;
    private final ReportService reports;
    private final JLabel productCount = new JLabel();
    private final JLabel stockUnits = new JLabel();
    private final JLabel lowCount = new JLabel();
    private final JLabel outCount = new JLabel();
    private final JLabel alerts = new JLabel();
    private final Runnable openProducts;
    private final Runnable openSales;
    private final Runnable openReports;

    public DashboardPanel(InventoryService inventory, ReportService reports,
                          Runnable openProducts, Runnable openSales, Runnable openReports) {
        this.inventory = inventory;
        this.reports = reports;
        this.openProducts = openProducts;
        this.openSales = openSales;
        this.openReports = openReports;
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.PAGE);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 16, 24));
        build();
    }

    private void build() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel title = new JPanel();
        title.setOpaque(false);
        title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
        title.add(Theme.title("Dashboard"));
        title.add(Theme.muted("Overview of your store's current status"));
        header.add(title, BorderLayout.WEST);
        header.add(Theme.muted("Store Admin"), BorderLayout.EAST);

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 16));
        stats.setOpaque(false);
        stats.add(statCard("Total Products", productCount));
        stats.add(statCard("Stock Units", stockUnits));
        stats.add(statCard("Low Stock", lowCount));
        stats.add(statCard("Out of Stock", outCount));

        JPanel alertCard = Theme.card(new BorderLayout(8, 8));
        JLabel alertTitle = new JLabel("Inventory Alerts");
        alertTitle.setFont(alertTitle.getFont().deriveFont(Font.BOLD, 16f));
        alertCard.add(alertTitle, BorderLayout.NORTH);
        alertCard.add(alerts, BorderLayout.CENTER);

        JPanel actionCard = Theme.card(new BorderLayout(8, 8));
        JLabel actionTitle = new JLabel("Quick Actions");
        actionTitle.setFont(actionTitle.getFont().deriveFont(Font.BOLD, 16f));
        actionCard.add(actionTitle, BorderLayout.NORTH);
        JPanel actions = new JPanel(new GridLayout(2, 2, 8, 8));
        actions.setOpaque(false);
        JButton sale = Theme.primary("New Sale");
        sale.addActionListener(e -> openSales.run());
        JButton manage = Theme.primary("Manage Products");
        manage.addActionListener(e -> openProducts.run());
        JButton report = Theme.secondary("View Reports");
        report.addActionListener(e -> openReports.run());
        JButton refresh = Theme.secondary("Refresh");
        refresh.addActionListener(e -> refresh());
        actions.add(sale);
        actions.add(manage);
        actions.add(report);
        actions.add(refresh);
        actionCard.add(actions, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 2, 16, 16));
        bottom.setOpaque(false);
        bottom.add(alertCard);
        bottom.add(actionCard);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(stats);
        content.add(Box.createVerticalStrut(16));
        content.add(bottom);
        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    private JPanel statCard(String title, JLabel value) {
        JPanel card = Theme.card(new BorderLayout(4, 8));
        card.add(Theme.muted(title), BorderLayout.NORTH);
        value.setFont(value.getFont().deriveFont(Font.BOLD, 26f));
        value.setForeground(Theme.TEXT);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    public void refresh() {
        productCount.setText(String.valueOf(inventory.getTotalProductCount()));
        stockUnits.setText(String.valueOf(inventory.getTotalUnitsInStock()));
        ArrayList<Product> low = reports.getLowStockProducts(5);
        ArrayList<Product> out = reports.getOutofStockProducts();
        lowCount.setText(String.valueOf(low.size()));
        outCount.setText(String.valueOf(out.size()));
        StringBuilder html = new StringBuilder("<html>");
        for (Product product : out) {
            html.append("<font color='#CF3F3F'><b>Out of stock</b></font> ")
                    .append(product.getName()).append(" &nbsp; 0 pcs<br>");
        }
        for (Product product : low) {
            if (product.getQuantity() > 0) {
                html.append("<font color='#B88600'><b>Low stock</b></font> ")
                        .append(product.getName()).append(" &nbsp; ")
                        .append(product.getQuantity()).append(" pcs<br>");
            }
        }
        if (out.isEmpty() && low.stream().noneMatch(p -> p.getQuantity() > 0)) {
            html.append("No inventory alerts");
        }
        alerts.setText(html.append("</html>").toString());
    }
}
