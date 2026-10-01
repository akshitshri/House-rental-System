Set-Location -Path $PSScriptRoot
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  Starting House Rental Web Application..." -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

# Enforce clean User.java to prevent editor auto-save bracket corruption
$cleanUser = @'
/**
 * User.java
 * Abstract Base Class demonstrating:
 * 1. Abstraction (cannot instantiate generic User directly, only Tenant or Landlord)
 * 2. Encapsulation (protected fields with getters/setters)
 * 3. Polymorphism (abstract method showDashboard() implemented differently by subclasses)
 */
public abstract class User {
    protected int userId;
    protected String name;
    protected String email;
    protected String password;
    protected String role;
    protected String phone;

    // Constructors
    public User(int userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public User(int userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public User(int userId, String name, String email, String role, String phone) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.phone = phone;
    }

    // Common method for all users
    public void displayProfile() {
        System.out.println("-------------------------------------------");
        System.out.println("User ID : " + userId);
        System.out.println("Name    : " + name);
        System.out.println("Email   : " + email);
        System.out.println("Role    : " + role);
        if (phone != null) System.out.println("Phone   : " + phone);
        System.out.println("-------------------------------------------");
    }

    // Abstract method: Every subclass must implement its specific dashboard
    public abstract void showDashboard();

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
'@
[IO.File]::WriteAllText("$PWD\User.java", $cleanUser)

# Compile all classes
javac -cp ".;lib/mysql-connector-j.jar" *.java
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit
}

# Free port 8080 if an old instance is running
$proc = (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue).OwningProcess
if ($proc) { Stop-Process -Id $proc -Force -ErrorAction SilentlyContinue }

Write-Host "========================================================" -ForegroundColor Green
Write-Host "  Web Server Started! Opening in browser: http://localhost:8080" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Green

Start-Process "http://localhost:8080"
java -cp ".;lib/mysql-connector-j.jar" AppServer
