import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authAPI, authUtils } from '../services/api';

function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    phoneNumber: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    if (form.password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    setLoading(true);
    setError('');

    // Register directly against auth-service via the gateway
    try {
      const response = await authAPI.register({
        username: form.username.trim(),
        email: form.email.trim(),
        password: form.password,
        role: 'ROLE_USER',
      });

      const data = response.data;

      if (data.token) {
        authUtils.setToken(data.token);
        authUtils.setUser({
          username: data.username,
          email: data.email,
          role: data.role || 'ROLE_USER',
        });
        navigate('/dashboard');
        return;
      }

      // Successful HTTP but service returned an error message
      setError(data.message || 'Registration failed. Please try again.');

    } catch (err) {
      const status = err.response?.status;
      const serverMsg = err.response?.data?.message
        || err.response?.data
        || err.message;

      if (status === 400) {
        setError(serverMsg || 'Invalid input. Please check your details.');
      } else if (status === 0 || err.code === 'ERR_NETWORK' || !err.response) {
        setError(
          'Cannot connect to the server. Make sure the API Gateway is running on port 8090.'
        );
      } else {
        setError(typeof serverMsg === 'string' ? serverMsg : 'Registration failed. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card" style={{ maxWidth: 520 }}>
        <div className="auth-logo">
          <h1>Banking Portal</h1>
          <p>Create your account</p>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="grid-2">
            <div className="form-group">
              <label>First Name *</label>
              <input
                type="text"
                name="firstName"
                className="form-control"
                placeholder="First name"
                value={form.firstName}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Last Name *</label>
              <input
                type="text"
                name="lastName"
                className="form-control"
                placeholder="Last name"
                value={form.lastName}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label>Username *</label>
            <input
              type="text"
              name="username"
              className="form-control"
              placeholder="Choose a username (min 3 chars)"
              value={form.username}
              onChange={handleChange}
              minLength={3}
              required
            />
          </div>

          <div className="form-group">
            <label>Email *</label>
            <input
              type="email"
              name="email"
              className="form-control"
              placeholder="your@email.com"
              value={form.email}
              onChange={handleChange}
              required
            />
          </div>

          <div className="grid-2">
            <div className="form-group">
              <label>Password *</label>
              <input
                type="password"
                name="password"
                className="form-control"
                placeholder="Min 6 characters"
                value={form.password}
                onChange={handleChange}
                minLength={6}
                required
              />
            </div>
            <div className="form-group">
              <label>Confirm Password *</label>
              <input
                type="password"
                name="confirmPassword"
                className="form-control"
                placeholder="Repeat password"
                value={form.confirmPassword}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label>Phone Number</label>
            <input
              type="tel"
              name="phoneNumber"
              className="form-control"
              placeholder="Optional"
              value={form.phoneNumber}
              onChange={handleChange}
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-full"
            disabled={loading}
          >
            {loading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <div className="auth-footer">
          Already have an account? <Link to="/login">Sign in here</Link>
        </div>
      </div>
    </div>
  );
}

export default Register;
