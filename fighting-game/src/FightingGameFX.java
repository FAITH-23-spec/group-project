import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.Random;

public class FightingGameFX extends Application {
    private static final int INITIAL_HEALTH = 10;
    private static final int DAMAGE = 2; // Set damage to 2

    private Player humanPlayer;
    private ComputerPlayer computerPlayer;
    private boolean isHumanTurn;

    private ProgressBar humanHealthBar, computerHealthBar;
    private Label humanHealthLabel, computerHealthLabel;
    private TextArea gameLog;
    private VBox actionButtons;
    private ImageView humanImageView, computerImageView;
    private Label turnIndicator;

    private Timeline attackAnimation;
    private TranslateTransition humanAttackTransition;
    private TranslateTransition computerAttackTransition;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        humanPlayer = new Player("Player", INITIAL_HEALTH);
        computerPlayer = new ComputerPlayer("Computer", INITIAL_HEALTH);
        isHumanTurn = true; // Start with the human player's turn
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            createUI(primaryStage);
            updateUI();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void createUI(Stage stage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a2a6c, #b21f1f, #fdbb2d);");

        HBox gameArea = new HBox(50);
        gameArea.setAlignment(Pos.CENTER);
        gameArea.setPadding(new Insets(20));

        VBox humanArea = createPlayerArea(humanPlayer, true);
        Label vsLabel = new Label("VS");
        vsLabel.setStyle("-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: white;");
        VBox computerArea = createPlayerArea(computerPlayer, false);

        gameArea.getChildren().addAll(humanArea, vsLabel, computerArea);

        gameLog = new TextArea();
        gameLog.setEditable(false);
        gameLog.setWrapText(true);
        gameLog.setStyle("-fx-font-family: monospace; -fx-font-size: 14;");
        gameLog.setPrefHeight(150);

        turnIndicator = new Label();
        turnIndicator.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(turnIndicator, gameArea, createActionButtons(), gameLog);

        root.getChildren().add(mainLayout);

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("JavaFX Fighting Game");
        stage.setScene(scene);
        stage.show();

        setupAnimations();
    }

