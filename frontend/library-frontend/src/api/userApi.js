import API from './axiosInstance';

export const getProfile = () =>
  API.get('/api/users/profile');

export const getAllUsers = () =>
  API.get('/api/users/list');

export const sendProfileUpdateOtp = () =>
  API.post('/api/users/profile/send-otp');

export const updateProfile = (data) =>
  API.put('/api/users/profile/update', data);

export const changePassword = (data) =>
  API.put('/api/users/profile/change-password', data);

export const adminUpdateUser = (id, data) =>
  API.put(`/api/users/admin/${id}`, data);

export const adminDeleteUser = (id) =>
  API.delete(`/api/users/admin/${id}`);
