import API from './axiosInstance';

export const initiatePayment = (data) => API.post('/api/payment/initiate', data);

const formatDateTimeParam = (value, boundary = 'start') => {
  if (!value) return null;

  const raw = String(value).trim();
  if (!raw) return null;

  // If UI sends a date-only string (YYYY-MM-DD), expand it to full day bounds.
  if (/^\d{4}-\d{2}-\d{2}$/.test(raw)) {
    return boundary === 'end' ? `${raw}T23:59:59.999` : `${raw}T00:00:00`;
  }

  const parsed = new Date(raw);
  if (Number.isNaN(parsed.getTime())) return null;

  // Spring LocalDateTime parser accepts ISO date-time without trailing "Z".
  return parsed.toISOString().replace('Z', '');
};

export const getAllPayments = (filters = {}, page = 0, size = 10, sortBy = 'createdAt', sortDir = 'DESC') => {
  let query = `/api/payment?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`;
  if (filters.status && filters.status !== 'ALL') query += `&status=${encodeURIComponent(filters.status)}`;
  if (filters.type && filters.type !== 'ALL') query += `&type=${encodeURIComponent(filters.type)}`;
  if (filters.userName) query += `&userName=${encodeURIComponent(filters.userName)}`;

  const startDate = formatDateTimeParam(filters.startDate, 'start');
  const endDate = formatDateTimeParam(filters.endDate, 'end');

  if (startDate) query += `&startDate=${encodeURIComponent(startDate)}`;
  if (endDate) query += `&endDate=${encodeURIComponent(endDate)}`;
  return API.get(query);
};

export const getMyPayments = (page = 0, size = 10, sortBy = 'createdAt', sortDir = 'DESC') =>
  API.get(`/api/payment/my-payments?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir}`);
