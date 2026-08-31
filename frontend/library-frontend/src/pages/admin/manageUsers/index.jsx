import { useState, useEffect } from 'react';
import { getAllUsers, adminUpdateUser, adminDeleteUser } from '../../../api/userApi';
import Modal from '../../../components/Modal';
import toast from 'react-hot-toast';
import { IoPeopleOutline, IoCreateOutline, IoTrashOutline } from 'react-icons/io5';

export default function ManageUsers() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [form, setForm] = useState({ fullName: '', phone: '', role: 'ROLE_USER' });
  const [saving, setSaving] = useState(false);

  useEffect(() => { fetchUsers(); }, []);

  const fetchUsers = async () => {
    setLoading(true);
    try { const res = await getAllUsers(); setUsers(res.data || []); }
    catch { toast.error('Failed to load users'); }
    finally { setLoading(false); }
  };

  const openEditModal = (user) => {
    setEditingUser(user);
    setForm({
      fullName: user.fullName || '',
      phone: user.phone || '',
      role: user.role || 'ROLE_USER'
    });
    setModalOpen(true);
  };

  const handleSave = async () => {
    if (!form.fullName) { toast.error('Full name is required'); return; }
    setSaving(true);
    try {
      await adminUpdateUser(editingUser.id, form);
      toast.success('User updated successfully');
      setModalOpen(false);
      fetchUsers();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update user');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Are you sure you want to delete this user? This action cannot be undone.')) return;
    try {
      await adminDeleteUser(id);
      toast.success('User deleted successfully');
      fetchUsers();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete user. They may have active records.');
    }
  };

  const formatDate = (d) => d ? new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : 'Never';

  return (
    <div className="page-container">
      <div className="page-header"><h1>Manage <span className="text-gradient">Users</span></h1><p>View registered library members</p></div>

      {loading ? <div className="spinner-container"><div className="spinner"></div></div> : users.length > 0 ? (
        <div className="card" style={{ padding: 0 }}>
          <div className="table-container">
            <table className="data-table">
              <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Role</th><th>Last Login</th><th>Actions</th></tr></thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td><strong>{u.fullName}</strong></td>
                    <td>{u.email}</td>
                    <td>{u.phone || '-'}</td>
                    <td><span className={`badge ${u.role === 'ROLE_ADMIN' ? 'badge-purple' : 'badge-info'}`}>{u.role === 'ROLE_ADMIN' ? 'Admin' : 'User'}</span></td>
                    <td className="text-sm text-muted">{formatDate(u.lastLogin)}</td>
                    <td>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button className="btn btn-ghost btn-sm" onClick={() => openEditModal(u)} style={{ padding: '6px' }} title="Edit"><IoCreateOutline size={18} /></button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(u.id)} style={{ padding: '6px' }} title="Delete"><IoTrashOutline size={18} /></button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="empty-state">
          <div className="empty-state-icon"><IoPeopleOutline /></div>
          <h3 className="empty-state-title">No Users Found</h3>
        </div>
      )}

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Edit User"
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button className="btn btn-primary" onClick={handleSave} disabled={saving}>{saving ? 'Saving...' : 'Save Changes'}</button>
          </>
        }
      >
        <div className="form-group">
          <label className="form-label">Full Name</label>
          <input className="form-input" value={form.fullName} onChange={e => setForm({...form, fullName: e.target.value})} />
        </div>
        <div className="form-group">
          <label className="form-label">Phone</label>
          <input className="form-input" value={form.phone} onChange={e => setForm({...form, phone: e.target.value})} />
        </div>
        <div className="form-group">
          <label className="form-label">Role</label>
          <select className="form-select" value={form.role} onChange={e => setForm({...form, role: e.target.value})}>
            <option value="ROLE_USER">User</option>
            <option value="ROLE_ADMIN">Admin</option>
          </select>
        </div>
      </Modal>

    </div>
  );
}
