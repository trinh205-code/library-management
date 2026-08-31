import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { IoRocketOutline, IoCheckmarkCircle } from 'react-icons/io5';
import toast from 'react-hot-toast';
import { getSubscriptionPlans, getUserActiveSubscriptions, subscribe } from '../../../api/subscriptionApi';
import './subscriptions.css';

export default function SubscriptionsPage() {
  const [plans, setPlans] = useState([]);
  const [activePlan, setActivePlan] = useState(null);
  const [loading, setLoading] = useState(true);
  const [now, setNow] = useState(Date.now());
  const [searchParams, setSearchParams] = useSearchParams();

  useEffect(() => {
    fetchData();

    const paymentStatus = searchParams.get('payment');
    if (paymentStatus === 'success') {
      toast.success('Your subscription was upgraded successfully!');
      setSearchParams({});
    } else if (paymentStatus === 'failed') {
      toast.error('Payment failed or cancelled.');
      setSearchParams({});
    }
  }, [searchParams, setSearchParams]);

  useEffect(() => {
    const intervalId = setInterval(() => {
      setNow(Date.now());
    }, 1000);

    return () => clearInterval(intervalId);
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [plansRes, activeRes] = await Promise.allSettled([
        getSubscriptionPlans(),
        getUserActiveSubscriptions()
      ]);
      
      if (plansRes.status === 'fulfilled') {
        const sortedPlans = (plansRes.value.data || []).sort((a,b) => a.displayOrder - b.displayOrder);
        setPlans(sortedPlans);
      }
      if (activeRes.status === 'fulfilled') {
        setActivePlan(activeRes.value.data || null);
      }
    } catch (err) {
      toast.error('Failed to load subscription details');
    } finally {
      setLoading(false);
    }
  };

  const handleUpgrade = async (planId) => {
    try {
      const res = await subscribe({ planId });
      if (res.data?.checkoutUrl) {
        window.location.href = res.data.checkoutUrl;
      } else {
        toast.success('Subscription updated successfully!');
        fetchData();
      }
    } catch (err) {
      toast.error(err.response?.data?.message || 'Upgrade failed');
    }
  };

  const formatCurrency = (amount) => {
    if (amount === 0 || !amount) return 'Free';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  const getExpiryCountdown = () => {
    if (!activePlan?.endDate) return null;

    const endAt = new Date(`${activePlan.endDate}T23:59:59`).getTime();
    const diff = endAt - now;

    if (Number.isNaN(endAt) || diff <= 0) {
      return { isExpired: true, label: 'Subscription expired' };
    }

    const totalSeconds = Math.floor(diff / 1000);
    const days = Math.floor(totalSeconds / (24 * 3600));
    const hours = Math.floor((totalSeconds % (24 * 3600)) / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    return {
      isExpired: false,
      label: `${days}d ${hours}h ${minutes}m ${seconds}s`,
      endDate: activePlan.endDate,
    };
  };

  const expiryCountdown = getExpiryCountdown();

  return (
    <div className="page-container subscriptions-page">
      <div className="subs-header">
        <h1>Choose Your <span className="text-gradient">Reading Journey</span></h1>
        <p>Unlock more books, extended loan periods, and premium features with our flexible plans.</p>
      </div>

      {activePlan && expiryCountdown && (
        <div className={`subs-countdown ${expiryCountdown.isExpired ? 'expired' : ''}`}>
          <span className="subs-countdown-label">
            {expiryCountdown.isExpired ? 'Current plan has expired' : `Current plan (${activePlan.planName}) expires in`}
          </span>
          <strong className="subs-countdown-value">
            {expiryCountdown.isExpired ? 'Please renew to continue premium benefits.' : expiryCountdown.label}
          </strong>
          {!expiryCountdown.isExpired && (
            <span className="subs-countdown-date">
              Expiry date: {new Date(expiryCountdown.endDate).toLocaleDateString()}
            </span>
          )}
        </div>
      )}

      {loading ? (
        <div className="spinner-container"><div className="spinner"></div></div>
      ) : (
        <div className="pricing-grid">
          {plans.map((plan) => (
            <div key={plan.id} className={`pricing-card ${plan.isFeatured ? 'featured' : ''}`}>
              {plan.isFeatured && (
                <div className="featured-badge-custom">{plan.badgeText || 'Most Popular'}</div>
              )}
              
              <h2 className="plan-name-custom">{plan.name}</h2>
              
              <div className="plan-price-box">
                <span className="plan-price-custom">{formatCurrency(plan.price)}</span>
                <span className="plan-price-period">/ {plan.durationDays} days</span>
              </div>
              
              <ul className="plan-features-list">
                <li>
                  <IoCheckmarkCircle className="check-icon-custom" /> 
                  Borrow up to {plan.maxBooksAllowed} books dynamically
                </li>
                <li>
                  <IoCheckmarkCircle className="check-icon-custom" /> 
                  {plan.maxDaysPerBook} days extended loan period
                </li>
                {plan.description && (
                  <li>
                    <IoCheckmarkCircle className="check-icon-custom" /> 
                    {plan.description}
                  </li>
                )}
              </ul>
              
              {activePlan && activePlan.planCode === plan.planCode ? (
                <button className="subs-btn btn-current">Current Plan</button>
              ) : (
                <button 
                  className="subs-btn btn-upgrade" 
                  onClick={() => handleUpgrade(plan.id)}
                >
                  Upgrade Now
                </button>
              )}
            </div>
          ))}
        </div>
      )}

      <div className="subs-footer">
        <div className="subs-footer-icon">
          <IoRocketOutline />
        </div>
        <h2>More features coming soon!</h2>
        <p>We are actively working on amazing new community features, digital book lending, and AI reading recommendations for our Pro and Elite members.</p>
      </div>
    </div>
  );
}
