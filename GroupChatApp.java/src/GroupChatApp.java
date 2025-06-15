import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Rectangle;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.*;

@SuppressWarnings("ALL")
public class GroupChatApp extends Application {

    private static final String[] MEMBERS = {"FAITH", "BLESSING", "STYVE", "TERRY"};
    private static final Map<String, String> MEMBER_COLORS = new HashMap<>();

    private VBox chatContainer;
    private ScrollPane chatScrollPane;
    private Label statusLabel;
    private VBox onlineUsersContainer;
    private Map<String, Boolean> userOnlineStatus;
    private TextArea messageInput;
    private String currentUser;
    private Stage primaryStage;
    private Timeline onlineStatusUpdate;

    static {
        // Set up member colors
        MEMBER_COLORS.put("FAITH", "#FF6B6B");
        MEMBER_COLORS.put("BLESSING", "#4ECDC4");
        MEMBER_COLORS.put("STYVE", "#45B7D1");
        MEMBER_COLORS.put("TERRY", "#96CEB4");

        // Initialize database
        DatabaseConnector.initializeDatabase();
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("Professional Group Chat - Login Required");

        // Show login screen first
        showLoginScreen();

        primaryStage.setOnCloseRequest(_ -> {
            if (onlineStatusUpdate != null) {
                onlineStatusUpdate.stop();
            }
            Platform.exit();
        });
    }

    private void showLoginScreen() {
        VBox loginRoot = new VBox(30);
        loginRoot.setAlignment(Pos.CENTER);
        loginRoot.setStyle("-fx-background-color: linear-gradient(to bottom, #2C3E50, #1a1a1a);");
        loginRoot.setPadding(new Insets(50));

        // Logo/Title area
        VBox titleArea = new VBox(10);
        titleArea.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("PROFESSIONAL CHAT");
        titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitleLabel = new Label("Exclusive Access for Team Members");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #BDC3C7; -fx-font-style: italic;");

        Rectangle separator = new Rectangle(200, 2);
        separator.setFill(Color.web("#3498DB"));

        titleArea.getChildren().addAll(titleLabel, subtitleLabel, separator);

        // Login form
        VBox loginForm = new VBox(20);
        loginForm.setAlignment(Pos.CENTER);
        loginForm.setMaxWidth(400);
        loginForm.setStyle("-fx-background-color: rgba(52, 73, 94, 0.8); -fx-background-radius: 15px; -fx-padding: 40px;");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.BLACK);
        shadow.setOffsetY(5);
        shadow.setRadius(10);
        loginForm.setEffect(shadow);

        Label loginTitle = new Label("Member Login");
        loginTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Username field
        VBox usernameBox = new VBox(5);
        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        ComboBox<String> usernameField = new ComboBox<>();
        usernameField.getItems().addAll(MEMBERS);
        usernameField.setPromptText("Select your name");
        usernameField.setStyle("-fx-background-color: #2C3E50; -fx-text-fill: white; -fx-prompt-text-fill: #BDC3C7; " +
                "-fx-control-inner-background: #2C3E50;");
        usernameField.setPrefWidth(300);
        usernameField.setPrefHeight(40);

        usernameBox.getChildren().addAll(usernameLabel, usernameField);

        // Password field
        VBox passwordBox = new VBox(5);
        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setStyle("-fx-background-color: #2C3E50; -fx-text-fill: white; -fx-prompt-text-fill: #BDC3C7; " +
                "-fx-control-inner-background: #2C3E50;");
        passwordField.setPrefWidth(300);
        passwordField.setPrefHeight(40);

        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Login button
        Button loginButton = new Button("LOGIN");
        loginButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-font-size: 16px; -fx-background-radius: 8px; -fx-padding: 12px 40px;");
        loginButton.setPrefWidth(300);

        // Error label
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 12px; -fx-font-weight: bold;");
        errorLabel.setVisible(false);

        // Login action
        Runnable loginAction = () -> {
            String username = usernameField.getValue();
            String password = passwordField.getText();

            if (username == null || username.isEmpty()) {
                showError(errorLabel, "Please select your username");
                return;
            }

            if (password == null || password.isEmpty()) {
                showError(errorLabel, "Please enter your password");
                return;
            }

            if (validateCredentials(username, password)) {
                currentUser = username;
                showMainApplication();
            } else {
                showError(errorLabel, "Invalid credentials. Access denied.");
                passwordField.clear();
            }
        };

