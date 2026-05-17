const API_BASE = "http://localhost:8080/api";
const USER_API = `${API_BASE}/users`;
const BOOKING_API = `${API_BASE}/bookings`;
const WORKSPACE_API = `${API_BASE}/workspaces`;
const REVIEW_API = `${API_BASE}/reviews`;
const INVOICE_API = `${API_BASE}/invoices`;

let availableWorkspaces = [];
let currentReviews = [];
let currentReviewOffset = 0;
let invoiceByBookingId = new Map();
const REVIEW_PAGE_SIZE = 5;

function getToken() {
    return localStorage.getItem("token");
}

function decodeToken(token = getToken()) {
    if (!token) return null;

    try {
        const payload = token.split(".")[1];
        const normalized = payload.replace(/-/g, "+").replace(/_/g, "/");
        return JSON.parse(decodeURIComponent(escape(atob(normalized))));
    } catch (error) {
        return null;
    }
}

function authHeaders() {
    const token = getToken();
    return token ? { Authorization: `Bearer ${token}` } : {};
}

async function requestJson(url, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        ...(options.headers || {})
    };

    const response = await fetch(url, { ...options, headers });
    const text = await response.text();
    let body = null;

    if (text) {
        try {
            body = JSON.parse(text);
        } catch (error) {
            body = text;
        }
    }

    if (!response.ok) {
        const message = typeof body === "string"
            ? body
            : body?.message
                || body?.error
                || (body && typeof body === "object" ? Object.values(body).join(" ") : "")
                || "Something went wrong while processing the request.";
        throw new Error(message);
    }

    return body;
}

function setMessage(id, text, type = "") {
    const element = document.getElementById(id);
    if (!element) return;
    element.textContent = text;
    element.className = `message ${type}`.trim();
}

function toggleForm() {
    document.getElementById("loginForm").classList.toggle("hidden");
    document.getElementById("registerForm").classList.toggle("hidden");
}

function logout() {
    localStorage.removeItem("token");
    window.location.href = "./index.htm";
}

function requireAuth() {
    const decoded = decodeToken();
    if (!decoded) {
        logout();
        return null;
    }

    const userElement = document.getElementById("displayUser");
    const roleElement = document.getElementById("displayRole");
    const adminLink = document.getElementById("adminLink");

    if (userElement) userElement.textContent = decoded.sub || "";
    if (roleElement) roleElement.textContent = decoded.role || "";
    if (adminLink && (decoded.role === "ADMIN" || decoded.role === "EMPLOYEE")) adminLink.classList.remove("hidden");

    return decoded;
}

function requireAdmin() {
    const decoded = decodeToken();
    if (!decoded || (decoded.role !== "ADMIN" && decoded.role !== "EMPLOYEE")) {
        window.location.href = "./dashboard.htm";
    }
}

function toLocalDateTimeValue(date) {
    const offset = date.getTimezoneOffset() * 60000;
    return new Date(date.getTime() - offset).toISOString().slice(0, 16);
}

function toApiDateTime(value) {
    return value ? `${value}:00` : "";
}

function formatDateTime(value) {
    if (!value) return "-";
    return new Date(value).toLocaleString("en-US", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    });
}

function formatCurrency(value) {
    const amount = Number(value || 0);
    return `${amount.toFixed(2)} EGP`;
}

function calculateHours(startDate, endDate) {
    return (endDate.getTime() - startDate.getTime()) / 3600000;
}

function readBookingWindow() {
    const startValue = document.getElementById("searchStart").value;
    const endValue = document.getElementById("searchEnd").value;
    const startDate = startValue ? new Date(startValue) : null;
    const endDate = endValue ? new Date(endValue) : null;

    return {
        startValue,
        endValue,
        startDate,
        endDate,
        start: toApiDateTime(startValue),
        end: toApiDateTime(endValue)
    };
}

function validateBookingWindow() {
    const windowData = readBookingWindow();

    if (!windowData.startValue || !windowData.endValue) {
        throw new Error("Choose both start and end time.");
    }

    if (Number.isNaN(windowData.startDate?.getTime()) || Number.isNaN(windowData.endDate?.getTime())) {
        throw new Error("Invalid time format.");
    }

    if (windowData.startDate.getTime() <= Date.now()) {
        throw new Error("Start time must be in the future.");
    }

    if (windowData.endDate.getTime() <= windowData.startDate.getTime()) {
        throw new Error("End time must be after start time.");
    }

    return windowData;
}

function setDefaultSearchTimes() {
    const start = new Date();
    start.setHours(start.getHours() + 1, 0, 0, 0);
    const end = new Date(start);
    end.setHours(end.getHours() + 2);

    const startInput = document.getElementById("searchStart");
    const endInput = document.getElementById("searchEnd");
    const minValue = toLocalDateTimeValue(new Date());

    startInput.min = minValue;
    endInput.min = minValue;
    startInput.value = toLocalDateTimeValue(start);
    endInput.value = toLocalDateTimeValue(end);
}

