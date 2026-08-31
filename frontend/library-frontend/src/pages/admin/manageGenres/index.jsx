import { useState, useEffect } from 'react';
import { getAllGenres, createGenre, updateGenre, deleteGenre } from '../../../api/genreApi';
import Modal from '../../../components/Modal';
import toast from 'react-hot-toast';
import { IoAddOutline, IoCreateOutline, IoTrashOutline, IoBookmarksOutline, IoEyeOutline, IoEyeOffOutline } from 'react-icons/io5';

export default function ManageGenres() {
  const [genres, setGenres] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ code: '', name: '', description: '', displayOrder: 0, parentGenreId: '' });
  const [saving, setSaving] = useState(false);

  useEffect(() => { fetchGenres(); }, []);

  const fetchGenres = async () => {
    setLoading(true);
    try { const res = await getAllGenres(); setGenres(res.data || []); }
    catch { toast.error('Failed to load genres'); }
    finally { setLoading(false); }
  };

  const openCreate = () => { setEditing(null); setForm({ code: '', name: '', description: '', displayOrder: 0, parentGenreId: '' }); setModalOpen(true); };

  const openEdit = (g) => {
    setEditing(g);
    setForm({ code: g.code, name: g.name, description: g.description || '', displayOrder: g.displayOrder || 0, parentGenreId: g.parentGenreId || '' });
    setModalOpen(true);
  };

  const handleSave = async () => {
    if (!form.code || !form.name) { toast.error('Code and Name are required'); return; }
    setSaving(true);
    try {
      const data = { ...form, parentGenreId: form.parentGenreId ? parseInt(form.parentGenreId) : null };
      if (editing) { await updateGenre(editing.id, data); toast.success('Genre updated!'); }
      else { await createGenre(data); toast.success('Genre created!'); }
      setModalOpen(false); fetchGenres();
    } catch (err) { toast.error(err.response?.data?.message || 'Save failed'); }
    finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this genre?')) return;
    try { await deleteGenre(id); toast.success('Genre deleted'); fetchGenres(); }
    catch { toast.error('Delete failed'); }
  };

  const handleToggleActive = async (g) => {
    if (!confirm(`Are you sure you want to ${g.active ? 'deactivate' : 'activate'} this genre?`)) return;
    try {
      const payload = { 
        ...g, 
        active: !g.active,
        parentGenreId: g.parentGenreId ? parseInt(g.parentGenreId) : null
      };
      await updateGenre(g.id, payload);
      toast.success(`Genre ${payload.active ? 'activated' : 'deactivated'}!`);
      fetchGenres();
    } catch (err) {
      toast.error('Failed to change status');
    }
  };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  return (
    <div className="page-container">
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div><h1>Manage <span className="text-gradient">Genres</span></h1><p>Organize your book categories</p></div>
        <button className="btn btn-primary" onClick={openCreate}><IoAddOutline /> Add Genre</button>
      </div>

      {loading ? <div className="spinner-container"><div className="spinner"></div></div> : (
        <div className="card" style={{ padding: 0 }}>
          <div className="table-container">
            <table className="data-table">
              <thead><tr><th>Code</th><th>Name</th><th>Description</th><th>Parent</th><th>Books</th><th>Order</th><th>Status</th><th>Actions</th></tr></thead>
              <tbody>
                {genres.map((g) => (
                  <tr key={g.id}>
                    <td><code style={{ background: 'var(--primary-bg)', color: 'var(--primary)', padding: '2px 8px', borderRadius: '4px', fontSize: '0.8rem' }}>{g.code}</code></td>
                    <td><strong>{g.name}</strong></td>
                    <td className="text-sm text-muted">{g.description?.substring(0, 50) || '-'}</td>
                    <td>{g.parentGenreName || '-'}</td>
                    <td>{g.bookCount || 0}</td>
                    <td>{g.displayOrder}</td>
                    <td>{g.active ? <span className="badge badge-success">Active</span> : <span className="badge badge-danger">Inactive</span>}</td>
                    <td>
                      <div className="flex gap-sm">
                        <button 
                          className="btn btn-ghost btn-sm" 
                          style={{ padding: '4px 8px', color: g.active ? '#64748b' : '#10b981' }} 
                          title={g.active ? 'Deactivate' : 'Activate'}
                          onClick={() => handleToggleActive(g)}
                        >
                          {g.active ? <IoEyeOffOutline size={18} /> : <IoEyeOutline size={18} />}
                        </button>
                        <button className="btn btn-ghost btn-sm" onClick={() => openEdit(g)} title="Edit"><IoCreateOutline size={18} /></button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(g.id)} title="Delete"><IoTrashOutline size={18} /></button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit Genre' : 'Add Genre'}
        footer={<><button className="btn btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button><button className="btn btn-primary" onClick={handleSave} disabled={saving}>{saving ? 'Saving...' : 'Save'}</button></>}
      >
        <div className="form-group"><label className="form-label">Code *</label><input name="code" className="form-input" value={form.code} onChange={handleChange} placeholder="e.g. FIC" /></div>
        <div className="form-group"><label className="form-label">Name *</label><input name="name" className="form-input" value={form.name} onChange={handleChange} placeholder="e.g. Fiction" /></div>
        <div className="form-group"><label className="form-label">Description</label><textarea name="description" className="form-input" value={form.description} onChange={handleChange} /></div>
        <div className="form-group"><label className="form-label">Parent Genre</label>
          <select name="parentGenreId" className="form-select" value={form.parentGenreId} onChange={handleChange}>
            <option value="">None (Top Level)</option>
            {genres.filter(g => g.id !== editing?.id).map(g => <option key={g.id} value={g.id}>{g.name}</option>)}
          </select>
        </div>
        <div className="form-group"><label className="form-label">Display Order</label><input name="displayOrder" type="number" className="form-input" value={form.displayOrder} onChange={handleChange} /></div>
      </Modal>
    </div>
  );
}
