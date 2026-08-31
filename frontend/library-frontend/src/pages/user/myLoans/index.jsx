import { useState, useEffect } from 'react';
import { getMyLoans } from '../../../api/bookLoanApi';
import { renewBook } from '../../../api/bookLoanApi';
import StatusBadge from '../../../components/StatusBadge';
import Pagination from '../../../components/Pagination';
import toast from 'react-hot-toast';
import { IoRefreshOutline, IoBookOutline, IoPersonOutline, IoCalendarOutline } from 'react-icons/io5';

export default function MyLoans() {
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [activeTab, setActiveTab] = useState('');

  useEffect(() => { fetchLoans(); }, [page, activeTab]);

  const fetchLoans = async () => {
    setLoading(true);
    try {
      const params = { page, size: 10, ...(activeTab && { status: activeTab }) };
      const res = await getMyLoans(params);
      setLoans(res.data?.content || []);
      setTotalPages(res.data?.totalPages || 0);
    } catch { toast.error('Failed to load loans'); }
    finally { setLoading(false); }
  };

  const handleRenew = async (loanId, bookId) => {
    try {
      await renewBook({ bookLoanId: loanId, bookId });
      toast.success('Book renewed successfully!');
      fetchLoans();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to renew');
    }
  };

  const formatDate = (d) => {
    if (!d) return '-';
    // Format YYYY-MM-DD from the string or date to match the screenshot
    const date = new Date(d);
    return date.toISOString().split('T')[0];
  };

  const tabs = [
    { key: '', label: 'ALL' },
    { key: 'CHECKED_OUT', label: 'ACTIVE' },
    { key: 'OVERDUE', label: 'OVERDUE' },
    { key: 'RETURNED', label: 'RETURNED' },
    { key: 'LOST', label: 'LOST' },
    { key: 'DAMEGED', label: 'DAMAGED' },
  ];

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>My <span className="text-gradient">Loans</span></h1>
        <p>Manage your borrowed books</p>
      </div>

      <div className="tabs">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            className={`tab ${activeTab === tab.key ? 'active' : ''}`}
            onClick={() => { setActiveTab(tab.key); setPage(0); }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="spinner-container"><div className="spinner"></div></div>
      ) : loans.length > 0 ? (
        <div className="loan-list" style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {loans.map((loan) => (
            <div key={loan.id} className="card loan-card" style={{ padding: '24px', display: 'flex', flexWrap: 'wrap', gap: '24px', alignItems: 'center', justifyContent: 'space-between' }}>
              
              <div 
                style={{ flex: '1 1 auto', display: 'flex', gap: '24px', minWidth: '300px', cursor: 'pointer' }}
                onClick={() => window.location.href=`/books/${loan.bookId}`}
                title="View book details"
              >
                <div style={{ width: '80px', height: '120px', borderRadius: '8px', overflow: 'hidden', flexShrink: 0, backgroundColor: '#f0f0f0', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}>
                  {loan.bookCoverImage ? (
                    <img src={loan.bookCoverImage} alt={loan.bookTitle} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  ) : (
                    <div style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white', fontSize: '2rem' }}>
                      <IoBookOutline />
                    </div>
                  )}
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', gap: '10px' }}>
                  <h3 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 600, color: 'var(--text-color)' }}>{loan.bookTitle}</h3>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
                    <IoPersonOutline /> {loan.bookAuthor || 'Unknown Author'}
                  </div>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                    # ISBN: {loan.bookIsbn}
                  </div>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '40px', paddingLeft: '40px', borderLeft: '1px solid var(--border-light)', minHeight: '80px', alignItems: 'center', flexWrap: 'wrap', flex: '1 1 auto' }}>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 500 }}>Checkout Date</span>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontWeight: 600, color: 'var(--text-color)' }}>
                    <IoCalendarOutline style={{ color: 'var(--primary)', fontSize: '1.1rem' }} /> {formatDate(loan.checkoutDate)}
                  </div>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', fontWeight: 500 }}>Due Date</span>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontWeight: 600, color: 'var(--text-color)' }}>
                    <IoCalendarOutline style={{ color: 'var(--primary)', fontSize: '1.1rem' }} /> {formatDate(loan.dueDate)}
                  </div>
                </div>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', alignItems: 'flex-end', minWidth: '140px' }}>
                <StatusBadge status={loan.status} />
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  Renewals: {loan.renewalCount}/{loan.maxRenewals}
                </div>
                {loan.status === 'CHECKED_OUT' && loan.renewalCount < loan.maxRenewals && (
                  <button className="btn btn-outline btn-sm" onClick={() => handleRenew(loan.id, loan.bookId)} style={{ width: '100%' }}>
                    <IoRefreshOutline /> Renew
                  </button>
                )}
                {loan.status === 'RETURNED' && !loan.hasReviewed && (
                  <button className="btn btn-outline btn-sm" onClick={() => window.location.href=`/books/${loan.bookId}`} style={{ width: '100%' }}>
                    <IoBookOutline /> Write Review
                  </button>
                )}
              </div>

            </div>
          ))}
        </div>
      ) : (
        <div className="empty-state">
          <div className="empty-state-icon"><IoBookOutline /></div>
          <h3 className="empty-state-title">No Loans Found</h3>
          <p className="empty-state-text">You don't have any {activeTab ? activeTab.toLowerCase().replace('_', ' ') : ''} loans</p>
        </div>
      )}
      <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