    private VBox createPlayerArea(Player player, boolean isHuman) {
        VBox playerArea = new VBox(10);
        playerArea.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(player.getName());
        nameLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white;");

        ProgressBar healthBar = new ProgressBar(1.0);
        healthBar.setPrefWidth(200);
        healthBar.setStyle("-fx-accent: " + (isHuman ? "green" : "red") + ";");

        Label healthLabel = new Label(player.getHealth() + "/" + INITIAL_HEALTH);
        healthLabel.setStyle("-fx-font-size: 16; -fx-text-fill: white;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setImage(createCharacterImage(isHuman));

        if (isHuman) {
            humanHealthBar = healthBar;
            humanHealthLabel = healthLabel;
            humanImageView = imageView;
        } else {
            computerHealthBar = healthBar;
            computerHealthLabel = healthLabel;
            computerImageView = imageView;
        }

        playerArea.getChildren().addAll(nameLabel, imageView, healthBar, healthLabel);
        return playerArea;
    }

    private Image createCharacterImage(boolean isHuman) {
        int width = 150;
        int height = 150;

        Group group = new Group();
        Circle head = new Circle((double) width / 2, (double) height / 2, (double) width / 2 - 10);
        head.setFill(isHuman ? Color.BLUE : Color.RED);

        Circle leftEye = new Circle((double) width / 2 - 20, (double) height / 2 - 15, 10);
        Circle rightEye = new Circle((double) width / 2 + 20, (double) height / 2 - 15, 10);
        leftEye.setFill(Color.WHITE);
        rightEye.setFill(Color.WHITE);

        Arc mouth = new Arc((double) width / 2, (double) height / 2 + 20, 30, 20, 0, -180);
        mouth.setType(ArcType.OPEN);
        mouth.setStroke(Color.WHITE);
        mouth.setStrokeWidth(3);
        mouth.setFill(null);

        group.getChildren().addAll(head, leftEye, rightEye, mouth);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return group.snapshot(params, null);
    }

    private HBox createActionButtons() {
        actionButtons = new VBox(10);
        actionButtons.setAlignment(Pos.CENTER);
        setupHumanTurnButtons(); // Set up buttons for human player

        HBox container = new HBox(actionButtons);
        container.setAlignment(Pos.CENTER);
        return container;
    }

    private void setupHumanTurnButtons() {
        actionButtons.getChildren().clear();

        Label instruction = new Label("Choose your action:");
        instruction.setStyle("-fx-font-size: 18; -fx-text-fill: white;");

        HBox actionButtonsContainer = new HBox(10);
        actionButtonsContainer.setAlignment(Pos.CENTER);

        // Attack buttons
        Button kickBtn = createActionButton("Kick", "#FF9800");
        Button punchBtn = createActionButton("Punch", "#FF5722");
        Button slapBtn = createActionButton("Slap", "#E91E63");

        kickBtn.setOnAction(_ -> performHumanAction(1, "kick"));
        punchBtn.setOnAction(_ -> performHumanAction(2, "punch"));
        slapBtn.setOnAction(_ -> performHumanAction(3, "slap"));

        actionButtonsContainer.getChildren().addAll(kickBtn, punchBtn, slapBtn);

        // Defend buttons
        Button jumpBtn = createActionButton("Jump", "#4CAF50");
        Button blockBtn = createActionButton("Block", "#2196F3");
        Button bendBtn = createActionButton("Bend", "#9C27B0");

        jumpBtn.setOnAction(_ -> performHumanDefense(1, 0)); // Placeholder for attack choice
        blockBtn.setOnAction(e -> performHumanDefense(2, 0)); // Placeholder for attack choice
        bendBtn.setOnAction(e -> performHumanDefense(3, 0)); // Placeholder for attack choice

        actionButtonsContainer.getChildren().addAll(jumpBtn, blockBtn, bendBtn);

        actionButtons.getChildren().addAll(instruction, actionButtonsContainer);
    }

    private Button createActionButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-background-color: " + color +
                "; -fx-text-fill: white; -fx-padding: 10 20;");
        button.setEffect(new DropShadow(5, Color.BLACK));
        return button;
    }

    private void performHumanAction(int choice, String action) {
        logAction(humanPlayer.getName() + " used " + action + "!");
        isHumanTurn = false; // Switch to computer's turn

        // Perform computer's action immediately
        performComputerAction();
    }

    private void performComputerAction() {
        int attack = computerPlayer.attack();
        logAction(computerPlayer.getName() + " attacks with " + getAttackName(attack));

        // Present defense options to the human player
        setupDefenseOptions(attack);
    }

    private void setupDefenseOptions(int attackChoice) {
        actionButtons.getChildren().clear();
        Label instruction = new Label("Choose your defense:");
        instruction.setStyle("-fx-font-size: 18; -fx-text-fill: white;");
        actionButtons.getChildren().add(instruction);

        // Defense buttons
        Button jumpBtn = createActionButton("Jump", "#4CAF50");
        Button blockBtn = createActionButton("Block", "#2196F3");
        Button bendBtn = createActionButton("Bend", "#9C27B0");

        jumpBtn.setOnAction(e -> performHumanDefense(1, attackChoice));
        blockBtn.setOnAction(e -> performHumanDefense(2, attackChoice));
        bendBtn.setOnAction(e -> performHumanDefense(3, attackChoice));

        actionButtons.getChildren().addAll(jumpBtn, blockBtn, bendBtn);
    }

    private void performHumanDefense(int choice, int attackChoice) {
        logAction(humanPlayer.getName() + " defends with " + getDefenseName(choice) + "!");

        if (attackChoice != choice) {
            computerPlayer.takeDamage(DAMAGE); // Reduce computer's health
            logAction("Attack hits! " + computerPlayer.getName() + " loses " + DAMAGE + " health!");
        } else {
            logAction("Defense succeeded! Attack was blocked.");
        }

        // Update UI after damage is taken
        updateUI();

        isHumanTurn = true; // Switch back to human's turn

        if (computerPlayer.getHealth() <= 0) {
            endGame();
        } else {
            setupHumanTurnButtons(); // Prepare for the next human turn
        }
    }

    private void setupAnimations() {
        attackAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    if (isHumanTurn) {
                        humanImageView.setScaleX(1.2);
                        humanImageView.setScaleY(1.2);
                    } else {
                        computerImageView.setScaleX(1.2);
                        computerImageView.setScaleY(1.2);
                    }
                }),
                new KeyFrame(Duration.millis(200), e -> {
                    if (isHumanTurn) {
                        humanImageView.setScaleX(1.0);
                        humanImageView.setScaleY(1.0);
                    } else {
                        computerImageView.setScaleX(1.0);
                        computerImageView.setScaleY(1.0);
                    }
                })
        );

        humanAttackTransition = new TranslateTransition(Duration.millis(200), humanImageView);
        humanAttackTransition.setByX(50);
        humanAttackTransition.setAutoReverse(true);
        humanAttackTransition.setCycleCount(2);

        computerAttackTransition = new TranslateTransition(Duration.millis(200), computerImageView);
        computerAttackTransition.setByX(-50);
        computerAttackTransition.setAutoReverse(true);
        computerAttackTransition.setCycleCount(2);
    }

    private void animateAttack(boolean isHumanAttacking) {
        if (isHumanAttacking) {
            humanAttackTransition.play();
        } else {
            computerAttackTransition.play();
        }
        attackAnimation.play();
    }

    private void updateUI() {
        humanHealthBar.setProgress((double) humanPlayer.getHealth() / INITIAL_HEALTH);
        computerHealthBar.setProgress((double) computerPlayer.getHealth() / INITIAL_HEALTH);

        humanHealthLabel.setText(humanPlayer.getHealth() + "/" + INITIAL_HEALTH);
        computerHealthLabel.setText(computerPlayer.getHealth() + "/" + INITIAL_HEALTH);

        turnIndicator.setText(isHumanTurn ? "Your Turn!" : "Computer's Turn");
    }

    private void logAction(String message) {
        gameLog.appendText("> " + message + "\n");
        gameLog.setScrollTop(Double.MAX_VALUE);
    }

    private void endGame() {
        actionButtons.getChildren().clear();

        String result;
        if (humanPlayer.getHealth() <= 0 && computerPlayer.getHealth() <= 0) {
            result = "It's a draw!";
        } else if (humanPlayer.getHealth() > computerPlayer.getHealth()) {
            result = humanPlayer.getName() + " wins!";
        } else {
            result = computerPlayer.getName() + " wins!";
        }

        Label resultLabel = new Label(result);
        resultLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: gold;");

        Button restartButton = createActionButton("Play Again", "#4CAF50");
        restartButton.setOnAction(e -> {
            init(); // Reinitialize game state
            updateUI();
            setupHumanTurnButtons();
            gameLog.clear();
        });

        actionButtons.getChildren().addAll(resultLabel, restartButton);
    }

    private String getAttackName(int choice) {
        return switch (choice) {
            case 1 -> "kick";
            case 2 -> "punch";
            case 3 -> "slap";
            default -> "unknown attack";
        };
    }

    private String getDefenseName(int choice) {
        return switch (choice) {
            case 1 -> "jump";
            case 2 -> "block";
            case 3 -> "bend";
            default -> "unknown defense";
        };
    }

    private static class Player {
        private final String name;
        private int health;

        public Player(String name, int health) {
            this.name = name;
            this.health = health;
        }

        public String getName() {
            return name;
        }

        public int getHealth() {
            return health;
        }

        public void takeDamage(int amount) {
            health = Math.max(0, health - amount);
        }
    }

    private static class ComputerPlayer extends Player {
        private final Random random = new Random();

        public ComputerPlayer(String name, int health) {
            super(name, health);
        }

        public int attack() {
            return random.nextInt(3) + 1;
        }

        public int defend() {
            return random.nextInt(3) + 1;
        }
    }
}