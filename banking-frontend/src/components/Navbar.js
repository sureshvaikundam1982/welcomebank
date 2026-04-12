import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { authUtils } from '../services/api';

function Navbar() {
  const navigate = useNavigate();
  const user = authUtils.getUser();

  const handleLogout = () => {
    authUtils.logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <span>Banking</span> Portal
      </div>
      <div className="navbar-links">
        <NavLink to="/dashboard" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
          Dashboard
        </NavLink>
        <NavLink to="/accounts" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
          Accounts
        </NavLink>
        <NavLink to="/transactions" className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
          Transactions
        </NavLink>
        {user && <span className="nav-user">Hi, {user.username}</span>}
        <button className="btn-logout" onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  );
}

export default Navbar;
