import API from './axiosInstance';

export const addToWishlist = (bookId, notes) =>
  API.post(`/api/wishlists/${bookId}`, null, { params: { notes } });

export const removeFromWishlist = (bookId) =>
  API.delete(`/api/wishlists/${bookId}`);

export const getWishlists = (params = {}) =>
  API.get('/api/wishlists', { params });
