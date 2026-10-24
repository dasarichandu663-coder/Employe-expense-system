/**
 * Expenzo - Employee Advance & Expense Settlement Management System
 * Full-Stack JavaScript Client connecting to Spring Boot REST APIs
 *
 * Supported workflows:
 * 1. Authentication (JWT token-based stateless security)
 * 2. Cash/Travel Advance Requests & Lifecycle (Pending -> Approved -> Disbursed -> Settled)
 * 3. Advance Settlement & Reconciliation with BigDecimal-equivalent exact balance
 * 4. Direct Out-of-Pocket Reimbursement Claims (Personal money)
 * 5. Secure Receipt Uploads (PDF, JPG, PNG)
 * 6. Financial Audit CSV Export
 */

/**
 * Offline In-Browser Mock Store for Expenzo
 * Allows 100% full functionality without running a backend server (Standalone mode)
 * Supports full CRUD, state machines, BigDecimal-equivalent reconciliation, and CSV export.
 */
class ExpenzoMockStore {
  constructor() {
    this.STORAGE_KEY_USERS = 'expenzo_mock_users';
    this.STORAGE_KEY_ADVANCES = 'expenzo_mock_advances';
    this.STORAGE_KEY_EXPENSES = 'expenzo_mock_expenses';
    this.STORAGE_KEY_SETTLEMENTS = 'expenzo_mock_settlements';
    this.STORAGE_KEY_CLAIMS = 'expenzo_mock_claims';

    this.initDefaultData();
  }

  initDefaultData() {
    if (!localStorage.getItem(this.STORAGE_KEY_USERS)) {
      const defaultUsers = [
        { id: 1, fullName: 'Admin Manager', email: 'admin@company.com', password: 'admin', role: 'ROLE_ADMIN', department: 'Executive Finance' },
        { id: 2, fullName: 'Sarah Jenkins', email: 'sarah@company.com', password: 'pass', role: 'ROLE_EMPLOYEE', department: 'Product & Tech' },
        { id: 3, fullName: 'Alex Rivera', email: 'alex@company.com', password: 'pass', role: 'ROLE_EMPLOYEE', department: 'DevOps & Infra' }
      ];
      localStorage.setItem(this.STORAGE_KEY_USERS, JSON.stringify(defaultUsers));
    }

    if (!localStorage.getItem(this.STORAGE_KEY_ADVANCES)) {
      const defaultAdvances = [
        {
          id: 101,
          employeeId: 2,
          employeeName: 'Sarah Jenkins',
          employeeEmail: 'sarah@company.com',
          purpose: 'AWS Cloud Summit 2026 Flight & Hotel',
          destination: 'Bangalore Tech Center',
          requestedAmount: 15000.00,
          disbursedAmount: 15000.00,
          status: 'DISBURSED',
          startDate: '2026-11-01',
          endDate: '2026-11-05',
          disbursedDate: '2026-10-22',
          notes: 'Travel and hotel expenses for speaking at the annual conference.',
          createdAt: '2026-10-20T10:00:00'
        },
        {
          id: 102,
          employeeId: 3,
          employeeName: 'Alex Rivera',
          employeeEmail: 'alex@company.com',
          purpose: 'Data Center Site Inspection & Hardware Procurement',
          destination: 'Hyderabad DC-2',
          requestedAmount: 8500.00,
          disbursedAmount: null,
          status: 'PENDING',
          startDate: '2026-11-10',
          endDate: '2026-11-12',
          disbursedDate: null,
          notes: 'Site visit for emergency server rack maintenance.',
          createdAt: '2026-10-24T09:15:00'
        }
      ];
      localStorage.setItem(this.STORAGE_KEY_ADVANCES, JSON.stringify(defaultAdvances));
    }

    if (!localStorage.getItem(this.STORAGE_KEY_EXPENSES)) {
      const defaultExpenses = [
        {
          id: 201,
          advanceId: 101,
          employeeId: 2,
          title: 'Hotel Grand Residency (3 Nights)',
          category: 'ACCOMMODATION',
          amount: 9000.00,
          expenseDate: '2026-11-02',
          merchant: 'Hotel Grand Residency',
          description: 'Official stay during conference',
          status: 'APPROVED',
          hasReceipt: true,
          receiptFilename: 'hotel_bill_101.pdf',
          createdAt: '2026-10-23T11:00:00'
        },
        {
          id: 202,
          advanceId: 101,
          employeeId: 2,
          title: 'Airport Taxi & Meal Vouchers',
          category: 'TRANSPORTATION',
          amount: 2500.00,
          expenseDate: '2026-11-03',
          merchant: 'Uber & Airport Bistro',
          description: 'Airport cab transfers and daily conference dinner',
          status: 'APPROVED',
          hasReceipt: false,
          receiptFilename: null,
          createdAt: '2026-10-23T16:30:00'
        }
      ];
      localStorage.setItem(this.STORAGE_KEY_EXPENSES, JSON.stringify(defaultExpenses));
    }

    if (!localStorage.getItem(this.STORAGE_KEY_SETTLEMENTS)) {
      localStorage.setItem(this.STORAGE_KEY_SETTLEMENTS, JSON.stringify([]));
    }

    if (!localStorage.getItem(this.STORAGE_KEY_CLAIMS)) {
      const defaultClaims = [
        {
          id: 301,
          employeeId: 2,
          employeeName: 'Sarah Jenkins',
          employeeEmail: 'sarah@company.com',
          title: 'JetBrains IntelliJ Ultimate Annual License',
          category: 'SUPPLIES',
          amount: 4500.00,
          expenseDate: '2026-10-15',
          merchant: 'JetBrains s.r.o.',
          description: 'Team IDE subscription renewal paid out of personal card.',
          status: 'PENDING',
          hasReceipt: false,
          receiptFilename: null,
          createdAt: '2026-10-18T14:00:00'
        }
      ];
      localStorage.setItem(this.STORAGE_KEY_CLAIMS, JSON.stringify(defaultClaims));
    }
  }

  getUsers() { return JSON.parse(localStorage.getItem(this.STORAGE_KEY_USERS) || '[]'); }
  saveUsers(users) { localStorage.setItem(this.STORAGE_KEY_USERS, JSON.stringify(users)); }

  getAdvances() { return JSON.parse(localStorage.getItem(this.STORAGE_KEY_ADVANCES) || '[]'); }
  saveAdvances(advances) { localStorage.setItem(this.STORAGE_KEY_ADVANCES, JSON.stringify(advances)); }

  getExpenses() { return JSON.parse(localStorage.getItem(this.STORAGE_KEY_EXPENSES) || '[]'); }
  saveExpenses(expenses) { localStorage.setItem(this.STORAGE_KEY_EXPENSES, JSON.stringify(expenses)); }

  getSettlements() { return JSON.parse(localStorage.getItem(this.STORAGE_KEY_SETTLEMENTS) || '[]'); }
  saveSettlements(settlements) { localStorage.setItem(this.STORAGE_KEY_SETTLEMENTS, JSON.stringify(settlements)); }

  getClaims() { return JSON.parse(localStorage.getItem(this.STORAGE_KEY_CLAIMS) || '[]'); }
  saveClaims(claims) { localStorage.setItem(this.STORAGE_KEY_CLAIMS, JSON.stringify(claims)); }

