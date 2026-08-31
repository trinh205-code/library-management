import { useState, useEffect } from 'react';
import { getMyReservations, cancelReservation, userConfirmReservation } from '../../../api/reservationApi';
import toast from 'react-hot-toast';
import { 
  IoJournal, IoTime, IoCheckmarkCircle, 
  IoJournalOutline, IoTimeOutline, IoCheckmarkCircleOutline,
  IoHourglassOutline, IoCalendarOutline
} from 'react-icons/io5';
import './myReservations.css';

export default function MyReservations() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('ALL'); // ALL, ACTIVE, COMPLETED

  // Load reservations, fetch unpaginated or large page for kanban
  useEffect(() => { fetchReservations(); }, []);

  const fetchReservations = async () => {
    setLoading(true);
    try {
      // Assuming a large size to get all relevant reservations for kanban display
      const res = await getMyReservations({ page: 0, size: 100 });
      setReservations(res.data?.content || []);
    } catch { 
      toast.error('Failed to load reservations'); 
    } finally { 
      setLoading(false); 
    }
  };

  const handleCancel = async (id) => {
    if (!confirm('Cancel this reservation?')) return;
    try {
      await cancelReservation(id);
      toast.success('Reservation cancelled');
      fetchReservations();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to cancel');
    }
  };

  const handleConfirm = async (id) => {
    try {
      await userConfirmReservation(id);
      toast.success('Confirmed! Please wait for admin to hand over the book.');
      fetchReservations();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to confirm');
    }
  };

  const formatDate = (d) => {
    if (!d) return '-';
    // Format roughly like: 2025-10-26T09:51:15.612260
    const date = new Date(d);
    return date.toISOString().replace('Z', '000'); 
  };

  // Grouping reservations
  const pendingRes = reservations.filter(r => r.status === 'PENDING');
  const readyRes = reservations.filter(r => r.status === 'READY');
  const confirmedRes = reservations.filter(r => r.status === 'USER_CONFIRMED');
  const fulfilledRes = reservations.filter(r => r.status === 'BORROWED');
  const cancelledRes = reservations.filter(r => ['CANCELLED', 'EXPIRED', 'REJECTED'].includes(r.status));

  // Stats
  const totalCount = reservations.length;
  const activeCount = pendingRes.length + readyRes.length + confirmedRes.length;
  const readyCount = readyRes.length;

  return (
    <div className="page-container my-reservations-page">
      <div className="page-title-row">
        <div className="title-icon-stack">
          <div className="title-icon-base"></div>
          <div className="title-icon-top"></div>
          <div className="title-icon-front"><IoJournal /></div>
        </div>
        <h1 className="my-res-title">My Reservation</h1>
      </div>
      <p className="my-res-subtitle">Manage and track your book reservations</p>

      {/* Stat Cards */}
      <div className="stats-row">
        <div className="stat-card-custom">
          <div className="stat-info">
            <span className="stat-label">TOTAL RESERVATION</span>
            <span className="stat-value">{totalCount}</span>
          </div>
          <div className="stat-icon-box icon-purple"><IoJournal /></div>
        </div>
        <div className="stat-card-custom">
          <div className="stat-info">
            <span className="stat-label">ACTIVE RESERVATION</span>
            <span className="stat-value">{activeCount}</span>
          </div>
          <div className="stat-icon-box icon-yellow"><IoTime /></div>
        </div>
        <div className="stat-card-custom">
          <div className="stat-info">
            <span className="stat-label">READY TO PICKUP</span>
            <span className="stat-value">{readyCount}</span>
          </div>
          <div className="stat-icon-box icon-green"><IoCalendarOutline /></div>
        </div>
      </div>

      {loading ? (
        <div className="spinner-container"><div className="spinner"></div></div>
      ) : (
        <>
          {/* Tabs */}
          <div className="tabs-container">
            <div 
              className={`custom-tab ${activeTab === 'ALL' ? 'active' : ''}`}
              onClick={() => setActiveTab('ALL')}
            >
              {activeTab === 'ALL' ? <IoJournal /> : <IoJournalOutline />} All Reservations
            </div>
            <div 
              className={`custom-tab ${activeTab === 'ACTIVE' ? 'active' : ''}`}
              onClick={() => setActiveTab('ACTIVE')}
            >
              {activeTab === 'ACTIVE' ? <IoTime /> : <IoTimeOutline />} Active
            </div>
            <div 
              className={`custom-tab ${activeTab === 'COMPLETED' ? 'active' : ''}`}
              onClick={() => setActiveTab('COMPLETED')}
            >
              {activeTab === 'COMPLETED' ? <IoCheckmarkCircle /> : <IoCheckmarkCircleOutline />} Completed
            </div>
          </div>

          {/* Kanban Board Layout */}
          {(activeTab === 'ALL' || activeTab === 'ACTIVE') && (
            <div className="kanban-board">
              {/* PENDING Column */}
              <div className="kanban-col pending">
                <div className="kanban-header">
                  <IoHourglassOutline /> PENDING
                </div>
                <div className="kanban-body">
                  {pendingRes.length > 0 ? pendingRes.map(r => (
                    <div key={r.id} className="res-card">
                      <div className="res-card-top">
                        <div className="res-book-icon"><IoJournal /></div>
                        <div className="res-book-info">
                          <div className="res-book-id">Book Id #{r.bookId || r.id}</div>
                          <div className="res-book-title">{r.bookTitle}</div>
                        </div>
                      </div>
                      <div className="res-divider"></div>
                      <div className="res-dates">
                        <div className="res-date-row date-reserved">
                          <IoTimeOutline />
                          <div>
                            <span className="res-date-label">RESERVED</span>
                            <span className="res-date-val">{formatDate(r.reservedAt)}</span>
                          </div>
                        </div>
                      </div>
                      {r.canBeCancelled && (
                        <div style={{ marginTop: '12px' }}>
                          <button className="btn btn-danger btn-sm" style={{width: '100%'}} onClick={() => handleCancel(r.id)}>Cancel Reservation</button>
                        </div>
                      )}
                    </div>
                  )) : <div className="kanban-empty">No pending reservations</div>}
                </div>
              </div>

              {/* READY Column */}
              <div className="kanban-col ready">
                <div className="kanban-header">
                  <IoTimeOutline /> READY
                </div>
                <div className="kanban-body">
                  {readyRes.length > 0 ? readyRes.map(r => (
                    <div key={r.id} className="res-card">
                      <div className="res-card-top">
                        <div className="res-book-icon"><IoJournal /></div>
                        <div className="res-book-info">
                          <div className="res-book-id">Book Id #{r.bookId || r.id}</div>
                          <div className="res-book-title">{r.bookTitle}</div>
                        </div>
                      </div>
                      <div className="res-divider"></div>
                      <div className="res-dates">
                        <div className="res-date-row date-reserved">
                          <IoTimeOutline />
                          <div>
                            <span className="res-date-label">RESERVED</span>
                            <span className="res-date-val">{formatDate(r.reservedAt)}</span>
                          </div>
                        </div>
                        <div className="res-date-row date-available">
                          <IoCalendarOutline />
                          <div>
                            <span className="res-date-label">READY</span>
                            <span className="res-date-val">{formatDate(r.availableAt || r.updatedAt)}</span>
                          </div>
                        </div>
                      </div>
                      <div style={{ marginTop: '12px', display: 'flex', gap: '8px', flexDirection: 'column' }}>
                        <button className="btn btn-success btn-sm" style={{width: '100%'}} onClick={() => handleConfirm(r.id)}>Confirm Borrow</button>
                        {r.canBeCancelled && (
                          <button className="btn btn-danger btn-sm" style={{width: '100%'}} onClick={() => handleCancel(r.id)}>Cancel</button>
                        )}
                      </div>
                    </div>
                  )) : <div className="kanban-empty">No books ready for pickup</div>}
                </div>
              </div>

              {/* CONFIRMED Column */}
              <div className="kanban-col confirmed">
                <div className="kanban-header">
                  <IoCheckmarkCircleOutline /> CONFIRMED
                </div>
                <div className="kanban-body">
                  {confirmedRes.length > 0 ? confirmedRes.map(r => (
                    <div key={r.id} className="res-card">
                      <div className="res-card-top">
                        <div className="res-book-icon"><IoJournal /></div>
                        <div className="res-book-info">
                          <div className="res-book-id">Book Id #{r.bookId || r.id}</div>
                          <div className="res-book-title">{r.bookTitle}</div>
                        </div>
                      </div>
                      <div className="res-divider"></div>
                      <div className="res-dates">
                        <div className="res-date-row date-reserved">
                          <IoCheckmarkCircleOutline />
                          <div>
                            <span className="res-date-label">CONFIRMED</span>
                            <span className="res-date-val">{formatDate(r.updatedAt)}</span>
                          </div>
                        </div>
                      </div>
                      <div style={{ marginTop: '12px' }}>
                         <div className="text-xs text-muted" style={{textAlign: 'center'}}>Waiting for admin to hand over book</div>
                      </div>
                    </div>
                  )) : <div className="kanban-empty">No user-confirmed reservations</div>}
                </div>
              </div>

              {/* BORROWED Column */}
              {activeTab === 'ALL' && (
              <div className="kanban-col fulfilled">
                <div className="kanban-header">
                  <IoCheckmarkCircle /> BORROWED
                </div>
                <div className="kanban-body">
                  {fulfilledRes.length > 0 ? fulfilledRes.map(r => (
                    <div key={r.id} className="res-card">
                      <div className="res-card-top">
                        <div className="res-book-icon"><IoJournal /></div>
                        <div className="res-book-info">
                          <div className="res-book-id">Book Id #{r.bookId || r.id}</div>
                          <div className="res-book-title">{r.bookTitle}</div>
                        </div>
                      </div>
                      <div className="res-divider"></div>
                      <div className="res-dates">
                        <div className="res-date-row date-reserved">
                          <IoTimeOutline />
                          <div>
                            <span className="res-date-label">RESERVED</span>
                            <span className="res-date-val">{formatDate(r.reservedAt)}</span>
                          </div>
                        </div>
                        {(r.availableAt || r.updatedAt) && (
                          <div className="res-date-row date-available">
                            <IoCalendarOutline />
                            <div>
                              <span className="res-date-label">AVAILABLE</span>
                              <span className="res-date-val">{formatDate(r.availableAt || r.updatedAt)}</span>
                            </div>
                          </div>
                        )}
                        <div className="res-date-row date-fulfilled">
                          <IoCheckmarkCircle />
                          <div>
                            <span className="res-date-label">BORROWED</span>
                            <span className="res-date-val">{formatDate(r.fulfilledAt || r.updatedAt)}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  )) : (
                    <div className="kanban-empty">
                      {activeTab === 'ALL' ? 'No fulfilled reservations yet' : 'No fulfilled reservations shown in this view'}
                    </div>
                  )}
                </div>
              </div>
              )}

              {/* CLOSED Column */}
              {activeTab === 'ALL' && (
              <div className="kanban-col closed">
                <div className="kanban-header">
                  <IoCheckmarkCircleOutline /> CLOSED
                </div>
                <div className="kanban-body">
                  {cancelledRes.length > 0 ? cancelledRes.map(r => (
                    <div key={r.id} className="res-card">
                      <div className="res-card-top">
                        <div className="res-book-icon" style={{background: r.status === 'REJECTED' ? 'var(--accent-red, #D32F2F)' : 'gray'}}><IoJournal /></div>
                        <div className="res-book-info">
                          <div className="res-book-id">Book Id #{r.bookId || r.id}</div>
                          <div className="res-book-title">{r.bookTitle}</div>
                        </div>
                      </div>
                      <div className="res-divider"></div>
                      <div className="res-dates">
                        <div className="res-date-row date-reserved">
                          <IoTimeOutline />
                          <div>
                            <span className="res-date-label">RESERVED</span>
                            <span className="res-date-val">{formatDate(r.reservedAt)}</span>
                          </div>
                        </div>
                        <div className="res-date-row" style={{color: r.status === 'REJECTED' ? '#D32F2F' : 'var(--text-secondary)'}}>
                          <IoCheckmarkCircleOutline />
                          <div>
                            <span className="res-date-label">STATUS</span>
                            <span className="res-date-val">{r.status}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  )) : (
                    <div className="kanban-empty">No closed reservations</div>
                  )}
                </div>
              </div>
              )}
            </div>
          )}

          {/* List display for Completed tab */}
          {activeTab === 'COMPLETED' && (
            <div className="kanban-board">
               <div className="kanban-col fulfilled" style={{gridColumn: '1 / -1'}}>
                <div className="kanban-header">
                  <IoCheckmarkCircle /> COMPLETED RESERVATIONS (BORROWED / CANCELLED / REJECTED)
                </div>
                <div className="kanban-body" style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '16px'}}>
                  {[...fulfilledRes, ...cancelledRes].map(r => (
                    <div key={r.id} className="res-card">
                      <div className="res-card-top">
                        <div className="res-book-icon" style={{background: r.status === 'CANCELLED' || r.status === 'EXPIRED' ? 'gray' : r.status === 'REJECTED' ? 'var(--accent-red, #D32F2F)' : 'var(--primary)'}}>
                          <IoJournal />
                        </div>
                        <div className="res-book-info">
                          <div className="res-book-id">Book Id #{r.bookId || r.id}</div>
                          <div className="res-book-title">{r.bookTitle}</div>
                        </div>
                      </div>
                      <div className="res-divider"></div>
                      <div className="res-dates">
                        <div className="res-date-row date-reserved">
                          <IoTimeOutline />
                          <div>
                            <span className="res-date-label">RESERVED</span>
                            <span className="res-date-val">{formatDate(r.reservedAt)}</span>
                          </div>
                        </div>
                        <div className="res-date-row" style={{color: r.status === 'CANCELLED' || r.status === 'EXPIRED' ? 'var(--text-secondary)' : r.status === 'REJECTED' ? '#D32F2F' : 'var(--accent-blue)'}}>
                          <IoCheckmarkCircleOutline />
                          <div>
                            <span className="res-date-label">STATUS</span>
                            <span className="res-date-val">{r.status}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                  {[...fulfilledRes, ...cancelledRes].length === 0 && (
                    <div className="kanban-empty">No completed reservations found.</div>
                  )}
                </div>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
