import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { authUtils } from './services/api';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import Accounts from './components/Accounts';
import Transactions from './components/Transactions';
import Navbar from './components/Navbar';
import './App.css';

function PrivateRoute({ children }) {
  return authUtils.isLoggedIn() ? children : <Navigate to="/login" replace />;
}

function App() {
  return (
    <Router>
      <div className="app">
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/*"
            element={
              <PrivateRoute>
                <Navbar />
                <div className="main-content">
                  <Routes>
                    <Route path="/" element={<Dashboard />} />
                    <Route path="/dashboard" element={<Dashboard />} />
                    <Route path="/accounts" element={<Accounts />} />
                    <Route path="/transactions" element={<Transactions />} />
                    <Route path="*" element={<Navigate to="/dashboard" replace />} />
                  </Routes>
                </div>
              </PrivateRoute>
            }
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
