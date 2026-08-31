import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getBookById } from '../../../api/bookApi';
import { checkoutBook } from '../../../api/bookLoanApi';
import { createReservation } from '../../../api/reservationApi';
import { addToWishlist } from '../../../api/wishlistApi';
import { getReviewsByBook, createReview } from '../../../api/reviewApi';
import Modal from '../../../components/Modal';
import StatusBadge from '../../../components/StatusBadge';
import toast from 'react-hot-toast';
import {
  IoBookOutline, IoArrowBackOutline, IoHeartOutline,
  IoCalendarOutline, IoStarOutline, IoStar, IoPersonOutline,
  IoLayersOutline, IoLanguageOutline, IoPricetagOutline, IoCashOutline
} from 'react-icons/io5';
import './bookDetail.css';

export default function BookDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [book, setBook] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [reviewModalOpen, setReviewModalOpen] = useState(false);
  const [reviewForm, setReviewForm] = useState({ rating: 5, title: '', reviewText: '' });
  const [submittingReview, setSubmittingReview] = useState(false);

  useEffect(() => {
    fetchBook();
    fetchReviews();
  }, [id]);

  const fetchBook = async () => {
    try {
      const res = await getBookById(id);
      setBook(res.data);
    } catch {
      toast.error('Book not found');
      navigate('/books');
    } finally {
      setLoading(false);
    }
  };

  const fetchReviews = async () => {
    try {
      const res = await getReviewsByBook(id, { page: 0, size: 10 });
      setReviews(res.data?.content || []);
    } catch { /* ignore */ }
  };

  const handleSubmitReview = async () => {
    if (!reviewForm.reviewText) { toast.error("Please enter a review text"); return; }
    setSubmittingReview(true);
    try {
      await createReview({ bookId: parseInt(id), ...reviewForm });
      toast.success("Review submitted!");
      setReviewModalOpen(false);
      setReviewForm({ rating: 5, title: '', reviewText: '' });
      fetchReviews();
      fetchBook(); // To reload averageRating
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to submit review");
    } finally {
      setSubmittingReview(false);
    }
  };

  const handleBorrow = async () => {
    setActionLoading(true);
    try {
      await checkoutBook({ bookId: parseInt(id), notes: '', checkoutDays: 14 });
      toast.success('Book borrowed successfully!');
      fetchBook();
    } catch (err) {
      const errorData = err.response?.data;
      const msg = errorData?.message || errorData?.error || JSON.stringify(errorData) || 'Failed to borrow';
      toast.error(msg, { duration: 6000 });
      console.error("Checkout Error Payload:", errorData);
    } finally {
      setActionLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    if (amount === 0 || amount === null || amount === undefined) return 'Free';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  const handleReserve = async () => {
    setActionLoading(true);
    try {
      await createReservation({ bookId: parseInt(id) });
      toast.success('Reservation placed!');
      fetchBook();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to reserve');
    } finally {
      setActionLoading(false);
    }
  };

  const handleWishlist = async () => {
    try {
      await addToWishlist(parseInt(id));
      toast.success('Added to wishlist!');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to add to wishlist');
    }
  };

  const renderStars = (rating) => {
    return Array.from({ length: 5 }, (_, i) =>
      i < rating ? <IoStar key={i} className="star-filled" /> : <IoStarOutline key={i} className="star-empty" />
    );
  };

  if (loading) {
    return <div className="spinner-container"><div className="spinner"></div></div>;
  }

  if (!book) return null;

  const isAvailable = book.availableCopies > 0;

  return (
    <div className="page-container">
      <button className="btn btn-ghost" onClick={() => navigate('/books')} style={{ marginBottom: '20px' }}>
        <IoArrowBackOutline /> Back to Books
      </button>

      <div className="book-detail">
        <div className="book-detail-cover">
          {book.coverImageUrl ? (
            <img src={book.coverImageUrl} alt={book.title} />
          ) : (
            <div className="book-detail-placeholder">
              <IoBookOutline />
            </div>
          )}
        </div>

        <div className="book-detail-info">
          <div className="book-detail-header">
            {book.genreName && <span className="book-card-genre">{book.genreName}</span>}
            <h1>{book.title}</h1>
            <p className="book-detail-author">by <strong>{book.author}</strong></p>
            {(book.averageRating > 0 || book.totalRatings > 0) && (
              <div style={{ display: 'flex', alignItems: 'center', gap: '5px', marginTop: '10px' }}>
                <IoStar style={{ color: '#f59e0b', fontSize: '1.2rem' }} />
                <strong style={{ fontSize: '1.1rem' }}>{book.averageRating?.toFixed(1) || '0.0'}</strong>
                <span className="text-muted">({book.totalRatings} reviews)</span>
              </div>
            )}
          </div>

          <div className="book-detail-meta">
            {book.price !== undefined && book.price !== null && (
              <div className="meta-item">
                <IoCashOutline />
                <span style={{ fontWeight: 'bold' }}>{formatCurrency(book.price)}</span>
              </div>
            )}
            {book.isbn && (
              <div className="meta-item">
                <IoPricetagOutline />
                <span>ISBN: {book.isbn}</span>
              </div>
            )}
            {book.publisher && (
              <div className="meta-item">
                <IoPersonOutline />
                <span>{book.publisher}</span>
              </div>
            )}
            {book.pages && (
              <div className="meta-item">
                <IoLayersOutline />
                <span>{book.pages} pages</span>
              </div>
            )}
            {book.language && (
              <div className="meta-item">
                <IoLanguageOutline />
                <span>{book.language}</span>
              </div>
            )}
          </div>

          <div className="book-detail-availability">
            <div className={`availability-indicator ${isAvailable ? 'available' : 'unavailable'}`}>
              {isAvailable ? '● Available' : '● Unavailable'}
            </div>
            <span className="copies-text">{book.availableCopies} of {book.totalCopies} copies available</span>
          </div>

          {book.description && (
            <div className="book-detail-description">
              <h3>Description</h3>
              <p>{book.description}</p>
            </div>
          )}

          <div className="book-detail-actions">
            {isAvailable && !book.alreadyHaveLoan && (
              <button className="btn btn-primary btn-lg" onClick={handleBorrow} disabled={actionLoading}>
                <IoBookOutline /> Borrow Book
              </button>
            )}
            {!isAvailable && !book.alreadyHaveReservation && (
              <button className="btn btn-warning btn-lg" onClick={handleReserve} disabled={actionLoading}>
                <IoCalendarOutline /> Reserve
              </button>
            )}
            <button className="btn btn-secondary btn-lg" onClick={handleWishlist}>
              <IoHeartOutline /> Add to Wishlist
            </button>
          </div>
        </div>
      </div>

      {/* Reviews Section */}
      <div className="card" style={{ marginTop: '28px' }}>
        <div className="card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3 className="card-title">Reviews</h3>
          <button className="btn btn-outline" onClick={() => setReviewModalOpen(true)}>Write a Review</button>
        </div>

        {reviews.length > 0 ? (
          <div className="reviews-list">
            {reviews.map((review) => (
              <div key={review.id} className="review-item">
                <div className="review-header">
                  <div className="review-user">{review.userFullName || 'Anonymous'}</div>
                  <div className="review-stars">{renderStars(review.rating)}</div>
                </div>
                {review.title && <h4 className="review-title">{review.title}</h4>}
                <p className="review-text">{review.reviewText}</p>
                <span className="review-date">
                  {new Date(review.createdAt).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}
                </span>
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-state" style={{ padding: '30px' }}>
            <p className="text-muted">No reviews yet. Be the first to review!</p>
          </div>
        )}
      </div>

      <Modal isOpen={reviewModalOpen} onClose={() => setReviewModalOpen(false)} title="Write a Review"
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setReviewModalOpen(false)}>Cancel</button>
            <button className="btn btn-primary" onClick={handleSubmitReview} disabled={submittingReview}>
              {submittingReview ? 'Submitting...' : 'Submit Review'}
            </button>
          </>
        }>
        <div className="form-group">
          <label className="form-label">Rating</label>
          <select className="form-select" value={reviewForm.rating} onChange={e => setReviewForm({ ...reviewForm, rating: parseInt(e.target.value) })}>
            <option value={5}>5 Stars - Excellent</option>
            <option value={4}>4 Stars - Good</option>
            <option value={3}>3 Stars - Average</option>
            <option value={2}>2 Stars - Poor</option>
            <option value={1}>1 Star - Terrible</option>
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Title (Optional)</label>
          <input className="form-input" value={reviewForm.title} onChange={e => setReviewForm({ ...reviewForm, title: e.target.value })} placeholder="Give your review a title" />
        </div>
        <div className="form-group">
          <label className="form-label">Review *</label>
          <textarea className="form-input" rows={4} value={reviewForm.reviewText} onChange={e => setReviewForm({ ...reviewForm, reviewText: e.target.value })} placeholder="What did you think of the book?" />
        </div>
      </Modal>
    </div>
  );
}
