package com.polynomialsolver;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import com.polynomialsolver.database.DatabaseConnection;
import com.polynomialsolver.database.TableInitializer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PolynomialSolverApp extends Application {

    private TextField polynomialInput;
    private TextFlow solutionDisplay;
    private TextFlow stepsDisplay;
    private Connection connection;
    private static final String DARK_BG = "#1E1E1E";
    private static final String DARK_FG = "#FFFFFF";
    private static final String ACCENT_COLOR = "#4CAF50";
    private static final String HIGHLIGHT_COLOR = "#2196F3";
    private static final String SECONDARY_BG = "#2D2D2D";

    @Override
    public void start(Stage primaryStage) {
        try {
            connection = DatabaseConnection.getConnection();
            TableInitializer.initializeTable();
        } catch (SQLException e) {
            showError("Database Connection Error", e.getMessage());
        }

        primaryStage.setTitle("Polynomial Solver");

        // Create main container with dark background
        VBox mainContainer = new VBox(20);
        mainContainer.setStyle("-fx-background-color: " + DARK_BG + ";");
        mainContainer.setPadding(new Insets(20));

        // Title with subtitle
        VBox titleBox = new VBox(5);
        Label titleLabel = new Label("Polynomial Solver");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web(DARK_FG));
        
        Label subtitleLabel = new Label("Solve quadratic and linear equations");
        subtitleLabel.setFont(Font.font("System", 14));
        subtitleLabel.setTextFill(Color.web("#AAAAAA"));
        
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        titleBox.setAlignment(javafx.geometry.Pos.CENTER);

        // Input section with card-like appearance
        VBox inputSection = new VBox(15);
        inputSection.setStyle("-fx-background-color: " + SECONDARY_BG + "; -fx-padding: 20px; -fx-background-radius: 10px;");
        
        Label inputLabel = new Label("Enter Polynomial");
        inputLabel.setTextFill(Color.web(DARK_FG));
        inputLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        polynomialInput = new TextField();
        polynomialInput.setStyle("-fx-background-color: " + DARK_BG + "; -fx-text-fill: " + DARK_FG + 
                               "; -fx-font-size: 14px; -fx-padding: 10px; -fx-background-radius: 5px;");
        polynomialInput.setPromptText("Example: 2x^2 + 3x - 1");
        polynomialInput.setPrefHeight(40);

        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Example button
        Button exampleButton = new Button("Load Example");
        exampleButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; " +
                             "-fx-padding: 10px 20px; -fx-background-radius: 5px;");
        exampleButton.setPrefHeight(40);
        exampleButton.setOnAction(e -> polynomialInput.setText("2x^2 + 3x - 1"));

        // Solve button
        Button solveButton = new Button("Solve");
        solveButton.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10px 30px; " +
                           "-fx-background-radius: 5px;");
        solveButton.setPrefHeight(40);
        solveButton.setPrefWidth(120);

        buttonBox.getChildren().addAll(exampleButton, solveButton);

        inputSection.getChildren().addAll(inputLabel, polynomialInput, buttonBox);

        // Solution display section with card-like appearance
        VBox solutionSection = new VBox(15);
        solutionSection.setStyle("-fx-background-color: " + SECONDARY_BG + "; -fx-padding: 20px; -fx-background-radius: 10px;");
        
        // Steps display
        Label stepsLabel = new Label("Solution Steps");
        stepsLabel.setTextFill(Color.web(DARK_FG));
        stepsLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        stepsDisplay = new TextFlow();
        stepsDisplay.setStyle("-fx-background-color: " + DARK_BG + "; -fx-padding: 15px; -fx-background-radius: 5px;");
        stepsDisplay.setPrefHeight(150);
        
        // Add ScrollPane for steps
        ScrollPane stepsScrollPane = new ScrollPane(stepsDisplay);
        stepsScrollPane.setFitToWidth(true);
        stepsScrollPane.setStyle("-fx-background-color: " + DARK_BG + ";");
        stepsScrollPane.setPrefHeight(200);
        stepsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        stepsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        // Final solution display
        Label solutionLabel = new Label("Final Solution");
        solutionLabel.setTextFill(Color.web(DARK_FG));
        solutionLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        solutionDisplay = new TextFlow();
        solutionDisplay.setStyle("-fx-background-color: " + DARK_BG + "; -fx-padding: 15px; -fx-background-radius: 5px;");
        solutionDisplay.setPrefHeight(80);

        solutionSection.getChildren().addAll(stepsLabel, stepsScrollPane, solutionLabel, solutionDisplay);

        // Add sections to main container
        mainContainer.getChildren().addAll(titleBox, inputSection, solutionSection);

        // Set up the solve button action
        solveButton.setOnAction(e -> {
            String polynomial = polynomialInput.getText().trim();
            if (!polynomial.isEmpty()) {
                try {
                    // Clean up the polynomial string
                    polynomial = polynomial.replaceAll("\\s+", " ").trim();
                    
                    // Validate polynomial format
                    if (!isValidPolynomial(polynomial)) {
                        showError("Input Error", "Invalid polynomial format. Please use format like: 2x^2 + 3x - 1");
                        return;
                    }

                    // Solve the polynomial and get the solution
                    String solution = solvePolynomialSteps(polynomial);
                    
                    // Update the displays
                    Platform.runLater(() -> {
                        // Clear previous displays
                        stepsDisplay.getChildren().clear();
                        solutionDisplay.getChildren().clear();
                        
                        // Add the steps to the display
                        String[] steps = solution.split("\n");
                        for (String step : steps) {
                            Text stepText = new Text(step + "\n");
                            stepText.setFill(Color.web(DARK_FG));
                            stepText.setFont(Font.font("System", 14));
                            stepsDisplay.getChildren().add(stepText);
                        }
                        
                        // Add the final solution to the display
                        Text solutionText = new Text(extractFinalSolution(solution));
                        solutionText.setFill(Color.web(HIGHLIGHT_COLOR));
                        solutionText.setFont(Font.font("System", FontWeight.BOLD, 18));
                        solutionDisplay.getChildren().add(solutionText);
                    });

                    // Save to database
                    try {
                        saveToDatabase(polynomial, solution);
                    } catch (SQLException ex) {
                        showError("Database Error", "Solution was computed but failed to save to database: " + ex.getMessage());
                    }
                } catch (Exception ex) {
                    showError("Solving Error", "Error: " + ex.getMessage());
                    Platform.runLater(() -> {
                        stepsDisplay.getChildren().clear();
                        solutionDisplay.getChildren().clear();
                        Text errorText = new Text("Error occurred while solving the polynomial.");
                        errorText.setFill(Color.RED);
                        errorText.setFont(Font.font("System", FontWeight.BOLD, 14));
                        solutionDisplay.getChildren().add(errorText);
                    });
                }
            } else {
                showError("Input Error", "Please enter a polynomial");
            }
        });

        Scene scene = new Scene(mainContainer, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private String extractFinalSolution(String fullSolution) {
        // Extract the final solution from the detailed steps
        String[] lines = fullSolution.split("\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            if (lines[i].contains("x₁ =") || lines[i].contains("x =")) {
                return "Solution: " + lines[i].trim();
            }
        }
        return "Solution: " + lines[lines.length - 1].trim();
    }

    private boolean isValidPolynomial(String polynomial) {
        // Basic validation for polynomial format
        String pattern = "^([+-]?\\d*\\.?\\d*x\\^\\d+|[+-]?\\d*\\.?\\d*x|[+-]?\\d+\\.?\\d*)(\\s*[+-]\\s*([+-]?\\d*\\.?\\d*x\\^\\d+|[+-]?\\d*\\.?\\d*x|[+-]?\\d+\\.?\\d*))*$";
        return polynomial.matches(pattern);
    }

    private String solvePolynomialSteps(String polynomial) {
        StringBuilder steps = new StringBuilder();
        steps.append("Solving: ").append(polynomial).append("\n\n");

        try {
            // Parse coefficients
            List<Double> coefficients = parseCoefficients(polynomial);
            int degree = coefficients.size() - 1;

            steps.append("1. Standard form: ").append(formatPolynomial(coefficients)).append("\n");
            steps.append("2. Degree of polynomial: ").append(degree).append("\n");
            steps.append("3. Coefficients: ").append(coefficients).append("\n\n");

            if (degree == 2) {
                // Quadratic equation
                double a = coefficients.get(2);
                double b = coefficients.get(1);
                double c = coefficients.get(0);

                if (Math.abs(a) < 1e-10) {
                    throw new IllegalArgumentException("Not a quadratic equation (a ≈ 0)");
                }

                steps.append("4. Using quadratic formula: x = (-b ± √(b² - 4ac)) / 2a\n");
                steps.append("   where a = ").append(String.format("%.4f", a))
                     .append(", b = ").append(String.format("%.4f", b))
                     .append(", c = ").append(String.format("%.4f", c)).append("\n");

                double discriminant = b * b - 4 * a * c;
                steps.append("5. Discriminant = ").append(String.format("%.4f", discriminant)).append("\n");

                if (discriminant > 0) {
                    double x1 = (-b + Math.sqrt(discriminant)) / (2 * a);
                    double x2 = (-b - Math.sqrt(discriminant)) / (2 * a);
                    steps.append("\n6. Two real solutions:\n");
                    steps.append("   x₁ = ").append(String.format("%.4f", x1)).append("\n");
                    steps.append("   x₂ = ").append(String.format("%.4f", x2)).append("\n");
                } else if (Math.abs(discriminant) < 1e-10) {
                    double x = -b / (2 * a);
                    steps.append("\n6. One real solution (double root):\n");
                    steps.append("   x = ").append(String.format("%.4f", x)).append("\n");
                } else {
                    double realPart = -b / (2 * a);
                    double imaginaryPart = Math.sqrt(-discriminant) / (2 * a);
                    steps.append("\n6. Two complex solutions:\n");
                    steps.append("   x₁ = ").append(String.format("%.4f", realPart))
                         .append(" + ").append(String.format("%.4f", imaginaryPart)).append("i\n");
                    steps.append("   x₂ = ").append(String.format("%.4f", realPart))
                         .append(" - ").append(String.format("%.4f", imaginaryPart)).append("i\n");
                }
            } else if (degree == 1) {
                // Linear equation
                double a = coefficients.get(1);
                double b = coefficients.get(0);

                if (Math.abs(a) < 1e-10) {
                    throw new IllegalArgumentException("Not a linear equation (a ≈ 0)");
                }

                double x = -b / a;
                steps.append("4. Linear equation: ax + b = 0\n");
                steps.append("5. Solution: x = -b/a\n");
                steps.append("6. x = ").append(String.format("%.4f", x)).append("\n");
            } else {
                steps.append("4. This polynomial is of degree ").append(degree).append("\n");
                steps.append("5. For polynomials of degree > 2, numerical methods are required.\n");
                steps.append("   Consider using Newton's method or other numerical approaches.\n");
            }
        } catch (Exception e) {
            steps.append("\nError during solving: ").append(e.getMessage());
        }

        return steps.toString();
    }

    private List<Double> parseCoefficients(String polynomial) {
        List<Double> coefficients = new ArrayList<>();
        // First, normalize the polynomial string
        polynomial = polynomial.replaceAll("\\s+", "").toLowerCase();
        
        // Handle special cases
        if (polynomial.startsWith("+")) {
            polynomial = polynomial.substring(1);
        }
        
        // Split the polynomial into terms
        String[] terms = polynomial.split("(?=[+-])");
        
        int maxDegree = 0;
        for (String term : terms) {
            if (term.isEmpty()) continue;
            
            double coefficient;
            int degree;
            
            if (term.contains("x^")) {
                // Handle terms with exponents
                String[] parts = term.split("x\\^");
                coefficient = parts[0].isEmpty() || parts[0].equals("+") ? 1.0 : 
                            parts[0].equals("-") ? -1.0 : Double.parseDouble(parts[0]);
                degree = Integer.parseInt(parts[1]);
            } else if (term.contains("x")) {
                // Handle linear terms
                String coef = term.replace("x", "");
                coefficient = coef.isEmpty() || coef.equals("+") ? 1.0 : 
                            coef.equals("-") ? -1.0 : Double.parseDouble(coef);
                degree = 1;
            } else {
                // Handle constant terms
                coefficient = Double.parseDouble(term);
                degree = 0;
            }
            
            maxDegree = Math.max(maxDegree, degree);
            
            // Ensure the coefficients list is large enough
            while (coefficients.size() <= degree) {
                coefficients.add(0.0);
            }
            
            coefficients.set(degree, coefficient);
        }
        
        return coefficients;
    }

    private String formatPolynomial(List<Double> coefficients) {
        StringBuilder result = new StringBuilder();
        boolean firstTerm = true;
        
        for (int i = coefficients.size() - 1; i >= 0; i--) {
            double coef = coefficients.get(i);
            if (Math.abs(coef) > 1e-10) {  // Check if coefficient is not effectively zero
                if (!firstTerm) {
                    result.append(coef > 0 ? " + " : " - ");
                    coef = Math.abs(coef);
                } else {
                    firstTerm = false;
                    if (coef < 0) {
                        result.append("-");
                        coef = -coef;
                    }
                }
                
                if (i == 0) {
                    result.append(String.format("%.4f", coef));
                } else if (i == 1) {
                    if (Math.abs(coef - 1.0) < 1e-10) {
                        result.append("x");
                    } else {
                        result.append(String.format("%.4f", coef)).append("x");
                    }
                } else {
                    if (Math.abs(coef - 1.0) < 1e-10) {
                        result.append("x^").append(i);
                    } else {
                        result.append(String.format("%.4f", coef)).append("x^").append(i);
                    }
                }
            }
        }
        
        if (result.length() == 0) {
            return "0";
        }
        
        return result.toString();
    }

    private void saveToDatabase(String polynomial, String solution) throws SQLException {
        String insertSQL = "INSERT INTO solved_polynomials (polynomial, solution, degree, method_used) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertSQL)) {
            statement.setString(1, polynomial);
            statement.setString(2, solution);
            statement.setInt(3, parseCoefficients(polynomial).size() - 1);
            statement.setString(4, "algebraic");
            statement.executeUpdate();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + DARK_BG + ";");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: " + DARK_FG + ";");
        
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}