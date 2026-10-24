# Expenzo — Employee Advance & Expense Settlement Management System

A production-ready full-stack enterprise financial web application built with **Spring Boot 3 (Java 17+)**, **MySQL**, **Spring Security (JWT + BCrypt)**, and responsive **HTML5/CSS3/JavaScript**.

Designed specifically for enterprise cash advance disbursals, exact post-activity settlement reconciliation, and direct out-of-pocket reimbursement claims.

---

## 🏗️ System Architecture & Technologies

- **Backend Framework**: Spring Boot 3.2.5
- **Language**: Java 17+
- **Security**: Spring Security with stateless JWT (JSON Web Tokens) and BCrypt password encryption
- **Database**: MySQL 8.x (`expenzo_db`) with Spring Data JPA & Hibernate
- **Validation**: Jakarta Bean Validation (`@Valid`, `@NotNull`, `@DecimalMin`, `@Email`)
- **Financial Calculations**: Strict `BigDecimal` arithmetic for exact penny/rupee precision (avoids floating-point errors)
- **Frontend**: Responsive HTML5, Modern CSS3 with Design Tokens, Vanilla JavaScript Fetch Client
- **Testing**: JUnit 5, Mockito, Spring Boot Test suite with in-memory H2 database

---

## 📁 Project Structure

```
EMPLOYE/
├── pom.xml                                  # Maven dependencies (Spring Boot, JJWT, MySQL, etc.)
├── mvnw.cmd / .mvn/                         # Maven Wrapper for Windows
├── schema.sql                               # MySQL DDL schema and indexes
├── index.html                               # Modern Expenzo UI (Landing, Employee Portal, Admin Console)
├── styles.css                               # Comprehensive stylesheet with design system & badges
├── app.js                                   # REST API client & workflow handlers
├── src/
│   ├── main/
│   │   ├── java/com/expenzo/
│   │   │   ├── ExpenzoApplication.java      # Spring Boot Main Entry Point
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java      # JWT filter chain, CSRF disable, CORS, RBAC
│   │   │   │   ├── WebConfig.java           # Static resource mappings & uploads directory
│   │   │   │   └── DataInitializer.java     # Seeds initial demo accounts and advances
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java      # Register, Login, Current User, Employee Directory
│   │   │   │   ├── AdvanceController.java   # Request, Review, Disburse advances
│   │   │   │   ├── ExpenseController.java   # Record bills against advance
│   │   │   │   ├── SettlementController.java# Settle advance, Audit, Complete
│   │   │   │   ├── ReimbursementClaimController.java # Direct out-of-pocket claims
│   │   │   │   ├── DashboardController.java # Summary metrics for staff & admin
│   │   │   │   ├── ReportController.java    # Financial Audit CSV export
│   │   │   │   └── ReceiptController.java   # Secure receipt download & preview
│   │   │   ├── dto/                         # Request & Response Data Transfer Objects
│   │   │   ├── entity/                      # JPA Entities (User, AdvanceRequest, Expense, Claim, Settlement)
│   │   │   ├── enums/                       # AdvanceStatus, SettlementStatus, Outcome, Roles, etc.
│   │   │   ├── exception/                   # GlobalExceptionHandler, Custom Exceptions
│   │   │   ├── repository/                  # Spring Data JPA Repositories
│   │   │   ├── security/                    # JwtUtil, JwtAuthenticationFilter, CustomUserDetailsService
│   │   │   └── service/                     # Business logic with BigDecimal math & storage
│   │   └── resources/
│   │       ├── application.properties       # Server port 8080, DB config, upload limits
│   │       ├── schema.sql                   # Embedded schema definition
│   │       └── static/                      # Static resources served by Spring Boot (index.html, styles.css, app.js)
│   └── test/
│       └── java/com/expenzo/
│           ├── SettlementCalculationTest.java # Rigorous unit tests for BigDecimal reconciliation
│           ├── AdvanceWorkflowTest.java      # Advance request -> approval -> disbursal flow tests
│           ├── SecurityOwnershipTest.java    # Data isolation and authorization tests
│           └── ExpenzoApplicationTests.java  # Spring Boot Context loads test
```

---

## 🚀 Step-by-Step Setup Guide

### 1. Prerequisites
- **Java Development Kit (JDK)**: JDK 17 or higher installed (`java -version`).
- **MySQL Server**: MySQL 8.x installed and running.
- **Maven**: Optional (Maven Wrapper `mvnw.cmd` is included in this repository).

---

### 2. Database Setup

1. Open your MySQL client (Command Line or MySQL Workbench):
   ```sql
   mysql -u root -p
   ```
2. Create the `expenzo_db` database:
   ```sql
   CREATE DATABASE IF NOT EXISTS expenzo_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. *(Optional)* Run [schema.sql](file:///c:/Users/dasar/OneDrive/Desktop/EMPLOYE/schema.sql) if you wish to initialize tables manually. Spring Boot will also automatically validate and create tables via Hibernate (`ddl-auto=update`).

4. Check your database password in [application.properties](file:///c:/Users/dasar/OneDrive/Desktop/EMPLOYE/src/main/resources/application.properties):
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/expenzo_db?createDatabaseIfNotExist=true&useSSL=false
   spring.datasource.username=root
   spring.datasource.password=your_mysql_password
   ```
   *(You can also set the environment variable `DB_PASSWORD=your_mysql_password` without modifying the file).*

