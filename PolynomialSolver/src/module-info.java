module PolynomialSolver {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    
    opens com.polynomialsolver to javafx.fxml;
    exports com.polynomialsolver;
} 