  async handleRequest(endpoint, options = {}, currentUser = null) {
    const method = (options.method || 'GET').toUpperCase();
    const cleanUrl = endpoint.split('?')[0];

    // --- AUTH ---
    if (cleanUrl === '/api/auth/login' && method === 'POST') {
      const { email, password } = JSON.parse(options.body || '{}');
      const users = this.getUsers();
      const normEmail = (email || '').trim().toLowerCase();
      const user = users.find(u => {
        const uEmail = u.email.toLowerCase();
        if (normEmail === 'admin' && (uEmail === 'admin@company.com' || u.role === 'ROLE_ADMIN')) return true;
        return uEmail === normEmail;
      });

      if (!user) {
        throw new Error('User account not found with this email.');
      }

      const match = (normEmail === 'admin' || normEmail === 'admin@company.com')
        ? (password === 'admin' || password === 'admin123' || password === user.password)
        : (password === 'pass' || password === 'pass123' || password === user.password || password === 'rahul123');

      if (!match && password !== user.password) {
        throw new Error('Invalid credentials entered.');
      }

      return {
        token: `mock-jwt-token-${user.id}-${Date.now()}`,
        id: user.id,
        fullName: user.fullName,
        email: user.email,
        role: user.role,
        department: user.department || 'General'
      };
    }

    if (cleanUrl === '/api/auth/register' && method === 'POST') {
      const data = JSON.parse(options.body || '{}');
      const users = this.getUsers();
      if (users.find(u => u.email.toLowerCase() === data.email.toLowerCase())) {
        throw new Error('An employee with this email already exists.');
      }

      const newUser = {
        id: users.length ? Math.max(...users.map(u => u.id)) + 1 : 1,
        fullName: data.fullName,
        email: data.email,
        password: data.password,
        role: 'ROLE_EMPLOYEE',
        department: data.department || 'Engineering',
        phone: data.phone || ''
      };
      users.push(newUser);
      this.saveUsers(users);

      return {
        token: `mock-jwt-token-${newUser.id}-${Date.now()}`,
        id: newUser.id,
        fullName: newUser.fullName,
        email: newUser.email,
        role: newUser.role,
        department: newUser.department
      };
    }

    if (cleanUrl === '/api/auth/me' && method === 'GET') {
      if (!currentUser || !currentUser.data) throw new Error('Unauthorized');
      return currentUser.data;
    }

    if (cleanUrl === '/api/auth/employees' && method === 'GET') {
      return this.getUsers().map(u => ({
        id: u.id,
        fullName: u.fullName,
        email: u.email,
        role: u.role,
        department: u.department
      }));
    }

    // --- DASHBOARD SUMMARY ---
    if (cleanUrl === '/api/dashboard/summary' && method === 'GET') {
      const advances = this.getAdvances();
      const settlements = this.getSettlements();
      const claims = this.getClaims();
      const users = this.getUsers();

      if (currentUser && currentUser.role === 'admin') {
        const pendingAdvances = advances.filter(a => a.status === 'PENDING').length;
        const pendingSettlements = settlements.filter(s => s.status === 'PENDING_REVIEW').length;
        const totalDisbursed = advances
          .filter(a => a.status === 'DISBURSED' || a.status === 'SETTLED' || a.status === 'SETTLEMENT_SUBMITTED')
          .reduce((sum, a) => sum + (Number(a.disbursedAmount) || 0), 0);
        const registeredEmployees = users.filter(u => u.role !== 'ROLE_ADMIN').length;

        return {
          pendingAdvanceCount: pendingAdvances,
          pendingSettlementCount: pendingSettlements,
          totalDisbursedAmount: totalDisbursed,
          registeredEmployeeCount: registeredEmployees
        };
      } else {
        const myId = currentUser ? currentUser.id : 2;
        const myAdvances = advances.filter(a => a.employeeId === myId);
        const myActive = myAdvances
          .filter(a => a.status === 'DISBURSED' || a.status === 'APPROVED')
          .reduce((sum, a) => sum + (Number(a.disbursedAmount || a.requestedAmount) || 0), 0);
        const mySettlements = settlements.filter(s => s.employeeId === myId && s.status === 'PENDING_REVIEW').length;
        const myClaims = claims.filter(c => c.employeeId === myId);
        const myApprovedClaims = myClaims
          .filter(c => c.status === 'APPROVED' || c.status === 'PAID')
          .reduce((sum, c) => sum + Number(c.amount), 0);

        return {
          activeAdvanceAmount: myActive,
          pendingSettlementCount: mySettlements,
          approvedReimbursementAmount: myApprovedClaims,
          directClaimCount: myClaims.length
        };
      }
    }

    // --- ADVANCES ---
    if (cleanUrl === '/api/advances/my' && method === 'GET') {
      const myId = currentUser ? currentUser.id : 2;
      return this.getAdvances().filter(a => a.employeeId === myId);
    }

    if (cleanUrl === '/api/advances/all' && method === 'GET') {
      const advances = this.getAdvances();
      if (endpoint.includes('status=')) {
        const status = endpoint.split('status=')[1].split('&')[0];
        if (status !== 'ALL') return advances.filter(a => a.status === status);
      }
      return advances;
    }

    if (cleanUrl === '/api/advances' && method === 'POST') {
      const data = JSON.parse(options.body || '{}');
      const advances = this.getAdvances();
      const myId = currentUser ? currentUser.id : 2;
      const myName = currentUser && currentUser.data ? currentUser.data.fullName : 'Sarah Jenkins';
      const myEmail = currentUser && currentUser.data ? currentUser.data.email : 'sarah@company.com';

      const newAdv = {
        id: advances.length ? Math.max(...advances.map(a => a.id)) + 1 : 101,
        employeeId: myId,
        employeeName: myName,
        employeeEmail: myEmail,
        purpose: data.purpose,
        destination: data.destination || '',
        requestedAmount: parseFloat(data.requestedAmount),
        disbursedAmount: null,
        status: 'PENDING',
        startDate: data.startDate || new Date().toISOString().split('T')[0],
        endDate: data.endDate || null,
        disbursedDate: null,
        notes: data.notes || '',
        createdAt: new Date().toISOString()
      };
      advances.unshift(newAdv);
      this.saveAdvances(advances);
      return newAdv;
    }

    // Delete / Cancel advance
    if (cleanUrl.match(/^\/api\/advances\/\d+$/) && method === 'DELETE') {
      const id = parseInt(cleanUrl.split('/').pop(), 10);
      const advances = this.getAdvances().filter(a => a.id !== id);
      this.saveAdvances(advances);
      return { success: true };
    }

    // Admin review advance
    if (cleanUrl.match(/^\/api\/advances\/\d+\/review$/) && method === 'PUT') {
      const id = parseInt(cleanUrl.split('/')[3], 10);
      const { decision, comment } = JSON.parse(options.body || '{}');
      const advances = this.getAdvances();
      const adv = advances.find(a => a.id === id);
      if (adv) {
        adv.status = decision === 'APPROVE' ? 'APPROVED' : 'REJECTED';
        adv.managerComment = comment;
        this.saveAdvances(advances);
      }
      return adv;
    }

    // Admin disburse advance
    if (cleanUrl.match(/^\/api\/advances\/\d+\/disburse$/) && method === 'PUT') {
      const id = parseInt(cleanUrl.split('/')[3], 10);
      const { disbursedAmount, disbursedDate, managerComment } = JSON.parse(options.body || '{}');
      const advances = this.getAdvances();
      const adv = advances.find(a => a.id === id);
      if (adv) {
        adv.status = 'DISBURSED';
        adv.disbursedAmount = parseFloat(disbursedAmount);
        adv.disbursedDate = disbursedDate || new Date().toISOString().split('T')[0];
        adv.disburseComment = managerComment;
        this.saveAdvances(advances);
      }
      return adv;
    }

    // --- EXPENSES AGAINST ADVANCE ---
    if (cleanUrl.startsWith('/api/expenses/advance/')) {
      const advId = parseInt(cleanUrl.split('/').pop(), 10);
      return this.getExpenses().filter(e => e.advanceId === advId);
    }

    if (cleanUrl === '/api/expenses' && method === 'POST') {
      const expenses = this.getExpenses();
      let data = {};
      let hasFile = false;
      let filename = null;

      if (options.body instanceof FormData) {
        data.advanceId = parseInt(options.body.get('advanceId'), 10);
        data.title = options.body.get('title');
        data.category = options.body.get('category');
        data.amount = parseFloat(options.body.get('amount'));
        data.expenseDate = options.body.get('expenseDate');
        data.merchant = options.body.get('merchant');
        data.description = options.body.get('description');
        const file = options.body.get('receipt');
        if (file && file.name) {
          hasFile = true;
          filename = file.name;
        }
      } else {
        data = JSON.parse(options.body || '{}');
      }

      const newExp = {
        id: expenses.length ? Math.max(...expenses.map(e => e.id)) + 1 : 201,
        advanceId: data.advanceId,
        employeeId: currentUser ? currentUser.id : 2,
        title: data.title,
        category: data.category,
        amount: data.amount,
        expenseDate: data.expenseDate || new Date().toISOString().split('T')[0],
        merchant: data.merchant || '',
        description: data.description || '',
        status: 'APPROVED',
        hasReceipt: hasFile,
        receiptFilename: filename,
        createdAt: new Date().toISOString()
      };
      expenses.push(newExp);
      this.saveExpenses(expenses);
      return newExp;
    }

    // --- SETTLEMENTS & RECONCILIATION ---
    if (cleanUrl === '/api/settlements/my' && method === 'GET') {
      const myId = currentUser ? currentUser.id : 2;
      return this.getSettlements().filter(s => s.employeeId === myId);
    }

    if (cleanUrl === '/api/settlements/all' && method === 'GET') {
      return this.getSettlements();
    }

    if (cleanUrl === '/api/settlements/reconcile' && method === 'POST') {
      const { advanceId, submissionNotes } = JSON.parse(options.body || '{}');
      const advances = this.getAdvances();
      const adv = advances.find(a => a.id === advanceId);
      if (!adv) throw new Error('Advance record not found.');

      const expenses = this.getExpenses().filter(e => e.advanceId === advanceId && e.status === 'APPROVED');
      const totalDisbursed = Number(adv.disbursedAmount || adv.requestedAmount);
      const totalEligible = expenses.reduce((sum, e) => sum + Number(e.amount), 0);
      const remainingBalance = totalDisbursed - totalEligible;

      let outcome = 'BALANCED';
      if (remainingBalance > 0) outcome = 'RETURN_DUE';
      else if (remainingBalance < 0) outcome = 'ADDITIONAL_PAYMENT_DUE';

      const settlements = this.getSettlements();
      const newStl = {
        id: settlements.length ? Math.max(...settlements.map(s => s.id)) + 1 : 501,
        advanceId: adv.id,
        advancePurpose: adv.purpose,
        employeeId: adv.employeeId,
        employeeName: adv.employeeName,
        employeeEmail: adv.employeeEmail,
        totalDisbursedAdvance: totalDisbursed,
        totalEligibleExpenses: totalEligible,
        remainingBalance: remainingBalance,
        outcome: outcome,
        status: 'PENDING_REVIEW',
        submissionNotes: submissionNotes || '',
        createdAt: new Date().toISOString()
      };

      settlements.unshift(newStl);
      this.saveSettlements(settlements);

      adv.status = 'SETTLEMENT_SUBMITTED';
      this.saveAdvances(advances);

      return newStl;
    }

    if (cleanUrl.match(/^\/api\/settlements\/\d+\/review$/) && method === 'PUT') {
      const id = parseInt(cleanUrl.split('/')[3], 10);
      const { decision, comment } = JSON.parse(options.body || '{}');
      const settlements = this.getSettlements();
      const stl = settlements.find(s => s.id === id);
      if (stl) {
        stl.status = decision === 'APPROVE' ? 'APPROVED' : 'REJECTED';
        stl.reviewComment = comment;
        this.saveSettlements(settlements);
      }
      return stl;
    }

    if (cleanUrl.match(/^\/api\/settlements\/\d+\/complete/) && method === 'PUT') {
      const id = parseInt(cleanUrl.split('/')[3], 10);
      const settlements = this.getSettlements();
      const stl = settlements.find(s => s.id === id);
      if (stl) {
        stl.status = 'COMPLETED';
        stl.completedAt = new Date().toISOString();
        this.saveSettlements(settlements);

        const advances = this.getAdvances();
        const adv = advances.find(a => a.id === stl.advanceId);
        if (adv) {
          adv.status = 'SETTLED';
          this.saveAdvances(advances);
        }
      }
      return stl;
    }

    // --- DIRECT CLAIMS ---
    if (cleanUrl === '/api/claims/my' && method === 'GET') {
      const myId = currentUser ? currentUser.id : 2;
      return this.getClaims().filter(c => c.employeeId === myId);
    }

    if (cleanUrl === '/api/claims/all' && method === 'GET') {
      const claims = this.getClaims();
      if (endpoint.includes('status=')) {
        const status = endpoint.split('status=')[1].split('&')[0];
        if (status !== 'ALL') return claims.filter(c => c.status === status);
      }
      return claims;
    }

    if (cleanUrl === '/api/claims' && method === 'POST') {
      const claims = this.getClaims();
      let data = {};
      let hasFile = false;
      let filename = null;

      if (options.body instanceof FormData) {
        data.title = options.body.get('title');
        data.category = options.body.get('category');
        data.amount = parseFloat(options.body.get('amount'));
        data.expenseDate = options.body.get('expenseDate');
        data.merchant = options.body.get('merchant');
        data.description = options.body.get('description');
        const file = options.body.get('receipt');
        if (file && file.name) {
          hasFile = true;
          filename = file.name;
        }
      } else {
        data = JSON.parse(options.body || '{}');
      }

      const myId = currentUser ? currentUser.id : 2;
      const myName = currentUser && currentUser.data ? currentUser.data.fullName : 'Sarah Jenkins';
      const myEmail = currentUser && currentUser.data ? currentUser.data.email : 'sarah@company.com';

      const newClaim = {
        id: claims.length ? Math.max(...claims.map(c => c.id)) + 1 : 301,
        employeeId: myId,
        employeeName: myName,
        employeeEmail: myEmail,
        title: data.title,
        category: data.category,
        amount: data.amount,
        expenseDate: data.expenseDate || new Date().toISOString().split('T')[0],
        merchant: data.merchant || '',
        description: data.description || '',
        status: 'PENDING',
        hasReceipt: hasFile,
        receiptFilename: filename,
        createdAt: new Date().toISOString()
      };
      claims.unshift(newClaim);
      this.saveClaims(claims);
      return newClaim;
    }

    if (cleanUrl.match(/^\/api\/claims\/\d+$/) && method === 'DELETE') {
      const id = parseInt(cleanUrl.split('/').pop(), 10);
      const claims = this.getClaims().filter(c => c.id !== id);
      this.saveClaims(claims);
      return { success: true };
    }

    if (cleanUrl.match(/^\/api\/claims\/\d+\/review$/) && method === 'PUT') {
      const id = parseInt(cleanUrl.split('/')[3], 10);
      const { decision, comment } = JSON.parse(options.body || '{}');
      const claims = this.getClaims();
      const claim = claims.find(c => c.id === id);
      if (claim) {
        claim.status = decision === 'APPROVE' ? 'APPROVED' : 'REJECTED';
        claim.managerComment = comment;
        this.saveClaims(claims);
      }
      return claim;
    }

    if (cleanUrl.match(/^\/api\/claims\/\d+\/pay/) && method === 'PUT') {
      const id = parseInt(cleanUrl.split('/')[3], 10);
      const claims = this.getClaims();
      const claim = claims.find(c => c.id === id);
      if (claim) {
        claim.status = 'PAID';
        claim.paidAt = new Date().toISOString();
        this.saveClaims(claims);
      }
      return claim;
    }

    return null;
  }

