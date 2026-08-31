import { useState, useEffect } from 'react';
import { searchLoans, checkinBook, updateOverdueLoans } from '../../../api/bookLoanApi';
import { createFine } from '../../../api/fineApi';
import Modal from '../../../components/Modal';
import StatusBadge from '../../../components/StatusBadge';
import Pagination from '../../../components/Pagination';
import toast from 'react-hot-toast';
import { IoReturnDownBackOutline, IoWarningOutline, IoReceiptOutline, IoPersonCircleOutline, IoBookOutline } from 'react-icons/io5';

const styles = `
  .ml-list-wrapper {
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 4px 15px rgba(0,0,0,0.03);
    margin-bottom: 24px;
  }
  .ml-grid {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
  .ml-card {
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
  .ml-card:hover {
    border-color: var(--accent-blue);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.08);
    transform: translateY(-2px);
  }
  .ml-col-user {
    flex: 1.5;
    min-width: 200px;
  }
  .ml-col-book {
    flex: 2;
    min-width: 250px;
  }
  .ml-col-dates {
    flex: 1.5;
    min-width: 200px;
    display: flex;
    flex-direction: column;
    gap: 6px;
  }
  .ml-col-actions {
    flex: 1;
    min-width: 150px;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 12px;
  }
  .ml-title {
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 4px;
  }
  .ml-subtitle {
    font-size: 0.85rem;
    color: var(--text-muted);
  }
  .ml-date-row {
    font-size: 0.85rem;
    display: flex;
    justify-content: space-between;
  }
  .ml-date-label {
    color: var(--text-muted);
  }
  .ml-date-val {
    font-weight: 500;
    color: var(--text-secondary);
  }
  @media (max-width: 1024px) {
    .ml-card {
      flex-direction: column;
      align-items: flex-start;
    }
    .ml-col-actions {
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

export default function ManageLoans() {
  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');

  const [fineModalOpen, setFineModalOpen] = useState(false);
  const [selectedLoanId, setSelectedLoanId] = useState(null);
  const [fineForm, setFineForm] = useState({ amount: 0, reason: '', note: '', type: 'OVERDUE' });
  const [submittingFine, setSubmittingFine] = useState(false);

  useEffect(() => { fetchLoans(); }, [page, statusFilter]);

  const fetchLoans = async () => {
    setLoading(true);
    try {
      const searchReq = { page, size: 10, ...(statusFilter && { status: statusFilter }) };
      const res = await searchLoans(searchReq);
      setLoans(res.data?.content || []);
      setTotalPages(res.data?.totalPages || 0);
    } catch { toast.error('Failed to load loans'); }
    finally { setLoading(false); }
  };

  const handleCheckin = async (loanId, bookId) => {
    try {
      await checkinBook({ bookLoanId: loanId, bookId });
      toast.success('Book checked in!');
      fetchLoans();
    } catch (err) { toast.error(err.response?.data?.message || 'Checkin failed'); }
  };

  const handleUpdateOverdue = async () => {
    try {
      const res = await updateOverdueLoans();
      toast.success(`Updated ${res.data} overdue loans`);
      fetchLoans();
    } catch { toast.error('Failed to update overdue'); }
  };

  const handleOpenFineModal = (loanId) => {
    setSelectedLoanId(loanId);
    setFineForm({ amount: 0, reason: '', note: '', type: 'OVERDUE' });
    setFineModalOpen(true);
  };

  const handleSubmitFine = async () => {
    if (fineForm.amount <= 0) { toast.error("Amount must be greater than 0"); return; }
    if (!fineForm.reason) { toast.error("Reason is required"); return; }
    
    setSubmittingFine(true);
    try {
      await createFine({ bookLoanId: selectedLoanId, ...fineForm });
      toast.success("Fine created successfully!");
      setFineModalOpen(false);
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to create fine");
    } finally {
      setSubmittingFine(false);
    }
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
    { key: 'CHECKED_OUT', label: 'Checked Out' },
    { key: 'OVERDUE', label: 'Overdue' },
    { key: 'RETURNED', label: 'Returned' },
  ];

  return (
    <div className="page-container">
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div><h1>Manage <span className="text-gradient">Loans</span></h1><p>Track and manage all book loans</p></div>
        <button className="btn btn-warning" onClick={handleUpdateOverdue}><IoWarningOutline /> Update Overdue</button>
      </div>

      <div className="tabs">
        {tabs.map(t => (
          <button key={t.key} className={`tab ${statusFilter === t.key ? 'active' : ''}`} onClick={() => { setStatusFilter(t.key); setPage(0); }}>{t.label}</button>
        ))}
      </div>

      {loading ? <div className="spinner-container"><div className="spinner"></div></div> : (
        <div className="ml-list-wrapper">
          <style>{styles}</style>
          <div className="ml-grid">
            {loans.map((l) => (
              <div key={l.id} className="ml-card">
                <div className="ml-col-user" style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <IoPersonCircleOutline size={40} style={{ color: 'var(--text-muted)' }} />
                  <div>
                    <div className="ml-title">{l.userName}</div>
                    <div className="ml-subtitle">{l.userEmail}</div>
                  </div>
                </div>
                
                <div className="ml-col-book" style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}>
                  <div style={{ marginTop: '2px', color: 'var(--accent-blue)' }}><IoBookOutline size={20} /></div>
                  <div>
                    <div className="ml-title">{l.bookTitle}</div>
                    <div className="ml-subtitle">ISBN: {l.bookIsbn}</div>
                  </div>
                </div>

                <div className="ml-col-dates">
                  <div className="ml-date-row"><span className="ml-date-label">Checkout:</span> <span className="ml-date-val">{formatDate(l.checkoutDate)}</span></div>
                  <div className="ml-date-row"><span className="ml-date-label">Due:</span> <span className="ml-date-val">{formatDate(l.dueDate)}</span></div>
                  {l.returnDate && <div className="ml-date-row"><span className="ml-date-label">Returned:</span> <span className="ml-date-val">{formatDate(l.returnDate)}</span></div>}
                  {l.isOverdue && (
                    <div className="ml-date-row" style={{ marginTop: '4px', padding: '4px 8px', background: 'rgba(239, 68, 68, 0.1)', borderRadius: '6px' }}>
                      <span className="ml-date-label" style={{ color: 'var(--accent-red)' }}>Overdue:</span> 
                      <span className="ml-date-val" style={{ color: 'var(--accent-red)', fontWeight: 'bold' }}>{l.overdueDays} days</span>
                    </div>
                  )}
                </div>

                <div className="ml-col-actions">
                  <StatusBadge status={l.status} />
                  <div style={{ display: 'flex', gap: '8px' }}>
                    {l.status === 'CHECKED_OUT' && (
                      <button className="btn btn-success btn-sm" onClick={() => handleCheckin(l.id, l.bookId)}>
                        <IoReturnDownBackOutline /> Check In
                      </button>
                    )}
                    {l.status !== 'RETURNED' && (
                      <button className="btn btn-danger btn-sm" onClick={() => handleOpenFineModal(l.id)}>
                        <IoReceiptOutline /> Fine
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
      <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />

      <Modal isOpen={fineModalOpen} onClose={() => setFineModalOpen(false)} title="Create Fine"
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setFineModalOpen(false)}>Cancel</button>
            <button className="btn btn-danger" onClick={handleSubmitFine} disabled={submittingFine}>
              {submittingFine ? 'Submitting...' : 'Create Fine'}
            </button>
          </>
        }>
        <div className="form-group">
          <label className="form-label">Fine Type</label>
          <select className="form-select" value={fineForm.type} onChange={e => setFineForm({ ...fineForm, type: e.target.value })}>
            <option value="OVERDUE">Late Return (OVERDUE)</option>
            <option value="DAMAGE">Book Damage</option>
            <option value="LOSS">Book Lost (LOSS)</option>
            <option value="PROCESSING">Other / Processing</option>
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Amount (VND)</label>
          <input type="number" className="form-input" min="0" value={fineForm.amount} onChange={e => setFineForm({ ...fineForm, amount: parseFloat(e.target.value || 0) })} />
        </div>
        <div className="form-group">
          <label className="form-label">Reason *</label>
          <input className="form-input" value={fineForm.reason} onChange={e => setFineForm({ ...fineForm, reason: e.target.value })} placeholder="e.g. Returned 5 days late" />
        </div>
        <div className="form-group">
          <label className="form-label">Internal Note</label>
          <textarea className="form-input" rows={2} value={fineForm.note} onChange={e => setFineForm({ ...fineForm, note: e.target.value })} placeholder="Optional notes for admin" />
        </div>
      </Modal>
    </div>
  );
}