function initDashboard() {
    setDefaultSearchTimes();
}

function initAdmin() {
    loadAllUsers();
    loadAllInvoices();
    loadAllBookings();
}

async function register() {
    setMessage("registerMessage", "Creating account...");

    try {
        await requestJson(`${USER_API}/register`, {
            method: "POST",
            body: JSON.stringify({
                username: document.getElementById("regUser").value.trim(),
                email: document.getElementById("regEmail").value.trim(),
                password: document.getElementById("regPass").value,
                role: "CUSTOMER"
            })
        });
        setMessage("registerMessage", "Account created. You can sign in now.", "success");
        toggleForm();
    } catch (error) {
        setMessage("registerMessage", error.message, "error");
    }
}

async function login() {
    setMessage("loginMessage", "Signing in...");

    try {
        const data = await requestJson(`${USER_API}/login`, {
            method: "POST",
            body: JSON.stringify({
                username: document.getElementById("loginUser").value.trim(),
                password: document.getElementById("loginPass").value
            })
        });
        localStorage.setItem("token", data.token);
        window.location.href = "./dashboard.htm";
    } catch (error) {
        setMessage("loginMessage", error.message, "error");
    }
}

async function loadAvailableWorkspaces() {
    const list = document.getElementById("workspaceList");
    setMessage("workspaceMessage", "Searching...");
    list.innerHTML = "";

    try {
        const windowData = validateBookingWindow();
        const workspaces = await requestJson(
            `${WORKSPACE_API}/available?start=${encodeURIComponent(windowData.start)}&end=${encodeURIComponent(windowData.end)}`,
            { method: "GET" }
        );

        availableWorkspaces = workspaces;

        if (!workspaces.length) {
            setMessage("workspaceMessage", "No workspaces are available for this time range.", "error");
            return;
        }

        setMessage("workspaceMessage", `Found ${workspaces.length} available workspace(s).`, "success");
        list.innerHTML = workspaces.map(workspace => `
            <article class="item-card">
                <h3>${workspace.name}</h3>
                <div class="meta">
                    <span>Workspace ID: ${workspace.id}</span>
                    <span>Type: ${workspace.type}</span>
                    <span>Capacity: ${workspace.capacity}</span>
                    <span>Price: ${formatCurrency(workspace.pricePerHour)} / hour</span>
                </div>
                <p class="muted">${workspace.description || "No description available."}</p>
                <div class="action-row">
                    <button onclick="createBooking(${workspace.id})">Book Workspace</button>
                    <button class="secondary" onclick="loadReviewsForWorkspace(${workspace.id})">Show Reviews</button>
                </div>
            </article>
        `).join("");
    } catch (error) {
        setMessage("workspaceMessage", error.message, "error");
    }
}

async function createBooking(workspaceId) {
    const decoded = requireAuth();
    setMessage("bookingStatus", "Creating booking...");

    try {
        const windowData = validateBookingWindow();
        const workspace = availableWorkspaces.find(item => item.id === workspaceId);
        if (!workspace) {
            throw new Error("This workspace is no longer available. Search again.");
        }

        const hours = calculateHours(windowData.startDate, windowData.endDate);
        const total = hours * Number(workspace.pricePerHour || 0);
        const confirmed = window.confirm(
            `Confirm booking\n\n` +
            `Workspace: ${workspace.name}\n` +
            `From: ${formatDateTime(windowData.startDate)}\n` +
            `To: ${formatDateTime(windowData.endDate)}\n` +
            `Duration: ${hours.toFixed(2)} hours\n` +
            `Total: ${formatCurrency(total)}`
        );

        if (!confirmed) {
            setMessage("bookingStatus", "Booking was cancelled before submission.");
            return;
        }

        const booking = await requestJson(`${BOOKING_API}/create`, {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify({
                userId: decoded.userId,
                workspaceId,
                startTime: windowData.start,
                endTime: windowData.end
            })
        });

        document.getElementById("bookingStatus").textContent = "Booking created";
        document.getElementById("latestBooking").innerHTML = `
            <strong>${booking.workspaceName}</strong>
            <div class="meta">
                <span>Booking ID: ${booking.bookingId}</span>
                <span>From: ${formatDateTime(booking.startTime)}</span>
                <span>To: ${formatDateTime(booking.endTime)}</span>
                <span>Total: ${formatCurrency(booking.totalAmount)}</span>
                <span>Invoice: ${booking.invoiceNumber || "-"}</span>
                <span>Status: ${booking.status}</span>
            </div>
        `;
    } catch (error) {
        document.getElementById("bookingStatus").textContent = error.message;
    }
}

