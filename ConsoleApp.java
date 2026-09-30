import java.util.List;
import java.util.Scanner;

/**
 * ConsoleApp.java
 * Interactive command-line interface for the House Rental System.
 * Demonstrates:
 * 1. Polymorphism (handling User currentUser which can be Tenant or Landlord)
 * 2. Menu-driven CLI suitable for University Project Demonstration & Viva
 */
public class ConsoleApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserDAO userDAO = new UserDAO();
    private static User currentUser = null;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   WELCOME TO HOUSE RENTAL MANAGEMENT SYSTEM");
        System.out.println("   (Java OOP + MySQL Raw JDBC Project Demo)");
        System.out.println("==================================================");

        boolean running = true;
        while (running) {
            if (currentUser == null) {
                showAuthMenu();
            } else {
                handleUserDashboard();
            }
        }
    }

    private static void showAuthMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println("1. Login");
        System.out.println("2. Register as New User");
        System.out.println("3. Browse Properties as Guest");
        System.out.println("4. Exit");
        System.out.print("Enter choice (1-4): ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                login();
                break;
            case "2":
                register();
                break;
            case "3":
                browseGuestProperties();
                break;
            case "4":
                System.out.println("Thank you for using House Rental System. Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private static void login() {
        System.out.println("\n--- USER LOGIN ---");
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        // Polymorphic assignment: currentUser can hold Tenant or Landlord
        currentUser = userDAO.loginUser(email, password);

        if (currentUser != null) {
            System.out.println("\n>>> Login Successful! Welcome, " + currentUser.getName() + " (" + currentUser.getRole() + ") <<<");
        } else {
            System.out.println("\n[!] Invalid email or password. Please try again.");
        }
    }

    private static void register() {
        System.out.println("\n--- USER REGISTRATION ---");
        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Enter Role (1 for TENANT, 2 for LANDLORD): ");
        String roleChoice = scanner.nextLine().trim();
        String role = roleChoice.equals("2") ? "LANDLORD" : "TENANT";
        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine().trim();

        boolean ok = userDAO.registerUser(name, email, password, role, phone);
        if (ok) {
            System.out.println("\n>>> Registration successful! You can now login. <<<");
        } else {
            System.out.println("\n[!] Registration failed. Email might already exist.");
        }
    }

    private static void browseGuestProperties() {
        Tenant guest = new Tenant(0, "Guest", "guest@system.com");
        List<Property> list = guest.getAllAvailableProperties();
        displayPropertiesList(list);
    }

    private static void handleUserDashboard() {
        // Polymorphism: calls Tenant.showDashboard() or Landlord.showDashboard() dynamically!
        currentUser.showDashboard();
        System.out.print("Enter option: ");
        String option = scanner.nextLine().trim();

        if (currentUser instanceof Tenant) {
            handleTenantAction((Tenant) currentUser, option);
        } else if (currentUser instanceof Landlord) {
            handleLandlordAction((Landlord) currentUser, option);
        }
    }

    private static void handleTenantAction(Tenant tenant, String option) {
        switch (option) {
            case "1": // Browse all
                List<Property> all = tenant.getAllAvailableProperties();
                displayPropertiesList(all);
                break;
            case "2": // Search & filter
                System.out.print("Enter City (or press enter to skip): ");
                String city = scanner.nextLine().trim();
                System.out.print("Enter Max Budget (or 0 to skip): ");
                String priceStr = scanner.nextLine().trim();
                Double maxPrice = priceStr.isEmpty() ? null : Double.parseDouble(priceStr);
                System.out.print("Enter BHK / Bedrooms (or 0 to skip): ");
                String bhkStr = scanner.nextLine().trim();
                Integer bhk = bhkStr.isEmpty() ? null : Integer.parseInt(bhkStr);

                List<Property> filtered = tenant.searchProperties(city, maxPrice, bhk);
                displayPropertiesList(filtered);
                break;
            case "3": // Book property
                System.out.print("Enter Property ID to book: ");
                int propId = Integer.parseInt(scanner.nextLine().trim());
                System.out.print("Enter Start Date (YYYY-MM-DD): ");
                String start = scanner.nextLine().trim();
                System.out.print("Enter End Date (YYYY-MM-DD): ");
                String end = scanner.nextLine().trim();
                System.out.print("Enter Estimated Total Rent: ");
                double amount = Double.parseDouble(scanner.nextLine().trim());

                boolean booked = tenant.bookProperty(propId, start, end, amount);
                if (booked) {
                    System.out.println("\n>>> Booking Request Submitted Successfully! Awaiting Landlord Approval. <<<");
                } else {
                    System.out.println("\n[!] Could not book property. Check ID or availability.");
                }
                break;
            case "4": // View my bookings
                List<Booking> myBookings = tenant.getMyBookings();
                displayBookingsList(myBookings);
                break;
            case "5": // Profile
                tenant.displayProfile();
                break;
            case "6": // Logout
                System.out.println("Logged out successfully.");
                currentUser = null;
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private static void handleLandlordAction(Landlord landlord, String option) {
        switch (option) {
            case "1": // Add property
                System.out.print("Enter Property Title: ");
                String title = scanner.nextLine().trim();
                System.out.print("Enter Description: ");
                String desc = scanner.nextLine().trim();
                System.out.print("Enter City: ");
                String city = scanner.nextLine().trim();
                System.out.print("Enter Address: ");
                String address = scanner.nextLine().trim();
                System.out.print("Enter Monthly Rent (INR): ");
                double rent = Double.parseDouble(scanner.nextLine().trim());
                System.out.print("Enter Bedrooms (BHK): ");
                int bhk = Integer.parseInt(scanner.nextLine().trim());
                System.out.print("Enter Image URL (or press Enter for default): ");
                String img = scanner.nextLine().trim();

                Property newProp = new Property(landlord.getUserId(), title, desc, city, address, rent, bhk, img);
                boolean added = landlord.addProperty(newProp);
                if (added) {
                    System.out.println("\n>>> Property listed successfully! <<<");
                } else {
                    System.out.println("\n[!] Failed to add property.");
                }
                break;
            case "2": // View my listings
                List<Property> myProps = landlord.getMyProperties();
                displayPropertiesList(myProps);
                break;
            case "3": // View booking requests
                List<Booking> requests = landlord.getIncomingBookingRequests();
                displayBookingsList(requests);
                break;
            case "4": // Approve / Reject request
                System.out.print("Enter Booking ID to update: ");
                int bId = Integer.parseInt(scanner.nextLine().trim());
                System.out.print("Enter Action (1 to APPROVE, 2 to REJECT): ");
                String act = scanner.nextLine().trim();
                String newStatus = act.equals("1") ? "APPROVED" : "REJECTED";

                boolean updated = landlord.updateBookingStatus(bId, newStatus);
                if (updated) {
                    System.out.println("\n>>> Booking status updated to " + newStatus + "! <<<");
                } else {
                    System.out.println("\n[!] Failed to update booking status.");
                }
                break;
            case "5": // Profile
                landlord.displayProfile();
                break;
            case "6": // Logout
                System.out.println("Logged out successfully.");
                currentUser = null;
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private static void displayPropertiesList(List<Property> list) {
        System.out.println("\n----------------- PROPERTY LISTINGS (" + list.size() + ") -----------------");
        if (list.isEmpty()) {
            System.out.println("No properties found matching your criteria.");
            return;
        }
        for (Property p : list) {
            p.displaySummary();
        }
        System.out.println("---------------------------------------------------------");
    }

    private static void displayBookingsList(List<Booking> list) {
        System.out.println("\n------------------- BOOKINGS (" + list.size() + ") -------------------");
        if (list.isEmpty()) {
            System.out.println("No bookings recorded.");
            return;
        }
        for (Booking b : list) {
            b.displaySummary();
        }
        System.out.println("---------------------------------------------------------");
    }
}
