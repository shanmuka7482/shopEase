import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AuthSystem extends JPanel {
    private static final String DB_URL = "jdbc:sqlite:users.db";
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField signupUsernameField;
    private JPasswordField signupPasswordField;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public AuthSystem(CardLayout cardLayout, JPanel mainPanel) {
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
        initializeDatabase();
        createLoginGUI();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            String createTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                                  "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                  "username TEXT UNIQUE NOT NULL," +
                                  "password TEXT NOT NULL)";
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error initializing database: " + e.getMessage());
        }
    }

    private void createLoginGUI() {
        setLayout(new BorderLayout());
        
        // Main panel with white background and shadow effect
        JPanel mainLoginPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 15, 15);
                
                // Draw white background
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 15, 15);
            }
        };
        mainLoginPanel.setLayout(new BoxLayout(mainLoginPanel, BoxLayout.Y_AXIS));
        mainLoginPanel.setPreferredSize(new Dimension(400, 500));
        mainLoginPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainLoginPanel.setOpaque(false);
        mainLoginPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainLoginPanel.setMaximumSize(new Dimension(500, Integer.MAX_VALUE)); // Or whatever max width you want
        // Logo panel (centered)
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setOpaque(false);
        ImageIcon originalIcon = new ImageIcon("res/logo3.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
        logoPanel.add(logoLabel);
        mainLoginPanel.add(logoPanel);
        mainLoginPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Welcome text panel (centered)
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setOpaque(false);
        
        JLabel welcomeLabel = new JLabel("Welcome Back");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(44, 52, 71));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Sign in to continue to your account");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        welcomePanel.add(subtitleLabel);
        mainLoginPanel.add(welcomePanel);
        mainLoginPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Form panel (left-aligned)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        // Email field
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(emailLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        emailField = createStyledTextField("Your email address");
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(emailField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(passwordLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        passwordField = createStyledPasswordField("Your password");
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(passwordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Remember me checkbox (left-aligned)
        JCheckBox rememberMeBox = new JCheckBox("Remember me");
        rememberMeBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rememberMeBox.setForeground(new Color(107, 114, 128));
        rememberMeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        rememberMeBox.setOpaque(false);
        formPanel.add(rememberMeBox);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Login button (same width as input fields)
        JButton loginButton = createStyledButton("Login");
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> handleLogin());
        formPanel.add(loginButton);
        
        mainLoginPanel.add(formPanel);
        mainLoginPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Sign up link (centered)
        JPanel signupPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        signupPanel.setOpaque(false);
        JLabel noAccountLabel = new JLabel("Don't have an account? ");
        noAccountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        noAccountLabel.setForeground(new Color(107, 114, 128));
        
        JLabel signupLink = new JLabel("Create an account");
        signupLink.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        signupLink.setForeground(new Color(29, 41, 57));
        signupLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signupLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                removeAll();
                createSignupGUI();
                revalidate();
                repaint();
            }
        });

        signupPanel.add(noAccountLabel);
        signupPanel.add(signupLink);
        mainLoginPanel.add(signupPanel);

        // Add the main panel to a wrapper panel for centering
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        wrapperPanel.add(mainLoginPanel, gbc);

        add(wrapperPanel, BorderLayout.CENTER);
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(156, 163, 175));
                    g2d.setFont(getFont().deriveFont(Font.PLAIN));
                    g2d.drawString(placeholder, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
                }
            }
        };
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField(20) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !hasFocus()) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setColor(new Color(156, 163, 175));
                    g2d.setFont(getFont().deriveFont(Font.PLAIN));
                    g2d.drawString(placeholder, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
                }
            }
        };
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2.setColor(new Color(30, 41, 138));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(45, 56, 153));
                } else {
                    g2.setColor(new Color(37, 49, 141));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();

                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // No border
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void createSignupGUI() {
        removeAll();
        setLayout(new BorderLayout());
        
        // Main panel with white background and shadow effect
        JPanel mainSignupPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw shadow
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 15, 15);
                
                // Draw white background
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 15, 15);
            }
        };
        mainSignupPanel.setLayout(new BoxLayout(mainSignupPanel, BoxLayout.Y_AXIS));
        mainSignupPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainSignupPanel.setOpaque(false);
        mainSignupPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainSignupPanel.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));

        // Logo panel (centered)
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoPanel.setOpaque(false);
        ImageIcon originalIcon = new ImageIcon("res/logo3.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledImage));
        logoPanel.add(logoLabel);
        mainSignupPanel.add(logoPanel);
        mainSignupPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Welcome text panel (centered)
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setOpaque(false);
        
        JLabel welcomeLabel = new JLabel("Create Account");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(44, 52, 71));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Sign up to get started with your account");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        welcomePanel.add(subtitleLabel);
        mainSignupPanel.add(welcomePanel);
        mainSignupPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Form panel (left-aligned)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Email field
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(emailLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        signupUsernameField = createStyledTextField("Your email address");
        signupUsernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(signupUsernameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(passwordLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        signupPasswordField = createStyledPasswordField("Your password");
        signupPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(signupPasswordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Create Account button
        JButton signupButton = createStyledButton("Create Account");
        signupButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        signupButton.addActionListener(e -> handleSignup());
        formPanel.add(signupButton);
        
        mainSignupPanel.add(formPanel);
        mainSignupPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Login link (centered)
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loginPanel.setOpaque(false);
        JLabel haveAccountLabel = new JLabel("Already have an account? ");
        haveAccountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        haveAccountLabel.setForeground(new Color(107, 114, 128));
        
        JLabel loginLink = new JLabel("Login");
        loginLink.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginLink.setForeground(new Color(29, 41, 57));
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                removeAll();
                createLoginGUI();
                revalidate();
                repaint();
            }
        });

        loginPanel.add(haveAccountLabel);
        loginPanel.add(loginLink);
        mainSignupPanel.add(loginPanel);

        // Add the main panel to a wrapper panel for centering
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBackground(new Color(245, 247, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        wrapperPanel.add(mainSignupPanel, gbc);

        add(wrapperPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void handleLogin() {
        String username = emailField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT * FROM users WHERE username = ? AND password = ?")) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login successful!");
                Main.showScreen("products");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error during login: " + e.getMessage());
        }
    }

    private void handleSignup() {
        String username = signupUsernameField.getText();
        String password = new String(signupPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO users (username, password) VALUES (?, ?)")) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Account created successfully!");
            removeAll();
            createLoginGUI();
            revalidate();
            repaint();
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                JOptionPane.showMessageDialog(this, "Username already exists!");
            } else {
                JOptionPane.showMessageDialog(this, "Error creating account: " + e.getMessage());
            }
        }
    }
} 