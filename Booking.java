/**
 * Booking.java
 * Model class representing a rental booking request.
 */
public class Booking {
    private int bookingId;
    private int propertyId;
    private String propertyTitle;
    private int tenantId;
    private String tenantName;
    private String startDate;
    private String endDate;
    private double totalAmount;
    private String status; // "PENDING", "APPROVED", "REJECTED"

    // Default Constructor
    public Booking() {
        this.status = "PENDING";
    }

    // Constructor for creating a new booking request
    public Booking(int propertyId, int tenantId, String startDate, String endDate, double totalAmount) {
        this.propertyId = propertyId;
        this.tenantId = tenantId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalAmount = totalAmount;
        this.status = "PENDING";
    }

    // Full constructor when reading from MySQL
    public Booking(int bookingId, int propertyId, String propertyTitle, int tenantId, 
                   String tenantName, String startDate, String endDate, double totalAmount, String status) {
        this.bookingId = bookingId;
        this.propertyId = propertyId;
        this.propertyTitle = propertyTitle;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // Getters and Setters
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getPropertyId() { return propertyId; }
    public void setPropertyId(int propertyId) { this.propertyId = propertyId; }

    public String getPropertyTitle() { return propertyTitle; }
    public void setPropertyTitle(String propertyTitle) { this.propertyTitle = propertyTitle; }

    public int getTenantId() { return tenantId; }
    public void setTenantId(int tenantId) { this.tenantId = tenantId; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public void displaySummary() {
        System.out.printf("Booking #%d | Property: %s | Tenant: %s | %s to %s | Rs. %.2f | Status: %s%n",
                bookingId, propertyTitle != null ? propertyTitle : "ID " + propertyId,
                tenantName != null ? tenantName : "ID " + tenantId,
                startDate, endDate, totalAmount, status);
    }
}
