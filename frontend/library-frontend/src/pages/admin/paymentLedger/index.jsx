import { useState, useEffect } from 'react';
import { getAllPayments } from '../../../api/paymentApi';
import { IoReceiptOutline, IoTimeOutline, IoCheckmarkCircleOutline, IoCloseCircleOutline, IoPersonOutline, IoSearchOutline } from 'react-icons/io5';

export default function AdminPaymentLedger() {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [startDateFilter, setStartDateFilter] = useState('');
  const [endDateFilter, setEndDateFilter] = useState('');

  useEffect(() => {
    const timeoutId = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm.trim());
    }, 400);

    return () => clearTimeout(timeoutId);
  }, [searchTerm]);

  useEffect(() => {
    fetchPayments({
      userName: debouncedSearchTerm || undefined,
      status: statusFilter,
      type: typeFilter,
      startDate: startDateFilter || undefined,
      endDate: endDateFilter || undefined,
    });
  }, [debouncedSearchTerm, statusFilter, typeFilter, startDateFilter, endDateFilter]);

  const fetchPayments = async (filters = {}) => {
    setLoading(true);
    try {
      const res = await getAllPayments(filters);
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

  const clearFilters = () => {
    setSearchTerm('');
    setDebouncedSearchTerm('');
    setStatusFilter('ALL');
    setTypeFilter('ALL');
    setStartDateFilter('');
    setEndDateFilter('');
  };

  const hasActiveFilters =
    debouncedSearchTerm || statusFilter !== 'ALL' || typeFilter !== 'ALL' || startDateFilter || endDateFilter;

  if (loading) {
    return <div className="spinner-container"><div className="spinner"></div></div>;
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>Global <span className="text-gradient">Transaction Ledger</span></h1>
        <p>Comprehensive overview of all platform payments and membership purchases</p>
      </div>

      <div className="card">
        <div style={{ marginBottom: '16px', display: 'grid', gridTemplateColumns: 'minmax(240px, 1.4fr) repeat(4, minmax(140px, 1fr)) auto', gap: '10px', alignItems: 'center' }}>
          <div style={{ position: 'relative' }}>
            <IoSearchOutline style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
            <input
              type="text"
              className="form-input"
              placeholder="Search by user name..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{ paddingLeft: '36px' }}
            />
          </div>

          <select className="form-select" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
            <option value="ALL">All Status</option>
            <option value="PENDING">Pending</option>
            <option value="SUCCESS">Success</option>
            <option value="FAILED">Failed</option>
          </select>

          <select className="form-select" value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)}>
            <option value="ALL">All Type</option>
            <option value="MEMBERSHIP">Membership</option>
            <option value="FINE">Fine</option>
          </select>

          <input
            type="date"
            className="form-input"
            value={startDateFilter}
            onChange={(e) => setStartDateFilter(e.target.value)}
            title="Start date"
          />
          <input
            type="date"
            className="form-input"
            value={endDateFilter}
            onChange={(e) => setEndDateFilter(e.target.value)}
            title="End date"
          />

          <button className="btn btn-secondary" onClick={clearFilters} type="button">
            Reset
          </button>
        </div>

        {payments.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon"><IoReceiptOutline /></div>
            <h3>No Transactions Found</h3>
            <p className="text-muted">
              {hasActiveFilters
                ? 'No transactions match your current filters.'
                : "The system hasn't processed any payments yet."}
            </p>
          </div>
        ) : (
          <div className="table-responsive">
            <table className="table">
              <thead>
                <tr>
                  <th>DATE</th>
                  <th>USER</th>
                  <th>TRANSACTION ID</th>
                  <th>AMOUNT</th>
                  <th>TYPE</th>
                  <th>GATEWAY</th>
                  <th>STATUS</th>
                </tr>
              </thead>
              <tbody>
                {payments.map(tx => (
                  <tr key={tx.id} className={getTransactionRowClass(tx.paymentStatus)}>
                    <td>
                      <div style={{ display: 'flex', flexDirection: 'column' }}>
                        <span>{new Date(tx.createdAt).toLocaleDateString()}</span>
                        <span className="text-muted text-sm">{new Date(tx.createdAt).toLocaleTimeString()}</span>
                      </div>
                    </td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <div className="user-avatar" style={{width: 30, height: 30}}>{tx.userName ? tx.userName.charAt(0).toUpperCase() : 'U'}</div>
                        <div style={{ display: 'flex', flexDirection: 'column' }}>
                          <span style={{ fontWeight: '500' }}>{tx.userName || 'Unknown'}</span>
                          <span className="text-muted text-xs">{tx.userEmail}</span>
                        </div>
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
