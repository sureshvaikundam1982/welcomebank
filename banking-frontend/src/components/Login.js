import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authAPI, authUtils } from '../services/api';

function Login() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      // Login directly via auth-service through the gateway
      const response = await authAPI.login(form);
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

      setError(data.message || 'Login failed. Please check your credentials.');

    } catch (err) {
      const status = err.response?.status;
      const serverMsg = err.response?.data?.message
        || err.response?.data
        || err.message;

      if (status === 401) {
        setError('Invalid username or password.');
      } else if (status === 0 || err.code === 'ERR_NETWORK' || !err.response) {
        setError(
          'Cannot connect to the server. Make sure the API Gateway is running on port 8090.'
        );
      } else {
        setError(typeof serverMsg === 'string' ? serverMsg : 'Login failed. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-logo">
          <h1>Banking Portal</h1>
          <p>Sign in to your account</p>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Username</label>
            <input
              type="text"
              name="username"
              className="form-control"
              placeholder="Enter your username"
              value={form.username}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              name="password"
              className="form-control"
              placeholder="Enter your password"
              value={form.password}
              onChange={handleChange}
              required
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-full"
            disabled={loading}
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <div className="auth-footer">
          Don't have an account? <Link to="/register">Register here</Link>
        </div>
      </div>
    </div>
  );
}

export default Login;
