import { useState, useEffect } from 'react';
import { advancedSearchBooks, createBook, updateBook, deleteBook } from '../../../api/bookApi';
import { getAllGenres } from '../../../api/genreApi';
import Modal from '../../../components/Modal';
import Pagination from '../../../components/Pagination';
import toast from 'react-hot-toast';
import {
  IoAddOutline, IoCreateOutline, IoTrashOutline, IoBookOutline,
  IoEyeOutline, IoEyeOffOutline, IoPersonOutline, IoSearchOutline,
  IoCloseOutline, IoRefreshOutline
} from 'react-icons/io5';

const emptyBook = {
  isbn: '', title: '', author: '', genreId: '', publisher: '',
  language: 'English', pages: '', description: '', totalCopies: 1,
  availableCopies: 1, coverImageUrl: '', price: ''
};

const styles = `
  .mb-page { padding: 28px 0; max-width: 950px; margin: 0 auto; }
  .mb-list-wrapper { background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; padding: 24px; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }
  .mb-topbar { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 24px; }
  .mb-heading { margin: 0 0 2px; font-size: 1.6rem; font-weight: 700; letter-spacing: -0.4px; color: var(--text-primary, #111); }
  .mb-heading span { color: #7c3aed; }
  .mb-subheading { margin: 0; font-size: 0.8rem; color: #6b7280; }
  .mb-btn-add { display: inline-flex; align-items: center; gap: 6px; background: #7c3aed; color: #fff; border: none; border-radius: 8px; padding: 8px 16px; font-size: 0.82rem; font-weight: 600; cursor: pointer; transition: background 0.15s; white-space: nowrap; }
  .mb-btn-add:hover { background: #6d28d9; }

  .mb-filters { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; margin-bottom: 16px; }
  .mb-search-wrap { position: relative; flex: 1; min-width: 200px; }
  .mb-search-icon { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); color: #9ca3af; pointer-events: none; font-size: 14px; }
  .mb-search-input { width: 100%; padding: 7px 10px 7px 30px; border: 1px solid #e5e7eb; border-radius: 7px; font-size: 0.82rem; background: #fff; outline: none; color: #111; box-sizing: border-box; transition: border-color 0.15s; }
  .mb-search-input:focus { border-color: #7c3aed; }
  .mb-select { padding: 7px 10px; border: 1px solid #e5e7eb; border-radius: 7px; font-size: 0.82rem; background: #fff; color: #374151; cursor: pointer; outline: none; min-width: 120px; }
  .mb-check-label { display: flex; align-items: center; gap: 6px; font-size: 0.82rem; color: #374151; cursor: pointer; white-space: nowrap; }
  .mb-check-label input[type=checkbox] { width: 14px; height: 14px; accent-color: #7c3aed; }
  .mb-btn-search { padding: 7px 14px; background: #7c3aed; color: #fff; border: none; border-radius: 7px; font-size: 0.82rem; font-weight: 600; cursor: pointer; }
  .mb-btn-search:hover { background: #6d28d9; }
  .mb-btn-clear { padding: 7px 12px; background: transparent; color: #6b7280; border: 1px solid #e5e7eb; border-radius: 7px; font-size: 0.82rem; cursor: pointer; display: flex; align-items: center; gap: 4px; }
  .mb-btn-clear:hover { background: #f9fafb; }

  .mb-list { display: flex; flex-direction: column; gap: 10px; align-items: flex-start; }
  .mb-row { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: 14px 18px; display: flex; align-items: center; gap: 16px; transition: box-shadow 0.15s; width: 100%; max-width: 850px; }
  .mb-row:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }

  .mb-cover { width: 48px; height: 64px; border-radius: 5px; overflow: hidden; flex-shrink: 0; }
  .mb-cover img { width: 100%; height: 100%; object-fit: cover; }
  .mb-cover-ph { width: 100%; height: 100%; background: linear-gradient(135deg, #a5b4fc, #7c3aed); display: flex; align-items: center; justify-content: center; color: #fff; }

  .mb-info { flex: 1; min-width: 0; }
  .mb-title { font-size: 0.9rem; font-weight: 600; color: #111; margin: 0 0 3px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .mb-author { display: flex; align-items: center; gap: 4px; font-size: 0.78rem; color: #6b7280; margin: 0 0 2px; }
  .mb-isbn { font-size: 0.75rem; color: #9ca3af; font-family: monospace; margin: 0; }

  .mb-divider { width: 1px; height: 40px; background: #e5e7eb; flex-shrink: 0; }

  .mb-meta { display: flex; gap: 28px; align-items: center; }
  .mb-meta-item { display: flex; flex-direction: column; gap: 2px; }
  .mb-meta-label { font-size: 0.7rem; color: #9ca3af; font-weight: 500; text-transform: uppercase; letter-spacing: 0.04em; }
  .mb-meta-val { font-size: 0.82rem; font-weight: 600; color: #111; }

  .mb-genre-badge { display: inline-block; background: #ede9fe; color: #6d28d9; padding: 2px 8px; border-radius: 20px; font-size: 0.7rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.04em; }
  .mb-copies-avail { color: #059669; }

  .mb-status-active { display: inline-flex; align-items: center; gap: 4px; background: #d1fae5; color: #065f46; padding: 2px 8px; border-radius: 20px; font-size: 0.7rem; font-weight: 700; text-transform: uppercase; }
  .mb-status-active::before { content: ''; width: 5px; height: 5px; border-radius: 50%; background: #059669; }
  .mb-status-inactive { display: inline-flex; align-items: center; gap: 4px; background: #fee2e2; color: #991b1b; padding: 2px 8px; border-radius: 20px; font-size: 0.7rem; font-weight: 700; text-transform: uppercase; }
  .mb-status-inactive::before { content: ''; width: 5px; height: 5px; border-radius: 50%; background: #ef4444; }

  .mb-actions { display: flex; gap: 4px; align-items: center; margin-left: auto; flex-shrink: 0; }
  .mb-icon-btn { width: 28px; height: 28px; border-radius: 6px; border: 1px solid #e5e7eb; background: transparent; display: flex; align-items: center; justify-content: center; cursor: pointer; color: #9ca3af; transition: all 0.15s; }
  .mb-icon-btn:hover { background: #f3f4f6; color: #374151; border-color: #d1d5db; }
  .mb-icon-btn.edit:hover { background: #eef2ff; color: #4f46e5; border-color: #c7d2fe; }
  .mb-icon-btn.tog-on:hover { background: #ecfdf5; color: #059669; border-color: #a7f3d0; }
  .mb-icon-btn.tog-off:hover { background: #fef2f2; color: #dc2626; border-color: #fecaca; }
  .mb-icon-btn.del:hover { background: #fef2f2; color: #dc2626; border-color: #fecaca; }

  .mb-empty { text-align: center; padding: 48px; color: #9ca3af; background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; }
  .mb-empty p { margin: 8px 0 0; font-size: 0.85rem; }
  .mb-spinner { display: flex; justify-content: center; padding: 60px; }
  .mb-spinner-dot { width: 32px; height: 32px; border: 2px solid #e5e7eb; border-top-color: #7c3aed; border-radius: 50%; animation: mbspin 0.7s linear infinite; }
  @keyframes mbspin { to { transform: rotate(360deg); } }

  .mb-form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
  .mb-fg { display: flex; flex-direction: column; gap: 4px; margin-bottom: 12px; }
  .mb-fl { font-size: 0.78rem; font-weight: 600; color: #374151; }
  .mb-fi { padding: 7px 10px; border: 1px solid #e5e7eb; border-radius: 7px; font-size: 0.85rem; outline: none; color: #111; transition: border-color 0.15s; width: 100%; box-sizing: border-box; }
  .mb-fi:focus { border-color: #7c3aed; }
  .mb-fta { padding: 7px 10px; border: 1px solid #e5e7eb; border-radius: 7px; font-size: 0.85rem; outline: none; color: #111; resize: vertical; min-height: 72px; font-family: inherit; width: 100%; box-sizing: border-box; }
  .mb-fta:focus { border-color: #7c3aed; }
`;

