import React, { useState, useEffect } from 'react';
import { accountAPI } from '../services/api';

function Accounts() {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ accountType: 'SAVINGS', initialBalance: '', currency: 'USD' });
  const [submitting, setSubmitting] = useState(false);

  const loadAccounts = async () => {
    setLoading(true);
    try {
      const res = await accountAPI.getMyAccounts();
      setAccounts(res.data || []);
    } catch (err) {
      setError('Failed to load accounts. ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadAccounts(); }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      await accountAPI.createAccount({
        accountType: form.accountType,
        initialBalance: parseFloat(form.initialBalance) || 0,
        currency: form.currency,
      });
      setSuccess('Account created successfully!');
      setShowModal(false);
      setForm({ accountType: 'SAVINGS', initialBalance: '', currency: 'USD' });
      loadAccounts();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError('Failed to create account: ' + (err.response?.data?.message || err.message));
    } finally {
      setSubmitting(false);
    }
  };

  const handleClose = async (accountNumber) => {
    if (!window.confirm(`Close account ${accountNumber}? This cannot be undone.`)) return;
    try {
      await accountAPI.closeAccount(accountNumber);
      setSuccess('Account closed successfully');
      loadAccounts();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError('Failed to close account: ' + (err.response?.data?.message || err.message));
    }
  };

  const formatCurrency = (amount, currency = 'USD') =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount);

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString();
  };

  const getStatusBadge = (status) => {
    const map = { ACTIVE: 'badge-success', CLOSED: 'badge-secondary', SUSPENDED: 'badge-danger', INACTIVE: 'badge-warning' };
    return `badge ${map[status] || 'badge-info'}`;
  };

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">My Accounts</h1>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          + Open New Account
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {loading ? (
        <div className="loading">Loading accounts...</div>
      ) : accounts.length === 0 ? (
        <div className="card">
          <div className="empty-state">
            <p>You don't have any accounts yet.</p>
            <button className="btn btn-primary" onClick={() => setShowModal(true)}>
              Open Your First Account
            </button>
          </div>
        </div>
      ) : (
        <div className="card">
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Account Number</th>
                  <th>Type</th>
                  <th>Balance</th>
                  <th>Currency</th>
                  <th>Status</th>
                  <th>Created</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {accounts.map((acc, idx) => (
                  <tr key={acc.id}>
                    <td>{idx + 1}</td>
                    <td><code style={{ background: '#f5f5f5', padding: '2px 6px', borderRadius: 4 }}>{acc.accountNumber}</code></td>
                    <td>{acc.accountType}</td>
                    <td style={{ fontWeight: 600, color: '#1a237e' }}>{formatCurrency(acc.balance, acc.currency)}</td>
                    <td>{acc.currency}</td>
                    <td><span className={getStatusBadge(acc.status)}>{acc.status}</span></td>
                    <td>{formatDate(acc.createdAt)}</td>
                    <td>
                      {acc.status === 'ACTIVE' && (
                        <button
                          className="btn btn-danger"
                          style={{ padding: '5px 12px', fontSize: 12 }}
                          onClick={() => handleClose(acc.accountNumber)}
                        >
                          Close
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Create Account Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-title">Open New Account</div>
            <form onSubmit={handleCreate}>
              <div className="form-group">
                <label>Account Type *</label>
                <select
                  className="form-control"
                  value={form.accountType}
                  onChange={e => setForm({ ...form, accountType: e.target.value })}
                >
                  <option value="SAVINGS">Savings Account</option>
                  <option value="CHECKING">Checking Account</option>
                  <option value="CURRENT">Current Account</option>
                </select>
              </div>
              <div className="form-group">
                <label>Initial Deposit (USD)</label>
                <input
                  type="number"
                  className="form-control"
                  placeholder="0.00"
                  min="0"
                  step="0.01"
                  value={form.initialBalance}
                  onChange={e => setForm({ ...form, initialBalance: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>Currency</label>
                <select
                  className="form-control"
                  value={form.currency}
                  onChange={e => setForm({ ...form, currency: e.target.value })}
                >
                  <option value="USD">USD - US Dollar</option>
                  <option value="EUR">EUR - Euro</option>
                  <option value="GBP">GBP - British Pound</option>
                  <option value="INR">INR - Indian Rupee</option>
                </select>
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? 'Creating...' : 'Create Account'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Accounts;