        loginButton.setOnAction(_ -> loginAction.run());
        passwordField.setOnAction(_ -> loginAction.run());

        // Member info
        Label infoLabel = new Label("Team Members: FAITH • BLESSING • STYVE • TERRY");
        infoLabel.setStyle("-fx-text-fill: #BDC3C7; -fx-font-size: 12px; -fx-font-style: italic;");

        loginForm.getChildren().addAll(loginTitle, usernameBox, passwordBox, loginButton, errorLabel);

        loginRoot.getChildren().addAll(titleArea, loginForm, infoLabel);

        Scene loginScene = new Scene(loginRoot, 800, 600);
        loginScene.setFill(Color.web("#1a1a1a"));

        primaryStage.setScene(loginScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private boolean validateCredentials(String username, String password) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT password FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return storedPassword.equals(password);
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        Timeline hideError = new Timeline(new KeyFrame(Duration.seconds(3), e -> errorLabel.setVisible(false)));
        hideError.play();
    }

    private void showMainApplication() {
        initializeUserStatus();
        primaryStage.setTitle("Professional Group Chat - Welcome " + currentUser);

        // Main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a1a;");

        // Create header
        VBox header = createHeader();
        root.setTop(header);

        // Create main content area
        HBox mainContent = new HBox(15);
        mainContent.setPadding(new Insets(15));

        // Left sidebar - Online users
        VBox sidebar = createSidebar();

        // Center - Chat area
        VBox chatArea = createChatArea();

        // Right sidebar - User controls
        VBox controlPanel = createControlPanel();

        mainContent.getChildren().addAll(sidebar, chatArea, controlPanel);
        HBox.setHgrow(chatArea, Priority.ALWAYS);

        root.setCenter(mainContent);

        // Create scene
        Scene scene = new Scene(root, 1400, 900);
        scene.setFill(Color.web("#1a1a1a"));

        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);

        // Load messages from database
        loadMessagesFromDatabase();

        // Add welcome message
        addSystemMessage("Welcome to the Professional Chat, " + currentUser + "! 👋");

        // Start periodic online status updates
        startOnlineStatusUpdates();