export default function ManageBooks() {
  const [books, setBooks] = useState([]);
  const [genres, setGenres] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterGenre, setFilterGenre] = useState('');
  const [filterAvailable, setFilterAvailable] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingBook, setEditingBook] = useState(null);
  const [form, setForm] = useState(emptyBook);
  const [saving, setSaving] = useState(false);

  const fetchBooks = async () => {
    setLoading(true);
    try {
      const payload = {
        page, size: 10,
        searchTerm: searchTerm.trim() || null,
        genreId: filterGenre ? parseInt(filterGenre) : null,
        availableOnly: filterAvailable,
        activeOnly: false
      };
      const res = await advancedSearchBooks(payload);
      setBooks(res.data?.content || []);
      setTotalPages(res.data?.totalPages || 0);
    } catch { toast.error('Failed to load books'); }
    finally { setLoading(false); }
  };

  const fetchGenres = async () => {
    try { const res = await getAllGenres(); setGenres(res.data || []); } catch { }
  };

  useEffect(() => { fetchBooks(); fetchGenres(); }, [page]);

  const handleSearch = (e) => { e.preventDefault(); setPage(0); fetchBooks(); };
  const clearFilters = () => { setSearchTerm(''); setFilterGenre(''); setFilterAvailable(false); setPage(0); };
  const openCreateModal = () => { setEditingBook(null); setForm(emptyBook); setModalOpen(true); };

  const openEditModal = (book) => {
    setEditingBook(book);
    setForm({
      isbn: book.isbn || '', title: book.title || '', author: book.author || '',
      genreId: book.genreId || '', publisher: book.publisher || '', language: book.language || '',
      pages: book.pages || '', description: book.description || '',
      totalCopies: book.totalCopies || 1, availableCopies: book.availableCopies || 0,
      coverImageUrl: book.coverImageUrl || '', price: book.price || ''
    });
    setModalOpen(true);
  };

  const handleSave = async () => {
    if (!form.title || !form.isbn || !form.author || !form.genreId) {
      toast.error('Please fill required fields'); return;
    }
    setSaving(true);
    try {
      const payload = { ...form, genreId: parseInt(form.genreId), price: form.price ? parseFloat(form.price) : null };
      if (editingBook) { await updateBook(editingBook.id, payload); toast.success('Book updated!'); }
      else { await createBook(payload); toast.success('Book created!'); }
      setModalOpen(false); fetchBooks();
    } catch (err) { toast.error(err.response?.data?.message || 'Save failed'); }
    finally { setSaving(false); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this book?')) return;
    try { await deleteBook(id); toast.success('Book deleted'); fetchBooks(); }
    catch { toast.error('Delete failed'); }
  };

  const handleToggleActive = async (book) => {
    if (!confirm(`${book.active ? 'Deactivate' : 'Activate'} this book?`)) return;
    try {
      const payload = { ...book, active: !book.active, genreId: book.genreId ? parseInt(book.genreId) : null, price: book.price ? parseFloat(book.price) : null };
      await updateBook(book.id, payload);
      toast.success(`Book ${payload.active ? 'activated' : 'deactivated'}!`);
      fetchBooks();
    } catch { toast.error('Failed to change status'); }
  };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  return (
    <div className="mb-page">
      <style>{styles}</style>

      {/* Header */}
      <div className="mb-topbar">
        <div>
          <h1 className="mb-heading">Manage <span>Books</span></h1>
          <p className="mb-subheading">Add, edit, and manage the book catalog</p>
        </div>
        <button className="mb-btn-add" onClick={openCreateModal}>
          <IoAddOutline size={15} /> Add Book
        </button>
      </div>

      {/* Filters */}
      <form className="mb-filters" onSubmit={handleSearch}>
        <div className="mb-search-wrap">
          <IoSearchOutline className="mb-search-icon" />
          <input type="text" className="mb-search-input" placeholder="Search title, author, ISBN..."
            value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
        </div>
        <select className="mb-select" value={filterGenre} onChange={(e) => setFilterGenre(e.target.value)}>
          <option value="">All Genres</option>
          {genres.map(g => <option key={g.id} value={g.id}>{g.name}</option>)}
        </select>
        <label className="mb-check-label">
          <input type="checkbox" checked={filterAvailable} onChange={(e) => setFilterAvailable(e.target.checked)} />
          In stock only
        </label>
        <button type="submit" className="mb-btn-search">Search</button>
        <button type="button" className="mb-btn-clear" onClick={clearFilters}>
          <IoCloseOutline size={13} /> Clear
        </button>
      </form>

      {/* Book List */}
      <div className="mb-list-wrapper">
      {loading ? (
        <div className="mb-spinner"><div className="mb-spinner-dot" /></div>
      ) : books.length === 0 ? (
        <div className="mb-empty">
          <IoBookOutline size={32} style={{ opacity: 0.3 }} />
          <p>No books found. Try adjusting your filters.</p>
        </div>
      ) : (
        <div className="mb-list">
          {books.map((book) => (
            <div key={book.id} className="mb-row">
              {/* Cover */}
              <div className="mb-cover">
                {book.coverImageUrl
                  ? <img src={book.coverImageUrl} alt={book.title} />
                  : <div className="mb-cover-ph"><IoBookOutline size={18} /></div>
                }
              </div>

              {/* Title / Author / ISBN */}
              <div className="mb-info">
                <p className="mb-title">{book.title}</p>
                <p className="mb-author"><IoPersonOutline size={11} /> {book.author}</p>
                <p className="mb-isbn"># ISBN: {book.isbn}</p>
              </div>

              <div className="mb-divider" />

              {/* Meta */}
              <div className="mb-meta">
                <div className="mb-meta-item">
                  <span className="mb-meta-label">Genre</span>
                  <span className="mb-genre-badge">{book.genreName || '—'}</span>
                </div>
                <div className="mb-meta-item">
                  <span className="mb-meta-label">Copies</span>
                  <span className="mb-meta-val">
                    <span className="mb-copies-avail">{book.availableCopies}</span>
                    <span style={{ color: '#9ca3af', fontWeight: 400 }}> / {book.totalCopies}</span>
                  </span>
                </div>
                <div className="mb-meta-item">
                  <span className="mb-meta-label">Status</span>
                  {book.active
                    ? <span className="mb-status-active">Active</span>
                    : <span className="mb-status-inactive">Inactive</span>
                  }
                </div>
              </div>

              {/* Actions */}
              <div className="mb-actions">
                <button
                  className={`mb-icon-btn ${book.active ? 'tog-on' : 'tog-off'}`}
                  title={book.active ? 'Deactivate' : 'Activate'}
                  onClick={() => handleToggleActive(book)}
                >
                  {book.active ? <IoEyeOutline size={13} /> : <IoEyeOffOutline size={13} />}
                </button>
                <button className="mb-icon-btn edit" title="Edit" onClick={() => openEditModal(book)}>
                  <IoCreateOutline size={13} />
                </button>
                <button className="mb-icon-btn del" title="Delete" onClick={() => handleDelete(book.id)}>
                  <IoTrashOutline size={13} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <div style={{ marginTop: '16px' }}>
        <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
      </div>
      </div>

      {/* Modal */}
      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editingBook ? 'Edit Book' : 'Add New Book'}
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setModalOpen(false)}>Cancel</button>
            <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
              {saving ? 'Saving…' : 'Save Book'}
            </button>
          </>
        }
      >
        <div className="mb-fg"><label className="mb-fl">Title *</label>
          <input name="title" className="mb-fi" value={form.title} onChange={handleChange} placeholder="Book title" />
        </div>
        <div className="mb-form-row">
          <div className="mb-fg"><label className="mb-fl">ISBN *</label>
            <input name="isbn" className="mb-fi" value={form.isbn} onChange={handleChange} placeholder="978-..." />
          </div>
          <div className="mb-fg"><label className="mb-fl">Author *</label>
            <input name="author" className="mb-fi" value={form.author} onChange={handleChange} />
          </div>
        </div>
        <div className="mb-fg"><label className="mb-fl">Genre *</label>
          <select name="genreId" className="mb-fi" value={form.genreId} onChange={handleChange}>
            <option value="">Select a genre</option>
            {genres.map(g => <option key={g.id} value={g.id}>{g.name}</option>)}
          </select>
        </div>
        <div className="mb-form-row">
          <div className="mb-fg"><label className="mb-fl">Total Copies</label>
            <input name="totalCopies" type="number" className="mb-fi" value={form.totalCopies} onChange={handleChange} />
          </div>
          <div className="mb-fg"><label className="mb-fl">Available Copies</label>
            <input name="availableCopies" type="number" className="mb-fi" value={form.availableCopies} onChange={handleChange} />
          </div>
        </div>
        <div className="mb-form-row">
          <div className="mb-fg"><label className="mb-fl">Publisher</label>
            <input name="publisher" className="mb-fi" value={form.publisher} onChange={handleChange} />
          </div>
          <div className="mb-fg"><label className="mb-fl">Language</label>
            <input name="language" className="mb-fi" value={form.language} onChange={handleChange} />
          </div>
        </div>
        <div className="mb-form-row">
          <div className="mb-fg"><label className="mb-fl">Pages</label>
            <input name="pages" type="number" className="mb-fi" value={form.pages} onChange={handleChange} />
          </div>
          <div className="mb-fg"><label className="mb-fl">Price</label>
            <input name="price" type="number" step="0.01" className="mb-fi" value={form.price} onChange={handleChange} placeholder="0.00" />
          </div>
        </div>
        <div className="mb-fg"><label className="mb-fl">Cover Image URL</label>
          <input name="coverImageUrl" className="mb-fi" value={form.coverImageUrl} onChange={handleChange} placeholder="https://..." />
        </div>
        <div className="mb-fg"><label className="mb-fl">Description</label>
          <textarea name="description" className="mb-fta" value={form.description} onChange={handleChange} placeholder="Brief description…" />
        </div>
      </Modal>
    </div>
  );
}