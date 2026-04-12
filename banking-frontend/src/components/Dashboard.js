import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { accountAPI, transactionAPI, authUtils } from '../services/api';

function Dashboard() {
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const user = authUtils.getUser();

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const [accRes, txnRes] = await Promise.allSettled([
          accountAPI.getMyAccounts(),
          transactionAPI.getMyTransactions(),
        ]);
        if (accRes.status === 'fulfilled') setAccounts(accRes.value.data || []);
        if (txnRes.status === 'fulfilled') setTransactions(txnRes.value.data || []);
      } catch (err) {
        console.error('Dashboard load error:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const totalBalance = accounts.reduce((sum, acc) => sum + (acc.balance || 0), 0);
  const activeAccounts = accounts.filter(a => a.status === 'ACTIVE').length;
  const recentTxns = transactions.slice(0, 5);

  const formatCurrency = (amount) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric', month: 'short', day: 'numeric',
    });
  };

  const getStatusBadge = (status) => {
    const classes = {
      COMPLETED: 'badge-success',
      ACTIVE: 'badge-success',
      FAILED: 'badge-danger',
      CLOSED: 'badge-secondary',
      PENDING: 'badge-warning',
    };
    return `badge ${classes[status] || 'badge-info'}`;
  };

  const getTxnTypeColor = (type) => {
    const colors = { DEPOSIT: '#2e7d32', WITHDRAWAL: '#c62828', TRANSFER: '#1565c0' };
    return colors[type] || '#616161';
  };

  if (loading) return <div className="loading">Loading dashboard...</div>;

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Welcome back, {user?.username || 'User'}</h1>
        <small style={{ color: '#757575' }}>{new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</small>
      </div>

      {/* Stats */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-label">Total Balance</div>
          <div className="stat-value" style={{ color: '#1a237e', fontSize: 24 }}>{formatCurrency(totalBalance)}</div>
        </div>
        <div className="stat-card green">
          <div className="stat-label">Active Accounts</div>
          <div className="stat-value">{activeAccounts}</div>
        </div>
        <div className="stat-card blue">
          <div className="stat-label">Total Transactions</div>
          <div className="stat-value">{transactions.length}</div>
        </div>
        <div className="stat-card orange">
          <div className="stat-label">Total Accounts</div>
          <div className="stat-value">{accounts.length}</div>
        </div>
      </div>

      <div className="grid-2" style={{ gap: 20, alignItems: 'start' }}>
        {/* Accounts summary */}
        <div className="card">
          <div className="card-title">My Accounts</div>
          {accounts.length === 0 ? (
            <div className="empty-state">
              <p>No accounts yet</p>
              <Link to="/accounts" className="btn btn-primary">Open Account</Link>
            </div>
          ) : (
            <>
              {accounts.map(acc => (
                <div key={acc.id} style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  padding: '12px 0',
                  borderBottom: '1px solid #f0f0f0'
                }}>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: 15 }}>{acc.accountType} Account</div>
                    <div style={{ color: '#757575', fontSize: 12, marginTop: 2 }}>{acc.accountNumber}</div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontWeight: 700, fontSize: 16, color: '#1a237e' }}>
                      {formatCurrency(acc.balance)}
                    </div>
                    <span className={getStatusBadge(acc.status)}>{acc.status}</span>
                  </div>
                </div>
              ))}
              <div style={{ marginTop: 16 }}>
                <Link to="/accounts" className="btn btn-outline" style={{ width: '100%' }}>
                  Manage Accounts
                </Link>
              </div>
            </>
          )}
        </div>

        {/* Recent transactions */}
        <div className="card">
          <div className="card-title">Recent Transactions</div>
          {recentTxns.length === 0 ? (
            <div className="empty-state">
              <p>No transactions yet</p>
              <Link to="/transactions" className="btn btn-primary">Make Transaction</Link>
            </div>
          ) : (
            <>
              {recentTxns.map(txn => (
                <div key={txn.id} style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  padding: '12px 0',
                  borderBottom: '1px solid #f0f0f0'
                }}>
                  <div>
                    <div style={{
                      fontWeight: 600,
                      fontSize: 14,
                      color: getTxnTypeColor(txn.type)
                    }}>
                      {txn.type}
                    </div>
                    <div style={{ color: '#757575', fontSize: 12, marginTop: 2 }}>
                      {formatDate(txn.createdAt)}
                    </div>
                  </div>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontWeight: 700, fontSize: 15 }}>
                      {formatCurrency(txn.amount)}
                    </div>
                    <span className={getStatusBadge(txn.status)}>{txn.status}</span>
                  </div>
                </div>
              ))}
              <div style={{ marginTop: 16 }}>
                <Link to="/transactions" className="btn btn-outline" style={{ width: '100%' }}>
                  View All Transactions
                </Link>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
