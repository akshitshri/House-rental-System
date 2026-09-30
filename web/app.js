/**
 * app.js
 * Client-side JavaScript handling API requests, DOM manipulation,
 * and user session state via localStorage.
 */

// Helper to get logged-in user
function getCurrentUser() {
    const raw = localStorage.getItem("rental_user");
    return raw ? JSON.parse(raw) : null;
}

function setCurrentUser(user) {
    localStorage.setItem("rental_user", JSON.stringify(user));
}

function logout() {
    localStorage.removeItem("rental_user");
    window.location.href = "/";
}

// Render Navbar based on user session
function updateNavbar() {
    const navLinks = document.getElementById("navLinks");
    if (!navLinks) return;

    const user = getCurrentUser();
    if (user) {
        navLinks.innerHTML = `
            <a href="/">Browse Listings</a>
            <a href="/dashboard.html" class="nav-btn" style="font-weight: 600;">👤 ${user.name} (${user.role})</a>
            <button class="nav-btn btn-danger" onclick="logout()">Logout</button>
        `;
    } else {
        navLinks.innerHTML = `
            <a href="/">Browse Listings</a>
            <a href="/login.html" class="nav-btn btn-primary">Sign In / Register</a>
        `;
    }
}

// =========================================================================
// HOMEPAGE LOGIC (index.html)
// =========================================================================
let currentProperties = [];

function initHomePage() {
    updateNavbar();
    loadProperties();

    const searchForm = document.getElementById("searchForm");
    if (searchForm) {
        searchForm.addEventListener("submit", (e) => {
            e.preventDefault();
            const city = document.getElementById("cityFilter").value.trim();
            const maxPrice = document.getElementById("maxPriceFilter").value.trim();
            const bhk = document.getElementById("bhkFilter").value.trim();
            loadProperties(city, maxPrice, bhk);
        });
    }

    // Auto-calculate total in booking modal
    const startInput = document.getElementById("bookStartDate");
    const endInput = document.getElementById("bookEndDate");
    if (startInput && endInput) {
        [startInput, endInput].forEach(inp => {
            inp.addEventListener("change", calculateEstimatedRent);
        });
    }

    const bookingForm = document.getElementById("bookingForm");
    if (bookingForm) {
        bookingForm.addEventListener("submit", handleBookingSubmit);
    }
}

