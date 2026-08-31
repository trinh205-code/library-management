import { useState, useEffect } from 'react';
import { useAuth } from '../../../context/AuthContext';
import { getProfile, sendProfileUpdateOtp, updateProfile, changePassword } from '../../../api/userApi';
import { getMyLoans } from '../../../api/bookLoanApi';
import { getMyFines } from '../../../api/fineApi';
import { getMyReservations } from '../../../api/reservationApi';
import { getWishlists } from '../../../api/wishlistApi';
import { getUserActiveSubscriptions } from '../../../api/subscriptionApi';
import StatCard from '../../../components/StatCard';
import StatusBadge from '../../../components/StatusBadge';
import { useNavigate } from 'react-router-dom';
import { IoPersonOutline, IoMailOutline, IoCallOutline, IoShieldCheckmarkOutline, IoTimeOutline, IoReceiptOutline, IoCalendarOutline, IoAlertCircleOutline, IoHeartOutline, IoArrowForwardOutline, IoStarOutline } from 'react-icons/io5';
import './profile.css';

export default function Profile() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  
  // Dashboard & Loans States
  const [stats, setStats] = useState({ loans: 0, reservations: 0, fines: 0, wishlist: 0 });
  const [recentLoans, setRecentLoans] = useState([]);
  const [activePlan, setActivePlan] = useState(null);
  
  // Edit & OTP states
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isChangePasswordModalOpen, setIsChangePasswordModalOpen] = useState(false);
  const [isOtpModalOpen, setIsOtpModalOpen] = useState(false);
  const [editData, setEditData] = useState({ fullName: '', phone: '' });
  const [newPassword, setNewPassword] = useState('');
  const [otp, setOtp] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    fetchProfile();
    fetchDashboardData();
  }, []);

  const fetchProfile = async () => {
    try {
      const res = await getProfile();
      setProfile(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const fetchDashboardData = async () => {
    try {
      const [loansRes, finesRes, reservationsRes, wishlistRes, planRes] = await Promise.allSettled([
        getMyLoans({ status: 'CHECKED_OUT', page: 0, size: 5 }),
        getMyFines({ status: 'PENDING' }),
        getMyReservations({ page: 0, size: 5 }),
        getWishlists({ page: 0, size: 5 }),
        getUserActiveSubscriptions()
      ]);

      const loanData = loansRes.status === 'fulfilled' ? loansRes.value.data : null;
      const fineData = finesRes.status === 'fulfilled' ? finesRes.value.data : [];
      const reservationData = reservationsRes.status === 'fulfilled' ? reservationsRes.value.data : null;
      const wishlistData = wishlistRes.status === 'fulfilled' ? wishlistRes.value.data : null;
      const activePlanData = planRes.status === 'fulfilled' ? planRes.value.data : null;

      setStats({
        loans: loanData?.totalElements || 0,
        reservations: reservationData?.totalElements || 0,
        fines: Array.isArray(fineData) ? fineData.length : 0,
        wishlist: wishlistData?.totalElements || 0,
      });

      setRecentLoans(loanData?.content || []);
      setActivePlan(activePlanData);
    } catch (err) {
      console.error('Dashboard fetch error:', err);
    }
  };

  const handleEditClick = () => {
    const userToEdit = profile || user;
    setEditData({
      fullName: userToEdit?.fullName || '',
      phone: userToEdit?.phone || ''
    });
    setErrorMsg('');
    setIsEditModalOpen(true);
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setActionLoading(true);
    setErrorMsg('');
    try {
      await updateProfile(editData);
      setIsEditModalOpen(false);
      fetchProfile();
      alert('Profile updated successfully!');
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Failed to update profile.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleChangePasswordClick = () => {
    setNewPassword('');
    setErrorMsg('');
    setIsChangePasswordModalOpen(true);
  };

  const handleChangePasswordSubmit = async (e) => {
    e.preventDefault();
    setActionLoading(true);
    setErrorMsg('');
    try {
      await sendProfileUpdateOtp();
      setIsChangePasswordModalOpen(false);
      setIsOtpModalOpen(true);
      setOtp('');
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Failed to send OTP.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleOtpSubmit = async (e) => {
    e.preventDefault();
    setActionLoading(true);
    setErrorMsg('');
    try {
      await changePassword({ newPassword, otp });
      setIsOtpModalOpen(false);
      alert('Password changed successfully!');
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Invalid OTP. Update failed.');
    } finally {
      setActionLoading(false);
    }
  };

  const formatDate = (d) => d ? new Date(d).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : 'Never';

  if (loading) {
    return <div className="spinner-container"><div className="spinner"></div></div>;
  }

  const displayUser = profile || user;

  return (
    <div className="page-container">
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1>My <span className="text-gradient">Profile</span></h1>
          <p>Your account information</p>
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <button className="btn btn-secondary" onClick={handleChangePasswordClick}>
            Change Password
          </button>
          <button className="btn btn-primary" onClick={handleEditClick}>
            Edit Profile
          </button>
        </div>
      </div>

      <div className="dashboard-stats-section" style={{ marginTop: '10px', marginBottom: '40px' }}>
        <h2 style={{ marginBottom: '20px' }}>My Library Activity</h2>
        <div className="stats-grid" style={{ marginBottom: '32px' }}>
          <StatCard
            icon={<IoReceiptOutline />}
            label="Current Loans"
            value={stats.loans}
            subText="Books you're reading"
            color="purple"
          />
          <StatCard
            icon={<IoCalendarOutline />}
            label="Reservations"
            value={stats.reservations}
            subText="Books on hold"
            color="green"
          />
          <StatCard
            icon={<IoAlertCircleOutline />}
            label="Pending Fines"
            value={stats.fines}
            subText="Need attention"
            color="orange"
          />
          <StatCard
            icon={<IoHeartOutline />}
            label="Wishlist"
            value={stats.wishlist}
            subText="Saved books"
            color="pink"
          />
        </div>

        {recentLoans.length > 0 && (
          <div className="card" style={{ marginBottom: '40px' }}>
            <div className="card-header">
              <h3 className="card-title">Current Loans</h3>
              <button className="btn btn-ghost btn-sm" onClick={() => navigate('/my-loans')}>
                Manage Loans <IoArrowForwardOutline />
              </button>
            </div>
            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Book</th>
                    <th>Checkout Date</th>
                    <th>Due Date</th>
                    <th>Status</th>
                    <th>Remaining</th>
                  </tr>
                </thead>
                <tbody>
                  {recentLoans.map((loan) => (
                    <tr key={loan.id}>
                      <td>
                        <div>
                          <strong>{loan.bookTitle}</strong>
                          <div className="text-xs text-muted">{loan.bookAuthor}</div>
                        </div>
                      </td>
                      <td>{formatDate(loan.checkoutDate)}</td>
                      <td>{formatDate(loan.dueDate)}</td>
                      <td><StatusBadge status={loan.status} /></td>
                      <td>
                        <span style={{ color: loan.remainingDays < 3 ? 'var(--accent-red)' : 'var(--accent-green)', fontWeight: 600 }}>
                          {loan.remainingDays != null ? `${loan.remainingDays} days` : '-'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>

      <div className="profile-card">
        <div className="profile-avatar-section">
          <div className="profile-avatar">
            {displayUser?.fullName?.charAt(0)?.toUpperCase() || 'U'}
          </div>
          <h2>{displayUser?.fullName || 'User'}</h2>
          <span className="badge badge-purple">{displayUser?.role === 'ROLE_ADMIN' ? 'Administrator' : 'Member'}</span>
        </div>

        <div className="profile-details">
          <div className="profile-item">
            <div className="profile-item-icon"><IoPersonOutline /></div>
            <div>
              <label>Full Name</label>
              <p>{displayUser?.fullName || '-'}</p>
            </div>
          </div>

          <div className="profile-item">
            <div className="profile-item-icon"><IoMailOutline /></div>
            <div>
              <label>Email Address</label>
              <p>{displayUser?.email || '-'}</p>
            </div>
          </div>

          <div className="profile-item">
            <div className="profile-item-icon"><IoCallOutline /></div>
            <div>
              <label>Phone Number</label>
              <p>{displayUser?.phone || 'Not provided'}</p>
            </div>
          </div>

          <div className="profile-item">
            <div className="profile-item-icon"><IoShieldCheckmarkOutline /></div>
            <div>
              <label>Role</label>
              <p>{displayUser?.role === 'ROLE_ADMIN' ? 'Administrator' : 'Member'}</p>
            </div>
          </div>

          <div className="profile-item">
            <div className="profile-item-icon"><IoTimeOutline /></div>
            <div>
              <label>Last Login</label>
              <p>{formatDate(displayUser?.lastLogin)}</p>
            </div>
          </div>

          <div className="profile-item">
            <div className="profile-item-icon"><IoStarOutline /></div>
            <div>
              <label>Current Subscription</label>
              {activePlan ? (
                <p>
                  <span style={{ fontWeight: '500', color: 'var(--primary-color)' }}>{activePlan.planName}</span>
                  <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginLeft: '8px' }}>
                    (Expires: {formatDate(activePlan.endDate)})
                  </span>
                </p>
              ) : (
                <p>Free / Basic Member</p>
              )}
            </div>
          </div>
        </div>
      </div>


      {isEditModalOpen && (
        <div className="modal-overlay">
          <div className="modal-content profile-modal">
            <h2>Edit Profile</h2>
            {errorMsg && <div className="error-message">{errorMsg}</div>}
            <form onSubmit={handleEditSubmit}>
              <div className="form-group">
                <label>Full Name</label>
                <input 
                  type="text" 
                  value={editData.fullName} 
                  onChange={(e) => setEditData({...editData, fullName: e.target.value})}
                  required
                />
              </div>
              <div className="form-group">
                <label>Phone Number</label>
                <input 
                  type="text" 
                  value={editData.phone} 
                  onChange={(e) => setEditData({...editData, phone: e.target.value})}
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setIsEditModalOpen(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={actionLoading}>
                  {actionLoading ? 'Saving...' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {isChangePasswordModalOpen && (
        <div className="modal-overlay">
          <div className="modal-content profile-modal">
            <h2>Change Password</h2>
            {errorMsg && <div className="error-message">{errorMsg}</div>}
            <form onSubmit={handleChangePasswordSubmit}>
              <div className="form-group">
                <label>New Password</label>
                <input 
                  type="password" 
                  value={newPassword} 
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Enter new password"
                  required
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setIsChangePasswordModalOpen(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={actionLoading}>
                  {actionLoading ? 'Sending OTP...' : 'Next'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {isOtpModalOpen && (
        <div className="modal-overlay">
          <div className="modal-content profile-modal">
            <h2>OTP Verification</h2>
            <p className="modal-subtitle">We have sent a 6-digit OTP to your email: {displayUser?.email}</p>
            {errorMsg && <div className="error-message">{errorMsg}</div>}
            <form onSubmit={handleOtpSubmit}>
              <div className="form-group">
                <label>Enter OTP</label>
                <input 
                  type="text" 
                  value={otp} 
                  onChange={(e) => setOtp(e.target.value)}
                  placeholder="e.g. 123456"
                  required
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setIsOtpModalOpen(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={actionLoading}>
                  {actionLoading ? 'Verifying...' : 'Verify & Change'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
