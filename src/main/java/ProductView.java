import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Rectangle2D;
import java.io.File;

public class ProductView extends JPanel {
    private static final String DB_URL = "jdbc:sqlite:products.db";
    // --- Make Connection and PreparedStatement members for potential reuse ---
    // --- Although for SQLite in Swing, separate connections per logical operation ---
    // --- as done below is often safer if managed correctly. ---
    // --- Let's stick to fixing the immediate issue first. ---

    private JPanel productsPanel;
    private JTextField searchField;
    private List<JPanel> allProductCards;
    private JPanel headerPanel;

    public ProductView() {
        allProductCards = new ArrayList<>();
        initializeDatabase();
        createGUI();
        loadProducts();
    }

    private void initializeDatabase() {
        // Use try-with-resources for automatic closing
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Configure SQLite for better concurrency (optional but can help)
            stmt.execute("PRAGMA journal_mode=WAL;"); // Write-Ahead Logging
            stmt.execute("PRAGMA busy_timeout = 5000;"); // Wait 5 seconds if busy

            // Create products table if it doesn't exist
            String createProductsTableSQL = "CREATE TABLE IF NOT EXISTS products (" +
                                          "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                          "name TEXT NOT NULL UNIQUE," + // Added UNIQUE constraint
                                          "price REAL NOT NULL," +
                                          "image_url TEXT NOT NULL)";
            stmt.execute(createProductsTableSQL);

            // Create cart table if it doesn't exist
            String createCartTableSQL = "CREATE TABLE IF NOT EXISTS cart (" +
                                      "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                      "product_id INTEGER NOT NULL UNIQUE," +
                                      "quantity INTEGER NOT NULL," +
                                      "FOREIGN KEY(product_id) REFERENCES products(id))";
            stmt.execute(createCartTableSQL);

            // Check if products table is empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM products");
            if (rs.next() && rs.getInt("count") == 0) {
                // Insert sample products (using PreparedStatement for safety)
                 String insertSQL = "INSERT INTO products (name, price, image_url) VALUES (?, ?, ?)";
                 try (PreparedStatement pstmtInsert = conn.prepareStatement(insertSQL)) {
                     String[][] productsData = {
                         {"Premium Wireless Mouse", "49.99", "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500"},
                         {"Mechanical Gaming Keyboard", "129.99", "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?w=500"},
                         {"4K Gaming Monitor", "399.99", "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500"},
                         {"Gaming Headset Pro", "89.99", "https://images.unsplash.com/photo-1599669454699-248893623440?w=500"},
                         {"RGB Mouse Pad XL", "29.99", "https://images.unsplash.com/photo-1616788494672-ec7ca25fdda9?w=500"},
                         {"Webcam 1080p", "79.99", "https://images.unsplash.com/photo-1587826080692-f439cd0b70da?w=500"},
                         {"USB-C Hub", "45.99", "https://images.unsplash.com/photo-1619953942547-233eab5a70d6?w=500"},
                         {"Wireless Charging Pad", "34.99", "https://images.unsplash.com/photo-1622957040873-8ea24e293885?w=500"},
                         {"Laptop Stand", "39.99", "https://images.unsplash.com/photo-1625842268584-8f3296236761?w=500"}
                     };

                     for (String[] product : productsData) {
                         pstmtInsert.setString(1, product[0]);
                         pstmtInsert.setDouble(2, Double.parseDouble(product[1]));
                         pstmtInsert.setString(3, product[2]);
                         pstmtInsert.executeUpdate();
                     }
                 }
            }
        } catch (SQLException e) {
            // Provide more context in the error message
            JOptionPane.showMessageDialog(this, "Error initializing database: " + e.getMessage() + "\nSQL State: " + e.getSQLState(), "Database Error", JOptionPane.ERROR_MESSAGE);
            // Optionally, print stack trace for debugging
            e.printStackTrace();
        }
    }

    // Modified addToCart to take product name and handle ID lookup internally
    private void addToCart(String productName) {
        String findIdSql = "SELECT id FROM products WHERE name = ?";
        String upsertCartSql = "INSERT INTO cart (product_id, quantity) VALUES (?, 1) " +
                               "ON CONFLICT(product_id) DO UPDATE SET quantity = quantity + 1";

        // Use a single connection for the entire operation
        Connection conn = null; // Declare outside try-finally for broader scope if needed
        try {
             conn = DriverManager.getConnection(DB_URL);
             // Optional: Set busy timeout for this specific connection if not done globally
             try (Statement stmt = conn.createStatement()) {
                 stmt.execute("PRAGMA busy_timeout = 5000;");
             }

             // Optional but recommended: Use a transaction
             conn.setAutoCommit(false);

             int productId = -1;

             // 1. Find the product ID
             try (PreparedStatement pstmtFind = conn.prepareStatement(findIdSql)) {
                 pstmtFind.setString(1, productName);
                 ResultSet rs = pstmtFind.executeQuery();
                 if (rs.next()) {
                     productId = rs.getInt("id");
                 } else {
                     // Product not found - handle this case
                     JOptionPane.showMessageDialog(this, "Error: Product '" + productName + "' not found in database.", "Add to Cart Error", JOptionPane.ERROR_MESSAGE);
                     conn.rollback(); // Rollback transaction
                     return; // Exit the method
                 }
             } // pstmtFind and rs are closed here

             // 2. Insert or update the cart
             if (productId != -1) {
                 try (PreparedStatement pstmtUpsert = conn.prepareStatement(upsertCartSql)) {
                     pstmtUpsert.setInt(1, productId);
                     int affectedRows = pstmtUpsert.executeUpdate();
                     if (affectedRows > 0) {
                         conn.commit(); // Commit transaction *only if successful*
                         JOptionPane.showMessageDialog(this, "'" + productName + "' added to cart successfully!");
                     } else {
                         // This shouldn't happen with ON CONFLICT but good to check
                         conn.rollback();
                         JOptionPane.showMessageDialog(this, "Error: Could not update cart.", "Add to Cart Error", JOptionPane.ERROR_MESSAGE);
                     }
                 } // pstmtUpsert is closed here
             }

        } catch (SQLException e) {
             // Handle potential exceptions during DB operations
             try {
                 if (conn != null) {
                     conn.rollback(); // Rollback on error
                 }
             } catch (SQLException ex) {
                 System.err.println("Error rolling back transaction: " + ex.getMessage());
                 ex.printStackTrace(); // Log rollback error
             }
             JOptionPane.showMessageDialog(this, "Error adding '" + productName + "' to cart: " + e.getMessage() + "\nSQL State: " + e.getSQLState(), "Database Error", JOptionPane.ERROR_MESSAGE);
             e.printStackTrace(); // Print stack trace for debugging
        } finally {
            // Ensure connection is always closed
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset auto-commit before closing (good practice)
                    conn.close();
                }
            } catch (SQLException ex) {
                System.err.println("Error closing connection: " + ex.getMessage());
                ex.printStackTrace(); // Log closing error
            }
        }
    }


    private void createGUI() {
        // ... (rest of your createGUI method is likely fine) ...
        // Make sure the Main class exists and has the showScreen method
         setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // Create header panel
        headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 20, 40));

        // Title
        JLabel titleLabel = new JLabel("Our Products");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(30, 41, 59));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Discover our collection of premium products designed for quality and performance.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(71, 85, 105));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Search panel
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        searchPanel.setBackground(new Color(245, 247, 250));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Removed setMaximumSize for searchPanel to allow it to potentially grow
        // searchPanel.setMaximumSize(new Dimension(300, 50)); // Consider removing or adjusting

        // Search field with icon and rounded borders
        searchField = new JTextField(20) {
            private String placeholder = "Search products...";
            private boolean showingPlaceholder = true;

            {
                setText(placeholder);
                setForeground(new Color(156, 163, 175));

                addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        if (showingPlaceholder) {
                            setText("");
                            setForeground(new Color(30, 41, 59));
                            showingPlaceholder = false;
                        }
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        if (getText().isEmpty()) {
                            setText(placeholder);
                            setForeground(new Color(156, 163, 175));
                            showingPlaceholder = true;
                        }
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create(); // Use create() to avoid modifying original Graphics
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw rounded background
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);

                // Draw rounded border
                g2d.setColor(new Color(209, 213, 219));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 25, 25);

                // Draw search icon
                int iconSize = 16;
                int padding = 12;
                try {
                    // Use direct file path instead of classpath
                    File searchIconFile = new File("res/search.jpg");
                    if (searchIconFile.exists()) {
                        ImageIcon searchIcon = new ImageIcon(searchIconFile.getAbsolutePath());
                        if (searchIcon.getImage() != null && searchIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                            g2d.drawImage(searchIcon.getImage(), padding, (getHeight() - iconSize) / 2, iconSize, iconSize, null);
                        }
                    } else {
                        System.err.println("Search icon file not found at: " + searchIconFile.getAbsolutePath());
                    }
                } catch (Exception ex) {
                    System.err.println("Error loading search icon: " + ex.getMessage());
                }

                // Set clip for text to not overlap with the icon
                Shape oldClip = g2d.getClip();
                g2d.clipRect(padding + iconSize + 5, 0, getWidth() - (padding + iconSize + 10), getHeight());

                // Draw the text
                super.paintComponent(g2d);

                // Restore the clip
                g2d.setClip(oldClip);
                g2d.dispose(); // Dispose of the created Graphics context
            }
        };
        searchField.setOpaque(false);
        searchField.setBorder(BorderFactory.createEmptyBorder(12, 35, 12, 12));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setMaximumSize(new Dimension(300, 45)); // Max size can constrain layout
        searchField.setPreferredSize(new Dimension(250, 45)); // Preferred size is often better
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Check placeholder status correctly
                JTextField source = (JTextField) e.getSource();
                 String text = source.getText();
                 boolean isPlaceholder = text.equals("Search products...") && source.getForeground().equals(new Color(156, 163, 175));

                 if (!isPlaceholder) {
                    filterProducts(text);
                 } else {
                    filterProducts(""); // Show all if placeholder is showing (e.g., after clearing text)
                 }
            }
        });

        // Add Cart Button
        JButton cartButton = new JButton("View Cart") {
             // Store color to avoid creating new Color objects repeatedly in paintComponent
             private final Color normalColor = new Color(29, 41, 113);
             private final Color hoverColor = new Color(45, 56, 153);
             private final Color pressedColor = new Color(30, 41, 138);

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(pressedColor);
                } else if (getModel().isRollover()) {
                    g2d.setColor(hoverColor);
                } else {
                    g2d.setColor(normalColor);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // Draw cart icon
                int iconSize = 16;
                int textIconGap = 5; // Gap between icon and text
                int totalContentWidth = iconSize + textIconGap + g2d.getFontMetrics().stringWidth(getText());
                int startX = (getWidth() - totalContentWidth) / 2;

                drawCartIcon(g2d, startX, (getHeight() - iconSize) / 2, iconSize);

                // Draw the text with adjusted position for icon
                FontMetrics fm = g2d.getFontMetrics();
                int textX = startX + iconSize + textIconGap;
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), textX, textY);
                g2d.dispose();
            }

            private void drawCartIcon(Graphics2D g2d, int x, int y, int size) {
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.5f));

                // Draw cart body more robustly
                int bodyHeight = size - 6;
                int bodyWidth = size - 2;
                g2d.drawRoundRect(x, y + 2, bodyWidth, bodyHeight, 2, 2);

                 // Draw wheels
                 int wheelSize = 3;
                 g2d.drawOval(x + 3, y + bodyHeight + 1, wheelSize, wheelSize);
                 g2d.drawOval(x + bodyWidth - wheelSize - 3, y + bodyHeight + 1, wheelSize, wheelSize);

                 // Draw handle
                 g2d.drawLine(x + bodyWidth / 2, y + 2, x + bodyWidth / 2, y); // Vertical part
                 g2d.drawLine(x + bodyWidth / 2, y, x + bodyWidth + 1, y); // Horizontal part
            }

             @Override
            public Dimension getPreferredSize() {
                 // Calculate preferred size based on text, icon, and padding
                 FontMetrics fm = getFontMetrics(getFont());
                 int iconSize = 16;
                 int textIconGap = 5;
                 int width = fm.stringWidth(getText()) + iconSize + textIconGap + getInsets().left + getInsets().right + 20; // Add some extra padding
                 int height = Math.max(fm.getHeight(), iconSize) + getInsets().top + getInsets().bottom + 10;
                 return new Dimension(width, height);
            }
        };
        cartButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cartButton.setForeground(Color.WHITE);
        cartButton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Adjusted padding
        cartButton.setFocusPainted(false);
        cartButton.setContentAreaFilled(false); // Important for custom painting
        cartButton.setOpaque(false);
        cartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cartButton.addActionListener(e -> {
            Main.showScreen("cart");
        });


        searchPanel.add(searchField);
        searchPanel.add(Box.createHorizontalStrut(20)); // Add space between search and button
        searchPanel.add(cartButton);
        searchPanel.add(Box.createHorizontalGlue()); // Pushes components left if needed

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        headerPanel.add(subtitleLabel);
        // Use a JPanel with FlowLayout for better alignment of search components
        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); // Align left, no gaps
        searchContainer.setBackground(new Color(245, 247, 250));
        searchContainer.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0)); // Top padding
        searchContainer.add(searchPanel);
        headerPanel.add(searchContainer);


        // Products panel with GridLayout (3 columns)
        productsPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        productsPanel.setBackground(new Color(245, 247, 250));
        productsPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Scroll pane for products
        JScrollPane scrollPane = new JScrollPane(productsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Prevent horizontal scroll
        scrollPane.setBackground(new Color(245, 247, 250));

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadProducts() {
        productsPanel.removeAll();
        allProductCards.clear();

        // Use try-with-resources for automatic closing
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, price, image_url FROM products")) { // Select only needed columns

            while (rs.next()) {
                // Retrieve data by column name for clarity
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                String imageUrl = rs.getString("image_url");

                // Pass the product name to createProductCard
                JPanel card = createProductCard(name, price, imageUrl);
                allProductCards.add(card);
                productsPanel.add(card);
            }

        } catch (SQLException e) {
             JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage() + "\nSQL State: " + e.getSQLState(), "Database Error", JOptionPane.ERROR_MESSAGE);
             e.printStackTrace();
        } finally {
            // Ensure UI updates happen after database operations
            productsPanel.revalidate();
            productsPanel.repaint();
        }
    }

    // Modified createProductCard to pass product NAME to the action listener
    private JPanel createProductCard(String name, double price, String imageUrl) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2d.setColor(new Color(229, 231, 235));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 15, 10));

        // Image panel
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        imagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        try {
            ImageIcon originalIcon = new ImageIcon(new URL(imageUrl));
            Image scaledImage = originalIcon.getImage().getScaledInstance(280, 280, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            // imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            imagePanel.add(imageLabel, BorderLayout.CENTER);
        } catch (IOException e) {
            JLabel errorLabel = new JLabel("Image not available");
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            imagePanel.add(errorLabel, BorderLayout.CENTER);
        }

        card.add(imagePanel);

        // Rating stars
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        ratingPanel.setBackground(Color.WHITE);
        int rating = (int) (Math.random() * 5) + 1;
        
        // Add custom star components
        for (int i = 0; i < 5; i++) {
            ratingPanel.add(new StarComponent(i < rating));
        }
        
        JLabel reviewCount = new JLabel(" (" + ((int)(Math.random() * 150) + 50) + ")");
        reviewCount.setForeground(new Color(107, 114, 128));
        reviewCount.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ratingPanel.add(reviewCount);
        ratingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(ratingPanel);

        // Product name
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(new Color(30, 41, 59));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(nameLabel);

        // Price panel
        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pricePanel.setBackground(Color.WHITE);
        
        // Current price
        JLabel priceLabel = new JLabel("$" + String.format("%.2f", price));
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        priceLabel.setForeground(new Color(30, 41, 59));
        
        pricePanel.add(priceLabel);
        pricePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(pricePanel);

        // Add to Cart button with cart icon
        JButton addToCartButton = new JButton("Add to Cart") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(30, 41, 138));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(45, 56, 153));
                } else {
                    g2d.setColor(new Color(29, 41, 113));
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Draw cart icon
                int iconSize = 16;
                int padding = 12;
                drawCartIcon(g2d, padding, (getHeight() - iconSize) / 2, iconSize);
                
                // Draw the text with adjusted position for icon
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2 + iconSize/2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                
                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), x, y);
            }
            
            private void drawCartIcon(Graphics2D g2d, int x, int y, int size) {
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.5f));
                
                // Draw cart body
                int[] xPoints = {x, x + size, x + size - 4, x + 2};
                int[] yPoints = {y + size - 4, y + size - 4, y + 4, y + 4};
                g2d.drawPolyline(xPoints, yPoints, 4);
                
                // Draw wheels
                g2d.drawOval(x + 3, y + size - 3, 3, 3);
                g2d.drawOval(x + size - 7, y + size - 3, 3, 3);
                
                // Draw handle
                g2d.drawLine(x + size - 4, y + 4, x + size + 2, y + 4);
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                // No border painting needed
            }
        };
        addToCartButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addToCartButton.setForeground(Color.WHITE);
        addToCartButton.setBackground(new Color(29, 41, 113));
        addToCartButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        addToCartButton.setFocusPainted(false);
        addToCartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addToCartButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addToCartButton.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        
        // Update the action listener to add to cart
        addToCartButton.addActionListener(e -> addToCart(name)); 
        // Add some spacing before the button
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(addToCartButton);

        return card;
    }
    // Custom star component (seems okay)
    private class StarComponent extends JComponent {
        private boolean filled;
        private final Color filledColor = new Color(234, 179, 8);
        private final Color emptyColor = new Color(209, 213, 219);
        private Shape starShape; // Cache shape for performance

        public StarComponent(boolean filled) {
            this.filled = filled;
            setPreferredSize(new Dimension(20, 20));
            setOpaque(false); // Important for transparent background
             starShape = createStarShape(16, 2, 2); // Create shape once
        }

         private Shape createStarShape(int size, int x, int y) {
             double angle = Math.PI / 5;
             Polygon p = new Polygon();
             for (int i = 0; i < 10; i++) {
                 double r = (i % 2 == 0) ? size / 2.0 : size / 4.0;
                 int px = x + size / 2 + (int) (r * Math.cos(angle * i - Math.PI / 2));
                 int py = y + size / 2 + (int) (r * Math.sin(angle * i - Math.PI / 2));
                 p.addPoint(px, py);
             }
             return p;
         }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(filled ? filledColor : emptyColor);
            g2.fill(starShape); // Draw the cached shape
            g2.dispose();
        }
    }

     private void filterProducts(String searchText) {
         productsPanel.removeAll(); // Clear the panel first
         boolean hasVisibleProducts = false;
         String lowerSearchText = searchText.toLowerCase().trim();

         for (JPanel card : allProductCards) {
             boolean visible = false;
             // Find the product name component (now using JLabel)
             for (Component comp : card.getComponents()) {
                 if (comp instanceof JLabel) {
                     JLabel nameLabel = (JLabel) comp;
                     // Check if it's the name label based on font properties
                     if (nameLabel.getFont().getStyle() == Font.BOLD && nameLabel.getFont().getSize() == 16) {
                         if (lowerSearchText.isEmpty() || nameLabel.getText().toLowerCase().contains(lowerSearchText)) {
                             visible = true;
                             break; // Found name, no need to check other components in this card
                         }
                     }
                 }
             }

             if (visible) {
                 productsPanel.add(card); // Add card back if it matches
                 hasVisibleProducts = true;
             }
         }

         // If no products match and search text is not empty, show a message
         if (!hasVisibleProducts && !lowerSearchText.isEmpty()) {
             JLabel noResultsLabel = new JLabel("No products found matching '" + searchText + "'");
             noResultsLabel.setName("NoResultsLabel"); // Set a name to identify it later
             noResultsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
             noResultsLabel.setForeground(new Color(107, 114, 128));
             noResultsLabel.setHorizontalAlignment(SwingConstants.CENTER);
             productsPanel.add(noResultsLabel);
         }

         // Revalidate and repaint the panel
         productsPanel.revalidate();
         productsPanel.repaint();
     }
}