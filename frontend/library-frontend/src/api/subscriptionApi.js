import API from './axiosInstance';

export const getSubscriptionPlans = () =>
  API.get('/api/subscription-plans');

export const subscribe = (subscriptionData) =>
  API.post('/api/subscriptions/subscribe', subscriptionData);

export const cancelSubscription = (subscriptionId, reason) =>
  API.post(`/api/subscriptions/cancel/${subscriptionId}`, null, { params: { reason } });

export const getUserActiveSubscriptions = (userId) =>
  API.get('/api/subscriptions/user/active', { params: { userId } });

export const getAllSubscriptions = () =>
  API.get('/api/subscriptions/admin');
