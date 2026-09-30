import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserDAO.java
 * Handles User registration and authentication using raw JDBC.
 * Demonstrates the Factory Method & Polymorphism:
 * Returns the abstract 'User' type as either a Tenant or Landlord instance.
 */
public class UserDAO {

    /**
     * Register a new user in MySQL.
     */
    public boolean registerUser(String name, String email, String password, String role, String phone) {
        String query = "INSERT INTO Users (name, email, password, role, phone) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, role.toUpperCase());
            pstmt.setString(5, phone != null ? phone : "");

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authenticate user credentials and return polymorphic User object.
     * Runtime Polymorphism in action!
     */
    public User loginUser(String email, String password) {
        String query = "SELECT * FROM Users WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("user_id");
                    String name = rs.getString("name");
                    String userEmail = rs.getString("email");
                    String role = rs.getString("role");
                    String phone = rs.getString("phone");

                    // Dynamic Polymorphic Object Creation based on DB Role
                    if ("TENANT".equalsIgnoreCase(role)) {
                        return new Tenant(id, name, userEmail, phone);
                    } else if ("LANDLORD".equalsIgnoreCase(role)) {
                        return new Landlord(id, name, userEmail, phone);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return null; // Invalid credentials or user not found
    }
}
