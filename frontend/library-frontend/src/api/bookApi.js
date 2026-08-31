import API from './axiosInstance';

export const searchBooks = (params = {}) =>
  API.get('/api/books', { params });

export const advancedSearchBooks = (searchRequest) =>
  API.post('/api/books/search', searchRequest);

export const getBookById = (id) =>
  API.get(`/api/books/${id}`);

export const createBook = (bookData) =>
  API.post('/api/books', bookData);

export const createBooksBulk = (books) =>
  API.post('/api/books/bulk', books);

export const updateBook = (id, bookData) =>
  API.put(`/api/books/${id}`, bookData);

export const deleteBook = (id) =>
  API.delete(`/api/books/${id}`);

export const hardDeleteBook = (id) =>
  API.delete(`/api/books/${id}/permanent`);

export const getBookStats = () =>
  API.get('/api/books/status');

export const getTrendingBooks = (limit = 10) =>
  API.get('/api/books/trending', { params: { limit } });

export const getRandomBooks = (limit = 10) =>
  API.get('/api/books/random', { params: { limit } });

export const getMostReadBooks = (limit = 10) =>
  API.get('/api/books/most-read', { params: { limit } });
