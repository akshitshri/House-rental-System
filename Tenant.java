public class Tenant extends User {

    public Tenant(int userId, String name, String email) {
        super(userId, name, email);
    }

    @Override
    public void showDashboard() {
        System.out.println("--- Tenant Dashboard ---");
        System.out.println("1. Search Properties");
        System.out.println("2. View My Bookings");
    }
    
    // Tenant-specific behavior
    public void searchProperty(String city) {
        // SQL query logic will go here
        System.out.println("Searching available houses in " + city);
    }
}