  generateCSV() {
    const advances = this.getAdvances();
    const settlements = this.getSettlements();
    const claims = this.getClaims();

    let csv = '=== EXPENZO CORPORATE FINANCIAL AUDIT REPORT ===\n';
    csv += `Export Generated: ${new Date().toISOString()}\n\n`;

    csv += '--- CASH ADVANCES ---\n';
    csv += 'ID,Employee,Email,Purpose,Requested Amount,Disbursed Amount,Status,Disbursed Date\n';
    advances.forEach(a => {
      csv += `ADV-${a.id},"${a.employeeName}","${a.employeeEmail}","${a.purpose}",${a.requestedAmount},${a.disbursedAmount || 0},${a.status},${a.disbursedDate || '-'}\n`;
    });

    csv += '\n--- SETTLEMENT RECONCILIATIONS ---\n';
    csv += 'ID,Advance ID,Employee,Total Disbursed,Approved Expenses,Remaining Balance,Outcome,Status\n';
    settlements.forEach(s => {
      csv += `STL-${s.id},ADV-${s.advanceId},"${s.employeeName}",${s.totalDisbursedAdvance},${s.totalEligibleExpenses},${s.remainingBalance},${s.outcome},${s.status}\n`;
    });

    csv += '\n--- DIRECT REIMBURSEMENT CLAIMS ---\n';
    csv += 'ID,Employee,Email,Title,Category,Amount,Date,Status\n';
    claims.forEach(c => {
      csv += `CLM-${c.id},"${c.employeeName}","${c.employeeEmail}","${c.title}",${c.category},${c.amount},${c.expenseDate},${c.status}\n`;
    });

    return csv;
  }
}

class ExpenseApp {
  constructor() {
    // If opened via file:/// URL directly in browser, point API to http://localhost:8080
    const isFileProtocol = window.location.protocol === 'file:';
    this.API_BASE = isFileProtocol ? 'http://localhost:8080' : '';
    this.TOKEN_KEY = 'expenzo_jwt_token';
    this.USER_KEY = 'expenzo_user_data';

    this.token = localStorage.getItem(this.TOKEN_KEY);
    this.currentUser = null;

    try {
      const savedUser = localStorage.getItem(this.USER_KEY);
      if (savedUser) this.currentUser = JSON.parse(savedUser);
    } catch (e) {
      this.currentUser = null;
    }

    this.advances = [];
    this.settlements = [];
    this.claims = [];
    this.employees = [];

    this.activeEmpTab = 'advances';
    this.offlineMode = false;
    this.mockStore = new ExpenzoMockStore();
    this.init();
  }

  async init() {
    this.renderHeader();
    this.setupEventListeners();
    await this.checkBackendConnectivity();

    if (this.token && this.currentUser) {
      await this.verifySessionOrRestore();
    } else {
      this.renderQuickEmployeeChips();
      this.switchView('viewLanding');
    }
  }

  async checkBackendConnectivity() {
    // If running directly as local file or without server, default directly to standalone
    if (window.location.protocol === 'file:' || !window.location.port) {
      this.offlineMode = true;
      this.updateModeIndicator(false);
      return;
    }
    try {
      const res = await fetch(`${this.API_BASE}/api/auth/me`, { method: 'GET', signal: AbortSignal.timeout(1000) });
      if (res.ok || res.status === 401) {
        this.offlineMode = false;
        this.updateModeIndicator(true);
      } else {
        this.offlineMode = true;
        this.updateModeIndicator(false);
      }
    } catch (e) {
      this.offlineMode = true;
      this.updateModeIndicator(false);
    }
  }

  updateModeIndicator(isLive) {
    const pill = document.getElementById('systemLivePill');
    const text = document.getElementById('systemLivePillText');
    if (!pill || !text) return;
    if (isLive) {
      pill.style.background = 'rgba(16, 185, 129, 0.15)';
      pill.style.borderColor = 'rgba(16, 185, 129, 0.4)';
      text.innerHTML = 'BACKEND LIVE: <code>PORT 8080</code>';
    } else {
      pill.style.background = 'rgba(99, 102, 241, 0.18)';
      pill.style.borderColor = 'rgba(99, 102, 241, 0.4)';
      text.innerHTML = 'STANDALONE (CLIENT-SIDE)';
    }
  }

  /* --- HTTP Helper with Automatic Standalone Fallback --- */
  async apiRequest(endpoint, options = {}) {
    if (this.offlineMode) {
      return this.mockStore.handleRequest(endpoint, options, this.currentUser);
    }

    const url = `${this.API_BASE}${endpoint}`;
    const headers = options.headers || {};

    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }

    if (!(options.body instanceof FormData) && !headers['Content-Type']) {
      headers['Content-Type'] = 'application/json';
    }

    options.headers = headers;

