import API from './axiosInstance';

export const checkoutBook = (checkoutRequest) =>
  API.post('/api/book-loans/checkout', checkoutRequest);

export const checkoutBookForUser = (userId, checkoutRequest) =>
  API.post(`/api/book-loans/checkout/user/${userId}`, checkoutRequest);

export const checkinBook = (checkinRequest) =>
  API.post('/api/book-loans/checkin', checkinRequest);

export const renewBook = (renewalRequest) =>
  API.post('/api/book-loans/renew', renewalRequest);

export const getMyLoans = (params = {}) =>
  API.get('/api/book-loans/my', { params });

export const searchLoans = (searchRequest) =>
  API.post('/api/book-loans/search', searchRequest);

export const updateOverdueLoans = () =>
  API.post('/api/book-loans/admin/update-overdue');
