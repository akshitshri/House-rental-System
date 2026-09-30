-- ==============================================================
-- House Rental Management System - Database Schema (MySQL)
-- 2nd Year B.Tech / BCA / MCA Project
-- ==============================================================

CREATE DATABASE IF NOT EXISTS house_rental_db;
USE house_rental_db;

-- 1. USERS TABLE
-- Stores credentials and user role (TENANT, LANDLORD, ADMIN)
CREATE TABLE IF NOT EXISTS Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('TENANT', 'LANDLORD', 'ADMIN') NOT NULL,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. PROPERTIES TABLE
-- Linked to Landlord via Foreign Key (landlord_id -> Users.user_id)
CREATE TABLE IF NOT EXISTS Properties (
    property_id INT AUTO_INCREMENT PRIMARY KEY,
    landlord_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    city VARCHAR(50) NOT NULL,
    address VARCHAR(255) NOT NULL,
    price_per_month DECIMAL(10, 2) NOT NULL,
    bedrooms INT NOT NULL,
    status ENUM('AVAILABLE', 'RENTED') DEFAULT 'AVAILABLE',
    image_url VARCHAR(255) DEFAULT 'https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=800',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (landlord_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- 3. BOOKINGS TABLE
-- Links Tenant and Property (Many-to-Many relationship resolved via Bookings)
CREATE TABLE IF NOT EXISTS Bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    property_id INT NOT NULL,
    tenant_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES Properties(property_id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- ==============================================================
-- SAMPLE SEED DATA FOR TESTING & VIVA DEMO
-- ==============================================================

-- Sample Users (Landlords & Tenants)
INSERT INTO Users (name, email, password, role, phone) VALUES
('Ramesh Sharma', 'ramesh@landlord.com', 'pass123', 'LANDLORD', '9876543210'),
('Priya Patel', 'priya@landlord.com', 'pass123', 'LANDLORD', '9876543211'),
('Aarav Mehta', 'aarav@tenant.com', 'pass123', 'TENANT', '9123456780'),
('Sneha Roy', 'sneha@tenant.com', 'pass123', 'TENANT', '9123456781')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Sample Properties listed by Landlords
INSERT INTO Properties (landlord_id, title, description, city, address, price_per_month, bedrooms, status, image_url) VALUES
(1, 'Spacious 2BHK Apartment in Indiranagar', 'Fully furnished 2BHK with balcony, modular kitchen, and 24/7 water supply.', 'Bangalore', '12th Main, Indiranagar', 28000.00, 2, 'AVAILABLE', 'https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800'),
(1, 'Cozy 1BHK Studio near Metro Station', 'Ideal for bachelors and students. Walking distance from MG Road metro.', 'Bangalore', 'Brigade Road, Ashok Nagar', 16000.00, 1, 'AVAILABLE', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800'),
(2, 'Luxury 3BHK Penthouse with Sea View', 'High-end penthouse with modern amenities, swimming pool access, and parking.', 'Mumbai', 'Bandra West, Hill Road', 65000.00, 3, 'AVAILABLE', 'https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=800'),
(2, 'Affordable 2BHK Flat in Andheri East', 'Semi-furnished flat near tech parks and metro station.', 'Mumbai', 'Marol, Andheri East', 32000.00, 2, 'AVAILABLE', 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800'),
(1, 'Independent 3BHK Villa with Lawn', 'Peaceful locality, private garden, car parking, pet friendly.', 'Delhi', 'Sector 15, Dwarka', 45000.00, 3, 'AVAILABLE', 'https://images.unsplash.com/photo-1580587771525-78b9dba3b914?w=800');

-- Sample Booking Request
INSERT INTO Bookings (property_id, tenant_id, start_date, end_date, total_amount, status) VALUES
(1, 3, '2026-11-01', '2027-10-31', 336000.00, 'PENDING');
