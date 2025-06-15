import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import java.util.Random;

public class PersistentLoveApp extends Application {

    private Label messageLabel;
    private Button yesButton;
    private Button noButton;
    private int attemptCount = 0;
    private Random random = new Random();

    private String[] funnyMessages = {
            "Nice try! But you can't escape my love! 💕",
            "The 'No' button is playing hard to get! 😏",
            "Oops! Looks like that button is broken! 🔧",
            "Did you really think it would be that easy? 😈",
            "The 'No' button has trust issues! 💔",
            "Error 404: 'No' option not found! 🤖",
            "That button is on vacation! Try the other one! 🏖️",
            "The 'No' button is practicing social distancing! 😷",
            "Whoops! Butter fingers! Try again! 🧈",
            "The 'No' button is camera shy! 📸",
            "That button has commitment issues! 💍",
            "The 'No' button is playing hide and seek! 🙈",
            "Error: Button.exe has stopped working! 💻",
            "The 'No' button is having an identity crisis! 🤔",
            "That button needs some personal space! 🚀"
    };

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Do You Like Me? 💖");

        // Create main container
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #ff9a9e, #fecfef);");

        // Title label
        Label titleLabel = new Label("💕 IMPORTANT QUESTION 💕");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.DARKMAGENTA);

        // Question label
        Label questionLabel = new Label("Do you like me?");
        questionLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        questionLabel.setTextFill(Color.DARKSLATEBLUE);

        // Message label for funny responses
        messageLabel = new Label("Choose wisely... 😉");
        messageLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        messageLabel.setTextFill(Color.DARKRED);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        messageLabel.setAlignment(Pos.CENTER);

        // Create buttons
        yesButton = new Button("Yes! 💖");
        yesButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        yesButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; " +
                "-fx-background-radius: 25; -fx-padding: 10 30 10 30;");

        noButton = new Button("No 😐");
        noButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        noButton.setStyle("-fx-background-color: #4ecdc4; -fx-text-fill: white; " +
                "-fx-background-radius: 25; -fx-padding: 10 30 10 30;");

        // Button container
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(yesButton, noButton);

        // Add hover effects
        addHoverEffects();

        // Button actions
        yesButton.setOnAction(e -> showSuccessMessage());
        noButton.setOnAction(e -> handleNoButtonClick());

        // Add all components to root
        root.getChildren().addAll(titleLabel, questionLabel, messageLabel, buttonBox);

        Scene scene = new Scene(root, 500, 350);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void addHoverEffects() {
        // Yes button hover effect
        yesButton.setOnMouseEntered(e -> {
            yesButton.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white; " +
                    "-fx-background-radius: 25; -fx-padding: 10 30 10 30; " +
                    "-fx-scale-x: 1.1; -fx-scale-y: 1.1;");
        });

        yesButton.setOnMouseExited(e -> {
            yesButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; " +
                    "-fx-background-radius: 25; -fx-padding: 10 30 10 30; " +
                    "-fx-scale-x: 1.0; -fx-scale-y: 1.0;");
        });

        // No button hover effect - it runs away!
        noButton.setOnMouseEntered(e -> moveNoButton());
    }

    private void handleNoButtonClick() {
        attemptCount++;
        moveNoButton();

        // Show funny message
        if (attemptCount <= funnyMessages.length) {
            messageLabel.setText(funnyMessages[attemptCount - 1]);
        } else {
            // After all messages, show random ones
            messageLabel.setText(funnyMessages[random.nextInt(funnyMessages.length)]);
        }

        // Make the message more dramatic with larger attempts
        if (attemptCount > 5) {
            messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }

        // Special message after many attempts
        if (attemptCount > 10) {
            messageLabel.setText("Seriously? Just click 'Yes' already! I'm getting tired! 😴");
        }

        if (attemptCount > 15) {
            messageLabel.setText("Fine! You win... but deep down, you like me! 😏💕");
            noButton.setText("You like me! 💖");
            noButton.setOnAction(e -> showSuccessMessage());
        }
    }

    private void moveNoButton() {
        // Create random movement animation
        TranslateTransition transition = new TranslateTransition(Duration.millis(200), noButton);

        // Random movement within reasonable bounds
        double newX = (random.nextDouble() - 0.5) * 200; // -100 to 100
        double newY = (random.nextDouble() - 0.5) * 100; // -50 to 50

        transition.setToX(newX);
        transition.setToY(newY);
        transition.play();

        // Reset position after animation for next movement
        transition.setOnFinished(e -> {
            // Don't reset immediately, let it stay in new position
        });
    }

    private void showSuccessMessage() {
        // Clear the scene and show relationship options
        VBox successBox = new VBox(25);
        successBox.setAlignment(Pos.CENTER);
        successBox.setPadding(new Insets(40));
        successBox.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e);");

        Label successTitle = new Label("🎉 Awesome! 🎉");
        successTitle.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        successTitle.setTextFill(Color.web("#e74c3c"));

        Label questionLabel = new Label("So... what are we? 💭");
        questionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        questionLabel.setTextFill(Color.web("#f39c12"));

        Label funFact = new Label("(You tried to escape " + attemptCount + " times! 😏)");
        funFact.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        funFact.setTextFill(Color.web("#95a5a6"));

        // Relationship option buttons
        VBox buttonContainer = new VBox(15);
        buttonContainer.setAlignment(Pos.CENTER);

        Button friendsButton = createRelationshipButton("Best Friends Forever! 👫", "#3498db");
        Button coupleButton = createRelationshipButton("Dating & In Love! 💕", "#e91e63");
        Button marriedButton = createRelationshipButton("Married! 💍", "#9b59b6");
        Button complicatedButton = createRelationshipButton("It's Complicated... 🤷", "#f39c12");
        Button secretButton = createRelationshipButton("Secret Admirers! 🤫", "#1abc9c");

        // Add click handlers for each relationship option
        friendsButton.setOnAction(e -> showFinalMessage("Best friends it is! We'll have so many adventures together! 🌟", (Button)e.getSource()));
        coupleButton.setOnAction(e -> showFinalMessage("Aww, you chose love! My heart is so happy right now! 💖✨", (Button)e.getSource()));
        marriedButton.setOnAction(e -> showFinalMessage("Married?! Wow, that escalated quickly! I accept! 💒👰", (Button)e.getSource()));
        complicatedButton.setOnAction(e -> showFinalMessage("Complicated? Perfect! The best relationships always are! 🎭💫", (Button)e.getSource()));
        secretButton.setOnAction(e -> showFinalMessage("Secret admirers? How mysterious and exciting! 🕵️‍♀️💜", (Button)e.getSource()));

        buttonContainer.getChildren().addAll(friendsButton, coupleButton, marriedButton,
                complicatedButton, secretButton);

        successBox.getChildren().addAll(successTitle, questionLabel, funFact, buttonContainer);

        Scene successScene = new Scene(successBox, 550, 450);
        Stage stage = (Stage) yesButton.getScene().getWindow();
        stage.setScene(successScene);
    }

    private Button createRelationshipButton(String text, String color) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                "-fx-background-radius: 25; -fx-padding: 12 25 12 25; " +
                "-fx-min-width: 280;");

        // Add hover effect
        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: derive(" + color + ", -20%); -fx-text-fill: white; " +
                    "-fx-background-radius: 25; -fx-padding: 12 25 12 25; " +
                    "-fx-min-width: 280; -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
        });

        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                    "-fx-background-radius: 25; -fx-padding: 12 25 12 25; " +
                    "-fx-min-width: 280; -fx-scale-x: 1.0; -fx-scale-y: 1.0;");
        });

        return button;
    }

    private void showFinalMessage(String message, Button sourceButton) {
        VBox finalBox = new VBox(30);
        finalBox.setAlignment(Pos.CENTER);
        finalBox.setPadding(new Insets(60));
        finalBox.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e);");

        Label heartTitle = new Label("💖 PERFECT! 💖");
        heartTitle.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        heartTitle.setTextFill(Color.web("#ff6b9d"));

        Label finalMessage = new Label(message);
        finalMessage.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        finalMessage.setTextFill(Color.web("#c7ecee"));
        finalMessage.setAlignment(Pos.CENTER);
        finalMessage.setWrapText(true);
        finalMessage.setMaxWidth(400);

        Label closingMessage = new Label("Thanks for playing along! 🎮✨");
        closingMessage.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        closingMessage.setTextFill(Color.web("#a8e6cf"));

        finalBox.getChildren().addAll(heartTitle, finalMessage, closingMessage);

        Scene finalScene = new Scene(finalBox, 550, 350);
        Stage stage = (Stage) sourceButton.getScene().getWindow();
        stage.setScene(finalScene);
    }



    public static void main(String[] args) {
        launch(args);
    }
}