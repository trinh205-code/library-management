import { IoBookOutline } from 'react-icons/io5';
import { useNavigate } from 'react-router-dom';

export default function BookCard({ book, actions }) {
  const navigate = useNavigate();
  const isAvailable = book.availableCopies > 0;

  return (
    <div className="book-card" onClick={() => navigate(`/books/${book.id}`)}>
      <div className="book-card-image">
        {book.coverImageUrl ? (
          <img src={book.coverImageUrl} alt={book.title} />
        ) : (
          <IoBookOutline className="book-placeholder-icon" />
        )}
        <span className={`book-card-availability ${isAvailable ? 'available' : 'unavailable'}`}>
          {isAvailable ? 'Available' : 'Unavailable'}
        </span>
      </div>
      <div className="book-card-body">
        <h3 className="book-card-title">{book.title}</h3>
        <p className="book-card-author">by {book.author}</p>
        {book.genreName && (
          <span className="book-card-genre">{book.genreName}</span>
        )}
        {(book.averageRating > 0 || book.totalRatings > 0) && (
          <div className="book-card-rating" style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.85rem', color: '#64748b', marginTop: '4px' }}>
            <span style={{ color: '#f59e0b' }}>★</span>
            <span style={{ fontWeight: '600', color: '#334155' }}>{book.averageRating?.toFixed(1) || '0.0'}</span>
            <span>({book.totalRatings})</span>
          </div>
        )}
        <div className="book-card-footer">
          <span className="book-card-copies">
            {book.availableCopies}/{book.totalCopies} copies
          </span>
          {(book.borrowCount > 0) && (
            <span className="book-card-borrows" style={{ fontSize: '0.75rem', color: '#64748b', marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: '4px' }}>
               <IoBookOutline size={12} /> {book.borrowCount} reads
            </span>
          )}
          {actions && (
            <div className="book-card-actions" onClick={e => e.stopPropagation()}>
              {actions}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
