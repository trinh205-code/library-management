import API from './axiosInstance';

export const getMyFines = (params = {}) =>
  API.get('/api/fines/my', { params });

export const getAllFines = (params = {}) =>
  API.get('/api/fines', { params });

export const createFine = (fineRequest) =>
  API.post('/api/fines', fineRequest);

export const waiveFine = (request) =>
  API.post('/api/fines/waive', request);

export const payFine = (id, transactionId) =>
  API.post(`/api/fines/${id}/pay`, null, { params: { transactionId } });
