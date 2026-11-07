package SariSariStore.gui;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;

public final class Theme {
    public static final Color HEADER = new Color(20, 72, 59);
    public static final Color NAV = new Color(29, 86, 70);
    public static final Color PAGE = new Color(244, 247, 245);
    public static final Color PRIMARY = new Color(0, 164, 73);
    public static final Color TEXT = new Color(36, 67, 58);
    public static final Color MUTED = new Color(105, 121, 115);
    public static final Color WARNING = new Color(225, 133, 34);
    public static final Color DANGER = new Color(207, 63, 63);

    private Theme() {
    }

    public static void install() {
        FlatLightLaf.setup();
        UIManager.put("Component.arc", 12);
        UIManager.put("Button.arc", 18);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Table.rowHeight", 36);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.selectionBackground", new Color(227, 243, 234));
        UIManager.put("Table.selectionForeground", TEXT);
    }

    public static JLabel title(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 22f));
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        return label;
    }

    public static JPanel card(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(16, new Color(232, 237, 234)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        return panel;
    }

    public static JButton primary(String text) {
        JButton button = new JButton(text);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        return button;
    }

    public static JButton secondary(String text) {
        JButton button = new JButton(text);
        button.putClientProperty("JButton.buttonType", "roundRect");
        return button;
    }

    private static final class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        private RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public Insets getBorderInsets(Component component) {
            return new Insets(1, 1, 1, 1);
        }

        @Override
        public void paintBorder(Component component, Graphics graphics, int x, int y,
                                int width, int height) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setColor(color);
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g.dispose();
        }
    }
}
