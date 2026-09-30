/**
 * Property.java
 * Model class representing a rental house listing.
 * Demonstrates Encapsulation with private fields and public getters/setters.
 */
public class Property {
    private int propertyId;
    private int landlordId;
    private String landlordName;
    private String title;
    private String description;
    private String city;
    private String address;
    private double pricePerMonth;
    private int bedrooms;
    private String status; // "AVAILABLE" or "RENTED"
    private String imageUrl;

    // Default Constructor
    public Property() {
        this.status = "AVAILABLE";
    }

    // Constructor for creating a new property (before DB auto-generates ID)
    public Property(int landlordId, String title, String description, String city, 
                    String address, double pricePerMonth, int bedrooms, String imageUrl) {
        this.landlordId = landlordId;
        this.title = title;
        this.description = description;
        this.city = city;
        this.address = address;
        this.pricePerMonth = pricePerMonth;
        this.bedrooms = bedrooms;
        this.status = "AVAILABLE";
        this.imageUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) 
                        ? imageUrl 
                        : "https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=800";
    }

    // Full Constructor (when fetching records from MySQL)
    public Property(int propertyId, int landlordId, String title, String description, 
                    String city, String address, double pricePerMonth, int bedrooms, 
                    String status, String imageUrl) {
        this.propertyId = propertyId;
        this.landlordId = landlordId;
        this.title = title;
        this.description = description;
        this.city = city;
        this.address = address;
        this.pricePerMonth = pricePerMonth;
        this.bedrooms = bedrooms;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public int getPropertyId() { return propertyId; }
    public void setPropertyId(int propertyId) { this.propertyId = propertyId; }

    public int getLandlordId() { return landlordId; }
    public void setLandlordId(int landlordId) { this.landlordId = landlordId; }

    public String getLandlordName() { return landlordName; }
    public void setLandlordName(String landlordName) { this.landlordName = landlordName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getPricePerMonth() { return pricePerMonth; }
    public void setPricePerMonth(double pricePerMonth) { this.pricePerMonth = pricePerMonth; }

    public int getBedrooms() { return bedrooms; }
    public void setBedrooms(int bedrooms) { this.bedrooms = bedrooms; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Helper method to display property summary
    public void displaySummary() {
        System.out.printf("[%d] %s (%d BHK) in %s - Rs. %.2f/mo [%s]%n",
                propertyId, title, bedrooms, city, pricePerMonth, status);
    }

    @Override
    public String toString() {
        return "Property ID: " + propertyId + " | " + title + " | " + bedrooms + " BHK | " 
                + city + " | Rs. " + pricePerMonth + "/month | Status: " + status;
    }
}
