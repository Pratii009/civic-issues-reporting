import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-app.js";
import { getDatabase, ref, onValue, update } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-database.js";
import { getAuth, onAuthStateChanged, signInWithEmailAndPassword, signOut } from "https://www.gstatic.com/firebasejs/11.0.1/firebase-auth.js";

// Your Firebase config
const firebaseConfig = {
  apiKey: "AIzaSyAnWbun5mUj7lm-e0krFJq7Z5xFxT1WA4I",
  authDomain: "civic-17a38.firebaseapp.com",
  databaseURL: "https://civic-17a38-default-rtdb.firebaseio.com/",
  projectId: "civic-17a38",
  storageBucket: "civic-17a38.appspot.com",
  messagingSenderId: "140025709431",
  appId: "1:140025709431:web:0178ec802c4fa477d89772",
  measurementId: "G-2KSTKSJKCX"
};
const app = initializeApp(firebaseConfig);
const db = getDatabase(app);
const auth = getAuth(app);

const loginContainer = document.getElementById("login-container");
const dashboard = document.getElementById("dashboard");
const loginForm = document.getElementById("login-form");
const loginBtn = document.getElementById("login-btn");
const loginError = document.getElementById("login-error");
const reportsBody = document.getElementById("reports-body");
const totalIssues = document.getElementById("total-issues");
const resolvedIssues = document.getElementById("resolved-issues");
const pendingIssues = document.getElementById("pending-issues");
const themeToggle = document.getElementById("theme-toggle");
const logoutBtn = document.getElementById("logout-btn");

// LOGIN
loginForm.addEventListener("submit", (e) => {
  e.preventDefault();
  const email = document.getElementById("admin-email").value;
  const password = document.getElementById("admin-password").value;
  login(email, password);
});

onAuthStateChanged(auth, (user) => {
  if (user) {
    loginContainer.style.display = "none";
    dashboard.style.display = "block";
    fetchReports();
  } else {
    dashboard.style.display = "none";
    loginContainer.style.display = "block";
  }
});

function login(email, password) {
  signInWithEmailAndPassword(auth, email, password)
    .then(() => {
      loginError.textContent = "";
    })
    .catch((error) => {
      loginError.textContent = error.message;
    });
}

// LOGOUT
logoutBtn.addEventListener("click", () => {
  signOut(auth)
    .then(() => {
      dashboard.style.display = "none";
      loginContainer.style.display = "block";
    })
    .catch((error) => {
      alert("Logout error: " + error.message);
    });
});

// THEME TOGGLE
themeToggle.addEventListener("click", () => {
  if (document.body.classList.contains("light-theme")) {
    document.body.classList.remove("light-theme");
    document.body.classList.add("dark-theme");
    themeToggle.textContent = "Switch to Light Theme";
  } else {
    document.body.classList.remove("dark-theme");
    document.body.classList.add("light-theme");
    themeToggle.textContent = "Switch to Dark Theme";
  }
});

// LOAD REPORTS
function fetchReports() {
  const reportsRef = ref(db, "reports");
  onValue(reportsRef, (snapshot) => {
    reportsBody.innerHTML = "";
    let resolvedCount = 0;
    let pendingCount = 0;

    if (!snapshot.exists()) {
      totalIssues.textContent = 0;
      resolvedIssues.textContent = 0;
      pendingIssues.textContent = 0;
      return;
    }
    const data = snapshot.val();
    const ids = Object.keys(data);
    totalIssues.textContent = ids.length;

    ids.forEach(id => {
      const r = data[id];
      const statusText = r.status ? r.status : "Pending";
      const workerText = r.assignedWorker ? r.assignedWorker : "Unassigned";

      if (statusText === "resolved") resolvedCount++;
      else pendingCount++;

      const locationText =
    (typeof r.latitude !== 'undefined' && typeof r.longitude !== 'undefined')
    ? `Lat: ${r.latitude}, Lng: ${r.longitude}`
    : (r.locationName ? r.locationName : "Unknown");

      const row = `
        <tr>
          <td>${id}</td>
          <td>${r.description}</td>
          <td>${locationText}</td>
          <td>${new Date(r.timestamp).toLocaleString()}</td>
          <td>${statusText}</td>
          <td>
            <div style="margin-bottom:5px;font-weight:600;">${workerText}</div>
            <div class="assign-inline">
              <input type="text" id="worker-${id}" placeholder="Assign worker">
              <button onclick="assignWorker('${id}')">Assign</button>
            </div>
          </td>
          <td>
            <button onclick="markResolved('${id}')">Mark Resolved</button>
          </td>
        </tr>
      `;
      reportsBody.innerHTML += row;
    });
    resolvedIssues.textContent = resolvedCount;
    pendingIssues.textContent = pendingCount;
  });
}

// MARK RESOLVED
window.markResolved = function(reportId) {
  const reportRef = ref(db, "reports/" + reportId);
  update(reportRef, { status: "resolved" })
    .then(() => {
      alert("Marked as resolved!");
    })
    .catch((err) => {
      alert("Error: " + err.message);
    });
};

// ASSIGN WORKER
window.assignWorker = function(reportId) {
  const workerInput = document.getElementById("worker-" + reportId);
  const workerName = workerInput.value.trim();
  if (!workerName) {
    alert("Enter a worker name to assign!");
    return;
  }
  const reportRef = ref(db, "reports/" + reportId);
  update(reportRef, { assignedWorker: workerName })
    .then(() => {
      alert("Worker assigned!");
      workerInput.value = "";
    })
    .catch((err) => {
      alert("Error: " + err.message);
    });
};