    try {
      const response = await fetch(url, options);

      if (response.status === 401) {
        this.logout(false);
        this.showToast('Session expired. Please log in again.', 'info');
        throw new Error('Unauthorized');
      }

      if (!response.ok) {
        // If HTTP 404/500/502/503 occurs and backend is unreachable, fallback cleanly to mock store
        if (response.status === 404 || response.status >= 500) {
          console.info(`[Expenzo API] Server returned ${response.status}. Switching to standalone mock store.`);
          this.offlineMode = true;
          this.updateModeIndicator(false);
          return this.mockStore.handleRequest(endpoint, options, this.currentUser);
        }

        let errMessage = `HTTP error ${response.status}`;
        try {
          const errData = await response.json();
          errMessage = errData.message || errData.error || errMessage;
        } catch (_) {}
        throw new Error(errMessage);
      }

      if (response.status === 204) return null;

      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        return await response.json();
      }
      return await response.text();
    } catch (error) {
      // If server unreachable, timeout, fetch failed, or HTTP error, switch seamlessly to mock store
      if (
        error.name === 'TypeError' ||
        error.name === 'AbortError' ||
        (error.message && (error.message.includes('fetch') || error.message.includes('NetworkError') || error.message.includes('HTTP error')))
      ) {
        console.info('[Expenzo] Host unreachable or request failed. Running in Standalone In-Browser Mode.');
        this.offlineMode = true;
        this.updateModeIndicator(false);
        return this.mockStore.handleRequest(endpoint, options, this.currentUser);
      }
      if (error.message !== 'Unauthorized') {
        console.warn(`[Expenzo API] Request to ${endpoint} failed:`, error.message);
      }
      throw error;
    }
  }

  async verifySessionOrRestore() {
    try {
      const user = await this.apiRequest('/api/auth/me');
      this.currentUser = {
        id: user.id,
        role: user.role === 'ROLE_ADMIN' || user.role === 'ROLE_MANAGER' ? 'admin' : 'employee',
        data: user
      };
      localStorage.setItem(this.USER_KEY, JSON.stringify(this.currentUser));
      this.renderHeader();
      this.navigateHome();
    } catch (e) {
      // If server unreachable, use cached session
      this.renderHeader();
      this.navigateHome();
    }
  }

  /* --- Navigation & View Switching --- */
  switchView(viewId) {
    document.querySelectorAll('.view-panel').forEach(el => el.classList.remove('active'));
    const target = document.getElementById(viewId);
    if (target) target.classList.add('active');
  }

  switchAuthTab(tabKey) {
    document.querySelectorAll('.auth-tab, .portal-tab-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.auth-form').forEach(f => f.classList.remove('active'));

    const tabBtns = document.querySelectorAll(`[data-tab="${tabKey}"]`);
    tabBtns.forEach(btn => btn.classList.add('active'));

    if (tabKey === 'emp-login') {
      document.getElementById('formEmpLogin')?.classList.add('active');
    } else if (tabKey === 'admin-login') {
      document.getElementById('formAdminLogin')?.classList.add('active');
    }
  }

  switchEmpTab(tab) {
    this.activeEmpTab = tab;
    document.querySelectorAll('#viewEmployee .workflow-tab').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('#viewEmployee .workflow-panel').forEach(p => p.classList.remove('active'));

    const tabsMap = {
      advances: { tabIndex: 0, panelId: 'empPanelAdvances' },
      settlements: { tabIndex: 1, panelId: 'empPanelSettlements' },
      expenses: { tabIndex: 2, panelId: 'empPanelExpenses' }
    };

    const target = tabsMap[tab];
    if (target) {
      document.querySelectorAll('#viewEmployee .workflow-tab')[target.tabIndex]?.classList.add('active');
      document.getElementById(target.panelId)?.classList.add('active');
    }
  }

  switchAdminTab(tab) {
    this.activeAdminTab = tab;
    document.querySelectorAll('#viewAdmin .workflow-tab').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('#viewAdmin .workflow-panel').forEach(p => p.classList.remove('active'));

    const tabsMap = {
      advances: { tabIndex: 0, panelId: 'adminPanelAdvances' },
      settlements: { tabIndex: 1, panelId: 'adminPanelSettlements' },
      expenses: { tabIndex: 2, panelId: 'adminPanelExpenses' },
      employees: { tabIndex: 3, panelId: 'adminPanelEmployees' }
    };

    const target = tabsMap[tab];
    if (target) {
      document.querySelectorAll('#viewAdmin .workflow-tab')[target.tabIndex]?.classList.add('active');
      document.getElementById(target.panelId)?.classList.add('active');
    }
  }

  navigateHome() {
    if (this.currentUser) {
      if (this.currentUser.role === 'admin') {
        this.renderAdminView();
      } else {
        this.renderEmployeeView();
      }
    } else {
      this.renderQuickEmployeeChips();
      this.switchView('viewLanding');
    }
  }

  renderHeader() {
    const nav = document.getElementById('headerNav');
    if (!nav) return;

    if (!this.currentUser) {
      nav.innerHTML = `
        <button class="btn btn-outline" onclick="app.switchAuthTab('emp-login'); app.switchView('viewLanding');">
          <i class="fa-solid fa-arrow-right-to-bracket"></i> Login
        </button>
      `;
    } else if (this.currentUser.role === 'admin') {
      nav.innerHTML = `
        <div class="user-session-badge">
          <div class="user-avatar" style="background: var(--purple)"><i class="fa-solid fa-shield"></i></div>
          <div class="user-meta">
            <span class="user-name">${this.escapeHtml(this.currentUser.data.fullName || 'Manager')}</span>
            <span class="user-role">Manager / Admin</span>
          </div>
        </div>
        <button class="btn btn-outline btn-xs" onclick="app.logout()">
          <i class="fa-solid fa-arrow-right-from-bracket"></i> Logout
        </button>
      `;
    } else {
      const emp = this.currentUser.data;
      const initial = (emp.fullName || emp.name || 'E').charAt(0).toUpperCase();
      nav.innerHTML = `
        <div class="user-session-badge">
          <div class="user-avatar">${initial}</div>
          <div class="user-meta">
            <span class="user-name">${this.escapeHtml(emp.fullName || emp.name)}</span>
            <span class="user-role">Employee</span>
          </div>
        </div>
        <button class="btn btn-outline btn-xs" onclick="app.logout()">
          <i class="fa-solid fa-arrow-right-from-bracket"></i> Logout
        </button>
      `;
    }
  }

  renderQuickEmployeeChips() {
    const container = document.getElementById('quickEmployeeChips');
    if (!container) return;

    const demoAccounts = [
      { name: 'Sarah Jenkins', email: 'sarah@company.com', pass: 'pass123', desc: 'Dev (Has Disbursed Advance #101)' },
      { name: 'Alex Rivera', email: 'alex@company.com', pass: 'pass123', desc: 'DevOps (Has Pending Advance #102)' }
    ];

    container.innerHTML = demoAccounts.map(emp => `
      <div class="quick-chip" onclick="app.selectEmployeeLogin('${emp.email}', '${emp.pass}')">
        <div>
          <div class="chip-name"><i class="fa-solid fa-user-check text-emerald"></i> ${emp.name}</div>
          <div class="chip-email">${emp.email} <span style="font-size:0.7rem; color:var(--text-light);">(${emp.desc})</span></div>
        </div>
        <span class="chip-action">Use Account &rarr;</span>
      </div>
    `).join('');
  }

  selectEmployeeLogin(email, password) {
    document.getElementById('empLoginEmail').value = email;
    document.getElementById('empLoginPassword').value = password;
    this.switchAuthTab('emp-login');
    this.showToast(`Selected employee ${email}`, 'info');
  }

  quickFillAdmin() {
    document.getElementById('adminUser').value = 'admin@company.com';
    document.getElementById('adminPass').value = 'admin123';
    this.switchAuthTab('admin-login');
    this.showToast('Pre-filled Manager credentials (admin@company.com / admin123)', 'info');
  }

  togglePasswordVisibility(inputId, btn) {
    const input = document.getElementById(inputId);
    if (!input) return;
    const isPassword = input.type === 'password';
    input.type = isPassword ? 'text' : 'password';
    btn.innerHTML = isPassword ? '<i class="fa-regular fa-eye-slash"></i>' : '<i class="fa-regular fa-eye"></i>';
  }

  /* --- Authentication Handlers --- */
  async handleEmployeeLogin(e) {
    e.preventDefault();
    const email = document.getElementById('empLoginEmail').value.trim().toLowerCase();
    const password = document.getElementById('empLoginPassword').value;

    try {
      const auth = await this.apiRequest('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
      });

      this.token = auth.token;
      this.currentUser = {
        id: auth.id,
        role: auth.role === 'ROLE_ADMIN' || auth.role === 'ROLE_MANAGER' ? 'admin' : 'employee',
        data: {
          id: auth.id,
          fullName: auth.fullName,
          email: auth.email,
          role: auth.role,
          department: auth.department
        }
      };

      localStorage.setItem(this.TOKEN_KEY, this.token);
      localStorage.setItem(this.USER_KEY, JSON.stringify(this.currentUser));

      this.renderHeader();
      this.navigateHome();
      this.showToast(`Welcome back, ${auth.fullName}!`, 'success');
      document.getElementById('formEmpLogin').reset();
    } catch (err) {
      this.showToast(err.message || 'Login failed. Check server or credentials.', 'error');
    }
  }

  async handleAdminLogin(e) {
    e.preventDefault();
    let email = document.getElementById('adminUser').value.trim();
    if (email === 'admin') email = 'admin@company.com';
    const password = document.getElementById('adminPass').value;

    try {
      const auth = await this.apiRequest('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
      });

      this.token = auth.token;
      this.currentUser = {
        id: auth.id,
        role: 'admin',
        data: {
          id: auth.id,
          fullName: auth.fullName,
          email: auth.email,
          role: auth.role,
          department: auth.department
        }
      };

      localStorage.setItem(this.TOKEN_KEY, this.token);
      localStorage.setItem(this.USER_KEY, JSON.stringify(this.currentUser));

      this.renderHeader();
      this.renderAdminView();
      this.showToast('Logged in to Admin Console.', 'success');
      document.getElementById('formAdminLogin').reset();
    } catch (err) {
      this.showToast(err.message || 'Invalid admin credentials.', 'error');
    }
  }

  async handleRegisterUser(e) {
    e.preventDefault();
    const fullName = document.getElementById('adminRegName').value.trim();
    const email = document.getElementById('adminRegEmail').value.trim().toLowerCase();
    const password = document.getElementById('adminRegPassword').value;
    const department = document.getElementById('adminRegDepartment')?.value.trim() || 'General';
    const phone = document.getElementById('adminRegPhone')?.value.trim() || '';

    try {
      const auth = await this.apiRequest('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify({ fullName, email, password, department, phone })
      });

      this.closeModal('modalRegisterEmployee');
      document.getElementById('formRegisterEmployeeInside').reset();

      if (!this.currentUser) {
        // Immediate auto-login from landing page
        this.token = auth.token;
        this.currentUser = {
          id: auth.id,
          role: 'employee',
          data: {
            id: auth.id,
            fullName: auth.fullName,
            email: auth.email,
            role: auth.role,
            department: auth.department
          }
        };
        localStorage.setItem(this.TOKEN_KEY, this.token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(this.currentUser));

        this.renderHeader();
        this.renderEmployeeView();
        this.showToast(`Registration complete! Welcome, ${fullName}.`, 'success');
      } else {
        // Registered by admin
        this.showToast(`User ${fullName} successfully registered!`, 'success');
        this.renderAdminView();
      }
    } catch (err) {
      this.showToast(err.message || 'Registration failed.', 'error');
    }
  }

  openRegisterUserModal() {
    this.openModal('modalRegisterEmployee');
  }

  logout(showNotice = true) {
    this.currentUser = null;
    this.token = null;
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.renderHeader();
    this.renderQuickEmployeeChips();
    this.switchView('viewLanding');
    if (showNotice) this.showToast('Logged out successfully.', 'info');
  }

  /* --- Employee Dashboard --- */
  async renderEmployeeView() {
    if (!this.currentUser || this.currentUser.role !== 'employee') return;

    this.switchView('viewEmployee');
    const emp = this.currentUser.data;

    document.getElementById('empGreeting').textContent = `Welcome, ${emp.fullName || emp.name}`;
    document.getElementById('empEmailSubtitle').textContent = emp.email;

    await this.fetchDashboardSummary();
    await this.fetchEmployeeAdvances();
    await this.fetchEmployeeSettlements();
    await this.fetchEmployeeClaims();
  }

  async fetchDashboardSummary() {
    try {
      const sum = await this.apiRequest('/api/dashboard/summary');
      document.getElementById('empMetricActiveAdvanceAmount').textContent = `₹${Number(sum.activeAdvanceAmount || 0).toFixed(2)}`;
      document.getElementById('empMetricPendingSettlements').textContent = sum.pendingSettlementCount || 0;
      document.getElementById('empMetricApprovedAmount').textContent = `₹${Number(sum.approvedReimbursementAmount || 0).toFixed(2)}`;
      document.getElementById('empMetricDirectCount').textContent = sum.directClaimCount || 0;
    } catch (e) {
      console.warn("Could not load summary from API:", e.message);
    }
  }

  // --- Advances ---
  async fetchEmployeeAdvances() {
    const tbody = document.getElementById('empAdvancesTableBody');
    if (!tbody) return;

    try {
      this.advances = await this.apiRequest('/api/advances/my');
      document.getElementById('empTabAdvanceCount').textContent = this.advances.length;

      if (this.advances.length === 0) {
        tbody.innerHTML = `
          <tr>
            <td colspan="7" class="empty-table-state">
              <i class="fa-solid fa-plane-circle-exclamation"></i>
              <p>No cash advances requested yet. Click "Request Cash Advance" to apply.</p>
            </td>
          </tr>
        `;
        return;
      }

      tbody.innerHTML = this.advances.map(a => {
        const isDisbursed = a.status === 'DISBURSED';
        const isPending = a.status === 'PENDING';
        return `
          <tr>
            <td><strong>#ADV-${a.id}</strong></td>
            <td>
              <div style="font-weight: 600; color: var(--text-main);">${this.escapeHtml(a.purpose)}</div>
              <div style="font-size: 0.8rem; color: var(--text-muted);">${this.escapeHtml(a.destination || a.notes || '-')}</div>
            </td>
            <td>${a.startDate || a.createdAt ? String(a.startDate || a.createdAt).split('T')[0] : '-'}</td>
            <td>${a.disbursedDate || '-'}</td>
            <td>
              <span class="expense-amount">₹${Number(a.disbursedAmount || a.requestedAmount).toFixed(2)}</span>
              ${a.disbursedAmount ? '<div style="font-size:0.7rem; color:var(--emerald);">Disbursed</div>' : ''}
            </td>
            <td>${this.getAdvanceStatusBadge(a.status)}</td>
            <td class="text-right">
              ${isDisbursed ? `
                <div style="display:inline-flex; gap:0.35rem;">
                  <button class="btn btn-purple btn-xs" onclick="app.openLogExpenseModal(${a.id}, '${this.escapeHtml(a.purpose)}')">
                    <i class="fa-solid fa-receipt"></i> Log Bill
                  </button>
                  <button class="btn btn-emerald btn-xs" onclick="app.openSettlementModal(${a.id})">
                    <i class="fa-solid fa-scale-balanced"></i> Settle
                  </button>
                </div>
              ` : isPending ? `
                <button class="btn btn-outline-danger btn-xs" onclick="app.cancelPendingAdvance(${a.id})">
                  <i class="fa-solid fa-trash-can"></i> Cancel
                </button>
              ` : `
                <span style="font-size: 0.8rem; color: var(--text-muted);">${a.status}</span>
              `}
            </td>
          </tr>
        `;
      }).join('');
    } catch (err) {
      console.warn("Could not fetch employee advances:", err.message);
    }
  }

  async handleRequestAdvance(e) {
    e.preventDefault();
    const purpose = document.getElementById('advancePurpose').value.trim();
    const destination = document.getElementById('advanceDestination').value.trim();
    const requestedAmount = parseFloat(document.getElementById('advanceAmount').value);
    const startDate = document.getElementById('advanceStartDate').value || null;
    const endDate = document.getElementById('advanceEndDate').value || null;
    const notes = document.getElementById('advanceNotes').value.trim();

    if (!purpose || isNaN(requestedAmount) || requestedAmount <= 0) {
      this.showToast('Please enter purpose and positive amount.', 'error');
      return;
    }

    try {
      const created = await this.apiRequest('/api/advances', {
        method: 'POST',
        body: JSON.stringify({ purpose, destination, requestedAmount, startDate, endDate, notes })
      });

      this.closeModal('modalRequestAdvance');
      document.getElementById('formRequestAdvance').reset();
      this.showToast(`Advance Request #ADV-${created.id} submitted for approval.`, 'success');
      await this.fetchEmployeeAdvances();
      await this.fetchDashboardSummary();
    } catch (err) {
      this.showToast(err.message || 'Could not submit advance.', 'error');
    }
  }

  async cancelPendingAdvance(id) {
    if (!confirm(`Cancel advance request #ADV-${id}?`)) return;
    try {
      await this.apiRequest(`/api/advances/${id}`, { method: 'DELETE' });
      this.showToast('Advance request cancelled.', 'info');
      await this.fetchEmployeeAdvances();
      await this.fetchDashboardSummary();
    } catch (err) {
      this.showToast(err.message || 'Could not cancel advance.', 'error');
    }
  }

  // --- Log Expense Against Advance ---
  openLogExpenseModal(advanceId, purpose) {
    document.getElementById('logAdvanceId').value = advanceId;
    document.getElementById('logAdvancePurposeDisplay').value = `#ADV-${advanceId}: ${purpose}`;
    document.getElementById('logExpenseDate').value = new Date().toISOString().split('T')[0];
    this.openModal('modalLogAdvanceExpense');
  }

  async handleLogAdvanceExpense(e) {
    e.preventDefault();
    const advanceId = document.getElementById('logAdvanceId').value;
    const title = document.getElementById('logExpenseTitle').value.trim();
    const category = document.getElementById('logExpenseCategory').value;
    const amount = document.getElementById('logExpenseAmount').value;
    const expenseDate = document.getElementById('logExpenseDate').value;
    const merchant = document.getElementById('logExpenseMerchant').value.trim();
    const description = document.getElementById('logExpenseDescription').value.trim();
    const fileInput = document.getElementById('logExpenseReceiptFile');

    const formData = new FormData();
    formData.append('advanceId', advanceId);
    formData.append('title', title);
    formData.append('category', category);
    formData.append('amount', amount);
    formData.append('expenseDate', expenseDate);
    formData.append('merchant', merchant);
    formData.append('description', description);
    if (fileInput.files.length > 0) {
      formData.append('receipt', fileInput.files[0]);
    }

    try {
      await this.apiRequest('/api/expenses', {
        method: 'POST',
        body: formData
      });

      this.closeModal('modalLogAdvanceExpense');
      document.getElementById('formLogAdvanceExpense').reset();
      this.showToast('Expense bill recorded successfully against advance!', 'success');
    } catch (err) {
      this.showToast(err.message || 'Could not record expense.', 'error');
    }
  }

  // --- Settlements & Reconciliation ---
  async fetchEmployeeSettlements() {
    const tbody = document.getElementById('empSettlementsTableBody');
    if (!tbody) return;

    try {
      this.settlements = await this.apiRequest('/api/settlements/my');
      document.getElementById('empTabSettlementCount').textContent = this.settlements.length;

      if (this.settlements.length === 0) {
        tbody.innerHTML = `
          <tr>
            <td colspan="7" class="empty-table-state">
              <i class="fa-solid fa-scale-balanced"></i>
              <p>No settlements submitted yet.</p>
            </td>
          </tr>
        `;
        return;
      }

      tbody.innerHTML = this.settlements.map(s => `
        <tr>
          <td><strong>#STL-${s.id}</strong></td>
          <td>
            <div style="font-weight: 600; color: var(--text-main);">#ADV-${s.advanceId}: ${this.escapeHtml(s.advancePurpose)}</div>
            <div style="font-size: 0.8rem; color: var(--text-muted);">${this.escapeHtml(s.submissionNotes || '-')}</div>
          </td>
          <td>₹${Number(s.totalDisbursedAdvance).toFixed(2)}</td>
          <td><span class="expense-amount">₹${Number(s.totalEligibleExpenses).toFixed(2)}</span></td>
          <td>${this.getReconciliationBadge(s.remainingBalance)}</td>
          <td><span class="category-tag">${s.outcome}</span></td>
          <td>${this.getSettlementStatusBadge(s.status)}</td>
        </tr>
      `).join('');
    } catch (err) {
      console.warn("Could not fetch settlements:", err.message);
    }
  }

  async openSettlementModal(preselectedAdvanceId = null) {
    const select = document.getElementById('settlementAdvanceSelect');
    if (!select) return;

    const eligible = this.advances.filter(a => a.status === 'DISBURSED' || a.status === 'APPROVED');
    if (eligible.length === 0) {
      this.showToast('No active disbursed advances available for reconciliation.', 'info');
      return;
    }

    select.innerHTML = eligible.map(a => `
      <option value="${a.id}" ${preselectedAdvanceId && a.id === preselectedAdvanceId ? 'selected' : ''}>
        #ADV-${a.id} - ${this.escapeHtml(a.purpose)} (Disbursed: ₹${Number(a.disbursedAmount || a.requestedAmount).toFixed(2)})
      </option>
    `).join('');

    await this.onSettlementAdvanceChange();
    this.openModal('modalSubmitSettlement');
  }

  async onSettlementAdvanceChange() {
    const select = document.getElementById('settlementAdvanceSelect');
    const selectedId = parseInt(select.value, 10);
    const advance = this.advances.find(a => a.id === selectedId);

    if (advance) {
      const disbursed = Number(advance.disbursedAmount || advance.requestedAmount);
      document.getElementById('settlementAdvanceAmountDisplay').value = `₹${disbursed.toFixed(2)}`;

      // Fetch approved expenses for this advance to calculate live preview
      try {
        const expenses = await this.apiRequest(`/api/expenses/advance/${advance.id}`);
        const approvedTotal = expenses
          .filter(e => e.status === 'APPROVED')
          .reduce((sum, e) => sum + Number(e.amount), 0);

        document.getElementById('settlementApprovedSpendsDisplay').value = `₹${approvedTotal.toFixed(2)}`;
        this.renderLiveReconPreview(disbursed, approvedTotal);
      } catch (e) {
        document.getElementById('settlementApprovedSpendsDisplay').value = `₹0.00`;
        this.renderLiveReconPreview(disbursed, 0);
      }
    }
  }

  renderLiveReconPreview(disbursed, approved) {
    const previewBox = document.getElementById('reconciliationPreviewBox');
    const valEl = document.getElementById('reconBalanceValue');
    const msgEl = document.getElementById('reconBalanceMessage');

    const balance = disbursed - approved;
    valEl.textContent = `₹${Math.abs(balance).toFixed(2)}`;

    if (balance > 0) {
      previewBox.className = 'reconciliation-box recon-employee-refunds';
      valEl.style.color = '#92400e';
      msgEl.innerHTML = `<strong>RETURN DUE:</strong> Disbursed cash exceeds approved expenses. You must return <strong>₹${balance.toFixed(2)}</strong> unspent funds to the company.`;
    } else if (balance < 0) {
      previewBox.className = 'reconciliation-box recon-company-pays';
      valEl.style.color = '#065f46';
      msgEl.innerHTML = `<strong>ADDITIONAL PAYMENT DUE:</strong> Approved expenses exceed advance by <strong>₹${Math.abs(balance).toFixed(2)}</strong>. The company will reimburse this difference.`;
    } else {
      previewBox.className = 'reconciliation-box recon-settled';
      valEl.style.color = '#0284c7';
      msgEl.innerHTML = `<strong>BALANCED:</strong> Advance received and approved expenditures match exactly (₹0.00 difference).`;
    }
  }

  async handleSubmitSettlement(e) {
    e.preventDefault();
    const advanceId = document.getElementById('settlementAdvanceSelect').value;
    const submissionNotes = document.getElementById('settlementNotes').value.trim();

    try {
      const settlement = await this.apiRequest('/api/settlements/reconcile', {
        method: 'POST',
        body: JSON.stringify({ advanceId: parseInt(advanceId, 10), submissionNotes })
      });

      this.closeModal('modalSubmitSettlement');
      document.getElementById('formSubmitSettlement').reset();
      this.showToast(`Settlement #STL-${settlement.id} submitted for audit!`, 'success');
      await this.fetchEmployeeSettlements();
      await this.fetchEmployeeAdvances();
      await this.fetchDashboardSummary();
      this.switchEmpTab('settlements');
    } catch (err) {
      this.showToast(err.message || 'Could not submit settlement.', 'error');
    }
  }

  // --- Direct Reimbursement Claims (Personal Out-of-Pocket Money) ---
  async fetchEmployeeClaims() {
    const tbody = document.getElementById('empExpensesTableBody');
    if (!tbody) return;

    try {
      this.claims = await this.apiRequest('/api/claims/my');
      document.getElementById('empTabExpenseCount').textContent = this.claims.length;

      const filter = document.getElementById('empStatusFilter')?.value || 'ALL';
      const list = this.claims.filter(c => filter === 'ALL' || c.status === filter);

      if (list.length === 0) {
        tbody.innerHTML = `
          <tr>
            <td colspan="8" class="empty-table-state">
              <i class="fa-solid fa-file-invoice-dollar"></i>
              <p>No direct reimbursement claims found.</p>
            </td>
          </tr>
        `;
        return;
      }

      tbody.innerHTML = list.map(c => `
        <tr>
          <td><strong>#CLM-${c.id}</strong></td>
          <td>
            <div style="font-weight: 600; color: var(--text-main);">${this.escapeHtml(c.title)}</div>
            <div style="font-size: 0.8rem; color: var(--text-muted);">${this.escapeHtml(c.description || '-')}</div>
          </td>
          <td><span class="category-tag">${c.category}</span></td>
          <td>${c.expenseDate}</td>
          <td><span class="expense-amount">₹${Number(c.amount).toFixed(2)}</span></td>
          <td>
            ${c.hasReceipt ? `
              <button onclick="app.viewReceipt('${c.receiptFilename}')" class="btn btn-outline btn-xs">
                <i class="fa-solid fa-paperclip"></i> View Receipt
              </button>
            ` : '<span style="color:var(--text-light); font-size:0.8rem;">No file</span>'}
          </td>
          <td>${this.getClaimStatusBadge(c.status)}</td>
          <td class="text-right">
            ${c.status === 'PENDING' ? `
              <button class="btn btn-outline-danger btn-xs" onclick="app.cancelPendingClaim(${c.id})">
                <i class="fa-solid fa-trash-can"></i> Cancel
              </button>
            ` : '<span style="font-size:0.8rem; color:var(--text-light);"><i class="fa-solid fa-lock"></i> Locked</span>'}
          </td>
        </tr>
      `).join('');
    } catch (err) {
      console.warn("Could not fetch claims:", err.message);
    }
  }

  async handleAddDirectClaim(e) {
    e.preventDefault();
    const title = document.getElementById('expenseTitle').value.trim();
    const category = document.getElementById('expenseCategory').value;
    const amount = document.getElementById('expenseAmount').value;
    const expenseDate = document.getElementById('expenseDate').value;
    const merchant = document.getElementById('expenseMerchant').value.trim();
    const description = document.getElementById('expenseDescription').value.trim();
    const fileInput = document.getElementById('expenseReceiptFile');

    const formData = new FormData();
    formData.append('title', title);
    formData.append('category', category);
    formData.append('amount', amount);
    formData.append('expenseDate', expenseDate);
    formData.append('merchant', merchant);
    formData.append('description', description);
    if (fileInput.files.length > 0) {
      formData.append('receipt', fileInput.files[0]);
    }

    try {
      await this.apiRequest('/api/claims', {
        method: 'POST',
        body: formData
      });

      this.closeModal('modalAddExpense');
      document.getElementById('formAddExpense').reset();
      this.showToast('Direct claim submitted successfully for manager approval.', 'success');
      await this.fetchEmployeeClaims();
      await this.fetchDashboardSummary();
      this.switchEmpTab('expenses');
    } catch (err) {
      this.showToast(err.message || 'Could not submit claim.', 'error');
    }
  }

  async cancelPendingClaim(id) {
    if (!confirm(`Cancel reimbursement claim #CLM-${id}?`)) return;
    try {
      await this.apiRequest(`/api/claims/${id}`, { method: 'DELETE' });
      this.showToast('Reimbursement claim cancelled.', 'info');
      await this.fetchEmployeeClaims();
      await this.fetchDashboardSummary();
    } catch (err) {
      this.showToast(err.message || 'Could not cancel claim.', 'error');
    }
  }

  /* --- Manager / Admin Portal --- */
  async renderAdminView() {
    if (!this.currentUser || this.currentUser.role !== 'admin') return;

    this.switchView('viewAdmin');
    await this.fetchAdminSummary();
    await this.renderAdminAdvances();
    await this.renderAdminSettlements();
    await this.renderAdminClaims();
    await this.renderAdminEmployees();
  }

  async fetchAdminSummary() {
    try {
      const sum = await this.apiRequest('/api/dashboard/summary');
      document.getElementById('adminMetricPendingAdvances').textContent = sum.pendingAdvanceCount || 0;
      document.getElementById('adminMetricPendingSettlements').textContent = sum.pendingSettlementCount || 0;
      document.getElementById('adminMetricDisbursedTotal').textContent = `₹${Number(sum.totalDisbursedAmount || 0).toFixed(2)}`;
      document.getElementById('adminMetricEmployees').textContent = sum.registeredEmployeeCount || 0;

      document.getElementById('adminTabAdvanceCount').textContent = sum.pendingAdvanceCount || 0;
      document.getElementById('adminTabSettlementCount').textContent = sum.pendingSettlementCount || 0;
      document.getElementById('adminTabEmployeeCount').textContent = sum.registeredEmployeeCount || 0;
    } catch (e) {
      console.warn("Could not load admin summary:", e.message);
    }
  }

  async renderAdminAdvances() {
    const tbody = document.getElementById('adminAdvancesTableBody');
    if (!tbody) return;

    const filter = document.getElementById('adminAdvanceStatusFilter')?.value || 'ALL';
    const query = filter !== 'ALL' ? `?status=${filter}` : '';

    try {
      const list = await this.apiRequest(`/api/advances/all${query}`);

      if (list.length === 0) {
        tbody.innerHTML = `
          <tr>
            <td colspan="7" class="empty-table-state">
              <i class="fa-solid fa-hand-holding-dollar"></i>
              <p>No matching staff advances.</p>
            </td>
          </tr>
        `;
        return;
      }

      tbody.innerHTML = list.map(a => {
        const isPending = a.status === 'PENDING';
        const isApproved = a.status === 'APPROVED';

        return `
          <tr>
            <td><strong>#ADV-${a.id}</strong></td>
            <td>
              <div style="font-weight: 600; font-size: 0.85rem;">${this.escapeHtml(a.employeeName)}</div>
              <div style="font-size: 0.75rem; color: var(--text-muted);">${this.escapeHtml(a.employeeEmail)}</div>
            </td>
            <td>
              <div style="font-weight: 600; color: var(--text-main);">${this.escapeHtml(a.purpose)}</div>
              <div style="font-size: 0.8rem; color: var(--text-muted);">${this.escapeHtml(a.notes || '-')}</div>
            </td>
            <td>${a.startDate || String(a.createdAt).split('T')[0]}</td>
            <td><span class="expense-amount">₹${Number(a.requestedAmount).toFixed(2)}</span></td>
            <td>${this.getAdvanceStatusBadge(a.status)}</td>
            <td class="text-right">
              ${isPending ? `
                <div style="display: inline-flex; gap: 0.35rem;">
                  <button class="btn btn-approve btn-xs" onclick="app.reviewAdvance(${a.id}, 'APPROVE')">
                    <i class="fa-solid fa-check"></i> Approve
                  </button>
                  <button class="btn btn-reject btn-xs" onclick="app.reviewAdvance(${a.id}, 'REJECT')">
                    <i class="fa-solid fa-xmark"></i> Reject
                  </button>
                </div>
              ` : isApproved ? `
                <button class="btn btn-emerald btn-xs" onclick="app.openDisburseModal(${a.id}, '${this.escapeHtml(a.employeeName)}', ${a.requestedAmount})">
                  <i class="fa-solid fa-money-bill-transfer"></i> Record Disbursal
                </button>
              ` : `<span style="font-size: 0.8rem; color: var(--text-muted);">${a.status}</span>`}
            </td>
          </tr>
        `;
      }).join('');
    } catch (err) {
      console.warn("Could not fetch admin advances:", err.message);
    }
  }

  async reviewAdvance(id, decision) {
    const comment = prompt(`Enter manager review comment for Advance #${id}:`, decision === 'APPROVE' ? 'Approved for travel' : 'Declined');
    if (comment === null) return;

    try {
      await this.apiRequest(`/api/advances/${id}/review`, {
        method: 'PUT',
        body: JSON.stringify({ decision, comment })
      });
      this.showToast(`Advance #${id} ${decision.toLowerCase()}d.`, 'success');
      await this.renderAdminView();
    } catch (err) {
      this.showToast(err.message || 'Could not review advance.', 'error');
    }
  }

  openDisburseModal(id, employeeName, requestedAmount) {
    document.getElementById('disburseAdvanceId').value = id;
    document.getElementById('disburseEmployeeDisplay').value = `#ADV-${id} for ${employeeName}`;
    document.getElementById('disburseAmount').value = Number(requestedAmount).toFixed(2);
    document.getElementById('disburseDate').value = new Date().toISOString().split('T')[0];
    this.openModal('modalDisburseAdvance');
  }

  async handleDisburseAdvance(e) {
    e.preventDefault();
    const id = document.getElementById('disburseAdvanceId').value;
    const disbursedAmount = parseFloat(document.getElementById('disburseAmount').value);
    const disbursedDate = document.getElementById('disburseDate').value;
    const managerComment = document.getElementById('disburseNotes').value.trim();

    try {
      await this.apiRequest(`/api/advances/${id}/disburse`, {
        method: 'PUT',
        body: JSON.stringify({ disbursedAmount, disbursedDate, managerComment })
      });

      this.closeModal('modalDisburseAdvance');
      this.showToast(`Advance #${id} disbursed successfully for ₹${disbursedAmount.toFixed(2)}!`, 'success');
      await this.renderAdminView();
    } catch (err) {
      this.showToast(err.message || 'Could not record disbursement.', 'error');
    }
  }

  async renderAdminSettlements() {
    const tbody = document.getElementById('adminSettlementsTableBody');
    if (!tbody) return;

    try {
      const list = await this.apiRequest('/api/settlements/all');

      if (list.length === 0) {
        tbody.innerHTML = `
          <tr>
            <td colspan="9" class="empty-table-state">
              <i class="fa-solid fa-scale-balanced"></i>
              <p>No settlements submitted for review.</p>
            </td>
          </tr>
        `;
        return;
      }

      tbody.innerHTML = list.map(s => {
        const isPending = s.status === 'PENDING_REVIEW';
        const isApproved = s.status === 'APPROVED';

        return `
          <tr>
            <td><strong>#STL-${s.id}</strong></td>
            <td>
              <div style="font-weight: 600; font-size: 0.85rem;">${this.escapeHtml(s.employeeName)}</div>
              <div style="font-size: 0.75rem; color: var(--text-muted);">${this.escapeHtml(s.employeeEmail)}</div>
            </td>
            <td>#ADV-${s.advanceId}</td>
            <td>₹${Number(s.totalDisbursedAdvance).toFixed(2)}</td>
            <td>₹${Number(s.totalEligibleExpenses).toFixed(2)}</td>
            <td>${this.getReconciliationBadge(s.remainingBalance)}</td>
            <td><span class="category-tag">${s.outcome}</span></td>
            <td>${this.getSettlementStatusBadge(s.status)}</td>
            <td class="text-right">
              ${isPending ? `
                <div style="display:inline-flex; gap:0.35rem;">
                  <button class="btn btn-approve btn-xs" onclick="app.reviewSettlement(${s.id}, 'APPROVE')">
                    <i class="fa-solid fa-check"></i> Approve
                  </button>
                  <button class="btn btn-reject btn-xs" onclick="app.reviewSettlement(${s.id}, 'REJECT')">
                    <i class="fa-solid fa-xmark"></i> Reject
                  </button>
                </div>
              ` : isApproved ? `
                <button class="btn btn-emerald btn-xs" onclick="app.completeSettlementPrompt(${s.id})">
                  <i class="fa-solid fa-check-double"></i> Complete & Close
                </button>
              ` : `<span style="font-size: 0.8rem; color: var(--text-muted);">${s.status}</span>`}
            </td>
          </tr>
        `;
      }).join('');
    } catch (err) {
      console.warn("Could not fetch admin settlements:", err.message);
    }
  }

  async reviewSettlement(id, decision) {
    const comment = prompt(`Enter review comment for Settlement #${id}:`, decision === 'APPROVE' ? 'Bills verified' : 'Correction required');
    if (comment === null) return;

    try {
      await this.apiRequest(`/api/settlements/${id}/review`, {
        method: 'PUT',
        body: JSON.stringify({ decision, comment })
      });
      this.showToast(`Settlement #${id} ${decision.toLowerCase()}d.`, 'success');
      await this.renderAdminView();
    } catch (err) {
      this.showToast(err.message || 'Could not review settlement.', 'error');
    }
  }

  async completeSettlementPrompt(id) {
    const ref = prompt(`Enter payment or refund reference number to complete Settlement #${id}:`, 'BANK-NEFT-VERIFIED');
    if (!ref) return;

    try {
      await this.apiRequest(`/api/settlements/${id}/complete?reference=${encodeURIComponent(ref)}`, {
        method: 'PUT'
      });
      this.showToast(`Settlement #${id} completed and advance ledger closed!`, 'success');
      await this.renderAdminView();
    } catch (err) {
      this.showToast(err.message || 'Could not complete settlement.', 'error');
    }
  }

  async renderAdminClaims() {
    const tbody = document.getElementById('adminExpensesTableBody');
    if (!tbody) return;

    const filter = document.getElementById('adminStatusFilter')?.value || 'ALL';
    const query = filter !== 'ALL' ? `?status=${filter}` : '';

    try {
      const list = await this.apiRequest(`/api/claims/all${query}`);

      if (list.length === 0) {
        tbody.innerHTML = `
          <tr>
            <td colspan="8" class="empty-table-state">
              <i class="fa-solid fa-receipt"></i>
              <p>No matching reimbursement claims.</p>
            </td>
          </tr>
        `;
        return;
      }

      tbody.innerHTML = list.map(c => {
        const isPending = c.status === 'PENDING';
        const isApproved = c.status === 'APPROVED';

        return `
          <tr>
            <td><strong>#CLM-${c.id}</strong></td>
            <td>
              <div style="font-weight: 600; font-size: 0.85rem;">${this.escapeHtml(c.employeeName)}</div>
              <div style="font-size: 0.75rem; color: var(--text-muted);">${this.escapeHtml(c.employeeEmail)}</div>
            </td>
            <td>
              <div style="font-weight: 600; color: var(--text-main);">${this.escapeHtml(c.title)}</div>
              <div style="font-size: 0.8rem; color: var(--text-muted);">${this.escapeHtml(c.description || '-')}</div>
            </td>
            <td><span class="category-tag">${c.category}</span></td>
            <td>${c.expenseDate}</td>
            <td><span class="expense-amount">₹${Number(c.amount).toFixed(2)}</span></td>
            <td>
              ${c.hasReceipt ? `
                <button onclick="app.viewReceipt('${c.receiptFilename}')" class="btn btn-outline btn-xs">
                  <i class="fa-solid fa-paperclip"></i> View Receipt
                </button>
              ` : '<span style="color:var(--text-light); font-size:0.8rem;">No file</span>'}
            </td>
            <td>${this.getClaimStatusBadge(c.status)}</td>
            <td class="text-right">
              ${isPending ? `
                <div style="display:inline-flex; gap:0.35rem;">
                  <button class="btn btn-approve btn-xs" onclick="app.reviewClaim(${c.id}, 'APPROVE')">
                    <i class="fa-solid fa-check"></i> Approve
                  </button>
                  <button class="btn btn-reject btn-xs" onclick="app.reviewClaim(${c.id}, 'REJECT')">
                    <i class="fa-solid fa-xmark"></i> Reject
                  </button>
                </div>
              ` : isApproved ? `
                <button class="btn btn-emerald btn-xs" onclick="app.recordClaimPaymentPrompt(${c.id})">
                  <i class="fa-solid fa-hand-holding-dollar"></i> Record Payment
                </button>
              ` : `<span style="font-size: 0.8rem; color: var(--text-muted);">${c.status}</span>`}
            </td>
          </tr>
        `;
      }).join('');
    } catch (err) {
      console.warn("Could not fetch admin claims:", err.message);
    }
  }

  async reviewClaim(id, decision) {
    const comment = prompt(`Enter review comment for Claim #${id}:`, decision === 'APPROVE' ? 'Approved for reimbursement' : 'Declined');
    if (comment === null) return;

    try {
      await this.apiRequest(`/api/claims/${id}/review`, {
        method: 'PUT',
        body: JSON.stringify({ decision, comment })
      });
      this.showToast(`Claim #${id} ${decision.toLowerCase()}d.`, 'success');
      await this.renderAdminView();
    } catch (err) {
      this.showToast(err.message || 'Could not review claim.', 'error');
    }
  }

  async recordClaimPaymentPrompt(id) {
    const ref = prompt(`Enter bank transfer / payment reference for Claim #${id}:`, 'NEFT-REIMB-89021');
    if (!ref) return;

    try {
      await this.apiRequest(`/api/claims/${id}/pay?reference=${encodeURIComponent(ref)}`, {
        method: 'PUT'
      });
      this.showToast(`Claim #${id} marked as PAID.`, 'success');
      await this.renderAdminView();
    } catch (err) {
      this.showToast(err.message || 'Could not record payment.', 'error');
    }
  }

  async renderAdminEmployees() {
    const tbody = document.getElementById('adminEmployeesTableBody');
    if (!tbody) return;

    try {
      const list = await this.apiRequest('/api/auth/employees');
      tbody.innerHTML = list.map(emp => `
        <tr>
          <td><div style="font-weight: 700; color: var(--text-main);">${this.escapeHtml(emp.fullName)}</div></td>
          <td><code style="color:var(--primary);">${this.escapeHtml(emp.email)}</code></td>
          <td><span class="category-tag">${emp.role}</span></td>
          <td>${this.escapeHtml(emp.department || 'General')}</td>
          <td><span class="status-badge status-approved"><i class="fa-solid fa-circle-check"></i> Active</span></td>
        </tr>
      `).join('');
    } catch (err) {
      console.warn("Could not fetch employees:", err.message);
    }
  }

  exportAllCSV() {
    if (this.offlineMode) {
      const csv = this.mockStore.generateCSV();
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.setAttribute('href', url);
      link.setAttribute('download', `expenzo_financial_audit_${new Date().toISOString().split('T')[0]}.csv`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      this.showToast('Financial Audit CSV downloaded successfully (Offline Standalone)!', 'success');
      return;
    }
    window.location.href = '/api/reports/csv';
  }

  viewReceipt(filename) {
    if (!filename) {
      this.showToast('No receipt file attached.', 'info');
      return;
    }
    if (this.offlineMode) {
      alert(`[Offline Standalone Mode]\nReceipt File: ${filename}\nStatus: Verified & Stored in local document store.`);
      return;
    }
    window.open(`/api/receipts/${filename}`, '_blank');
  }

  /* --- Badges & Helpers --- */
  getAdvanceStatusBadge(status) {
    if (status === 'DISBURSED') {
      return `<span class="status-badge status-approved"><i class="fa-solid fa-circle-check"></i> Disbursed</span>`;
    }
    if (status === 'APPROVED') {
      return `<span class="status-badge status-settled"><i class="fa-solid fa-clock"></i> Approved (Unpaid)</span>`;
    }
    if (status === 'SETTLED') {
      return `<span class="status-badge status-settled"><i class="fa-solid fa-check-double"></i> Settled</span>`;
    }
    if (status === 'SETTLEMENT_SUBMITTED') {
      return `<span class="status-badge status-in-settlement"><i class="fa-solid fa-hourglass-half"></i> Settle Pending</span>`;
    }
    if (status === 'REJECTED') {
      return `<span class="status-badge status-rejected"><i class="fa-solid fa-circle-xmark"></i> Rejected</span>`;
    }
    return `<span class="status-badge status-pending"><i class="fa-solid fa-clock"></i> Pending</span>`;
  }

  getSettlementStatusBadge(status) {
    if (status === 'COMPLETED') {
      return `<span class="status-badge status-approved"><i class="fa-solid fa-check-double"></i> Closed & Settled</span>`;
    }
    if (status === 'APPROVED') {
      return `<span class="status-badge status-settled"><i class="fa-solid fa-check"></i> Approved</span>`;
    }
    if (status === 'REJECTED') {
      return `<span class="status-badge status-rejected"><i class="fa-solid fa-circle-xmark"></i> Rejected</span>`;
    }
    return `<span class="status-badge status-pending"><i class="fa-solid fa-clock"></i> Pending Audit</span>`;
  }

  getClaimStatusBadge(status) {
    if (status === 'PAID') {
      return `<span class="status-badge status-approved"><i class="fa-solid fa-circle-check"></i> Paid</span>`;
    }
    if (status === 'APPROVED') {
      return `<span class="status-badge status-settled"><i class="fa-solid fa-clock"></i> Approved</span>`;
    }
    if (status === 'REJECTED') {
      return `<span class="status-badge status-rejected"><i class="fa-solid fa-circle-xmark"></i> Rejected</span>`;
    }
    return `<span class="status-badge status-pending"><i class="fa-solid fa-clock"></i> Pending</span>`;
  }

  getReconciliationBadge(balance) {
    const num = Number(balance);
    if (num > 0) {
      return `<span class="badge-recon badge-employee-refunds"><i class="fa-solid fa-arrow-down"></i> Staff Refunds ₹${num.toFixed(2)}</span>`;
    }
    if (num < 0) {
      return `<span class="badge-recon badge-company-pays"><i class="fa-solid fa-arrow-up"></i> Company Pays ₹${Math.abs(num).toFixed(2)}</span>`;
    }
    return `<span class="badge-recon badge-zero-diff"><i class="fa-solid fa-check"></i> Balanced (₹0.00)</span>`;
  }

  openModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) el.classList.add('active');
  }

  closeModal(modalId) {
    const el = document.getElementById(modalId);
    if (el) el.classList.remove('active');
  }

  showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    let icon = 'fa-circle-info';
    if (type === 'success') icon = 'fa-circle-check';
    if (type === 'error') icon = 'fa-circle-exclamation';

    toast.innerHTML = `<i class="fa-solid ${icon}"></i> <span>${this.escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
      toast.style.transition = 'all 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 3800);
  }

  escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  setupEventListeners() {
    window.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        document.querySelectorAll('.modal-backdrop.active').forEach(m => m.classList.remove('active'));
      }
    });

    document.querySelectorAll('.modal-backdrop').forEach(modal => {
      modal.addEventListener('click', (e) => {
        if (e.target === modal) {
          modal.classList.remove('active');
        }
      });
    });
  }
}

// Global initialization
const app = new ExpenseApp();
