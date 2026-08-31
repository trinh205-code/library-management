import API from './axiosInstance';

export const createReview = (request) =>
  API.post('/api/reviews', request);

export const updateReview = (reviewId, request) =>
  API.put(`/api/reviews/${reviewId}`, request);

export const deleteReview = (reviewId) =>
  API.delete(`/api/reviews/${reviewId}`);

export const getReviewsByBook = (bookId, params = {}) =>
  API.get(`/api/reviews/book/${bookId}`, { params });

export const checkUserReview = (bookId) =>
  API.get(`/api/reviews/book/${bookId}/check`);
