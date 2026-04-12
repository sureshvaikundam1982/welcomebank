import axios from 'axios';

// React dev server proxies all /service-name/** requests to http://localhost:8090 (API Gateway)
// This avoids CORS — browser sees same-origin requests to localhost:3000
const AUTH_SERVICE         = '/auth-service/api/auth';
const REGISTRATION_SERVICE = '/registration-service/api/register';
const LOGIN_SERVICE        = '/login-service/api/login';
const ACCOUNT_SERVICE      = '/account-service/api/accounts';
const TRANSACTION_SERVICE  = '/transaction-service/api/transactions';

// Token helpers
const getToken = () => localStorage.getItem('token');
const setToken = (token) => localStorage.setItem('token', token);
const removeToken = () => localStorage.removeItem('token');
const getUser = () => {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
};
const setUser = (user) => localStorage.setItem('user', JSON.stringify(user));
const removeUser = () => localStorage.removeItem('user');

// Axios instance with auth header
const authAxios = axios.create();
authAxios.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  const user = getUser();
  if (user?.username) {
    config.headers['X-Auth-Username'] = user.username;
  }
  return config;
});

authAxios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      removeToken();
      removeUser();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth APIs
export const authAPI = {
  register: (data) => axios.post(`${AUTH_SERVICE}/register`, data),
  login: (data) => axios.post(`${AUTH_SERVICE}/login`, data),
  validateToken: (token) => axios.post(`${AUTH_SERVICE}/validate`, { token }),
};

// Registration API
export const registrationAPI = {
  register: (data) => axios.post(REGISTRATION_SERVICE, data),
};

// Login API
export const loginAPI = {
  login: (data) => axios.post(LOGIN_SERVICE, data),
};

// Account APIs
export const accountAPI = {
  createAccount: (data) => authAxios.post(ACCOUNT_SERVICE, data),
  getMyAccounts: () => authAxios.get(ACCOUNT_SERVICE),
  getAccountById: (id) => authAxios.get(`${ACCOUNT_SERVICE}/${id}`),
  getAccountByNumber: (number) => authAxios.get(`${ACCOUNT_SERVICE}/number/${number}`),
  updateBalance: (data) => authAxios.put(`${ACCOUNT_SERVICE}/balance`, data),
  closeAccount: (accountNumber) => authAxios.delete(`${ACCOUNT_SERVICE}/${accountNumber}/close`),
};

// Transaction APIs
export const transactionAPI = {
  processTransaction: (data) => authAxios.post(TRANSACTION_SERVICE, data),
  getMyTransactions: () => authAxios.get(TRANSACTION_SERVICE),
  getTransactionById: (id) => authAxios.get(`${TRANSACTION_SERVICE}/${id}`),
  getTransactionsByAccount: (accountNumber) => authAxios.get(`${TRANSACTION_SERVICE}/account/${accountNumber}`),
};

// Auth utilities
export const authUtils = {
  getToken,
  setToken,
  removeToken,
  getUser,
  setUser,
  removeUser,
  isLoggedIn: () => !!getToken(),
  logout: () => {
    removeToken();
    removeUser();
  },
};

export default authAxios;