async function addReview() {
    const decoded = requireAuth();
    const workspaceId = Number(document.getElementById("reviewWorkspaceId").value);

    try {
        await requestJson(`${REVIEW_API}/add`, {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify({
                userId: decoded.userId,
                workspaceId,
                rating: Number(document.getElementById("reviewRating").value),
                comment: document.getElementById("reviewComment").value.trim()
            })
        });
        setMessage("reviewMessage", "Review submitted.", "success");
        await loadReviewsForWorkspace(workspaceId);
    } catch (error) {
        setMessage("reviewMessage", error.message, "error");
    }
}

async function loadReviewsFromInput() {
    const workspaceId = Number(document.getElementById("reviewWorkspaceId").value);
    await loadReviewsForWorkspace(workspaceId);
}

async function loadReviewsForWorkspace(workspaceId) {
    const list = document.getElementById("reviewList");
    if (!list) return;

    if (!workspaceId) {
        list.innerHTML = `<p class="message error">Enter a valid workspace ID first.</p>`;
        return;
    }

    list.innerHTML = "Loading reviews...";

    try {
        const reviews = await requestJson(`${REVIEW_API}/workspace/${workspaceId}`, {
            method: "GET",
            headers: authHeaders()
        });

        const userIds = [...new Set(reviews.map(review => review.userId).filter(Boolean))];
        let usernameMap = {};
        if (userIds.length) {
            const query = userIds.map(id => `ids=${encodeURIComponent(id)}`).join("&");
            usernameMap = await requestJson(`${USER_API}/lookup?${query}`, {
                method: "GET",
                headers: authHeaders()
            });
        }

        currentReviews = reviews.map(review => ({
            ...review,
            username: usernameMap[review.userId] || `User #${review.userId}`
        }));
        currentReviewOffset = 0;

        if (!currentReviews.length) {
            list.innerHTML = "<p class='muted'>No reviews for this workspace yet.</p>";
            return;
        }

        renderMoreReviews();
    } catch (error) {
        list.innerHTML = `<p class="message error">${error.message}</p>`;
    }
}

function renderMoreReviews() {
    const list = document.getElementById("reviewList");
    if (!list) return;

    const nextOffset = currentReviewOffset + REVIEW_PAGE_SIZE;
    const visibleReviews = currentReviews.slice(0, nextOffset);
    currentReviewOffset = visibleReviews.length;

    const cards = visibleReviews.map(review => `
        <article class="stack-item">
            <div class="row">
                <strong>${review.workspaceName}</strong>
                <span class="role-pill review-pill">${review.rating} / 5</span>
            </div>
            <span class="muted">User: ${review.username}</span>
            <p>${review.comment || "No comment provided."}</p>
        </article>
    `).join("");

    const hasMore = currentReviewOffset < currentReviews.length;
    const moreButton = hasMore
        ? `<button class="secondary" onclick="renderMoreReviews()">Show More</button>`
        : "";

    list.innerHTML = cards + moreButton;
}

async function addEmployee() {
    setMessage("adminMessage", "Adding user...");

    try {
        await requestJson(`${USER_API}/admin/create-user`, {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify({
                username: document.getElementById("empUser").value.trim(),
                email: document.getElementById("empEmail").value.trim(),
                password: document.getElementById("empPass").value,
                role: document.getElementById("empRole").value
            })
        });
        setMessage("adminMessage", "User added.", "success");
        loadAllUsers();
    } catch (error) {
        setMessage("adminMessage", error.message, "error");
    }
}

async function loadAllUsers() {
    const list = document.getElementById("userList");
    if (!list) return;
    list.innerHTML = "Loading...";

    const decoded = decodeToken();

    try {
        const users = await requestJson(USER_API, {
            method: "GET",
            headers: authHeaders()
        });
        list.innerHTML = users.map(user => `
            <article class="stack-item">
                <div class="row">
                    <strong>${user.username}</strong>
                    <span class="role-pill">${user.role}</span>
                </div>
                <span class="muted">${user.email}</span>
                ${decoded && decoded.role === "ADMIN" && user.role !== "ADMIN" ? `<button class="danger" onclick="deleteUser(${user.id})">Delete</button>` : ""}
            </article>
        `).join("");
    } catch (error) {
        list.innerHTML = `<p class="message error">${error.message}</p>`;
    }
}

async function deleteUser(userId) {
    if (!confirm("Delete this user?")) return;

    try {
        await fetch(`${USER_API}/admin/delete/${userId}`, {
            method: "DELETE",
            headers: authHeaders()
        });
        loadAllUsers();
    } catch (error) {
        setMessage("adminMessage", error.message, "error");
    }
}