---

### 3. Running the Application

Open a Command Prompt or PowerShell in this project folder (`c:\Users\dasar\OneDrive\Desktop\EMPLOYE`):

```bash
# Using Maven Wrapper (Windows):
.\mvnw.cmd clean spring-boot:run

# Or if you have Apache Maven installed globally:
mvn clean spring-boot:run
```

Once you see:
```text
Started ExpenzoApplication in X.XXX seconds (process running for ...)
```
Open your browser and visit: **[http://localhost:8080](http://localhost:8080)**

---

## 🔑 Default Accounts (Auto-Provisioned)

When the application launches for the first time, `DataInitializer.java` automatically seeds the following credentials:

| Role | Name | Email / Username | Password | Notes |
|---|---|---|---|---|
| **Manager / Admin** | System Administrator | `admin@expenzo.com` | `Admin@1234` | Full review, disbursal, audit & CSV access |
| **Manager / Admin** | Corporate Manager | `admin@company.com` | `admin123` | Master manager console |
| **Employee** | Sarah Jenkins | `sarah@company.com` | `pass123` | Pre-loaded with Disbursed Advance `#ADV-101` (₹15,000) |
| **Employee** | Alex Rivera | `alex@company.com` | `pass123` | Pre-loaded with Pending Advance `#ADV-102` (₹8,500) |

> 💡 **Self-Registration**: New employees can also register directly via the **"Register User"** modal on the landing page or internally via the Admin Console's **"Employee Directory"** tab.

---

## 💼 Core Business Workflows

### Workflow 1: Cash/Travel Advance & Settlement Reconciliation
1. **Employee Requests Advance**:
   - Employee clicks **"Request Cash Advance"**, enters ₹25,000 for "Client Visit to Bangalore", dates, and destination.
   - Status: `PENDING`.
2. **Manager Reviews & Disburses**:
   - Manager logs into the Admin Console, reviews the request, and clicks **Approve**.
   - When company funds are transferred, manager clicks **"Record Disbursal"** and enters ₹25,000.
   - Status: `DISBURSED`.
3. **Employee Logs Expenses**:
   - During/after travel, employee clicks **"Log Bill"** against Advance `#ADV-101`.
   - Records bills (Hotel ₹12,000, Flight ₹9,000, Meals ₹3,000) and uploads receipt files.
4. **Reconciliation & Settlement**:
   - Employee clicks **"Reconcile & Settle"**.
   - Live reconciliation compares disbursed cash vs approved bills using exact `BigDecimal` calculation:
     - **RETURN DUE**: Disbursed cash exceeds approved spends. Employee returns leftover money.
     - **ADDITIONAL PAYMENT DUE**: Approved spends exceed advance. Company pays the difference.
     - **BALANCED (₹0.00)**: Advance and expenditures match evenly.
5. **Manager Audits & Ledger Closure**:
   - Manager reviews receipts and verifies settlement.
   - Manager records payment reference (`BANK-NEFT-REF`) and clicks **"Complete & Close"**.
   - Advance status becomes `SETTLED`.

---

### Workflow 2: Direct Reimbursement Claims (Personal Out-of-Pocket Money)
- For everyday business expenses paid using employee's personal funds (e.g., software license, client dinner).
- Employee clicks **"Submit Direct Expense"**, enters category, amount, date, and attaches receipt.
- Manager reviews: **Approve** or **Reject**.
- Once accounts team initiates bank transfer, manager clicks **"Record Payment"** $\rightarrow$ Status changes to `PAID`.

---

### Workflow 3: Receipt Uploads & Security
- Supports `.pdf`, `.jpg`, `.jpeg`, and `.png` file formats up to 10MB.
- Stored safely in the application upload directory (`./uploads/receipts/`).
- Only authenticated users can access receipts through the protected `/api/receipts/{filename}` endpoint.

---

### Workflow 4: Financial Audit CSV Export
- Managers can download a comprehensive audit CSV at `/api/reports/csv` or by clicking **"Export CSV"** in the Admin Console.
- Contains all disbursements, approved spends, and settlement reconciliation statuses.

---

## 🧪 Running Automated Tests

Run the test suite using Maven:

```bash
.\mvnw.cmd test
```

The test suite covers:
1. `SettlementCalculationTest`: Verifies `RETURN_DUE`, `ADDITIONAL_PAYMENT_DUE`, and `BALANCED` outcomes with zero rounding errors.
2. `AdvanceWorkflowTest`: Ensures lifecycle from `PENDING` $\rightarrow$ `APPROVED` $\rightarrow$ `DISBURSED` $\rightarrow$ `SETTLED`.
3. `SecurityOwnershipTest`: Ensures staff cannot view or modify other employees' records.

---

## 🛠️ Common Troubleshooting

- **Port 8080 already in use**:
  Change `server.port=8081` in `src/main/resources/application.properties` and visit `http://localhost:8081`.
- **MySQL Access Denied**:
  Confirm your password in `application.properties` or create the database with:
  `mysql -u root -p -e "CREATE DATABASE expenzo_db;"`
- **Receipt file upload fails**:
  Ensure the directory `./uploads/receipts/` has write permissions (it will be created automatically upon server startup).
