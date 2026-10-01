import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tenant.java
 * Subclass representing a Tenant (Renter).
 * Demonstrates Inheritance (extends User) and Method Overriding.
 */
public class Tenant extends User {

    public Tenant(int userId, String name, String email) {
        super(userId, name, email, "TENANT");
    }

    public Tenant(int userId, String name, String email, String phone) {
        super(userId, name, email, "TENANT", phone);
    }

    @Override
    public void showDashboard() {
        System.out.println("\n==========================================");
        System.out.println("          TENANT DASHBOARD: " + this.name);
        System.out.println("==========================================");
        System.out.println("1. Browse All Available Properties");
        System.out.println("2. Search & Filter Properties (City, Price, BHK)");
        System.out.println("3. Book a Property");
        System.out.println("4. View My Bookings & Request Status");
        System.out.println("5. View My Profile");
        System.out.println("6. Logout");
        System.out.println("==========================================");
    }

    // Basic method signature matching starter notes
    public void searchProperty(String city) {
        System.out.println("Searching available houses in " + city + "...");
        List<Property> results = searchProperties(city, null, null);
        for (Property p : results) {
            p.displaySummary();
        }
    }

    /**
     * Fetch all currently available properties.
     */
    public List<Property> getAllAvailableProperties() {
        return searchProperties(null, null, null);
    }

    /**
     * Search properties with dynamic filtering (City, Max Price, BHK).
     * Demonstrates raw JDBC PreparedStatement with parameterized queries.
     */
    public List<Property> searchProperties(String city, Double maxPrice, Integer bedrooms) {
        List<Property> properties = new ArrayList<>();
        StringBuilder query = new StringBuilder(
            "SELECT p.*, u.name AS landlord_name " +
            "FROM Properties p " +
            "JOIN Users u ON p.landlord_id = u.user_id " +
            "WHERE p.status = 'AVAILABLE' "
        );

        if (city != null && !city.trim().isEmpty()) {
            query.append("AND LOWER(p.city) LIKE LOWER(?) ");
        }
        if (maxPrice != null && maxPrice > 0) {
            query.append("AND p.price_per_month <= ? ");
        }
        if (bedrooms != null && bedrooms > 0) {
            query.append("AND p.bedrooms = ? ");
        }
        query.append("ORDER BY p.price_per_month ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query.toString())) {

            int paramIndex = 1;
            if (city != null && !city.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + city.trim() + "%");
            }
            if (maxPrice != null && maxPrice > 0) {
                pstmt.setDouble(paramIndex++, maxPrice);
            }
            if (bedrooms != null && bedrooms > 0) {
                pstmt.setInt(paramIndex++, bedrooms);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Property p = new Property(
                        rs.getInt("property_id"),
                        rs.getInt("landlord_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("city"),
                        rs.getString("address"),
                        rs.getDouble("price_per_month"),
                        rs.getInt("bedrooms"),
                        rs.getString("status"),
                        rs.getString("image_url")
                    );
                    p.setLandlordName(rs.getString("landlord_name"));
                    properties.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching properties: " + e.getMessage());
            e.printStackTrace();
        }
        return properties;
    }

    /**
     * Book a property by creating a new booking record with status 'PENDING'.
     */
    public boolean bookProperty(int propertyId, String startDate, String endDate, double totalAmount) {
        String checkQuery = "SELECT status FROM Properties WHERE property_id = ?";
        String insertQuery = "INSERT INTO Bookings (property_id, tenant_id, start_date, end_date, total_amount, status) VALUES (?, ?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, propertyId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next() || !"AVAILABLE".equalsIgnoreCase(rs.getString("status"))) {
                        System.out.println("Property not available!");
                        return false;
                    }
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, propertyId);
                insertStmt.setInt(2, this.userId);
                insertStmt.setDate(3, java.sql.Date.valueOf(startDate));
                insertStmt.setDate(4, java.sql.Date.valueOf(endDate));
                insertStmt.setDouble(5, totalAmount);

                return insertStmt.executeUpdate() > 0;
            }
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Booking failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Fetch all bookings made by this tenant.
     */
    public List<Booking> getMyBookings() {
        List<Booking> bookings = new ArrayList<>();
        String query = 
            "SELECT b.*, p.title AS property_title " +
            "FROM Bookings b " +
            "JOIN Properties p ON b.property_id = p.property_id " +
            "WHERE b.tenant_id = ? " +
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
                        this.userId,
                        this.name,
                        rs.getDate("start_date").toString(),
                        rs.getDate("end_date").toString(),
                        rs.getDouble("total_amount"),
                        rs.getString("status")
                    );
                    bookings.add(b);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bookings: " + e.getMessage());
        }
        return bookings;
    }
}