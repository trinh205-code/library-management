import { useState, useEffect } from 'react';
import { getAllFines, waiveFine } from '../../../api/fineApi';
import StatusBadge from '../../../components/StatusBadge';
import Pagination from '../../../components/Pagination';
import Modal from '../../../components/Modal';
import toast from 'react-hot-toast';
import { IoShieldCheckmarkOutline, IoCashOutline } from 'react-icons/io5';

export default function ManageFines() {
  const [fines, setFines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const [waiveModal, setWaiveModal] = useState(false);
  const [selectedFine, setSelectedFine] = useState(null);
  const [waiveReason, setWaiveReason] = useState('');

  useEffect(() => { fetchFines(); }, [page, statusFilter]);

  const fetchFines = async () => {
    setLoading(true);
    try {
      const params = { page, size: 10, ...(statusFilter && { status: statusFilter }) };
      const res = await getAllFines(params);
      setFines(res.data?.content || []);
      setTotalPages(res.data?.totalPages || 0);
    } catch { toast.error('Failed to load fines'); }
    finally { setLoading(false); }
  };

  const handleWaive = async () => {
    if (!waiveReason) { toast.error('Please provide a reason'); return; }
    try {
      await waiveFine({ fineId: selectedFine.id, reason: waiveReason });
      toast.success('Fine waived');
      setWaiveModal(false);
      setWaiveReason('');
      fetchFines();
    } catch (err) { toast.error(err.response?.data || 'Waive failed'); }
  };

  const formatCurrency = (a) => a != null ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(a) : '-';
  const formatDate = (d) => d ? new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) : '-';

  const tabs = [
    { key: '', label: 'All' },
    { key: 'PENDING', label: 'Pending' },
    { key: 'PAID', label: 'Paid' },
    { key: 'WAIVED', label: 'Waived' },
  ];

  return (
    <div className="page-container">
      <div className="page-header"><h1>Manage <span className="text-gradient">Fines</span></h1><p>Review and manage library fines</p></div>

      <div className="tabs">
        {tabs.map(t => <button key={t.key} className={`tab ${statusFilter === t.key ? 'active' : ''}`} onClick={() => { setStatusFilter(t.key); setPage(0); }}>{t.label}</button>)}
      </div>

      {loading ? <div className="spinner-container"><div className="spinner"></div></div> : (
        <div className="card" style={{ padding: 0 }}>
          <div className="table-container">
            <table className="data-table">
              <thead><tr><th>User</th><th>Book</th><th>Amount</th><th>Outstanding</th><th>Status</th><th>Reason</th><th>Created</th><th>Actions</th></tr></thead>
              <tbody>
                {fines.map((f) => (
                  <tr key={f.id}>
                    <td><strong>{f.userName}</strong><div className="text-xs text-muted">{f.userEmail}</div></td>
                    <td>{f.bookTitle || '-'}</td>
                    <td>{formatCurrency(f.amount)}</td>
                    <td style={{ fontWeight: 600, color: f.amountOutstanding > 0 ? 'var(--accent-red)' : 'var(--accent-green)' }}>{formatCurrency(f.amountOutstanding)}</td>
                    <td><StatusBadge status={f.status} /></td>
                    <td className="text-sm text-muted">{f.reason || '-'}</td>
                    <td>{formatDate(f.createdAt)}</td>
                    <td>
                      {f.status === 'PENDING' && (
                        <button className="btn btn-ghost btn-sm" onClick={() => { setSelectedFine(f); setWaiveModal(true); }}><IoShieldCheckmarkOutline /> Waive</button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
      <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />

      <Modal isOpen={waiveModal} onClose={() => setWaiveModal(false)} title="Waive Fine"
        footer={<><button className="btn btn-secondary" onClick={() => setWaiveModal(false)}>Cancel</button><button className="btn btn-primary" onClick={handleWaive}>Waive Fine</button></>}
      >
        <p className="mb-md">Waive fine of <strong>{formatCurrency(selectedFine?.amount)}</strong> for <strong>{selectedFine?.userName}</strong>?</p>
        <div className="form-group">
          <label className="form-label">Reason *</label>
          <textarea className="form-input" value={waiveReason} onChange={(e) => setWaiveReason(e.target.value)} placeholder="Enter reason for waiving..." />
        </div>
      </Modal>
    </div>
  );
}
