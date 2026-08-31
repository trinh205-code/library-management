import { useState, useEffect } from 'react';
import { searchReservations, readyReservation, rejectReservation, confirmBorrowedReservation, cancelReservation } from '../../../api/reservationApi';
import StatusBadge from '../../../components/StatusBadge';
import Pagination from '../../../components/Pagination';
import toast from 'react-hot-toast';
import { IoCheckmarkCircleOutline, IoCloseCircleOutline, IoCalendarOutline, IoPersonCircleOutline, IoBookOutline } from 'react-icons/io5';

const styles = `
  .mr-list-wrapper {
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 4px 15px rgba(0,0,0,0.03);
    margin-bottom: 24px;
  }
  .mr-grid {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
  .mr-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px;
    background: var(--bg-white);
    border: 1px solid var(--border-light);
    border-radius: 12px;
    transition: all 0.2s ease;
    gap: 20px;
  }
  .mr-card:hover {
    border-color: var(--accent-blue);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.08);
    transform: translateY(-2px);
  }
  .mr-col-user {
    flex: 1.5;
    min-width: 200px;
  }
  .mr-col-book {
    flex: 2;
    min-width: 250px;
  }
  .mr-col-dates {
    flex: 1.5;
    min-width: 200px;
    display: flex;
    flex-direction: column;
    gap: 6px;
  }
  .mr-col-actions {
    flex: 1;
    min-width: 150px;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 12px;
  }
  .mr-title {
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 4px;
  }
  .mr-subtitle {
    font-size: 0.85rem;
    color: var(--text-muted);
  }
  .mr-date-row {
    font-size: 0.85rem;
    display: flex;
    justify-content: space-between;
  }
  .mr-date-label {
    color: var(--text-muted);
  }
  .mr-date-val {
    font-weight: 500;
    color: var(--text-secondary);
  }
  @media (max-width: 1024px) {
    .mr-card {
      flex-direction: column;
      align-items: flex-start;
    }
    .mr-col-actions {
      align-items: flex-start;
      flex-direction: row;
      width: 100%;
      justify-content: space-between;
      margin-top: 10px;
      padding-top: 15px;
      border-top: 1px solid var(--border-light);
    }
  }
`;

export default function ManageReservations() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => { fetchReservations(); }, [page, statusFilter]);

  const fetchReservations = async () => {
    setLoading(true);
    try {
      const params = { page, size: 10, ...(statusFilter && { status: statusFilter }) };
      const res = await searchReservations(params);
      setReservations(res.data?.content || []);
      setTotalPages(res.data?.totalPages || 0);
    } catch { toast.error('Failed to load reservations'); }
    finally { setLoading(false); }
  };

  const handleReady = async (id) => {
    try { await readyReservation(id); toast.success('Reservation is ready!'); fetchReservations(); }
    catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  const handleReject = async (id) => {
    if (!confirm('Reject this reservation?')) return;
    try { await rejectReservation(id); toast.success('Reservation rejected!'); fetchReservations(); }
    catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  const handleConfirmBorrowed = async (id) => {
    try { await confirmBorrowedReservation(id); toast.success('Marked as borrowed!'); fetchReservations(); }
    catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  const handleCancel = async (id) => {
    if (!confirm('Cancel this reservation?')) return;
    try { await cancelReservation(id); toast.success('Reservation cancelled'); fetchReservations(); }
    catch (err) { toast.error(err.response?.data?.message || 'Failed'); }
  };

  const formatDate = (d) => {
    if (!d) return '-';
    return new Date(d).toLocaleString('en-GB', { 
      day: '2-digit', month: '2-digit', year: 'numeric', 
      hour: '2-digit', minute: '2-digit' 
    });
  };

  const tabs = [
    { key: '', label: 'All' },
    { key: 'PENDING', label: 'Pending' },
    { key: 'READY', label: 'Ready' },
    { key: 'USER_CONFIRMED', label: 'Confirmed' },
    { key: 'BORROWED', label: 'Borrowed' },
    { key: 'REJECTED', label: 'Rejected' },
    { key: 'CANCELLED', label: 'Cancelled' },
  ];

  return (
    <div className="page-container">
      <div className="page-header"><h1>Manage <span className="text-gradient">Reservations</span></h1><p>Track and process book reservations</p></div>

      <div className="tabs">
        {tabs.map(t => <button key={t.key} className={`tab ${statusFilter === t.key ? 'active' : ''}`} onClick={() => { setStatusFilter(t.key); setPage(0); }}>{t.label}</button>)}
      </div>

      {loading ? <div className="spinner-container"><div className="spinner"></div></div> : (
        <div className="mr-list-wrapper">
          <style>{styles}</style>
          <div className="mr-grid">
            {reservations.map((r) => (
              <div key={r.id} className="mr-card">
                <div className="mr-col-user" style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <IoPersonCircleOutline size={40} style={{ color: 'var(--text-muted)' }} />
                  <div>
                    <div className="mr-title">{r.userName}</div>
                    <div className="mr-subtitle">{r.userEmail}</div>
                  </div>
                </div>
                
                <div className="mr-col-book" style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}>
                  <div style={{ marginTop: '2px', color: 'var(--accent-blue)' }}><IoBookOutline size={20} /></div>
                  <div>
                    <div className="mr-title">{r.bookTitle}</div>
                    <div className="mr-subtitle">By {r.bookAuthor}</div>
                  </div>
                </div>

                <div className="mr-col-dates">
                  <div className="mr-date-row">
                    <span className="mr-date-label">Reserved At:</span> 
                    <span className="mr-date-val"><IoCalendarOutline style={{verticalAlign: 'middle', marginRight: '4px'}}/>{formatDate(r.reservedAt)}</span>
                  </div>
                  <div className="mr-date-row">
                    <span className="mr-date-label">Queue Position:</span> 
                    <span className="mr-date-val" style={{ color: r.queuePosition ? 'var(--text-primary)' : 'var(--text-muted)', fontWeight: r.queuePosition ? 'bold' : 'normal' }}>
                      {r.queuePosition ? `#${r.queuePosition}` : '-'}
                    </span>
                  </div>
                </div>

                <div className="mr-col-actions">
                  <StatusBadge status={r.status} />
                  <div className="flex gap-sm" style={{ marginTop: '4px' }}>
                    {r.status === 'PENDING' && (
                      <>
                        <button className="btn btn-success btn-sm" onClick={() => handleReady(r.id)}><IoCheckmarkCircleOutline /> Ready</button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleReject(r.id)}><IoCloseCircleOutline /> Reject</button>
                      </>
                    )}
                    {r.status === 'USER_CONFIRMED' && (
                      <button className="btn btn-primary btn-sm" onClick={() => handleConfirmBorrowed(r.id)}><IoCheckmarkCircleOutline /> Confirm</button>
                    )}
                    {(r.status === 'READY' || r.status === 'USER_CONFIRMED') && (
                      <button className="btn btn-danger btn-sm" onClick={() => handleCancel(r.id)}><IoCloseCircleOutline /> Cancel</button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
      <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
    </div>
  );
}
