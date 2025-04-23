import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ProductManagementSystem extends JPanel {
    private static final String DB_URL = "jdbc:sqlite:products.db";
    private JPanel mainPanel;
    private JButton addButton;
    private CardLayout cardLayout;
    private ProductView productView;

    public ProductManagementSystem(CardLayout cardLayout, JPanel mainPanel) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
        initializeDatabase();
        createGUI();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            String createTableSQL = "CREATE TABLE IF NOT EXISTS products (" +
                                  "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                  "name TEXT NOT NULL," +
                                  "price REAL NOT NULL," +
                                  "image_url TEXT NOT NULL)";
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error initializing database: " + e.getMessage());
        }
    }

    private void createGUI() {
        setLayout(new BorderLayout());

        // Create buttons panel
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add Test Products");
        JButton backButton = new JButton("Back");
        buttonPanel.add(addButton);
        buttonPanel.add(backButton);

        // Create product view
        productView = new ProductView();

        // Add components to frame
        add(buttonPanel, BorderLayout.NORTH);
        add(productView, BorderLayout.CENTER);

        // Add action listeners
        addButton.addActionListener(e -> addDummyProducts());
        backButton.addActionListener(e -> Main.showScreen("auth"));
    }

    private void addDummyProducts() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO products (name, price, image_url) VALUES (?, ?, ?)")) {
            
            // Add dummy products with image URLs
            String[][] dummyProducts = {
                {"Smart Fitness Watch", "89.99", "https://fakestoreapi.com/img/71-3HjGNDUL._AC_SY879._SX._UX._SY._UY_.jpg"},
                {"Portable Bluetooth Speaker", "59.99", "https://fakestoreapi.com/img/71li-ujtlUL._AC_UX679_.jpg"},
                {"4K Ultra HD Smart TV", "699.99", "https://fakestoreapi.com/img/81QpkIctqPL._AC_SX679_.jpg"},
                {"Professional DSLR Camera", "1299.99", "https://fakestoreapi.com/img/81Zt42ioCgL._AC_SX679_.jpg"},
                {"Gaming Laptop", "1499.99", "https://fakestoreapi.com/img/81XH0e8fefL._AC_UY879_.jpg"}
            };

            for (String[] product : dummyProducts) {
                pstmt.setString(1, product[0]);
                pstmt.setDouble(2, Double.parseDouble(product[1]));
                pstmt.setString(3, product[2]);
                pstmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Dummy products added successfully!");
            // Refresh the product view
            remove(productView);
            productView = new ProductView();
            add(productView, BorderLayout.CENTER);
            revalidate();
            repaint();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding products: " + e.getMessage());
        }
    }
}