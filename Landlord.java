import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Landlord.java
 * Subclass representing a Landlord (Property Owner).
 * Demonstrates Inheritance (extends User), Method Overriding, 
 * and Database Transactions (commit/rollback) for rental approvals.
 */
public class Landlord extends User {

    public Landlord(int userId, String name, String email) {
        super(userId, name, email, "LANDLORD");
    }

    public Landlord(int userId, String name, String email, String phone) {
        super(userId, name, email, "LANDLORD", phone);
    }

    @Override
    public void showDashboard() {
        System.out.println("\n==========================================");
        System.out.println("         LANDLORD DASHBOARD: " + this.name);
        System.out.println("==========================================");
        System.out.println("1. List a New Rental Property");
        System.out.println("2. View My Listed Properties");
        System.out.println("3. View Incoming Tenant Booking Requests");
        System.out.println("4. Approve / Reject Booking Request");
        System.out.println("5. View My Profile");
        System.out.println("6. Logout");
        System.out.println("==========================================");
    }

    // Overloaded helper matching starter notes
    public void addProperty(String title, double price) {
        Property p = new Property(this.userId, title, "Standard rental", "Default City", "Address", price, 2, "");
        if (addProperty(p)) {
            System.out.println("Listing added: " + title + " for Rs. " + price);
        } else {
            System.out.println("Failed to add property.");
        }
    }

    /**
     * Add a new property to MySQL database.
     */
    public boolean addProperty(Property property) {
        String query = 
            "INSERT INTO Properties (landlord_id, title, description, city, address, price_per_month, bedrooms, status, image_url) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, 'AVAILABLE', ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, this.userId);
            pstmt.setString(2, property.getTitle());
            pstmt.setString(3, property.getDescription());
            pstmt.setString(4, property.getCity());
            pstmt.setString(5, property.getAddress());
            pstmt.setDouble(6, property.getPricePerMonth());
            pstmt.setInt(7, property.getBedrooms());
            pstmt.setString(8, property.getImageUrl());

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding property: " + e.getMessage());
            return false;
        }
    }

    /**
     * Fetch all properties owned by this Landlord.
     */
    public List<Property> getMyProperties() {
        List<Property> list = new ArrayList<>();
        String query = "SELECT * FROM Properties WHERE landlord_id = ? ORDER BY property_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, this.userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Property p = new Property(
                        rs.getInt("property_id"),
                        this.userId,
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("city"),
                        rs.getString("address"),
                        rs.getDouble("price_per_month"),
                        rs.getInt("bedrooms"),
                        rs.getString("status"),
                        rs.getString("image_url")
                    );
                    p.setLandlordName(this.name);
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching landlord properties: " + e.getMessage());
        }
        return list;
    }

    /**
     * View all booking requests submitted by tenants.
     */
    public List<Booking> getIncomingBookingRequests() {
        List<Booking> requests = new ArrayList<>();
        String query = 
            "SELECT b.booking_id, b.property_id, p.title AS property_title, " +
            "       b.tenant_id, u.name AS tenant_name, " +
            "       b.start_date, b.end_date, b.total_amount, b.status " +
            "FROM Bookings b " +
            "JOIN Properties p ON b.property_id = p.property_id " +
            "JOIN Users u ON b.tenant_id = u.user_id " +
            "WHERE p.landlord_id = ? " +
            "ORDER BY b.booking_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, this.userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Booking b = new Booking(
                        rs.getInt("booking_id"),
                        rs.getInt("property_id"),
                        rs.getString("property_title"),
                        rs.getInt("tenant_id"),
                        rs.getString("tenant_name"),
                        rs.getDate("start_date").toString(),
                        rs.getDate("end_date").toString(),
                        rs.getDouble("total_amount"),
                        rs.getString("status")
                    );
                    requests.add(b);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching booking requests: " + e.getMessage());
        }
        return requests;
    }

    /**
     * Approve or reject a tenant's booking request using ACID transactions.
     */
    public boolean updateBookingStatus(int bookingId, String newStatus) {
        if (!"APPROVED".equalsIgnoreCase(newStatus) && !"REJECTED".equalsIgnoreCase(newStatus)) {
            System.out.println("Invalid status! Must be 'APPROVED' or 'REJECTED'");
            return false;
        }

        String updateBookingSQL = "UPDATE Bookings SET status = ? WHERE booking_id = ?";
        String findPropertySQL  = "SELECT property_id FROM Bookings WHERE booking_id = ?";
        String updatePropertySQL = "UPDATE Properties SET status = 'RENTED' WHERE property_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin SQL Transaction

            int propertyId = -1;
            try (PreparedStatement findStmt = conn.prepareStatement(findPropertySQL)) {
                findStmt.setInt(1, bookingId);
                try (ResultSet rs = findStmt.executeQuery()) {
                    if (rs.next()) {
                        propertyId = rs.getInt("property_id");
                    }
                }
            }

            if (propertyId == -1) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement updateBookingStmt = conn.prepareStatement(updateBookingSQL)) {
                updateBookingStmt.setString(1, newStatus.toUpperCase());
                updateBookingStmt.setInt(2, bookingId);
                updateBookingStmt.executeUpdate();
            }

            if ("APPROVED".equalsIgnoreCase(newStatus)) {
                try (PreparedStatement updatePropStmt = conn.prepareStatement(updatePropertySQL)) {
                    updatePropStmt.setInt(1, propertyId);
                    updatePropStmt.executeUpdate();
                }
            }

            conn.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            System.err.println("Transaction failed: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}