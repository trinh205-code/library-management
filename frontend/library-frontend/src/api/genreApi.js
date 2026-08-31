import API from './axiosInstance';

export const getAllGenres = () =>
  API.get('/api/genres');

export const getGenreById = (id) =>
  API.get(`/api/genres/${id}`);

export const getTopLevelGenres = () =>
  API.get('/api/genres/top-level');

export const createGenre = (genre) =>
  API.post('/api/genres/create', genre);

export const updateGenre = (id, genre) =>
  API.put(`/api/genres/${id}`, genre);

export const deleteGenre = (id) =>
  API.delete(`/api/genres/${id}`);

export const hardDeleteGenre = (id) =>
  API.delete(`/api/genres/${id}/hard`);

export const getTotalActiveGenres = () =>
  API.get('/api/genres/count');

export const getBookCountByGenre = (id) =>
  API.get(`/api/genres/${id}/book-count`);
