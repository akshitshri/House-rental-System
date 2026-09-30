public class Landlord extends User {

    public Landlord(int userId, String name, String email) {
        super(userId, name, email);
    }

    @Override
    public void showDashboard() {
        System.out.println("--- Landlord Dashboard ---");
        System.out.println("1. Add New Property");
        System.out.println("2. View Incoming Booking Requests");
    }

    // Landlord-specific behavior
    public void addProperty(String title, double price) {
        // SQL query logic will go here
        System.out.println("Listing added: " + title + " for $" + price);
    }
}