async function loadProperties(city = "", maxPrice = "", bedrooms = "") {
    const grid = document.getElementById("propertiesGrid");
    const countSpan = document.getElementById("resultsCount");
    grid.innerHTML = "<p>Loading listings...</p>";

    let url = "/api/properties?";
    if (city) url += `city=${encodeURIComponent(city)}&`;
    if (maxPrice) url += `maxPrice=${encodeURIComponent(maxPrice)}&`;
    if (bedrooms) url += `bedrooms=${encodeURIComponent(bedrooms)}&`;

    try {
        const res = await fetch(url);
        currentProperties = await res.json();

        if (countSpan) {
            countSpan.textContent = `Showing ${currentProperties.length} available homes`;
        }

        if (currentProperties.length === 0) {
            grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; padding: 3rem; color: var(--text-muted);">
                <h3>No rental properties found</h3>
                <p>Try broadening your city or budget filters.</p>
            </div>`;
            return;
        }

        grid.innerHTML = currentProperties.map(p => `
            <div class="card">
                <div class="card-img-wrap">
                    <img src="${p.imageUrl}" alt="${p.title}" class="card-img" onerror="this.src='https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=800'">
                    <span class="badge-status ${p.status === 'AVAILABLE' ? 'status-available' : 'status-rented'}">${p.status}</span>
                </div>
                <div class="card-body">
                    <div class="card-price">₹${Number(p.pricePerMonth).toLocaleString('en-IN')}<span style="font-size: 0.85rem; font-weight: normal; color: var(--text-muted);"> / month</span></div>
                    <h3 class="card-title">${p.title}</h3>
                    <div class="card-meta">
                        <span>📍 ${p.city}</span>
                        <span>🛏️ ${p.bedrooms} BHK</span>
                    </div>
                    <p class="card-desc">${p.description}</p>
                    <div class="card-footer">
                        <span style="font-size: 0.8rem; color: var(--text-muted);">Host: <strong>${p.landlordName || 'Landlord'}</strong></span>
                        <button class="search-btn btn-primary" onclick="openBookingModal(${p.propertyId})" ${p.status !== 'AVAILABLE' ? 'disabled style="opacity:0.5;cursor:not-allowed;"' : ''}>
                            ${p.status === 'AVAILABLE' ? 'Book Now' : 'Rented'}
                        </button>
                    </div>
                </div>
            </div>
        `).join("");

    } catch (err) {
        console.error(err);
        grid.innerHTML = "<p>Error loading properties. Make sure backend is running.</p>";
    }
}

let activeBookingProp = null;

function openBookingModal(propertyId) {
    const user = getCurrentUser();
    if (!user) {
        alert("Please sign in or register as a Tenant to book a property!");
        window.location.href = "/login.html";
        return;
    }
    if (user.role !== "TENANT") {
        alert("Only Tenant accounts can book properties! You are signed in as " + user.role);
        return;
    }

    activeBookingProp = currentProperties.find(p => p.propertyId === propertyId);
    if (!activeBookingProp) return;

    document.getElementById("bookPropertyId").value = propertyId;
    document.getElementById("modalPropertyTitle").textContent = `Book: ${activeBookingProp.title}`;
    document.getElementById("modalPropertyPrice").textContent = `Rent: ₹${Number(activeBookingProp.pricePerMonth).toLocaleString('en-IN')} / month (in ${activeBookingProp.city})`;

    // Reset dates
    const today = new Date().toISOString().split("T")[0];
    document.getElementById("bookStartDate").min = today;
    document.getElementById("bookStartDate").value = today;

    // Set end date to 1 month later by default
    const nextMonth = new Date();
    nextMonth.setMonth(nextMonth.getMonth() + 1);
    document.getElementById("bookEndDate").value = nextMonth.toISOString().split("T")[0];

    calculateEstimatedRent();
    document.getElementById("bookingModal").style.display = "flex";
}

function closeBookingModal() {
    document.getElementById("bookingModal").style.display = "none";
}

function calculateEstimatedRent() {
    if (!activeBookingProp) return;
    const start = new Date(document.getElementById("bookStartDate").value);
    const end = new Date(document.getElementById("bookEndDate").value);

    if (end > start) {
        const diffDays = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
        const total = Math.round((diffDays / 30) * activeBookingProp.pricePerMonth);
        document.getElementById("bookTotalAmount").value = total;
    } else {
        document.getElementById("bookTotalAmount").value = activeBookingProp.pricePerMonth;
    }
}

async function handleBookingSubmit(e) {
    e.preventDefault();
    const user = getCurrentUser();
    const propId = document.getElementById("bookPropertyId").value;
    const startDate = document.getElementById("bookStartDate").value;
    const endDate = document.getElementById("bookEndDate").value;
    const totalAmount = document.getElementById("bookTotalAmount").value;

    try {
        const res = await fetch("/api/book", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `tenantId=${user.userId}&propertyId=${propId}&startDate=${startDate}&endDate=${endDate}&totalAmount=${totalAmount}`
        });

        const data = await res.json();
        if (data.success) {
            alert("Booking request submitted! The Landlord will review your request.");
            closeBookingModal();
            window.location.href = "/dashboard.html";
        } else {
            alert("Booking error: " + data.message);
        }
    } catch (err) {
        alert("Failed to submit booking: " + err.message);
    }
}

// =========================================================================
// AUTH LOGIC (login.html)
// =========================================================================
function initAuthPage() {
    const loginForm = document.getElementById("loginForm");
    const regForm = document.getElementById("registerForm");

    if (loginForm) {
        loginForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const email = document.getElementById("loginEmail").value.trim();
            const password = document.getElementById("loginPassword").value.trim();

            try {
                const res = await fetch("/api/login", {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: `email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`
                });
                const data = await res.json();
                if (data.success) {
                    setCurrentUser(data);
                    window.location.href = "/dashboard.html";
                } else {
                    alert(data.message || "Invalid credentials!");
                }
            } catch (err) {
                alert("Login failed: " + err.message);
            }
        });
    }

    if (regForm) {
        regForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const name = document.getElementById("regName").value.trim();
            const email = document.getElementById("regEmail").value.trim();
            const password = document.getElementById("regPassword").value.trim();
            const role = document.getElementById("regRole").value;
            const phone = document.getElementById("regPhone").value.trim();

            try {
                const res = await fetch("/api/register", {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    body: `name=${encodeURIComponent(name)}&email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}&role=${role}&phone=${encodeURIComponent(phone)}`
                });
                const data = await res.json();
                if (data.success) {
                    alert("Registration successful! Please sign in with your credentials.");
                    switchTab("login");
                    document.getElementById("loginEmail").value = email;
                } else {
                    alert(data.message || "Registration failed!");
                }
            } catch (err) {
                alert("Registration failed: " + err.message);
            }
        });
    }
}

// =========================================================================
// DASHBOARD LOGIC (dashboard.html)
// =========================================================================
function initDashboardPage() {
    const user = getCurrentUser();
    if (!user) {
        window.location.href = "/login.html";
        return;
    }

    document.getElementById("userGreeting").textContent = `Welcome back, ${user.name}!`;
    document.getElementById("userRoleBadge").textContent = `Logged in as: ${user.role} | Email: ${user.email}`;

    if (user.role === "TENANT") {
        document.getElementById("tenantSection").style.display = "block";
        loadTenantBookings(user.userId);
    } else if (user.role === "LANDLORD") {
        document.getElementById("landlordSection").style.display = "block";
        document.getElementById("landlordActions").style.display = "block";
        loadLandlordData(user.userId);

        const addPropForm = document.getElementById("addPropertyForm");
        if (addPropForm) {
            addPropForm.addEventListener("submit", handleAddProperty);
        }
    }
}

// Tenant Dashboard
async function loadTenantBookings(tenantId) {
    const tbody = document.getElementById("tenantBookingsBody");
    try {
        const res = await fetch(`/api/bookings?userId=${tenantId}&role=TENANT`);
        const list = await res.json();

        if (list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; color:var(--text-muted);">You have not booked any rental properties yet. <a href="/">Browse houses here</a></td></tr>`;
            return;
        }

        tbody.innerHTML = list.map(b => `
            <tr>
                <td>#${b.bookingId}</td>
                <td><strong>${b.propertyTitle}</strong></td>
                <td>${b.startDate} to ${b.endDate}</td>
                <td>₹${Number(b.totalAmount).toLocaleString('en-IN')}</td>
                <td>
                    <span class="badge-status ${b.status === 'APPROVED' ? 'status-available' : (b.status === 'REJECTED' ? 'status-rented' : 'status-pending')}">
                        ${b.status}
                    </span>
                </td>
            </tr>
        `).join("");
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="5">Error loading bookings.</td></tr>`;
    }
}

// Landlord Dashboard
async function loadLandlordData(landlordId) {
    const reqBody = document.getElementById("landlordRequestsBody");
    const grid = document.getElementById("landlordPropsGrid");

    // Load Requests
    try {
        const res = await fetch(`/api/bookings?userId=${landlordId}&role=LANDLORD`);
        const requests = await res.json();

        if (requests.length === 0) {
            reqBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-muted);">No booking requests received yet.</td></tr>`;
        } else {
            reqBody.innerHTML = requests.map(r => `
                <tr>
                    <td>#${r.bookingId}</td>
                    <td><strong>${r.propertyTitle}</strong></td>
                    <td>${r.tenantName}</td>
                    <td>${r.startDate} to ${r.endDate}</td>
                    <td>₹${Number(r.totalAmount).toLocaleString('en-IN')}</td>
                    <td>
                        <span class="badge-status ${r.status === 'APPROVED' ? 'status-available' : (r.status === 'REJECTED' ? 'status-rented' : 'status-pending')}">
                            ${r.status}
                        </span>
                    </td>
                    <td>
                        ${r.status === 'PENDING' ? `
                            <button class="btn-sm btn-success" onclick="updateStatus(${r.bookingId}, 'APPROVED')">Approve</button>
                            <button class="btn-sm btn-danger" onclick="updateStatus(${r.bookingId}, 'REJECTED')">Reject</button>
                        ` : `<span style="color:var(--text-muted); font-size:0.85rem;">Completed</span>`}
                    </td>
                </tr>
            `).join("");
        }
    } catch (err) {
        reqBody.innerHTML = `<tr><td colspan="7">Error loading requests.</td></tr>`;
    }

    // Load Landlord's Properties
    try {
        const res = await fetch(`/api/properties`);
        const all = await res.json();
        const myProps = all.filter(p => p.landlordId === landlordId);

        if (myProps.length === 0) {
            grid.innerHTML = `<p style="grid-column: 1/-1; color: var(--text-muted);">You have not listed any properties yet. Click "+ Add New Property" above to list one!</p>`;
        } else {
            grid.innerHTML = myProps.map(p => `
                <div class="card">
                    <div class="card-img-wrap">
                        <img src="${p.imageUrl}" alt="${p.title}" class="card-img">
                        <span class="badge-status ${p.status === 'AVAILABLE' ? 'status-available' : 'status-rented'}">${p.status}</span>
                    </div>
                    <div class="card-body">
                        <div class="card-price">₹${Number(p.pricePerMonth).toLocaleString('en-IN')} / mo</div>
                        <h4 class="card-title">${p.title}</h4>
                        <div class="card-meta">
                            <span>📍 ${p.city}</span>
                            <span>🛏️ ${p.bedrooms} BHK</span>
                        </div>
                        <p class="card-desc">${p.description}</p>
                        <p style="font-size:0.8rem; color:var(--text-muted);">Address: ${p.address}</p>
                    </div>
                </div>
            `).join("");
        }
    } catch (err) {
        grid.innerHTML = `<p>Error loading your properties.</p>`;
    }
}

