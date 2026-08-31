import API from './axiosInstance';

export const login = (email, password) =>
  API.post('/auth/login', { email, password });

export const signup = (userData) =>
  API.post('/auth/signup', userData);

export const forgotPassword = (email) =>
  API.post('/auth/forgot-password', { email });

export const resetPassword = (token, password) =>
  API.post('/auth/reset-password', { token, password });
