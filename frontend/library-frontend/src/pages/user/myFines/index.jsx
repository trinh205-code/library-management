import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { getMyFines, payFine } from '../../../api/fineApi';
import StatusBadge from '../../../components/StatusBadge';
import toast from 'react-hot-toast';
import { IoCashOutline, IoAlertCircleOutline } from 'react-icons/io5';

export default function MyFines() {
  const [fines, setFines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchParams, setSearchParams] = useSearchParams();

  useEffect(() => { 
    fetchFines(); 
    
    // Check for VNPay redirect status
    const paymentStatus = searchParams.get('payment');
    if (paymentStatus === 'success') {
      toast.success('Your payment was completed successfully!');
      setSearchParams({}); // Clear query params
    } else if (paymentStatus === 'failed') {
      toast.error('Payment failed or cancelled.');
      setSearchParams({}); // Clear query params
    }
  }, [searchParams, setSearchParams]);

  const fetchFines = async () => {
    setLoading(true);
    try {
      const res = await getMyFines();
      setFines(res.data || []);
    } catch { toast.error('Failed to load fines'); }
    finally { setLoading(false); }
  };

  const handlePay = async (id) => {
    try {
      const res = await payFine(id);
      if (res.data?.checkoutUrl) {
        // VNPay opens in the current window so that the return url works seamlessly
        window.location.href = res.data.checkoutUrl;
      } else {
        toast.success('Payment initiated');
      }
      fetchFines();
    } catch (err) {
      toast.error(err.response?.data || 'Payment failed');
    }
  };

  const formatCurrency = (amount) => {
    if (amount == null) return '-';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  const formatDate = (d) => d ? new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : '-';

  const totalOutstanding = fines.reduce((sum, f) => sum + (f.amountOutstanding || 0), 0);

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>My <span className="text-gradient">Fines</span></h1>
        <p>View and pay your outstanding fines</p>
      </div>

      {totalOutstanding > 0 && (
        <div className="card" style={{ background: 'linear-gradient(135deg, #fff5f5, #fed7d7)', border: '1px solid #feb2b2', marginBottom: '24px' }}>
          <div className="flex items-center gap-md">
            <IoAlertCircleOutline style={{ fontSize: '2rem', color: 'var(--accent-red)' }} />
            <div>
              <h3 style={{ color: 'var(--accent-red)' }}>Outstanding Balance</h3>
              <p style={{ fontSize: '1.5rem', fontWeight: '700', color: 'var(--accent-red)' }}>
                {formatCurrency(totalOutstanding)}
              </p>
            </div>
          </div>
        </div>
      )}

      {loading ? (
        <div className="spinner-container"><div className="spinner"></div></div>
      ) : fines.length > 0 ? (
        <div className="card" style={{ padding: 0 }}>
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Book</th>
                  <th>Reason</th>
                  <th>Amount</th>
                  <th>Paid</th>
                  <th>Outstanding</th>
                  <th>Status</th>
                  <th>Created</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {fines.map((fine) => (
                  <tr key={fine.id}>
                    <td><strong>{fine.bookTitle || '-'}</strong></td>
                    <td className="text-sm">{fine.reason || '-'}</td>
                    <td>{formatCurrency(fine.amount)}</td>
                    <td>{formatCurrency(fine.amountPaid)}</td>
                    <td style={{ fontWeight: 600, color: fine.amountOutstanding > 0 ? 'var(--accent-red)' : 'var(--accent-green)' }}>
                      {formatCurrency(fine.amountOutstanding)}
                    </td>
                    <td><StatusBadge status={fine.status} /></td>
                    <td>{formatDate(fine.createdAt)}</td>
                    <td>
                      {fine.status === 'PENDING' && (
                        <button className="btn btn-primary btn-sm" onClick={() => handlePay(fine.id)}>
                          <IoCashOutline /> Pay
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="empty-state">
          <div className="empty-state-icon"><IoCashOutline /></div>
          <h3 className="empty-state-title">No Fines</h3>
          <p className="empty-state-text">You don't have any fines. Keep up the good work!</p>
        </div>
      )}
    </div>
  );
}
