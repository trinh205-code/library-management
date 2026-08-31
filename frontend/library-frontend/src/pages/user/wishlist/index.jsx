import { useState, useEffect } from 'react';
import { getWishlists, removeFromWishlist } from '../../../api/wishlistApi';
import BookCard from '../../../components/BookCard';
import Pagination from '../../../components/Pagination';
import toast from 'react-hot-toast';
import { IoHeartOutline, IoTrashOutline } from 'react-icons/io5';

export default function Wishlist() {
  const [wishlists, setWishlists] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => { fetchWishlists(); }, [page]);

  const fetchWishlists = async () => {
    setLoading(true);
    try {
      const res = await getWishlists({ page, size: 12 });
      setWishlists(res.data?.content || []);
      setTotalPages(res.data?.totalPages || 0);
    } catch { toast.error('Failed to load wishlist'); }
    finally { setLoading(false); }
  };

  const handleRemove = async (bookId) => {
    try {
      await removeFromWishlist(bookId);
      toast.success('Removed from wishlist');
      fetchWishlists();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to remove');
    }
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>My <span className="text-gradient">Wishlist</span></h1>
        <p>Books you've saved for later</p>
      </div>

      {loading ? (
        <div className="spinner-container"><div className="spinner"></div></div>
      ) : wishlists.length > 0 ? (
        <>
          <div className="content-grid">
            {wishlists.map((w) => (
              <BookCard
                key={w.id}
                book={w.book}
                actions={
                  <button
                    className="btn btn-danger btn-sm"
                    onClick={() => handleRemove(w.book.id)}
                    title="Remove from wishlist"
                  >
                    <IoTrashOutline />
                  </button>
                }
              />
            ))}
          </div>
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      ) : (
        <div className="empty-state">
          <div className="empty-state-icon"><IoHeartOutline /></div>
          <h3 className="empty-state-title">Wishlist is Empty</h3>
          <p className="empty-state-text">Browse books and add your favorites here</p>
        </div>
      )}
    </div>
  );
}
