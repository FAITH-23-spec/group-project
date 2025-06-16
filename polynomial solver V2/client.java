import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class client extends Application {

    private TextField degreeField = new TextField();
    private VBox coeffInputs = new VBox(5);
    private TextArea outputArea = new TextArea();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Polynomial Solver Client");

        // Degree input
        Label degreeLabel = new Label("Degree of Polynomial:");
        degreeField.setPromptText("Enter degree (e.g., 2)");

        Button setDegreeBtn = new Button("Set Degree");
        setDegreeBtn.setOnAction(e -> setupCoeffInputs());

        HBox degreeBox = new HBox(10, degreeLabel, degreeField, setDegreeBtn);

        // Submit button
        Button submitBtn = new Button("Submit to Server");
        submitBtn.setOnAction(e -> submitToServer());

        outputArea.setEditable(false);
        outputArea.setPrefHeight(150);

        VBox root = new VBox(15, degreeBox, coeffInputs, submitBtn, new Label("Roots:"), outputArea);
        root.setPadding(new Insets(15));

        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupCoeffInputs() {
        coeffInputs.getChildren().clear();
        try {
            int degree = Integer.parseInt(degreeField.getText());
            for (int i = degree; i >= 0; i--) {
                TextField coeffField = new TextField();
                coeffField.setPromptText("Coefficient for x^" + i);
                coeffInputs.getChildren().add(coeffField);
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid input", "Please enter a valid integer degree.");
        }
    }

    private void submitToServer() {
        try {
            int degree = Integer.parseInt(degreeField.getText());
            double[] coeffs = new double[degree + 1];
            for (int i = 0; i <= degree; i++) {
                TextField tf = (TextField) coeffInputs.getChildren().get(i);
                coeffs[i] = Double.parseDouble(tf.getText());
            }

            try (Socket socket = new Socket("localhost", 5000);
                 DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                 DataInputStream input = new DataInputStream(socket.getInputStream())) {

                output.writeInt(degree);
                for (double c : coeffs) {
                    output.writeDouble(c);
                }

                int rootCount = input.readInt();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < rootCount; i++) {
                    double re = input.readDouble();
                    double im = input.readDouble();
                    sb.append(String.format("Root %d: %.5f %s %.5fi%n",
                            i + 1, re, (im >= 0 ? "+" : "-"), Math.abs(im)));
                }
                outputArea.setText(sb.toString());
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid input", "Please enter valid numbers for degree and coefficients.");
        } catch (IOException e) {
            showAlert("Connection error", "Failed to connect to the server.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
