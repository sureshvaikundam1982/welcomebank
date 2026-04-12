import React, { useState, useEffect } from 'react';
import { transactionAPI, accountAPI } from '../services/api';

function Transactions() {
  const [transactions, setTransactions] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState({
    type: 'DEPOSIT',
    amount: '',
    fromAccountNumber: '',
    toAccountNumber: '',
    description: '',
  });

  const loadData = async () => {
    setLoading(true);
    try {
      const [txnRes, accRes] = await Promise.allSettled([
        transactionAPI.getMyTransactions(),
        accountAPI.getMyAccounts(),
      ]);
      if (txnRes.status === 'fulfilled') setTransactions(txnRes.value.data || []);
      if (accRes.status === 'fulfilled') setAccounts(accRes.value.data || []);
    } catch (err) {
      setError('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadData(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');

    try {
      const payload = {
        type: form.type,
        amount: parseFloat(form.amount),
        description: form.description,
      };

      if (form.type === 'DEPOSIT') {
        payload.toAccountNumber = form.toAccountNumber;
      } else if (form.type === 'WITHDRAWAL') {
        payload.fromAccountNumber = form.fromAccountNumber;
      } else if (form.type === 'TRANSFER') {
        payload.fromAccountNumber = form.fromAccountNumber;
        payload.toAccountNumber = form.toAccountNumber;
      }

      await transactionAPI.processTransaction(payload);
      setSuccess(`${form.type} of $${form.amount} processed successfully!`);
      setShowModal(false);
      setForm({ type: 'DEPOSIT', amount: '', fromAccountNumber: '', toAccountNumber: '', description: '' });
      loadData();
      setTimeout(() => setSuccess(''), 5000);
    } catch (err) {
      setError('Transaction failed: ' + (err.response?.data?.message || err.message));
    } finally {
      setSubmitting(false);
    }
  };

  const formatCurrency = (amount) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString();
  };

  const getStatusBadge = (status) => {
    const map = { COMPLETED: 'badge-success', FAILED: 'badge-danger', PENDING: 'badge-warning' };
    return `badge ${map[status] || 'badge-info'}`;
  };

  const getTypeStyle = (type) => {
    const colors = { DEPOSIT: '#2e7d32', WITHDRAWAL: '#c62828', TRANSFER: '#1565c0' };
    return { color: colors[type] || '#616161', fontWeight: 700, fontSize: 13 };
  };

  const activeAccounts = accounts.filter(a => a.status === 'ACTIVE');

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Transactions</h1>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          + New Transaction
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      {loading ? (
        <div className="loading">Loading transactions...</div>
      ) : transactions.length === 0 ? (
        <div className="card">
          <div className="empty-state">
            <p>No transactions yet</p>
            <button className="btn btn-primary" onClick={() => setShowModal(true)}>
              Make Your First Transaction
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
                  <th>Transaction ID</th>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>From Account</th>
                  <th>To Account</th>
                  <th>Status</th>
                  <th>Description</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((txn, idx) => (
                  <tr key={txn.id}>
                    <td>{idx + 1}</td>
                    <td>
                      <code style={{ background: '#f5f5f5', padding: '2px 6px', borderRadius: 4, fontSize: 11 }}>
                        {txn.transactionId}
                      </code>
                    </td>
                    <td><span style={getTypeStyle(txn.type)}>{txn.type}</span></td>
                    <td style={{ fontWeight: 600 }}>{formatCurrency(txn.amount)}</td>
                    <td style={{ fontSize: 12 }}>{txn.fromAccountNumber || '-'}</td>
                    <td style={{ fontSize: 12 }}>{txn.toAccountNumber || '-'}</td>
                    <td><span className={getStatusBadge(txn.status)}>{txn.status}</span></td>
                    <td style={{ color: '#757575', fontSize: 13 }}>{txn.description || '-'}</td>
                    <td style={{ fontSize: 12 }}>{formatDate(txn.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Transaction Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-title">New Transaction</div>

            {error && <div className="alert alert-error">{error}</div>}

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Transaction Type *</label>
                <select
                  className="form-control"
                  value={form.type}
                  onChange={e => setForm({ ...form, type: e.target.value, fromAccountNumber: '', toAccountNumber: '' })}
                >
                  <option value="DEPOSIT">Deposit</option>
                  <option value="WITHDRAWAL">Withdrawal</option>
                  <option value="TRANSFER">Transfer</option>
                </select>
              </div>

              <div className="form-group">
                <label>Amount *</label>
                <input
                  type="number"
                  className="form-control"
                  placeholder="0.00"
                  min="0.01"
                  step="0.01"
                  value={form.amount}
                  onChange={e => setForm({ ...form, amount: e.target.value })}
                  required
                />
              </div>

              {(form.type === 'WITHDRAWAL' || form.type === 'TRANSFER') && (
                <div className="form-group">
                  <label>From Account *</label>
                  <select
                    className="form-control"
                    value={form.fromAccountNumber}
                    onChange={e => setForm({ ...form, fromAccountNumber: e.target.value })}
                    required
                  >
                    <option value="">Select source account</option>
                    {activeAccounts.map(acc => (
                      <option key={acc.id} value={acc.accountNumber}>
                        {acc.accountNumber} ({acc.accountType}) - ${acc.balance}
                      </option>
                    ))}
                  </select>
                </div>
              )}

              {(form.type === 'DEPOSIT' || form.type === 'TRANSFER') && (
                <div className="form-group">
                  <label>
                    {form.type === 'TRANSFER' ? 'To Account *' : 'Destination Account *'}
                  </label>
                  {form.type === 'DEPOSIT' ? (
                    <select
                      className="form-control"
                      value={form.toAccountNumber}
                      onChange={e => setForm({ ...form, toAccountNumber: e.target.value })}
                      required
                    >
                      <option value="">Select destination account</option>
                      {activeAccounts.map(acc => (
                        <option key={acc.id} value={acc.accountNumber}>
                          {acc.accountNumber} ({acc.accountType})
                        </option>
                      ))}
                    </select>
                  ) : (
                    <input
                      type="text"
                      className="form-control"
                      placeholder="Enter destination account number"
                      value={form.toAccountNumber}
                      onChange={e => setForm({ ...form, toAccountNumber: e.target.value })}
                      required
                    />
                  )}
                </div>
              )}

              <div className="form-group">
                <label>Description</label>
                <input
                  type="text"
                  className="form-control"
                  placeholder="Optional description"
                  value={form.description}
                  onChange={e => setForm({ ...form, description: e.target.value })}
                />
              </div>

              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => { setShowModal(false); setError(''); }}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? 'Processing...' : 'Submit Transaction'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Transactions;
