import { useState, useEffect } from 'react';
import { getBookStats } from '../../../api/bookApi';
import { getTotalActiveGenres } from '../../../api/genreApi';
import { getAllUsers } from '../../../api/userApi';
import { getDashboardRevenueMetrics } from '../../../api/dashboardApi';
import StatCard from '../../../components/StatCard';
import {
  LineChart, Line, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, ReferenceLine
} from 'recharts';
import {
  IoBookOutline, IoBookmarksOutline, IoPeopleOutline,
  IoReceiptOutline, IoTrendingUpOutline, IoAlertCircleOutline,
  IoCashOutline, IoStarOutline
} from 'react-icons/io5';

export default function AdminDashboard() {
  const [stats, setStats] = useState({
    totalBooks: 0, availableBooks: 0, totalGenres: 0, totalUsers: 0
  });
  
  const [revenueStats, setRevenueStats] = useState({
    totalRevenue: 0, subscriptionRevenue: 0, fineRevenue: 0,
    activeSubscriptions: 0, topSellingPlan: 'N/A', chartData: []
  });

  const [loading, setLoading] = useState(true);
  const [revenueLoading, setRevenueLoading] = useState(false);
  const [timeFrame, setTimeFrame] = useState('month');

  useEffect(() => { 
    fetchStats(); 
  }, []);

  useEffect(() => {
    fetchRevenueData();
  }, [timeFrame]);

  const fetchRevenueData = async () => {
    setRevenueLoading(true);
    try {
      const revRes = await getDashboardRevenueMetrics(timeFrame);
      if (revRes.data) {
        setRevenueStats(revRes.data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setRevenueLoading(false);
    }
  };

  const fetchStats = async () => {
    try {
      const [bookRes, genreRes, userRes] = await Promise.allSettled([
        getBookStats(),
        getTotalActiveGenres(),
        getAllUsers()
      ]);

      setStats({
        totalBooks: bookRes.status === 'fulfilled' ? (bookRes.value.data.totalActiveBooks || 0) : 0,
        availableBooks: bookRes.status === 'fulfilled' ? (bookRes.value.data.totalAvailableBooks || 0) : 0,
        totalGenres: genreRes.status === 'fulfilled' ? genreRes.value.data : 0,
        totalUsers: userRes.status === 'fulfilled' ? (userRes.value.data?.length || 0) : 0,
      });

    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="spinner-container"><div className="spinner"></div></div>;
  }

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
  };

  const formatCompactCurrency = (value) => {
    const amount = Number(value) || 0;
    if (amount >= 1_000_000_000) return `${(amount / 1_000_000_000).toFixed(1)}B`;
    if (amount >= 1_000_000) return `${(amount / 1_000_000).toFixed(1)}M`;
    if (amount >= 1_000) return `${(amount / 1_000).toFixed(0)}K`;
    return `${amount}`;
  };

  const RevenueTooltip = ({ active, payload, label }) => {
    if (!active || !payload || payload.length === 0) return null;
    return (
      <div
        style={{
          background: 'var(--bg-card)',
          border: '1px solid var(--border-light)',
          borderRadius: '10px',
          padding: '10px 12px',
          boxShadow: 'var(--shadow-card)',
          minWidth: '170px',
        }}
      >
        <p style={{ margin: '0 0 6px 0', color: 'var(--text-secondary)', fontSize: '0.78rem' }}>
          {label}
        </p>
        <p style={{ margin: 0, color: 'var(--text-primary)', fontWeight: 700 }}>
          {formatCurrency(payload[0].value)}
        </p>
      </div>
    );
  };

  const chartDataArray = revenueStats.chartData || [];
  const hasSingleRevenuePoint = chartDataArray.length === 1;

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>Admin <span className="text-gradient">Dashboard</span></h1>
        <p>Library overview and revenue analytics</p>
      </div>

      <div className="stats-grid" style={{ marginBottom: '20px' }}>
        <StatCard icon={<IoCashOutline />} label="Total Revenue" value={formatCurrency(revenueStats.totalRevenue)} subText="All-time processed" color="orange" />
        <StatCard icon={<IoTrendingUpOutline />} label="Subscription Rev." value={formatCurrency(revenueStats.subscriptionRevenue)} subText="Membership funds" color="purple" />
        <StatCard icon={<IoAlertCircleOutline />} label="Total Fines" value={formatCurrency(revenueStats.fineRevenue)} subText="Total penalties issued" color="red" />
        <StatCard icon={<IoStarOutline />} label="Active Memberships" value={revenueStats.activeSubscriptions || 0} subText={`Top Plan: ${revenueStats.topSellingPlan}`} color="blue" />
      </div>

      <div className="card" style={{ marginBottom: '20px', padding: '20px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h3>Revenue Trend</h3>
          <div style={{ display: 'flex', gap: '8px', background: 'var(--bg-primary)', padding: '4px', borderRadius: '8px' }}>
            {['day', 'week', 'month'].map((tf) => (
              <button
                key={tf}
                onClick={() => setTimeFrame(tf)}
                style={{
                  padding: '6px 16px',
                  borderRadius: '6px',
                  border: 'none',
                  fontSize: '0.85rem',
                  fontWeight: timeFrame === tf ? 600 : 400,
                  background: timeFrame === tf ? 'var(--bg-card)' : 'transparent',
                  color: timeFrame === tf ? 'var(--primary)' : 'var(--text-secondary)',
                  boxShadow: timeFrame === tf ? 'var(--shadow-sm)' : 'none',
                  cursor: 'pointer',
                  textTransform: 'capitalize',
                  transition: 'all 0.2s ease'
                }}
              >
                {tf}
              </button>
            ))}
          </div>
        </div>
        <div style={{ width: '100%', height: 300, position: 'relative' }}>
          {revenueLoading && (
            <div style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(255,255,255,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 10 }}>
              <div className="spinner"></div>
            </div>
          )}
          {chartDataArray.length > 0 ? (
            <ResponsiveContainer>
              <LineChart data={chartDataArray} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                <defs>
                  <linearGradient id="revenueAreaGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="var(--primary)" stopOpacity={0.25} />
                    <stop offset="95%" stopColor="var(--primary)" stopOpacity={0.02} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="4 4" stroke="var(--border-light)" />
                <XAxis
                  dataKey="label"
                  tick={{ fill: 'var(--text-secondary)', fontSize: 12 }}
                  tickLine={false}
                  axisLine={{ stroke: 'var(--border-color)' }}
                />
                <YAxis
                  tickFormatter={formatCompactCurrency}
                  tick={{ fill: 'var(--text-secondary)', fontSize: 12 }}
                  tickLine={false}
                  axisLine={false}
                  width={70}
                />
                <Tooltip content={<RevenueTooltip />} cursor={{ stroke: 'var(--primary)', strokeOpacity: 0.3 }} />
                {hasSingleRevenuePoint && (
                  <ReferenceLine
                    y={chartDataArray[0]?.revenue || 0}
                    stroke="var(--primary)"
                    strokeDasharray="6 6"
                    strokeOpacity={0.45}
                  />
                )}
                <Area type="monotone" dataKey="revenue" stroke="none" fill="url(#revenueAreaGradient)" />
                <Line
                  type="monotone"
                  dataKey="revenue"
                  stroke="var(--primary)"
                  strokeWidth={3}
                  dot={{ r: hasSingleRevenuePoint ? 6 : 3, strokeWidth: 2, fill: 'var(--bg-card)' }}
                  activeDot={{ r: hasSingleRevenuePoint ? 8 : 6, stroke: 'var(--primary)', strokeWidth: 2, fill: 'var(--bg-card)' }}
                />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <div className="empty-state" style={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <p className="text-muted">No revenue data available for the last 12 months.</p>
            </div>
          )}
        </div>
      </div>

      <div className="stats-grid" style={{ marginBottom: '20px' }}>
        <StatCard icon={<IoBookOutline />} label="Total Books" value={stats.totalBooks} subText="Active in catalog" color="purple" />
        <StatCard icon={<IoTrendingUpOutline />} label="Available" value={stats.availableBooks} subText="Ready to borrow" color="green" />
        <StatCard icon={<IoBookmarksOutline />} label="Genres" value={stats.totalGenres} subText="Active categories" color="blue" />
        <StatCard icon={<IoPeopleOutline />} label="Users" value={stats.totalUsers} subText="Registered members" color="orange" />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Quick Actions</h3>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            <a href="/admin/books" className="btn btn-secondary w-full" style={{ justifyContent: 'flex-start' }}>
              <IoBookOutline /> Manage Books
            </a>
            <a href="/admin/genres" className="btn btn-secondary w-full" style={{ justifyContent: 'flex-start' }}>
              <IoBookmarksOutline /> Manage Genres
            </a>
            <a href="/admin/loans" className="btn btn-secondary w-full" style={{ justifyContent: 'flex-start' }}>
              <IoReceiptOutline /> Manage Loans
            </a>
            <a href="/admin/fines" className="btn btn-secondary w-full" style={{ justifyContent: 'flex-start' }}>
              <IoAlertCircleOutline /> Manage Fines
            </a>
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <h3 className="card-title">System Status</h3>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted">Book Availability Rate</span>
              <strong>{stats.totalBooks > 0 ? Math.round((stats.availableBooks / stats.totalBooks) * 100) : 0}%</strong>
            </div>
            <div style={{ height: '8px', background: 'var(--bg-primary)', borderRadius: '4px', overflow: 'hidden' }}>
              <div style={{
                height: '100%',
                width: `${stats.totalBooks > 0 ? (stats.availableBooks / stats.totalBooks) * 100 : 0}%`,
                background: 'linear-gradient(90deg, var(--accent-green), var(--primary))',
                borderRadius: '4px',
                transition: 'width 1s ease'
              }}></div>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted">Books in Circulation</span>
              <strong>{stats.totalBooks - stats.availableBooks}</strong>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}