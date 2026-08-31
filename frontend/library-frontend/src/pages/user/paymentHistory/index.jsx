import { useState, useEffect } from 'react';
import { getMyPayments } from '../../../api/paymentApi';
import { IoReceiptOutline, IoTimeOutline, IoCheckmarkCircleOutline, IoCloseCircleOutline } from 'react-icons/io5';

export default function UserPaymentHistory() {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchPayments();
  }, []);

  const fetchPayments = async () => {
    try {
      const res = await getMyPayments();
      // Assumes paginated response like { content: [...] }
      setPayments(res.data.content || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount || 0);
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'SUCCESS':
        return <span className="badge badge-green"><IoCheckmarkCircleOutline className="mr-1" /> Success</span>;
      case 'FAILED':
        return <span className="badge badge-red"><IoCloseCircleOutline className="mr-1" /> Failed</span>;
      case 'PENDING':
        return <span className="badge badge-orange"><IoTimeOutline className="mr-1" /> Pending</span>;
      default:
        return <span className="badge badge-gray">{status}</span>;
    }
  };

  const getTransactionRowClass = (status) => {
    if (status === 'SUCCESS') return 'transaction-row-success';
    if (status === 'FAILED') return 'transaction-row-failed';
    return '';
  };

  if (loading) {
    return <div className="spinner-container"><div className="spinner"></div></div>;
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>Transaction <span className="text-gradient">Log</span></h1>
        <p>A history of all your processed library payments and membership subscriptions</p>
      </div>

      <div className="card">
        {payments.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon"><IoReceiptOutline /></div>
            <h3>No Transactions Found</h3>
            <p className="text-muted">You haven't made any payments yet.</p>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="table">
              <thead>
                <tr>
                  <th>DATE</th>
                  <th>TRANSACTION ID</th>
                  <th>AMOUNT</th>
                  <th>TYPE</th>
                  <th>GATEWAY</th>
                  <th>STATUS</th>
                </tr>
              </thead>
              <tbody>
                {payments.map((tx, index) => (
                  <tr key={tx.id || index} className={getTransactionRowClass(tx.paymentStatus)}>
                    <td>
                      <div style={{ display: 'flex', flexDirection: 'column' }}>
                        <span>{new Date(tx.createdAt).toLocaleDateString()}</span>
                        <span className="text-muted text-sm">{new Date(tx.createdAt).toLocaleTimeString()}</span>
                      </div>
                    </td>
                    <td><span className="text-muted" style={{ fontFamily: 'monospace' }}>{tx.transactionId}</span></td>
                    <td><strong>{formatCurrency(tx.amount)}</strong></td>
                    <td><span className="badge badge-purple">{tx.paymentType}</span></td>
                    <td>{tx.paymentGateway}</td>
                    <td>{getStatusBadge(tx.paymentStatus)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
