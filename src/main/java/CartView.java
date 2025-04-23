import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.net.URL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Rectangle2D;

public class CartView extends JPanel {
    private static final String DB_URL = "jdbc:sqlite:products.db";
    private static final double SHIPPING_COST = 4.99;
    private static final double TAX_RATE = 0.10; // 10% tax
    private JPanel cartItemsPanel;
    private JLabel subtotalLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;
    private JLabel cartItemsLabel;
    private List<CartItem> cartItems;

    public CartView() {
        cartItems = new ArrayList<>();
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        createGUI();
        loadCartItems();
    }

    private void createGUI() {
        // Main content panel with padding
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(245, 247, 250));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Title
        JLabel titleLabel = new JLabel("Your Cart");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(30, 41, 59));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Main panel with cart items and order summary
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();

        // Cart items section
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        cartItemsLabel = new JLabel("Cart Items (0)");
        cartItemsLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        cartItemsLabel.setForeground(new Color(30, 41, 59));
        leftPanel.add(cartItemsLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Scrollable cart items panel
        cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(cartItemsPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Color.WHITE);
        leftPanel.add(scrollPane);

        // Order summary section
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel summaryLabel = new JLabel("Order Summary");
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        summaryLabel.setForeground(new Color(30, 41, 59));
        rightPanel.add(summaryLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Summary details
        subtotalLabel = new JLabel("Subtotal (0 items) $0.00");
        JLabel shippingLabel = new JLabel("Shipping $" + String.format("%.2f", SHIPPING_COST));
        taxLabel = new JLabel("Tax $0.00");
        totalLabel = new JLabel("Total $0.00");

        for (JLabel label : new JLabel[]{subtotalLabel, shippingLabel, taxLabel, totalLabel}) {
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setForeground(new Color(30, 41, 59));
            rightPanel.add(label);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        // Checkout button
        JButton checkoutButton = new JButton("Proceed to Checkout") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(30, 41, 138));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(45, 56, 153));
                } else {
                    g2d.setColor(new Color(29, 41, 113));
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Draw the text with adjusted position for icon
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                
                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), x, y);
                g2d.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                // No border painting needed
            }

            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(getFont());
                int width = fm.stringWidth(getText()) + getInsets().left + getInsets().right + 40; // Add padding
                int height = Math.max(fm.getHeight(), 16) + getInsets().top + getInsets().bottom + 20; // Add vertical padding
                return new Dimension(width, height);
            }
        };
        checkoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        checkoutButton.setFocusPainted(false);
        checkoutButton.setContentAreaFilled(false);
        checkoutButton.setOpaque(false);
        checkoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        checkoutButton.addActionListener(e -> handleCheckout());
        rightPanel.add(checkoutButton);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add bottom padding

        // Add panels to main panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 20);
        mainPanel.add(leftPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.3;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(rightPanel, gbc);

        contentPanel.add(mainPanel);

        // Continue Shopping button
        JButton continueShoppingButton = new JButton("← Continue Shopping");
        continueShoppingButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        continueShoppingButton.setForeground(new Color(29, 41, 113));
        continueShoppingButton.setBorderPainted(false);
        continueShoppingButton.setContentAreaFilled(false);
        continueShoppingButton.setFocusPainted(false);
        continueShoppingButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        continueShoppingButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        continueShoppingButton.addActionListener(e -> Main.showScreen("products"));
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(continueShoppingButton);

        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
    }

    private void loadCartItems() {
        cartItems.clear();
        cartItemsPanel.removeAll();
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT p.*, c.quantity FROM products p " +
                 "JOIN cart c ON p.id = c.product_id")) {
            
            while (rs.next()) {
                CartItem item = new CartItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getInt("quantity"),
                    rs.getString("image_url")
                );
                cartItems.add(item);
                cartItemsPanel.add(createCartItemPanel(item));
                cartItemsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
            
            cartItemsLabel.setText(String.format("Cart Items (%d)", cartItems.size()));
            
            updateOrderSummary();
            cartItemsPanel.revalidate();
            cartItemsPanel.repaint();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading cart: " + e.getMessage());
        }
    }

    private JPanel createCartItemPanel(CartItem item) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));

        // Product image
        try {
            ImageIcon originalIcon = new ImageIcon(new URL(item.imageUrl));
            Image scaledImage = originalIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
            panel.add(imageLabel, BorderLayout.WEST);
        } catch (IOException e) {
            JLabel errorLabel = new JLabel("No image");
            errorLabel.setPreferredSize(new Dimension(80, 80));
            panel.add(errorLabel, BorderLayout.WEST);
        }

        // Product details
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(item.name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(new Color(30, 41, 59));
        
        // Quantity controls
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        quantityPanel.setBackground(Color.WHITE);
        
        JButton decreaseButton = new JButton("-");
        JTextField quantityField = new JTextField(String.valueOf(item.quantity), 3);
        JButton increaseButton = new JButton("+");
        
        // Style quantity controls
        for (JButton button : new JButton[]{decreaseButton, increaseButton}) {
            button.setFont(new Font("Segoe UI", Font.BOLD, 14));
            button.setForeground(new Color(30, 41, 59));
            button.setBackground(Color.WHITE);
            button.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)));
            button.setPreferredSize(new Dimension(30, 30));
            button.setFocusPainted(false);
        }
        
        quantityField.setHorizontalAlignment(JTextField.CENTER);
        quantityField.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)));
        
        // Add quantity control listeners
        decreaseButton.addActionListener(e -> updateQuantity(item, item.quantity - 1));
        increaseButton.addActionListener(e -> updateQuantity(item, item.quantity + 1));
        quantityField.addActionListener(e -> {
            try {
                int newQuantity = Integer.parseInt(quantityField.getText());
                if (newQuantity > 0) {
                    updateQuantity(item, newQuantity);
                }
            } catch (NumberFormatException ex) {
                quantityField.setText(String.valueOf(item.quantity));
            }
        });
        
        quantityPanel.add(decreaseButton);
        quantityPanel.add(quantityField);
        quantityPanel.add(increaseButton);

        detailsPanel.add(nameLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        detailsPanel.add(quantityPanel);
        
        panel.add(detailsPanel, BorderLayout.CENTER);

        // Price and remove button
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        
        JLabel priceLabel = new JLabel("$" + String.format("%.2f", item.price));
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        priceLabel.setForeground(new Color(30, 41, 59));
        rightPanel.add(priceLabel, BorderLayout.NORTH);
        
        JButton removeButton = new JButton("Remove");
        removeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        removeButton.setForeground(new Color(220, 38, 38));
        removeButton.setBorderPainted(false);
        removeButton.setContentAreaFilled(false);
        removeButton.setFocusPainted(false);
        removeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeButton.addActionListener(e -> removeFromCart(item));
        rightPanel.add(removeButton, BorderLayout.SOUTH);
        
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private void updateQuantity(CartItem item, int newQuantity) {
        if (newQuantity < 1) return;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                 "UPDATE cart SET quantity = ? WHERE product_id = ?")) {
            
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, item.id);
            pstmt.executeUpdate();
            
            loadCartItems(); // Reload cart to refresh display
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating quantity: " + e.getMessage());
        }
    }

    private void removeFromCart(CartItem item) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                 "DELETE FROM cart WHERE product_id = ?")) {
            
            pstmt.setInt(1, item.id);
            pstmt.executeUpdate();
            
            loadCartItems(); // Reload cart to refresh display
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error removing item: " + e.getMessage());
        }
    }

    private void updateOrderSummary() {
        double subtotal = cartItems.stream()
            .mapToDouble(item -> item.price * item.quantity)
            .sum();
        
        double tax = subtotal * TAX_RATE;
        double total = subtotal + SHIPPING_COST + tax;

        subtotalLabel.setText(String.format("Subtotal (%d items) $%.2f", 
            cartItems.stream().mapToInt(item -> item.quantity).sum(), subtotal));
        taxLabel.setText(String.format("Tax $%.2f", tax));
        totalLabel.setText(String.format("Total $%.2f", total));
    }

    private void handleCheckout() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Clear the cart table
            stmt.execute("DELETE FROM cart");
            
            // Show thank you message
            JOptionPane.showMessageDialog(this, 
                "Thank you for your purchase!\nYour order has been confirmed.", 
                "Order Confirmation", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh the cart view
            loadCartItems();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error processing checkout: " + e.getMessage());
        }
    }

    private static class CartItem {
        final int id;
        final String name;
        final double price;
        int quantity;
        final String imageUrl;

        CartItem(int id, String name, double price, int quantity, String imageUrl) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.imageUrl = imageUrl;
        }
    }
} 