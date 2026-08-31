import API from './axiosInstance';

export const createReservation = (request) =>
  API.post('/api/reservations', request);

export const createReservationForUser = (userId, request) =>
  API.post(`/api/reservations/user/${userId}`, request);

export const cancelReservation = (reservationId) =>
  API.put(`/api/reservations/${reservationId}/cancel`);

export const readyReservation = (reservationId) =>
  API.put(`/api/reservations/${reservationId}/ready`);

export const rejectReservation = (reservationId) =>
  API.put(`/api/reservations/${reservationId}/reject`);

export const userConfirmReservation = (reservationId) =>
  API.put(`/api/reservations/${reservationId}/user-confirm`);

export const confirmBorrowedReservation = (reservationId) =>
  API.put(`/api/reservations/${reservationId}/confirm-borrowed`);

export const getMyReservations = (params = {}) =>
  API.get('/api/reservations/my', { params });

export const searchReservations = (params = {}) =>
  API.get('/api/reservations', { params });
