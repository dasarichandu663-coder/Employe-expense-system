import java.util.*;
import java.io.*;
import java.time.LocalDate;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static ArrayList<Employee> employees = new ArrayList<>();
    static ArrayList<Expense> expenses = new ArrayList<>();
    static ArrayList<AdvanceRequest> advances = new ArrayList<>();
    static ArrayList<Settlement> settlements = new ArrayList<>();

    static final String ADMIN_USERNAME = "admin";
    static final String ADMIN_PASSWORD = "admin123";

    public static void main(String[] args) {
        loadData();

        while (true) {
            System.out.println("\n=======================================================");
            System.out.println("   EXPENZO - ENTERPRISE ADVANCE & EXPENSE SYSTEM      ");
            System.out.println("=======================================================");
            System.out.println("1. Employee Login");
            System.out.println("2. Admin Login");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");

            int choice = readInt();

            switch (choice) {
                case 1 -> employeeLogin();
                case 2 -> adminLogin();
                case 3 -> {
                    saveData();
                    System.out.println("Application closed. Data saved.");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    static int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static double readDouble() {
        try {
            return Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static void employeeLogin() {
        System.out.print("Email: ");
        String email = sc.nextLine().trim().toLowerCase();

        System.out.print("Password: ");
        String password = sc.nextLine();

        for (Employee e : employees) {
            if (e.email.equalsIgnoreCase(email) && e.password.equals(password)) {
                employeeMenu(e);
                return;
            }
        }

        System.out.println("Invalid email or password. Please contact Admin if you need an account.");
    }

    static void employeeMenu(Employee employee) {
        while (true) {
            System.out.println("\n-------------------------------------------------------");
            System.out.println(" EMPLOYEE DASHBOARD: " + employee.name + " (" + employee.email + ")");
            System.out.println("-------------------------------------------------------");
            System.out.println("--- DIRECT EXPENSES ---");
            System.out.println("1. Submit Direct Expense");
            System.out.println("2. View My Direct Expenses");
            System.out.println("3. Delete Pending Direct Expense");
            System.out.println("--- ADVANCE & SETTLEMENT WORKFLOW ---");
            System.out.println("4. Request Travel / Cash Advance");
            System.out.println("5. View My Advance Requests & Balances");
            System.out.println("6. Submit Settlement for an Approved Advance");
            System.out.println("7. View My Settlement Reconciliations");
            System.out.println("8. Logout");
            System.out.print("Choose option: ");

            int choice = readInt();

            switch (choice) {
                case 1 -> addExpense(employee);
                case 2 -> viewMyExpenses(employee);
                case 3 -> deleteExpense(employee);
                case 4 -> requestAdvance(employee);
                case 5 -> viewMyAdvances(employee);
                case 6 -> submitSettlement(employee);
                case 7 -> viewMySettlements(employee);
                case 8 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // --- Direct Expense methods ---
    static void addExpense(Employee employee) {
        System.out.print("Expense title: ");
        String title = sc.nextLine().trim();

        System.out.print("Category (Travel/Food/Office/Other): ");
        String category = sc.nextLine().trim();

        System.out.print("Amount in ₹: ");
        double amount = readDouble();

        System.out.print("Description: ");
        String description = sc.nextLine().trim();

        if (title.isEmpty() || category.isEmpty() || amount <= 0 || !Double.isFinite(amount)) {
            System.out.println("Enter valid expense details.");
            return;
        }

        int nextId = expenses.stream().mapToInt(e -> e.id).max().orElse(0) + 1;
        Expense expense = new Expense(
            nextId,
            employee.email,
            title,
            category,
            amount,
            description,
            LocalDate.now().toString()
        );

        expenses.add(expense);
        saveData();
        System.out.println("Expense #" + nextId + " submitted successfully. Status: Pending");
    }

    static void viewMyExpenses(Employee employee) {
        boolean found = false;
        System.out.println("\n--- MY DIRECT EXPENSES ---");
        for (Expense e : expenses) {
            if (e.employeeEmail.equalsIgnoreCase(employee.email)) {
                e.display();
                found = true;
            }
        }
        if (!found) System.out.println("No direct expenses found.");
    }

    static void deleteExpense(Employee employee) {
        System.out.print("Enter expense ID to cancel: ");
        int id = readInt();

        for (Expense e : expenses) {
            if (e.id == id && e.employeeEmail.equalsIgnoreCase(employee.email)) {
                if (e.status.equals("Pending")) {
                    expenses.remove(e);
                    saveData();
                    System.out.println("Expense deleted successfully.");
                } else {
                    System.out.println("Only pending expenses can be deleted.");
                }
                return;
            }
        }
        System.out.println("Expense not found.");
    }

    // --- Advance Request Workflow ---
    static void requestAdvance(Employee employee) {
        System.out.print("Purpose of Advance (e.g., Client Visit to Mumbai, Tech Expo): ");
        String purpose = sc.nextLine().trim();

        System.out.print("Requested Amount in ₹: ");
        double amount = readDouble();

        System.out.print("Estimated travel/event date (YYYY-MM-DD) or notes: ");
        String notes = sc.nextLine().trim();

        if (purpose.isEmpty() || amount <= 0 || !Double.isFinite(amount)) {
            System.out.println("Error: Valid purpose and positive amount required.");
            return;
        }

        int nextId = advances.stream().mapToInt(a -> a.id).max().orElse(0) + 1;
        AdvanceRequest advance = new AdvanceRequest(
            nextId,
            employee.email,
            purpose,
            amount,
            LocalDate.now().toString(),
            notes
        );

        advances.add(advance);
        saveData();
        System.out.printf("Advance Request #%d of ₹%.2f created! Status: Pending Approval.%n", nextId, amount);
    }

    static void viewMyAdvances(Employee employee) {
        boolean found = false;
        System.out.println("\n--- MY CASH / TRAVEL ADVANCES ---");
        for (AdvanceRequest a : advances) {
            if (a.employeeEmail.equalsIgnoreCase(employee.email)) {
                a.display();
                found = true;
            }
        }
        if (!found) System.out.println("No advance requests recorded.");
    }

    // --- Settlement Workflow ---
    static void submitSettlement(Employee employee) {
        ArrayList<AdvanceRequest> eligible = new ArrayList<>();
        for (AdvanceRequest a : advances) {
            if (a.employeeEmail.equalsIgnoreCase(employee.email) && a.status.equals("Approved")) {
                eligible.add(a);
            }
        }

        if (eligible.isEmpty()) {
            System.out.println("No active approved advances found requiring settlement.");
            System.out.println("Note: Only advances approved by Admin can be settled.");
            return;
        }

        System.out.println("\nActive Approved Advances available for Settlement:");
        for (AdvanceRequest a : eligible) {
            System.out.printf("  [Advance ID: %d] Purpose: %s | Advance Amount: ₹%.2f (Approved on: %s)%n",
                a.id, a.purpose, a.amount, a.disbursedDate);
        }

        System.out.print("Enter Advance ID you are settling: ");
        int advanceId = readInt();

        AdvanceRequest selectedAdvance = null;
        for (AdvanceRequest a : eligible) {
            if (a.id == advanceId) {
                selectedAdvance = a;
                break;
            }
        }

        if (selectedAdvance == null) {
            System.out.println("Invalid Advance ID or not approved.");
            return;
        }

        System.out.print("Enter Total Actual Amount Spent in ₹: ");
        double actualSpent = readDouble();
        if (actualSpent < 0 || !Double.isFinite(actualSpent)) {
            System.out.println("Invalid actual spent amount.");
            return;
        }

        System.out.print("Detailed reconciliation notes / expense breakdown: ");
        String notes = sc.nextLine().trim();

        double balance = actualSpent - selectedAdvance.amount;

        int nextId = settlements.stream().mapToInt(s -> s.id).max().orElse(0) + 1;
        Settlement settlement = new Settlement(
            nextId,
            selectedAdvance.id,
            employee.email,
            selectedAdvance.purpose,
            selectedAdvance.amount,
            actualSpent,
            balance,
            LocalDate.now().toString(),
            notes
        );

        settlements.add(settlement);
        selectedAdvance.status = "Settlement Pending";
        saveData();

        System.out.println("\n=== SETTLEMENT SUMMARY ===");
        System.out.printf("Advance Amount Received : ₹%.2f%n", selectedAdvance.amount);
        System.out.printf("Total Actual Spent       : ₹%.2f%n", actualSpent);
        if (balance > 0) {
            System.out.printf("RECONCILIATION RESULT    : Company owes Employee ₹%.2f (Claim reimbursement)%n", balance);
        } else if (balance < 0) {
            System.out.printf("RECONCILIATION RESULT    : Employee must Refund to Company ₹%.2f (Unused advance)%n", Math.abs(balance));
        } else {
            System.out.println("RECONCILIATION RESULT    : Exactly balanced (₹0.00 difference).");
        }
        System.out.println("Settlement submitted for Admin verification.");
    }

    static void viewMySettlements(Employee employee) {
        boolean found = false;
        System.out.println("\n--- MY SETTLEMENT RECONCILIATIONS ---");
        for (Settlement s : settlements) {
            if (s.employeeEmail.equalsIgnoreCase(employee.email)) {
                s.display();
                found = true;
            }
        }
        if (!found) System.out.println("No settlements submitted.");
    }

    // --- Admin Portal Methods ---
    static void adminLogin() {
        System.out.print("Admin username: ");
        String username = sc.nextLine();

        System.out.print("Admin password: ");
        String password = sc.nextLine();

        if (username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD)) {
            adminMenu();
        } else {
            System.out.println("Invalid admin credentials.");
        }
    }

    static void adminMenu() {
        while (true) {
            System.out.println("\n=======================================================");
            System.out.println("       ADMIN DASHBOARD - OVERSIGHT & DIRECTORY        ");
            System.out.println("=======================================================");
            System.out.println("--- EMPLOYEE MANAGEMENT (INSIDE PORTAL) ---");
            System.out.println("1. Register / Onboard New Employee");
            System.out.println("2. View Registered Employees Directory");
            System.out.println("--- DIRECT EXPENSES ---");
            System.out.println("3. View All Direct Expenses");
            System.out.println("4. Approve / Reject Direct Expense");
            System.out.println("--- ADVANCE REQUESTS ---");
            System.out.println("5. View All Advance Requests");
            System.out.println("6. Approve / Disburse Advance Request");
            System.out.println("7. Reject Advance Request");
            System.out.println("--- SETTLEMENT & RECONCILIATION ---");
            System.out.println("8. View All Advance Settlements");
            System.out.println("9. Approve & Finalize Settlement");
            System.out.println("10. Reject Settlement");
            System.out.println("11. Logout");
            System.out.print("Choose option: ");

            int choice = readInt();

            switch (choice) {
                case 1 -> adminRegisterEmployee();
                case 2 -> viewEmployeesDirectory();
                case 3 -> viewAllExpenses();
                case 4 -> reviewDirectExpense();
                case 5 -> viewAllAdvances();
                case 6 -> reviewAdvance("Approved");
                case 7 -> reviewAdvance("Rejected");
                case 8 -> viewAllSettlements();
                case 9 -> reviewSettlement("Approved");
                case 10 -> reviewSettlement("Rejected");
                case 11 -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    static void adminRegisterEmployee() {
        System.out.print("Enter employee full name: ");
        String name = sc.nextLine().trim();

        System.out.print("Enter corporate email: ");
        String email = sc.nextLine().trim().toLowerCase();

        System.out.print("Assign initial password: ");
        String password = sc.nextLine();

        if (name.isEmpty() || !email.contains("@") || password.length() < 4) {
            System.out.println("Error: Name required, valid email with '@', password at least 4 chars.");
            return;
        }

        for (Employee e : employees) {
            if (e.email.equalsIgnoreCase(email)) {
                System.out.println("Error: Email already registered in the directory.");
                return;
            }
        }

        employees.add(new Employee(name, email, password));
        saveData();
        System.out.println("Employee '" + name + "' successfully onboarded!");
    }

    static void viewEmployeesDirectory() {
        if (employees.isEmpty()) {
            System.out.println("No employees currently registered.");
            return;
        }
        System.out.println("\n--- CORPORATE EMPLOYEE DIRECTORY ---");
        for (Employee e : employees) {
            System.out.printf("Staff: %-20s | Email: %-25s | Password: %s%n", e.name, e.email, e.password);
        }
    }

    static void viewAllExpenses() {
        if (expenses.isEmpty()) {
            System.out.println("No direct expenses recorded.");
            return;
        }
        for (Expense e : expenses) e.display();
    }

    static void reviewDirectExpense() {
        System.out.print("Enter direct expense ID: ");
        int id = readInt();

        for (Expense e : expenses) {
            if (e.id == id) {
                if (!e.status.equals("Pending")) {
                    System.out.println("This expense has already been reviewed (" + e.status + ").");
                    return;
                }
                System.out.print("Approve or Reject? (A/R): ");
                String action = sc.nextLine().trim().toUpperCase();
                if (action.startsWith("A")) {
                    e.status = "Approved";
                    System.out.println("Expense approved.");
                } else if (action.startsWith("R")) {
                    e.status = "Rejected";
                    System.out.println("Expense rejected.");
                } else {
                    System.out.println("Cancelled.");
                    return;
                }
                saveData();
                return;
            }
        }
        System.out.println("Expense not found.");
    }

    static void viewAllAdvances() {
        if (advances.isEmpty()) {
            System.out.println("No advance requests found.");
            return;
        }
        System.out.println("\n--- ALL ADVANCE REQUESTS ---");
        for (AdvanceRequest a : advances) a.display();
    }

    static void reviewAdvance(String status) {
        System.out.print("Enter Advance Request ID: ");
        int id = readInt();

        for (AdvanceRequest a : advances) {
            if (a.id == id) {
                if (!a.status.equals("Pending")) {
                    System.out.println("This advance request has already been reviewed (" + a.status + ").");
                    return;
                }
                a.status = status;
                if (status.equals("Approved")) {
                    a.disbursedDate = LocalDate.now().toString();
                    System.out.printf("Advance #%d Approved and Disbursed for ₹%.2f!%n", a.id, a.amount);
                } else {
                    System.out.printf("Advance #%d Rejected.%n", a.id);
                }
                saveData();
                return;
            }
        }
        System.out.println("Advance request not found.");
    }

    static void viewAllSettlements() {
        if (settlements.isEmpty()) {
            System.out.println("No settlements submitted.");
            return;
        }
        System.out.println("\n--- ALL ADVANCE SETTLEMENTS ---");
        for (Settlement s : settlements) s.display();
    }

    static void reviewSettlement(String status) {
        System.out.print("Enter Settlement ID: ");
        int id = readInt();

        for (Settlement s : settlements) {
            if (s.id == id) {
                if (!s.status.equals("Pending Review")) {
                    System.out.println("This settlement has already been processed (" + s.status + ").");
                    return;
                }
                s.status = status.equals("Approved") ? "Approved & Settled" : "Rejected";

                for (AdvanceRequest a : advances) {
                    if (a.id == s.advanceId) {
                        if (status.equals("Approved")) {
                            a.status = "Settled";
                        } else {
                            a.status = "Approved";
                        }
                        break;
                    }
                }

                saveData();
                System.out.println("Settlement #" + s.id + " " + s.status + ".");
                return;
            }
        }
        System.out.println("Settlement not found.");
    }

    // --- Persistence (expenses.dat) ---
    static void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("expenses.dat"))) {
            out.writeObject(employees);
            out.writeObject(expenses);
            out.writeObject(advances);
            out.writeObject(settlements);
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    static void loadData() {
        File file = new File("expenses.dat");
        if (!file.exists()) {
            // Seed initial employees if file does not exist
            employees.add(new Employee("Sarah Jenkins", "sarah@company.com", "pass123"));
            employees.add(new Employee("Alex Rivera", "alex@company.com", "pass123"));
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            employees = (ArrayList<Employee>) in.readObject();
            expenses = (ArrayList<Expense>) in.readObject();
            try {
                advances = (ArrayList<AdvanceRequest>) in.readObject();
                settlements = (ArrayList<Settlement>) in.readObject();
            } catch (EOFException | OptionalDataException ignored) {
                advances = new ArrayList<>();
                settlements = new ArrayList<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Initialized fresh data store.");
        }
    }
}

class Employee implements Serializable {
    private static final long serialVersionUID = 2L;

    String name;
    String email;
    String password;

    Employee(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}

class Expense implements Serializable {
    private static final long serialVersionUID = 2L;

    int id;
    String employeeEmail;
    String title;
    String category;
    double amount;
    String description;
    String date;
    String status = "Pending";

    Expense(int id, String employeeEmail, String title,
            String category, double amount, String description,
            String date) {
        this.id = id;
        this.employeeEmail = employeeEmail;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.date = date;
    }

    void display() {
        System.out.println("\nExpense ID: " + id);
        System.out.println("Employee Email: " + employeeEmail);
        System.out.println("Title: " + title);
        System.out.println("Category: " + category);
        System.out.printf("Amount: ₹%.2f%n", amount);
        System.out.println("Description: " + description);
        System.out.println("Date: " + date);
        System.out.println("Status: " + status);
    }
}

class AdvanceRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    int id;
    String employeeEmail;
    String purpose;
    double amount;
    String requestDate;
    String disbursedDate = "-";
    String notes;
    String status = "Pending";

    AdvanceRequest(int id, String employeeEmail, String purpose, double amount, String requestDate, String notes) {
        this.id = id;
        this.employeeEmail = employeeEmail;
        this.purpose = purpose;
        this.amount = amount;
        this.requestDate = requestDate;
        this.notes = notes;
    }

    void display() {
        System.out.println("\nAdvance ID: " + id);
        System.out.println("Employee: " + employeeEmail);
        System.out.println("Purpose: " + purpose);
        System.out.printf("Advance Amount: ₹%.2f%n", amount);
        System.out.println("Requested on: " + requestDate + " | Disbursed on: " + disbursedDate);
        System.out.println("Notes: " + notes);
        System.out.println("Status: " + status);
    }
}

class Settlement implements Serializable {
    private static final long serialVersionUID = 1L;

    int id;
    int advanceId;
    String employeeEmail;
    String purpose;
    double advanceAmount;
    double actualSpent;
    double balance;
    String date;
    String notes;
    String status = "Pending Review";

    Settlement(int id, int advanceId, String employeeEmail, String purpose, double advanceAmount,
               double actualSpent, double balance, String date, String notes) {
        this.id = id;
        this.advanceId = advanceId;
        this.employeeEmail = employeeEmail;
        this.purpose = purpose;
        this.advanceAmount = advanceAmount;
        this.actualSpent = actualSpent;
        this.balance = balance;
        this.date = date;
        this.notes = notes;
    }

    void display() {
        System.out.println("\nSettlement ID: " + id + " (Linked Advance ID: #" + advanceId + ")");
        System.out.println("Employee: " + employeeEmail);
        System.out.println("Event/Purpose: " + purpose);
        System.out.printf("Advance Granted: ₹%.2f | Actual Spent: ₹%.2f%n", advanceAmount, actualSpent);
        if (balance > 0) {
            System.out.printf("Reconciliation: Company owes Employee ₹%.2f (Claim)%n", balance);
        } else if (balance < 0) {
            System.out.printf("Reconciliation: Employee owes Company ₹%.2f (Refund)%n", Math.abs(balance));
        } else {
            System.out.println("Reconciliation: Exactly Balanced (₹0.00)");
        }
        System.out.println("Date: " + date);
        System.out.println("Notes: " + notes);
        System.out.println("Status: " + status);
    }
}
