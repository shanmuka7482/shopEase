import javax.swing.*;
import java.awt.*;

public class Main {
    private static JFrame frame;
    private static JPanel mainPanel;
    private static CardLayout cardLayout;
    private static CartView cartView;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("ShopEase");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // Set full screen but keep window decorations
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            // Center the frame on screen
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setSize(screenSize.width, screenSize.height);
            frame.setLocationRelativeTo(null);

            // Set minimum size to prevent too small window
            frame.setMinimumSize(new Dimension(1024, 768));

            cardLayout = new CardLayout();
            mainPanel = new JPanel(cardLayout);
            
            // Add all screens to the main panel
            GetStarted getStarted = new GetStarted(cardLayout, mainPanel);
            mainPanel.add(getStarted, "getStarted");
            
            // Add AuthSystem screen explicitly during initialization
            AuthSystem authSystem = new AuthSystem(cardLayout, mainPanel);
            mainPanel.add(authSystem, "auth");
            
            ProductView productView = new ProductView();
            mainPanel.add(productView, "products");
            
            cartView = new CartView();
            mainPanel.add(cartView, "cart");

            frame.add(mainPanel);
            
            // Start with getStarted screen
            cardLayout.show(mainPanel, "getStarted");
            
            frame.setVisible(true);
        });
    }

    public static void showScreen(String screenName) {
        System.out.println("Switching to screen: " + screenName); // Debug
        if (screenName.equals("cart")) {
            // Remove existing CartView
            mainPanel.remove(cartView);
            // Create new CartView instance
            cartView = new CartView();
            mainPanel.add(cartView, "cart");
            mainPanel.revalidate();
        }
        cardLayout.show(mainPanel, screenName);
    }
}