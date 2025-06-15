import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.Random;

public class FightingGameFX extends Application {
    private static final int INITIAL_HEALTH = 10;
    private static final int BASE_DAMAGE = 2;

    private Player humanPlayer;
    private ComputerPlayer computerPlayer;
    private boolean isHumanTurn;

    private ProgressBar humanHealthBar, computerHealthBar;
    private Label humanHealthLabel, computerHealthLabel;
    private Label humanNameLabel, computerNameLabel;
    private TextArea gameLog;
    private VBox actionButtons;
    private ImageView humanImageView, computerImageView;
    private Label turnIndicator;
    private TextField nameInput;
    private Button startGameButton;

    private Timeline attackAnimation;
    private TranslateTransition humanAttackTransition;
    private TranslateTransition computerAttackTransition;
    private FadeTransition hitEffect;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        // Initialization moved to startGame()
    }

    @Override
    public void start(Stage primaryStage) {
        createStartScreen(primaryStage);
    }

    private void createStartScreen(Stage stage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #0f2027, #203a43, #2c5364);");

        VBox startScreen = new VBox(30);
        startScreen.setAlignment(Pos.CENTER);
        startScreen.setPadding(new Insets(50));

        Label title = new Label("FIGHTING GAME");
        title.setStyle("-fx-font-size: 48; -fx-font-weight: bold; -fx-text-fill: white;");
        title.setEffect(new DropShadow(10, Color.ORANGERED));

        HBox nameInputBox = new HBox(10);
        nameInputBox.setAlignment(Pos.CENTER);
        Label nameLabel = new Label("Enter your name:");
        nameLabel.setStyle("-fx-font-size: 18; -fx-text-fill: white;");
        nameInput = new TextField();
        nameInput.setPromptText("Player Name");
        nameInput.setStyle("-fx-font-size: 16; -fx-pref-width: 200;");
        nameInputBox.getChildren().addAll(nameLabel, nameInput);

        startGameButton = new Button("START GAME");
        startGameButton.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-background-color: #4CAF50; " +
                "-fx-text-fill: white; -fx-padding: 15 30;");
        startGameButton.setEffect(new Glow(0.5));
        startGameButton.setOnAction(e -> startGame(stage));

        startScreen.getChildren().addAll(title, nameInputBox, startGameButton);
        root.getChildren().add(startScreen);

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("JavaFX Fighting Game");
        stage.setScene(scene);
        stage.show();
    }

    private void startGame(Stage stage) {
        String playerName = nameInput.getText().trim();
        if (playerName.isEmpty()) {
            playerName = "Player";
        }

        humanPlayer = new Player(playerName, INITIAL_HEALTH);
        computerPlayer = new ComputerPlayer("Computer", INITIAL_HEALTH);
        isHumanTurn = true;

        createMainUI(stage);
        updateUI();
    }

    private void createMainUI(Stage stage) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a2a6c, #b21f1f, #fdbb2d);");

        HBox gameArea = new HBox(50);
        gameArea.setAlignment(Pos.CENTER);
        gameArea.setPadding(new Insets(20));

        VBox humanArea = createPlayerArea(humanPlayer, true);
        Label vsLabel = new Label("VS");
        vsLabel.setStyle("-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: white;");
        vsLabel.setEffect(new InnerShadow(10, Color.BLACK));
        VBox computerArea = createPlayerArea(computerPlayer, false);

        gameArea.getChildren().addAll(humanArea, vsLabel, computerArea);

        gameLog = new TextArea();
        gameLog.setEditable(false);
        gameLog.setWrapText(true);
        gameLog.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14; -fx-background-color: rgba(0,0,0,0.3); " +
                "-fx-text-fill: white; -fx-border-color: gold; -fx-border-width: 2;");
        gameLog.setPrefHeight(150);

        turnIndicator = new Label();
        turnIndicator.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: white;");
        turnIndicator.setEffect(new DropShadow(5, Color.BLACK));

        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(turnIndicator, gameArea, createActionButtons(), gameLog);

        root.getChildren().add(mainLayout);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);

        setupAnimations();
    }

    private VBox createPlayerArea(Player player, boolean isHuman) {
        VBox playerArea = new VBox(10);
        playerArea.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(player.getName());
        nameLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white;");
        nameLabel.setEffect(new DropShadow(3, Color.BLACK));

        if (isHuman) {
            humanNameLabel = nameLabel;
        } else {
            computerNameLabel = nameLabel;
        }

        ImageView imageView = new ImageView();
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageView.setImage(createCharacterImage(isHuman));

        ProgressBar healthBar = new ProgressBar(1.0);
        healthBar.setPrefWidth(250);
        healthBar.setStyle("-fx-accent: " + (isHuman ? "#4CAF50" : "#F44336") + "; -fx-border-color: white; -fx-border-width: 1;");

        Label healthLabel = new Label(player.getHealth() + "/" + INITIAL_HEALTH);
        healthLabel.setStyle("-fx-font-size: 16; -fx-text-fill: white;");

        if (isHuman) {
            humanHealthBar = healthBar;
            humanHealthLabel = healthLabel;
            humanImageView = imageView;
        } else {
            computerHealthBar = healthBar;
            computerHealthLabel = healthLabel;
            computerImageView = imageView;
        }

        VBox statsBox = new VBox(5);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.getChildren().addAll(healthBar, healthLabel);

        playerArea.getChildren().addAll(nameLabel, imageView, statsBox);
        return playerArea;
    }

    private Image createCharacterImage(boolean isHuman) {
        int width = 200;
        int height = 200;

        Group group = new Group();

        // Body
        Ellipse body = new Ellipse(width/2, height/2 + 30, width/3, height/3);
        body.setFill(isHuman ? Color.DODGERBLUE : Color.CRIMSON);
        body.setEffect(new DropShadow(10, Color.BLACK));

        // Head
        Circle head = new Circle(width/2, height/3, width/4);
        head.setFill(isHuman ? Color.SKYBLUE : Color.INDIANRED);

        // Eyes
        Circle leftEye = new Circle(width/2 - 20, height/3 - 10, 10);
        Circle rightEye = new Circle(width/2 + 20, height/3 - 10, 10);
        leftEye.setFill(Color.WHITE);
        rightEye.setFill(Color.WHITE);

        // Pupils
        Circle leftPupil = new Circle(width/2 - 20, height/3 - 10, 5);
        Circle rightPupil = new Circle(width/2 + 20, height/3 - 10, 5);
        leftPupil.setFill(Color.BLACK);
        rightPupil.setFill(Color.BLACK);

        // Mouth
        Arc mouth = new Arc(width/2, height/3 + 20, 30, 20, 0, -180);
        mouth.setType(ArcType.OPEN);
        mouth.setStroke(Color.BLACK);
        mouth.setStrokeWidth(3);
        mouth.setFill(null);

        // Arms
        Line leftArm = new Line(width/2 - 50, height/2, width/2 - 100, height/2 + 20);
        Line rightArm = new Line(width/2 + 50, height/2, width/2 + 100, height/2 + 20);
        leftArm.setStrokeWidth(10);
        rightArm.setStrokeWidth(10);
        leftArm.setStroke(isHuman ? Color.SKYBLUE : Color.INDIANRED);
        rightArm.setStroke(isHuman ? Color.SKYBLUE : Color.INDIANRED);

        group.getChildren().addAll(body, head, leftEye, rightEye, leftPupil, rightPupil, mouth, leftArm, rightArm);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return group.snapshot(params, null);
    }

    private HBox createActionButtons() {
        actionButtons = new VBox(10);
        actionButtons.setAlignment(Pos.CENTER);
        setupHumanTurnButtons();

        HBox container = new HBox(actionButtons);
        container.setAlignment(Pos.CENTER);
        return container;
    }

    private void setupHumanTurnButtons() {
        actionButtons.getChildren().clear();

        Label instruction = new Label("Choose your action:");
        instruction.setStyle("-fx-font-size: 18; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox attackButtons = new HBox(10);
        attackButtons.setAlignment(Pos.CENTER);

        // Basic attack buttons
        Button kickBtn = createActionButton("Kick (" + BASE_DAMAGE + " dmg)", "#FF9800");
        Button punchBtn = createActionButton("Punch (" + BASE_DAMAGE + " dmg)", "#FF5722");
        Button slapBtn = createActionButton("Slap (" + BASE_DAMAGE + " dmg)", "#E91E63");

        kickBtn.setOnAction(_ -> performHumanAttack(1, "kick", BASE_DAMAGE));
        punchBtn.setOnAction(_ -> performHumanAttack(2, "punch", BASE_DAMAGE));
        slapBtn.setOnAction(_ -> performHumanAttack(3, "slap", BASE_DAMAGE));

        attackButtons.getChildren().addAll(kickBtn, punchBtn, slapBtn);

        HBox defenseButtons = new HBox(10);
        defenseButtons.setAlignment(Pos.CENTER);

        // Defend buttons
        Button jumpBtn = createActionButton("Jump", "#4CAF50");
        Button blockBtn = createActionButton("Block", "#2196F3");
        Button bendBtn = createActionButton("Bend", "#9C27B0");

        jumpBtn.setOnAction(_ -> performHumanDefense(1, 0, 0));
        blockBtn.setOnAction(e -> performHumanDefense(2, 0, 0));
        bendBtn.setOnAction(e -> performHumanDefense(3, 0, 0));

        defenseButtons.getChildren().addAll(jumpBtn, blockBtn, bendBtn);

        actionButtons.getChildren().addAll(instruction, attackButtons, defenseButtons);
    }

    private Button createActionButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-background-color: " + color +
                "; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 10;");
        button.setEffect(new DropShadow(5, Color.BLACK));
        return button;
    }

    private void performHumanAttack(int choice, String action, int damage) {
        logAction(humanPlayer.getName() + " used " + action + "!");
        animateAttack(true);

        // Computer chooses defense
        int computerDefense = computerPlayer.defend();
        logAction(computerPlayer.getName() + " tries to defend with " + getDefenseName(computerDefense));

        if (computerDefense != choice) {
            computerPlayer.takeDamage(damage);
            logAction("Attack hits! " + computerPlayer.getName() + " loses " + damage + " health!");
            animateHit(false);
        } else {
            logAction("Defense succeeded! Attack was blocked.");
        }

        updateUI();

        if (computerPlayer.getHealth() <= 0) {
            endGame();
        } else {
            isHumanTurn = false;
            performComputerAction();
        }
    }

    private void performComputerAction() {
        turnIndicator.setText("Computer's Turn");

        // Computer chooses attack
        int computerAttack = computerPlayer.attack();
        String attackName = getAttackName(computerAttack);
        int damage = BASE_DAMAGE;

        logAction(computerPlayer.getName() + " uses " + attackName + "!");
        animateAttack(false);

        // Present defense options to human
        setupDefenseOptions(computerAttack, damage);
    }

    private void setupDefenseOptions(int attackChoice, int damage) {
        actionButtons.getChildren().clear();
        Label instruction = new Label("Defend against " + getAttackName(attackChoice) + "!");
        instruction.setStyle("-fx-font-size: 18; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox defenseButtons = new HBox(10);
        defenseButtons.setAlignment(Pos.CENTER);

        Button jumpBtn = createActionButton("Jump", "#4CAF50");
        Button blockBtn = createActionButton("Block", "#2196F3");
        Button bendBtn = createActionButton("Bend", "#9C27B0");

        jumpBtn.setOnAction(e -> {
            performHumanDefense(1, attackChoice, damage);
            animateHit(true);
        });
        blockBtn.setOnAction(e -> {
            performHumanDefense(2, attackChoice, damage);
            animateHit(true);
        });
        bendBtn.setOnAction(e -> {
            performHumanDefense(3, attackChoice, damage);
            animateHit(true);
        });

        defenseButtons.getChildren().addAll(jumpBtn, blockBtn, bendBtn);
        actionButtons.getChildren().addAll(instruction, defenseButtons);
    }

    private void performHumanDefense(int choice, int attackChoice, int damage) {
        logAction(humanPlayer.getName() + " defends with " + getDefenseName(choice) + "!");

        if (attackChoice != choice) {
            humanPlayer.takeDamage(damage);
            logAction("Attack hits! " + humanPlayer.getName() + " loses " + damage + " health!");
        } else {
            logAction("Defense succeeded! Attack was blocked.");
        }

        updateUI();

        if (humanPlayer.getHealth() <= 0) {
            endGame();
        } else {
            isHumanTurn = true;
            setupHumanTurnButtons();
        }
    }

    private void setupAnimations() {
        // Attack animation
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
                    humanImageView.setScaleX(1.0);
                    humanImageView.setScaleY(1.0);
                    computerImageView.setScaleX(1.0);
                    computerImageView.setScaleY(1.0);
                })
        );

        // Movement animations
        humanAttackTransition = new TranslateTransition(Duration.millis(200), humanImageView);
        humanAttackTransition.setByX(50);
        humanAttackTransition.setAutoReverse(true);
        humanAttackTransition.setCycleCount(2);

        computerAttackTransition = new TranslateTransition(Duration.millis(200), computerImageView);
        computerAttackTransition.setByX(-50);
        computerAttackTransition.setAutoReverse(true);
        computerAttackTransition.setCycleCount(2);

        // Hit effect
        hitEffect = new FadeTransition(Duration.millis(100));
        hitEffect.setFromValue(1.0);
        hitEffect.setToValue(0.3);
        hitEffect.setAutoReverse(true);
        hitEffect.setCycleCount(4);
    }

    private void animateAttack(boolean isHumanAttacking) {
        if (isHumanAttacking) {
            humanAttackTransition.play();
        } else {
            computerAttackTransition.play();
        }
        attackAnimation.play();
    }

    private void animateHit(boolean isHumanHit) {
        if (isHumanHit) {
            hitEffect.setNode(humanImageView);
        } else {
            hitEffect.setNode(computerImageView);
        }
        hitEffect.play();
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
        resultLabel.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: gold; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

        Button restartButton = createActionButton("Play Again", "#4CAF50");
        restartButton.setStyle("-fx-font-size: 20; -fx-padding: 15 30;");
        restartButton.setOnAction(e -> {
            startGame((Stage) restartButton.getScene().getWindow());
            gameLog.clear();
        });

        VBox endBox = new VBox(20, resultLabel, restartButton);
        endBox.setAlignment(Pos.CENTER);
        actionButtons.getChildren().add(endBox);
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