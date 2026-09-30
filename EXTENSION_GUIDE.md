# 🛠️ Future Extension & Customization Guide

This guide explains how your **House Rental System** is structured and shows you exactly how to add new features in the future without breaking any existing code.

---

## 📂 1. Project File Overview

```text
HouseRental/
├── config.properties        <-- Edit DB password or port anytime (NO recompile needed!)
├── schema.sql               <-- MySQL database tables and seed data
│
├── [OOP Backend Models & Logic]
├── DatabaseConnection.java  <-- Reads config.properties & provides JDBC Connection
├── User.java                <-- Abstract Base User (defines common fields & showDashboard())
├── Tenant.java              <-- Subclass: searchProperties(), bookProperty(), getMyBookings()
├── Landlord.java            <-- Subclass: addProperty(), getMyProperties(), updateBookingStatus()
├── Property.java            <-- House listing model (encapsulation with getters/setters)
├── Booking.java             <-- Rental booking model (encapsulation with getters/setters)
├── UserDAO.java             <-- Authentication & Factory (returns polymorphic User)
│
├── [Fullstack Web & CLI Runners]
├── AppServer.java           <-- Built-in Java Web Server + REST API endpoints
├── ConsoleApp.java          <-- Command-line interface for Viva demonstration
├── run-web.bat              <-- 1-Click launcher for Browser Web App
├── run-console.bat          <-- 1-Click launcher for Terminal CLI App
├── backup-project.bat       <-- 1-Click launcher to create a ZIP backup
│
├── [Frontend UI]
└── web/
    ├── index.html           <-- Homepage with search filters & listing cards
    ├── login.html           <-- Login and Signup forms (Tenant vs Landlord toggle)
    ├── dashboard.html       <-- Tenant / Landlord personalized portals
    ├── style.css            <-- Clean, modern responsive design
    └── app.js               <-- Client-side JavaScript (fetch calls to AppServer)
```

---

## 🔌 2. How to Add New Features (Step-by-Step)

### Extension A: Adding Property Ratings & Reviews
1. **Database** (`schema.sql`):
   ```sql
   CREATE TABLE Reviews (
       review_id INT AUTO_INCREMENT PRIMARY KEY,
       property_id INT,
       tenant_id INT,
       rating INT CHECK (rating BETWEEN 1 AND 5),
       comment TEXT,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (property_id) REFERENCES Properties(property_id) ON DELETE CASCADE,
       FOREIGN KEY (tenant_id) REFERENCES Users(user_id) ON DELETE CASCADE
   );
   ```
2. **OOP Model**:
   - Create `Review.java` with attributes: `reviewId`, `propertyId`, `tenantId`, `rating`, `comment`.
3. **Subclass Behavior** (`Tenant.java`):
   - In `Tenant.java`, add method:
     ```java
     public boolean leaveReview(int propertyId, int rating, String comment) {
         String sql = "INSERT INTO Reviews (property_id, tenant_id, rating, comment) VALUES (?, ?, ?, ?)";
         // execute PreparedStatement
     }
     ```
4. **AppServer & UI**:
   - Add handler for `POST /api/reviews` in `AppServer.java`.
   - Add stars and review input in `web/index.html` or `web/dashboard.html`.

---

### Extension B: Adding an Admin Subclass
1. **OOP Inheritance**:
   - Create `Admin.java extends User`:
     ```java
     public class Admin extends User {
         public Admin(int userId, String name, String email, String phone) {
             super(userId, name, email, "ADMIN", phone);
         }

         @Override
         public void showDashboard() {
             System.out.println("--- ADMIN CONTROL PANEL ---");
         }

         public void deleteListing(int propertyId) { ... }
         public void banUser(int userId) { ... }
     }
     ```
2. **UserDAO**:
   - In `UserDAO.java`, add `else if ("ADMIN".equalsIgnoreCase(role)) return new Admin(...);`.

---

### Extension C: Adding Extra Property Filters (e.g. Furnishing, Pet Friendly, Budget Range)
1. In `Properties` table, add a column:
   ```sql
   ALTER TABLE Properties ADD COLUMN furnishing ENUM('FURNISHED', 'SEMI_FURNISHED', 'UNFURNISHED') DEFAULT 'SEMI_FURNISHED';
   ```
2. In `Property.java`, add private field `furnishing`, constructor update, and `getFurnishing()` / `setFurnishing()`.
3. In `Tenant.java` `searchProperties()`, append `AND furnishing = ?` to the query if provided.
4. In `web/index.html`, add a `<select id="furnishingFilter">` dropdown in the hero search form.

---

## 💾 3. How to Save & Backup Your Work

Whenever you make changes in the future, simply:
1. Double-click **`backup-project.bat`**.
2. It will automatically generate an archive `HouseRental_Backup.zip` inside `C:\Users\Lenovo\Desktop\text\` containing your latest code.