async function addWorkspace() {
    setMessage("workspaceAdminMessage", "Adding workspace...");

    try {
        await requestJson(`${WORKSPACE_API}/add`, {
            method: "POST",
            headers: authHeaders(),
            body: JSON.stringify({
                name: document.getElementById("workspaceName").value.trim(),
                type: document.getElementById("workspaceType").value,
                pricePerHour: Number(document.getElementById("workspacePrice").value),
                capacity: Number(document.getElementById("workspaceCapacity").value),
                description: document.getElementById("workspaceDescription").value.trim(),
                available: true
            })
        });
        setMessage("workspaceAdminMessage", "Workspace added.", "success");
    } catch (error) {
        setMessage("workspaceAdminMessage", error.message, "error");
    }
}

async function loadInvoicesData() {
    const invoices = await requestJson(`${INVOICE_API}/all`, {
        method: "GET",
        headers: authHeaders()
    });
    invoiceByBookingId = new Map(invoices.map(invoice => [Number(invoice.bookingId), invoice]));
    return invoices;
}

function renderBookingActions(booking) {
    const invoice = invoiceByBookingId.get(Number(booking.bookingId));
    const isPaid = invoice?.paymentStatus === "PAID";

    if (booking.status === "CANCELLED") {
        return `<span class="muted">This booking is cancelled</span>`;
    }

    if (isPaid) {
        return `<span class="muted">Paid booking cannot be cancelled</span>`;
    }

    return `<button class="danger" onclick="cancelBooking(${booking.bookingId})">Cancel Booking</button>`;
}

async function loadAllBookings() {
    const list = document.getElementById("bookingList");
    if (!list) return;
    list.innerHTML = "Loading...";

    try {
        await loadInvoicesData();
        const bookings = await requestJson(`${BOOKING_API}/all`, {
            method: "GET",
            headers: authHeaders()
        });
        list.innerHTML = bookings.map(booking => {
            const invoice = invoiceByBookingId.get(Number(booking.bookingId));
            return `
                <article class="stack-item">
                    <h3>${booking.workspaceName}</h3>
                    <div class="meta">
                        <span>Booking ID: ${booking.bookingId}</span>
                        <span>From: ${formatDateTime(booking.startTime)}</span>
                        <span>To: ${formatDateTime(booking.endTime)}</span>
                        <span>Status: ${booking.status}</span>
                        <span>Total: ${formatCurrency(booking.totalAmount)}</span>
                        <span>Invoice: ${invoice?.invoiceNumber || "-"}</span>
                        <span>Payment: ${invoice?.paymentStatus || "PENDING"}</span>
                    </div>
                    ${renderBookingActions(booking)}
                </article>
            `;
        }).join("") || "<p class='muted'>No bookings found.</p>";
    } catch (error) {
        list.innerHTML = `<p class="message error">${error.message}</p>`;
    }
}

async function cancelBooking(bookingId) {
    if (!confirm("Cancel this booking?")) return;

    try {
        await requestJson(`${BOOKING_API}/${bookingId}/cancel`, {
            method: "PATCH",
            headers: authHeaders()
        });
        loadAllBookings();
        loadAllInvoices();
    } catch (error) {
        alert(error.message);
    }
}

async function loadAllInvoices() {
    const list = document.getElementById("invoiceList");
    if (!list) return;
    list.innerHTML = "Loading...";

    try {
        const invoices = await loadInvoicesData();
        list.innerHTML = invoices.map(invoice => `
            <article class="stack-item">
                <div class="row">
                    <strong>${invoice.invoiceNumber}</strong>
                    <span class="role-pill ${invoice.paymentStatus === "PAID" ? "paid-pill" : ""}">${invoice.paymentStatus}</span>
                </div>
                <div class="meta">
                    <span>Booking ID: ${invoice.bookingId}</span>
                    <span>Amount: ${formatCurrency(invoice.amount)}</span>
                    <span>Issued At: ${formatDateTime(invoice.issuedAt)}</span>
                </div>
                ${invoice.paymentStatus !== "PAID" ? `<button onclick="markInvoicePaid(${invoice.id})">Mark as PAID</button>` : `<span class="muted">Paid</span>`}
            </article>
        `).join("") || "<p class='muted'>No invoices found.</p>";
    } catch (error) {
        list.innerHTML = `<p class="message error">${error.message}</p>`;
    }
}

async function markInvoicePaid(invoiceId) {
    if (!confirm("Mark this invoice as PAID? Paid bookings cannot be cancelled.")) return;

    try {
        await requestJson(`${INVOICE_API}/${invoiceId}/payment-status?status=PAID`, {
            method: "PATCH",
            headers: authHeaders()
        });
        loadAllInvoices();
        loadAllBookings();
    } catch (error) {
        alert(error.message);
    }
}