        // Focus on message input
        Platform.runLater(() -> messageInput.requestFocus());
    }

    private void initializeUserStatus() {
        userOnlineStatus = new HashMap<>();
        for (String member : MEMBERS) {
            // Current user is online, others will be updated from database
            userOnlineStatus.put(member, member.equals(currentUser));
        }
    }

    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setStyle("-fx-background-color: linear-gradient(to right, #2C3E50, #34495E); " +
                "-fx-padding: 25px;");

        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER);

        Label title = new Label("Professional Group Chat");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Current user indicator
        HBox userInfo = new HBox(10);
        userInfo.setAlignment(Pos.CENTER_RIGHT);

        Rectangle userIndicator = new Rectangle(12, 12);
        userIndicator.setFill(Color.web(MEMBER_COLORS.get(currentUser)));
        userIndicator.setArcWidth(12);
        userIndicator.setArcHeight(12);

        Label currentUserLabel = new Label("Logged in as: " + currentUser);
        currentUserLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-background-radius: 5px; -fx-padding: 5px 15px;");
        logoutButton.setOnAction(e -> logout());

        userInfo.getChildren().addAll(userIndicator, currentUserLabel, logoutButton);

        titleBox.getChildren().addAll(title, spacer, userInfo);

        Label subtitle = new Label("Secure communication for team members");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #BDC3C7; -fx-font-style: italic;");
        subtitle.setAlignment(Pos.CENTER);

        header.getChildren().addAll(titleBox, subtitle);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.BLACK);
        shadow.setOffsetY(3);
        header.setEffect(shadow);

        return header;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 12px; -fx-padding: 20px;");

        Label onlineTitle = new Label("Team Members");
        onlineTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #34495E;");

        onlineUsersContainer = new VBox(12);
        updateOnlineUsersList();

        sidebar.getChildren().addAll(onlineTitle, separator, onlineUsersContainer);

        return sidebar;
    }

    private VBox createChatArea() {
        VBox chatArea = new VBox(15);
        chatArea.setStyle("-fx-background-color: #34495E; -fx-background-radius: 12px; -fx-padding: 20px;");

        Label chatTitle = new Label("Team Discussion");
        chatTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        chatContainer = new VBox(10);
        chatContainer.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 10px; -fx-padding: 15px;");

        chatScrollPane = new ScrollPane(chatContainer);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScrollPane.setStyle("-fx-background: #2C3E50; -fx-background-color: transparent;");

        VBox.setVgrow(chatScrollPane, Priority.ALWAYS);

        chatArea.getChildren().addAll(chatTitle, chatScrollPane);

        return chatArea;
    }

    private VBox createControlPanel() {
        VBox controlPanel = new VBox(20);
        controlPanel.setPrefWidth(280);
        controlPanel.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 12px; -fx-padding: 20px;");

        Label messageLabel = new Label("Compose Message");
        messageLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        messageInput = new TextArea();
        messageInput.setPromptText("Type your professional message here...");
        messageInput.setPrefRowCount(5);
        messageInput.setStyle("-fx-background-color: #34495E; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: #BDC3C7; -fx-background-radius: 8px; " +
                "-fx-font-size: 13px; -fx-padding: 10px; -fx-control-inner-background: #34495E; " +
                "-fx-text-inner-color: white; -fx-highlight-fill: #3498DB; -fx-highlight-text-fill: white;");
        messageInput.setWrapText(true);

        Button sendButton = new Button("Send Message");
        sendButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 8px; " +
                "-fx-padding: 12px 30px; -fx-font-size: 14px;");
        sendButton.setPrefWidth(240);

        sendButton.setOnAction(e -> sendMessage());

        messageInput.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER") && e.isControlDown()) {
                sendMessage();
            }
        });

        statusLabel = new Label("Ready to send");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #BDC3C7; -fx-font-style: italic;");

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #34495E;");

        Button clearButton = new Button("Clear Chat History");
        clearButton.setStyle("-fx-background-color: #E67E22; -fx-text-fill: white; " +
                "-fx-font-size: 12px; -fx-background-radius: 5px; -fx-padding: 8px 16px;");
        clearButton.setOnAction(e -> clearChat());

        Label helpLabel = new Label("Press Ctrl+Enter to send quickly");
        helpLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #95A5A6; -fx-font-style: italic;");

        controlPanel.getChildren().addAll(
                messageLabel, messageInput, sendButton, statusLabel,
                separator, clearButton, helpLabel
        );

        return controlPanel;
    }

    private void updateOnlineUsersList() {
        onlineUsersContainer.getChildren().clear();

        for (String member : MEMBERS) {
            VBox userItem = new VBox(5);

            HBox userHeader = new HBox(12);
            userHeader.setAlignment(Pos.CENTER_LEFT);

            Rectangle statusIndicator = new Rectangle(10, 10);
            statusIndicator.setFill(userOnlineStatus.get(member) ? Color.web("#2ECC71") : Color.web("#95A5A6"));
            statusIndicator.setArcWidth(10);
            statusIndicator.setArcHeight(10);

            Label username = new Label(member);
            username.setStyle("-fx-text-fill: " + MEMBER_COLORS.get(member) + "; -fx-font-weight: bold; -fx-font-size: 14px;");

            if (member.equals(currentUser)) {
                username.setText(member + " (You)");
            }

            userHeader.getChildren().addAll(statusIndicator, username);

            Label status = new Label(userOnlineStatus.get(member) ? "Online" : "Offline");
            status.setStyle("-fx-text-fill: " + (userOnlineStatus.get(member) ? "#2ECC71" : "#95A5A6") +
                    "; -fx-font-size: 11px; -fx-padding: 0 0 0 22px;");

            userItem.getChildren().addAll(userHeader, status);
            onlineUsersContainer.getChildren().add(userItem);
        }
    }

    private void loadMessagesFromDatabase() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT sender, content, timestamp FROM messages ORDER BY timestamp");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String sender = rs.getString("sender");
                String content = rs.getString("content");
                LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();

                Platform.runLater(() -> addMessage(sender, content, timestamp));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            addSystemMessage("Error loading message history");
        }
    }

    private void saveMessageToDatabase(String sender, String message) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO messages (sender, content) VALUES (?, ?)");
            stmt.setString(1, sender);
            stmt.setString(2, message);
            stmt.executeUpdate();

            // Update user's last online time
            PreparedStatement updateUser = conn.prepareStatement(
                    "UPDATE users SET last_online = CURRENT_TIMESTAMP WHERE username = ?");
            updateUser.setString(1, sender);
            updateUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            Platform.runLater(() ->
                    statusLabel.setText("Error saving message to database"));
        }
    }

    private void updateUserOnlineStatus() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            // Update current user's status
            PreparedStatement updateCurrentUser = conn.prepareStatement(
                    "UPDATE users SET last_online = CURRENT_TIMESTAMP WHERE username = ?");
            updateCurrentUser.setString(1, currentUser);
            updateCurrentUser.executeUpdate();

            // Check other users' status
            PreparedStatement checkUsers = conn.prepareStatement(
                    "SELECT username, last_online FROM users WHERE username != ?");
            checkUsers.setString(1, currentUser);
            ResultSet rs = checkUsers.executeQuery();

            while (rs.next()) {
                String username = rs.getString("username");
                Timestamp lastOnline = rs.getTimestamp("last_online");
                boolean isOnline = lastOnline != null &&
                        System.currentTimeMillis() - lastOnline.getTime() < 120000; // 2 minutes

                if (userOnlineStatus.get(username) != isOnline) {
                    userOnlineStatus.put(username, isOnline);
                    String statusMessage = username + " is now " + (isOnline ? "online" : "offline");
                    Platform.runLater(() -> addSystemMessage(statusMessage));
                }
            }

            Platform.runLater(this::updateOnlineUsersList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = messageInput.getText().trim();

        if (!message.isEmpty()) {
            addMessage(currentUser, message, LocalDateTime.now());
            saveMessageToDatabase(currentUser, message);
            messageInput.clear();
            statusLabel.setText("Message sent successfully");

            Timeline statusReset = new Timeline(new KeyFrame(Duration.seconds(2), e ->
                    statusLabel.setText("Ready to send")));
            statusReset.play();
        }
    }

    private void addMessage(String sender, String message, LocalDateTime timestamp) {
        VBox messageBox = new VBox(8);
        messageBox.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 10px; " +
                "-fx-padding: 15px; -fx-border-color: " + MEMBER_COLORS.get(sender) +
                "; -fx-border-width: 1.5px; -fx-border-radius: 10px;");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-text-fill: " + MEMBER_COLORS.get(sender) +
                "; -fx-font-weight: bold; -fx-font-size: 15px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timeLabel = new Label(timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")));
        timeLabel.setStyle("-fx-text-fill: #BDC3C7; -fx-font-size: 11px;");

        header.getChildren().addAll(senderLabel, spacer, timeLabel);

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-line-spacing: 2px;");
        messageLabel.setWrapText(true);

        messageBox.getChildren().addAll(header, messageLabel);
        chatContainer.getChildren().add(messageBox);

        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

    private void addSystemMessage(String message) {
        VBox messageBox = new VBox();
        messageBox.setStyle("-fx-background-color: rgba(52, 152, 219, 0.1); -fx-background-radius: 8px; " +
                "-fx-padding: 10px; -fx-alignment: center; -fx-border-color: #3498DB; " +
                "-fx-border-width: 1px; -fx-border-radius: 8px;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: #3498DB; -fx-font-size: 13px; -fx-font-style: italic; -fx-font-weight: bold;");
        messageLabel.setWrapText(true);

        messageBox.getChildren().add(messageLabel);
        chatContainer.getChildren().add(messageBox);

        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

    private void startOnlineStatusUpdates() {
        onlineStatusUpdate = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            updateUserOnlineStatus();
        }));

        onlineStatusUpdate.setCycleCount(Timeline.INDEFINITE);
        onlineStatusUpdate.play();
    }

    private void clearChat() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Chat");
        alert.setHeaderText("Clear all messages?");
        alert.setContentText("This action cannot be undone. All chat history will be permanently deleted.");

        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2C3E50;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: white;");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnector.getConnection()) {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("DELETE FROM messages");

                Platform.runLater(() -> {
                    chatContainer.getChildren().clear();
                    addSystemMessage("Chat history cleared by " + currentUser);
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        statusLabel.setText("Error clearing chat history"));
            }
        }
    }

    private void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Logout Confirmation");
        alert.setContentText("Are you sure you want to logout?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (onlineStatusUpdate != null) {
                onlineStatusUpdate.stop();
            }
            currentUser = null;
            showLoginScreen();
        }
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        launch(args);
    }
}