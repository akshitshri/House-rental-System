import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AppServer.java
 * Lightweight HTTP Web Server using Java's built-in HttpServer (com.sun.net.httpserver).
 * Requires ZERO external frameworks (No Spring, No Tomcat, No Maven).
 * Bridges the HTML/CSS/JS frontend directly to the Java OOP models & MySQL JDBC backend!
 */
public class AppServer {
    private static final int PORT = 8080;
    private static final UserDAO userDAO = new UserDAO();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API Endpoints
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/properties", new PropertiesHandler());
        server.createContext("/api/book", new BookingHandler());
        server.createContext("/api/bookings", new GetBookingsHandler());
        server.createContext("/api/booking-status", new UpdateBookingStatusHandler());

        // Static Web Files Handler (HTML, CSS, JS)
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null); // Default executor
        System.out.println("=======================================================");
        System.out.println("   HOUSE RENTAL SYSTEM WEB SERVER STARTED!");
        System.out.println("   Open in your browser: http://localhost:" + PORT);
        System.out.println("=======================================================");
        server.start();
    }

    // =========================================================================
    // API HANDLERS
    // =========================================================================

    /**
     * POST /api/login
     */
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);
                String email = params.get("email");
                String password = params.get("password");

                User user = userDAO.loginUser(email, password);
                if (user != null) {
                    String json = String.format(
                        "{\"success\": true, \"userId\": %d, \"name\": \"%s\", \"email\": \"%s\", \"role\": \"%s\"}",
                        user.getUserId(), escapeJson(user.getName()), escapeJson(user.getEmail()), user.getRole()
                    );
                    sendResponse(exchange, 200, "application/json", json);
                } else {
                    sendResponse(exchange, 401, "application/json", "{\"success\": false, \"message\": \"Invalid credentials\"}");
                }
            } else {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            }
        }
    }

    /**
     * POST /api/register
     */
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);
                String name = params.get("name");
                String email = params.get("email");
                String password = params.get("password");
                String role = params.get("role");
                String phone = params.get("phone");

                boolean ok = userDAO.registerUser(name, email, password, role, phone);
                if (ok) {
                    sendResponse(exchange, 200, "application/json", "{\"success\": true, \"message\": \"Registration successful\"}");
                } else {
                    sendResponse(exchange, 400, "application/json", "{\"success\": false, \"message\": \"Registration failed. Email may already exist.\"}");
                }
            } else {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            }
        }
    }

    /**
     * GET /api/properties (Search & filter)
     * POST /api/properties (Add listing by Landlord)
     */
    static class PropertiesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String city = params.get("city");
                String maxPriceStr = params.get("maxPrice");
                String bhkStr = params.get("bedrooms");

                Double maxPrice = (maxPriceStr != null && !maxPriceStr.isEmpty()) ? Double.parseDouble(maxPriceStr) : null;
                Integer bhk = (bhkStr != null && !bhkStr.isEmpty()) ? Integer.parseInt(bhkStr) : null;

                // Use Tenant object to search properties
                Tenant queryRunner = new Tenant(0, "Searcher", "system@rental.com");
                List<Property> list = queryRunner.searchProperties(city, maxPrice, bhk);

                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Property p = list.get(i);
                    json.append(String.format(
                        "{\"propertyId\": %d, \"landlordId\": %d, \"landlordName\": \"%s\", \"title\": \"%s\", " +
                        "\"description\": \"%s\", \"city\": \"%s\", \"address\": \"%s\", \"pricePerMonth\": %.2f, " +
                        "\"bedrooms\": %d, \"status\": \"%s\", \"imageUrl\": \"%s\"}",
                        p.getPropertyId(), p.getLandlordId(), escapeJson(p.getLandlordName()),
                        escapeJson(p.getTitle()), escapeJson(p.getDescription()),
                        escapeJson(p.getCity()), escapeJson(p.getAddress()),
                        p.getPricePerMonth(), p.getBedrooms(), p.getStatus(), escapeJson(p.getImageUrl())
                    ));
                    if (i < list.size() - 1) json.append(",");
                }
                json.append("]");

                sendResponse(exchange, 200, "application/json", json.toString());

            } else if ("POST".equalsIgnoreCase(method)) {
                Map<String, String> params = parseRequestBody(exchange);
                int landlordId = Integer.parseInt(params.get("landlordId"));
                String title = params.get("title");
                String desc = params.get("description");
                String city = params.get("city");
                String addr = params.get("address");
                double price = Double.parseDouble(params.get("pricePerMonth"));
                int bhk = Integer.parseInt(params.get("bedrooms"));
                String img = params.get("imageUrl");

                Landlord landlord = new Landlord(landlordId, "Landlord", "");
                Property newProp = new Property(landlordId, title, desc, city, addr, price, bhk, img);

                boolean ok = landlord.addProperty(newProp);
                if (ok) {
                    sendResponse(exchange, 200, "application/json", "{\"success\": true, \"message\": \"Property added\"}");
                } else {
                    sendResponse(exchange, 500, "application/json", "{\"success\": false, \"message\": \"Failed to add property\"}");
                }
            } else {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            }
        }
    }

    /**
     * POST /api/book (Tenant submits booking)
     */
    static class BookingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);
                int tenantId = Integer.parseInt(params.get("tenantId"));
                int propertyId = Integer.parseInt(params.get("propertyId"));
                String startDate = params.get("startDate");
                String endDate = params.get("endDate");
                double total = Double.parseDouble(params.get("totalAmount"));

                Tenant tenant = new Tenant(tenantId, "Tenant", "");
                boolean ok = tenant.bookProperty(propertyId, startDate, endDate, total);

                if (ok) {
                    sendResponse(exchange, 200, "application/json", "{\"success\": true, \"message\": \"Booking request submitted\"}");
                } else {
                    sendResponse(exchange, 400, "application/json", "{\"success\": false, \"message\": \"Booking failed. Property may already be rented.\"}");
                }
            } else {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            }
        }
    }

    /**
     * GET /api/bookings?userId=...&role=...
     */
    static class GetBookingsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String role = params.get("role");
                int userId = Integer.parseInt(params.get("userId"));

                List<Booking> list;
                if ("LANDLORD".equalsIgnoreCase(role)) {
                    Landlord landlord = new Landlord(userId, "", "");
                    list = landlord.getIncomingBookingRequests();
                } else {
                    Tenant tenant = new Tenant(userId, "", "");
                    list = tenant.getMyBookings();
                }

                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    Booking b = list.get(i);
                    json.append(String.format(
                        "{\"bookingId\": %d, \"propertyId\": %d, \"propertyTitle\": \"%s\", " +
                        "\"tenantId\": %d, \"tenantName\": \"%s\", \"startDate\": \"%s\", " +
                        "\"endDate\": \"%s\", \"totalAmount\": %.2f, \"status\": \"%s\"}",
                        b.getBookingId(), b.getPropertyId(), escapeJson(b.getPropertyTitle()),
                        b.getTenantId(), escapeJson(b.getTenantName()), b.getStartDate(),
                        b.getEndDate(), b.getTotalAmount(), b.getStatus()
                    ));
                    if (i < list.size() - 1) json.append(",");
                }
                json.append("]");

                sendResponse(exchange, 200, "application/json", json.toString());
            } else {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            }
        }
    }

    /**
     * POST /api/booking-status (Landlord approves/rejects)
     */
    static class UpdateBookingStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseRequestBody(exchange);
                int landlordId = Integer.parseInt(params.get("landlordId"));
                int bookingId = Integer.parseInt(params.get("bookingId"));
                String status = params.get("status");

                Landlord landlord = new Landlord(landlordId, "", "");
                boolean ok = landlord.updateBookingStatus(bookingId, status);

                if (ok) {
                    sendResponse(exchange, 200, "application/json", "{\"success\": true, \"message\": \"Status updated\"}");
                } else {
                    sendResponse(exchange, 400, "application/json", "{\"success\": false, \"message\": \"Update failed\"}");
                }
            } else {
                sendResponse(exchange, 405, "text/plain", "Method Not Allowed");
            }
        }
    }

    // =========================================================================
    // STATIC FILE HANDLER (Serves web/ folder)
    // =========================================================================
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File file = new File("web" + path);
            if (!file.exists() || file.isDirectory()) {
                file = new File("web/index.html");
            }

            if (file.exists() && !file.isDirectory()) {
                String mime = "text/plain";
                String name = file.getName().toLowerCase();
                if (name.endsWith(".html")) mime = "text/html; charset=UTF-8";
                else if (name.endsWith(".css")) mime = "text/css; charset=UTF-8";
                else if (name.endsWith(".js")) mime = "application/javascript; charset=UTF-8";
                else if (name.endsWith(".png")) mime = "image/png";
                else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) mime = "image/jpeg";

                byte[] bytes = Files.readAllBytes(file.toPath());
                exchange.getResponseHeaders().set("Content-Type", mime);
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } else {
                sendResponse(exchange, 404, "text/plain", "404 Not Found");
            }
        }
    }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================
    private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseRequestBody(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(reader);
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            body.append(line);
        }
        return parseFormData(body.toString());
    }

    private static Map<String, String> parseFormData(String formData) {
        Map<String, String> map = new HashMap<>();
        if (formData == null || formData.trim().isEmpty()) return map;

        // If JSON format
        if (formData.trim().startsWith("{")) {
            String clean = formData.trim().replaceAll("[{}\"]", "");
            String[] pairs = clean.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    map.put(kv[0].trim(), kv[1].trim());
                }
            }
            return map;
        }

        // URL encoded format
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try {
                    String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8.name());
                    String v = URLDecoder.decode(kv[1], StandardCharsets.UTF_8.name());
                    map.put(k, v);
                } catch (UnsupportedEncodingException ignored) {}
            }
        }
        return map;
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try {
                    map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8.name()),
                            URLDecoder.decode(kv[1], StandardCharsets.UTF_8.name()));
                } catch (Exception ignored) {}
            }
        }
        return map;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
