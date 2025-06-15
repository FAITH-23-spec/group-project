import java.sql.*;

public class DatabaseConnector {
    private static final String URL = "jdbc:mysql://localhost:3306/group_chat";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            // Create tables if they don't exist
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "color VARCHAR(7) NOT NULL, " +
                    "last_online TIMESTAMP)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS messages (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "sender VARCHAR(50) NOT NULL, " +
                    "content TEXT NOT NULL, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (sender) REFERENCES users(username))");

            // Initialize users if table is empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            if (rs.getInt(1) == 0) {
                String[] members = {"FAITH", "BLESSING", "STYVE", "TERRY"};
                String[] passwords = {"faith2024", "blessing2024", "styve2024", "terry2024"};
                String[] colors = {"#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4"};

                PreparedStatement insertUser = conn.prepareStatement(
                        "INSERT INTO users (username, password, color) VALUES (?, ?, ?)");

                for (int i = 0; i < members.length; i++) {
                    insertUser.setString(1, members[i]);
                    insertUser.setString(2, passwords[i]);
                    insertUser.setString(3, colors[i]);
                    insertUser.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}