async function updateStatus(bookingId, status) {
    const user = getCurrentUser();
    if (!confirm(`Are you sure you want to set this booking request to ${status}?`)) return;

    try {
        const res = await fetch("/api/booking-status", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `landlordId=${user.userId}&bookingId=${bookingId}&status=${status}`
        });
        const data = await res.json();
        if (data.success) {
            alert(`Booking marked as ${status}!`);
            loadLandlordData(user.userId);
        } else {
            alert("Error: " + data.message);
        }
    } catch (err) {
        alert("Failed to update status: " + err.message);
    }
}

function openAddPropertyModal() {
    document.getElementById("addPropertyModal").style.display = "flex";
}

function closeAddPropertyModal() {
    document.getElementById("addPropertyModal").style.display = "none";
}

async function handleAddProperty(e) {
    e.preventDefault();
    const user = getCurrentUser();

    const title = document.getElementById("newTitle").value.trim();
    const description = document.getElementById("newDesc").value.trim();
    const city = document.getElementById("newCity").value.trim();
    const bedrooms = document.getElementById("newBhk").value;
    const address = document.getElementById("newAddress").value.trim();
    const pricePerMonth = document.getElementById("newPrice").value.trim();
    let imageUrl = document.getElementById("newImg").value.trim();
    if (!imageUrl) {
        imageUrl = "https://images.unsplash.com/photo-1570129477492-45c003edd2be?w=800";
    }

    try {
        const res = await fetch("/api/properties", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `landlordId=${user.userId}&title=${encodeURIComponent(title)}&description=${encodeURIComponent(description)}&city=${encodeURIComponent(city)}&bedrooms=${bedrooms}&address=${encodeURIComponent(address)}&pricePerMonth=${pricePerMonth}&imageUrl=${encodeURIComponent(imageUrl)}`
        });

        const data = await res.json();
        if (data.success) {
            alert("Property successfully listed for rent!");
            closeAddPropertyModal();
            document.getElementById("addPropertyForm").reset();
            loadLandlordData(user.userId);
        } else {
            alert("Failed to add property: " + data.message);
        }
    } catch (err) {
        alert("Error adding property: " + err.message);
    }
}
