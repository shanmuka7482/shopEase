import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class GetStarted extends JPanel {
    private Image backgroundImage;

    public GetStarted(CardLayout cardLayout, JPanel mainPanel) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Center panel for main content
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));

        // Logo
        ImageIcon originalIcon = new ImageIcon("res/logo2.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(logoLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Heading
        JLabel heading = new JLabel("Shopping Made Effortless", JLabel.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 36));
        heading.setForeground(new Color(44, 52, 71));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(heading);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Subtitle
        JLabel subtitle = new JLabel("Discover a seamless shopping experience with", JLabel.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitle.setForeground(new Color(107, 114, 128));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(subtitle);

        JLabel subtitle2 = new JLabel("personalized recommendations and exclusive deals.", JLabel.CENTER);
        subtitle2.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitle2.setForeground(new Color(107, 114, 128));
        subtitle2.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(subtitle2);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        // Buttons Panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonsPanel.setBackground(Color.WHITE);

        // Login Button
        JButton loginButton = createStyledButton("Log In", true);
        loginButton.addActionListener(e -> {
            if (mainPanel.getComponentCount() < 2) {
                AuthSystem authSystem = new AuthSystem(cardLayout, mainPanel);
                mainPanel.add(authSystem, "auth");
            }
            Main.showScreen("auth");
        });

        // Sign Up Button
        JButton signupButton = createStyledButton("Sign Up", false);
        signupButton.addActionListener(e -> {
            if (mainPanel.getComponentCount() < 2) {
                AuthSystem authSystem = new AuthSystem(cardLayout, mainPanel);
                mainPanel.add(authSystem, "auth");
            }
            Main.showScreen("auth");
        });

        buttonsPanel.add(loginButton);
        buttonsPanel.add(signupButton);
        centerPanel.add(buttonsPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 60)));

        // Trust Indicators Panel
        JPanel trustPanel = new JPanel();
        trustPanel.setLayout(new BoxLayout(trustPanel, BoxLayout.Y_AXIS));
        trustPanel.setBackground(Color.WHITE);

        // Trusted text
        JLabel trustedLabel = new JLabel("Trusted by thousands of customers");
        trustedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        trustedLabel.setForeground(new Color(107, 114, 128));
        trustedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        trustPanel.add(trustedLabel);
        trustPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Panel for rating, secure, and returns
        JPanel indicatorsPanel = new JPanel();
        indicatorsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
        indicatorsPanel.setBackground(Color.WHITE);

        // Rating
        JPanel ratingPanel = createTrustIndicator("★ 4.9 Rating", new Color(234, 179, 8));
        indicatorsPanel.add(ratingPanel);

        // Secure
        JPanel securePanel = createTrustIndicator("🔒 Secure", new Color(44, 52, 71));
        indicatorsPanel.add(securePanel);

        // Free Returns
        JPanel returnsPanel = createTrustIndicator("↺ Free Returns", new Color(44, 52, 71));
        indicatorsPanel.add(returnsPanel);

        trustPanel.add(indicatorsPanel);
        centerPanel.add(trustPanel);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text, boolean isPrimary) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (isPrimary) {
                    g2.setColor(new Color(44, 52, 71));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                    g2.setColor(new Color(44, 52, 71));
                    g2.setStroke(new BasicStroke(2)); // Make border thicker
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 25, 25);
                }
                
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Don't paint the default border
            }

            @Override
            public boolean isOpaque() {
                return false;
            }
        };

        button.setPreferredSize(new Dimension(150, 45));
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        if (isPrimary) {
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(44, 52, 71));
        } else {
            button.setForeground(new Color(44, 52, 71));
            button.setBackground(Color.WHITE);
        }
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (isPrimary) {
                    button.setBackground(new Color(55, 65, 89)); // Slightly lighter
                } else {
                    button.setBackground(new Color(245, 245, 245)); // Slightly darker
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (isPrimary) {
                    button.setBackground(new Color(44, 52, 71));
                } else {
                    button.setBackground(Color.WHITE);
                }
            }
        });

        return button;
    }

    private JPanel createTrustIndicator(String text, Color textColor) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setBackground(Color.WHITE);

        // Create icon based on text
        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (text.contains("Rating")) {
                    drawStar(g2, textColor);
                } else if (text.contains("Secure")) {
                    drawLock(g2, textColor);
                } else if (text.contains("Returns")) {
                    drawReturnArrow(g2, textColor);
                }

                g2.dispose();
            }

            private void drawStar(Graphics2D g2, Color color) {
                g2.setColor(new Color(234, 179, 8));
                int size = 16;
                int x = 2;
                int y = 2;

                double angle = Math.PI / 5;
                int[] xPoints = new int[10];
                int[] yPoints = new int[10];

                for (int i = 0; i < 10; i++) {
                    double r = (i % 2 == 0) ? size/2 : size/4;
                    xPoints[i] = x + size/2 + (int) (r * Math.cos(angle * i - Math.PI/2));
                    yPoints[i] = y + size/2 + (int) (r * Math.sin(angle * i - Math.PI/2));
                }

                g2.fillPolygon(xPoints, yPoints, 10);
            }

            private void drawLock(Graphics2D g2, Color color) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f));

                // Lock body
                RoundRectangle2D body = new RoundRectangle2D.Float(4, 8, 12, 10, 2, 2);
                g2.fill(body);

                // Lock shackle
                Arc2D shackle = new Arc2D.Float(6, 3, 8, 8, 0, 180, Arc2D.OPEN);
                g2.draw(shackle);
            }

            private void drawReturnArrow(Graphics2D g2, Color color) {
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1.5f));

                // Draw circular arrow
                Arc2D arc = new Arc2D.Float(4, 4, 12, 12, 45, 270, Arc2D.OPEN);
                g2.draw(arc);

                // Draw arrowhead
                int[] xPoints = {16, 16, 13};
                int[] yPoints = {4, 7, 5};
                g2.fillPolygon(xPoints, yPoints, 3);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(20, 20);
            }
        };

        panel.add(iconLabel);

        // Add text without the icon character
        String displayText = text.replaceAll("[★🔒↺]", "").trim();
        JLabel label = new JLabel(displayText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        // label.setForeground(textColor);
        panel.add(label);
        
        return panel;
    }
} 