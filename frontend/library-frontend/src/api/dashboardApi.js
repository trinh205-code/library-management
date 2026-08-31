import API from './axiosInstance';

export const getDashboardRevenueMetrics = (timeFrame = 'month') =>
  API.get('/api/admin/dashboard/revenue', { params: { timeFrame } });
