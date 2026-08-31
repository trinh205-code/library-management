import { useState, useEffect } from 'react';
import { searchBooks, getTrendingBooks, getRandomBooks, getMostReadBooks } from '../../../api/bookApi';
import { getAllGenres } from '../../../api/genreApi';
import BookCard from '../../../components/BookCard';
import Pagination from '../../../components/Pagination';
import { IoSearchOutline, IoGridOutline, IoListOutline, IoFunnelOutline, IoArrowForwardOutline, IoChevronBackOutline, IoChevronForwardOutline } from 'react-icons/io5';
import './browseBooks.css';

export default function BrowseBooks() {
  const [books, setBooks] = useState([]);
  const [genres, setGenres] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [viewMode, setViewMode] = useState('grid');
  const [filters, setFilters] = useState({
    genreId: '',
    availableOnly: false,
    sortBy: 'createdAt',
    sortDirection: 'DESC',
  });
  const [searchTerm, setSearchTerm] = useState('');

  // Book Carousel States
  const [trendingBooks, setTrendingBooks] = useState([]);
  const [topRatedBooks, setTopRatedBooks] = useState([]);
  const [newArrivals, setNewArrivals] = useState([]);
  const [randomBooks, setRandomBooks] = useState([]);
  const [carouselPages, setCarouselPages] = useState({
    trending: 0,
    topRated: 0,
    newArrivals: 0,
    mostRead: 0
  });

  useEffect(() => {
    fetchGenres();
    fetchCarouselData();
  }, []);

  const fetchCarouselData = async () => {
    try {
      const [trendingRes, topRatedRes, newArrivalsRes, mostReadRes] = await Promise.allSettled([
        getTrendingBooks(24),
        searchBooks({ page: 0, size: 24, sortBy: 'averageRating', sortDirection: 'DESC' }),
        searchBooks({ page: 0, size: 24, sortBy: 'createdAt', sortDirection: 'DESC' }),
        getMostReadBooks(24),
      ]);

      setTrendingBooks(trendingRes.status === 'fulfilled' ? trendingRes.value.data : []);
      setTopRatedBooks(topRatedRes.status === 'fulfilled' ? topRatedRes.value.data.content : []);
      setNewArrivals(newArrivalsRes.status === 'fulfilled' ? newArrivalsRes.value.data.content : []);
      setRandomBooks(mostReadRes.status === 'fulfilled' ? mostReadRes.value.data : []);
    } catch (err) {
      console.error('Carousel fetch error:', err);
    }
  };

  const handleViewMore = (type) => {
    // Scroll down to the search / browse area
    const browseSection = document.querySelector('.browse-toolbar');
    if (browseSection) {
      browseSection.scrollIntoView({ behavior: 'smooth' });
    }
    
    // Auto apply filter if applicable
    if (type === 'topRated') {
      setFilters({ ...filters, sortBy: 'averageRating', sortDirection: 'DESC' });
      setPage(0);
    } else if (type === 'newArrivals') {
      setFilters({ ...filters, sortBy: 'createdAt', sortDirection: 'DESC' });
      setPage(0);
    }
  };

  const getSectionStyle = (type) => {
    const baseStyle = { padding: '28px 24px', borderRadius: '24px', marginBottom: '32px', boxShadow: '0 4px 15px rgba(0,0,0,0.03)' };
    switch(type) {
      case 'trending':
        return { ...baseStyle, backgroundColor: 'var(--bg-card)', backgroundImage: 'linear-gradient(to bottom right, rgba(255, 99, 71, 0.08), rgba(255, 99, 71, 0.01))', border: '1px solid rgba(255, 99, 71, 0.15)' };
      case 'topRated':
        return { ...baseStyle, backgroundColor: 'var(--bg-card)', backgroundImage: 'linear-gradient(to bottom right, rgba(255, 184, 0, 0.08), rgba(255, 184, 0, 0.01))', border: '1px solid rgba(255, 184, 0, 0.15)' };
      case 'newArrivals':
        return { ...baseStyle, backgroundColor: 'var(--bg-card)', backgroundImage: 'linear-gradient(to bottom right, rgba(156, 39, 176, 0.08), rgba(156, 39, 176, 0.01))', border: '1px solid rgba(156, 39, 176, 0.15)' };
      case 'mostRead':
        return { ...baseStyle, backgroundColor: 'var(--bg-card)', backgroundImage: 'linear-gradient(to bottom right, rgba(0, 200, 150, 0.08), rgba(0, 200, 150, 0.01))', border: '1px solid rgba(0, 200, 150, 0.15)' };
      default:
        return { ...baseStyle, backgroundColor: 'var(--bg-card)', border: '1px solid var(--border-light)' };
    }
  };

  const renderCarousel = (title, items, isNew = false, type = null) => {
    if (!items || items.length === 0) return null;
    
    const currentPage = carouselPages[type] || 0;
    const itemsPerPage = 6;
    const maxPages = Math.ceil(items.length / itemsPerPage);
    const displayedItems = items.slice(currentPage * itemsPerPage, (currentPage + 1) * itemsPerPage);

    return (
      <div className="book-carousel-section" style={getSectionStyle(type)}>
        <div className="book-carousel-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
          <h3 className="book-carousel-title" style={{ margin: 0 }}>
            {title} {isNew && <span className="badge badge-purple" style={{ marginLeft: '8px' }}>NEW</span>}
          </h3>
          <button 
            className="btn btn-sm btn-outline"
            onClick={() => handleViewMore(type)}
            style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '50%', width: '36px', height: '36px', padding: 0 }}
            title="Xem thêm"
          >
            <IoArrowForwardOutline size={18} />
          </button>
        </div>
        <div style={{ display: 'flex', justifyContent: 'center', width: '100%' }}>
          <div style={{ position: 'relative', width: 'fit-content', maxWidth: '100%' }}>
            {maxPages > 1 && (
            <button 
              className="btn btn-icon" 
              onClick={() => setCarouselPages(prev => ({ ...prev, [type]: (prev[type] - 1 + maxPages) % maxPages }))}
              style={{ position: 'absolute', left: '-15px', top: 'calc(50% - 10px)', transform: 'translateY(-50%)', zIndex: 10, width: '40px', height: '40px', padding: 0, borderRadius: '50%', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-white)', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', color: 'var(--text-primary)', boxShadow: '0 4px 10px rgba(0,0,0,0.1)' }}
              title="Previous"
            >
              <IoChevronBackOutline size={20} />
            </button>
          )}

          <div className="book-carousel">
            {displayedItems.map((book) => (
              <BookCard key={book.id} book={book} />
            ))}
          </div>

          {maxPages > 1 && (
            <button 
              className="btn btn-icon" 
              onClick={() => setCarouselPages(prev => ({ ...prev, [type]: (prev[type] + 1) % maxPages }))}
              style={{ position: 'absolute', right: '-15px', top: 'calc(50% - 10px)', transform: 'translateY(-50%)', zIndex: 10, width: '40px', height: '40px', padding: 0, borderRadius: '50%', border: '1px solid var(--border-color)', backgroundColor: 'var(--bg-white)', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', color: 'var(--text-primary)', boxShadow: '0 4px 10px rgba(0,0,0,0.1)' }}
              title="Next"
            >
              <IoChevronForwardOutline size={20} />
            </button>
          )}
          </div>
        </div>
      </div>
    );
  };

  useEffect(() => {
    fetchBooks();
  }, [page, filters]);

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    // If page is already 0, fetchBooks won't be triggered by useEffect, so we call it manually
    if (page === 0) {
      fetchBooks();
    }
  };

  const fetchGenres = async () => {
    try {
      const res = await getAllGenres();
      setGenres(res.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchBooks = async () => {
    setLoading(true);
    try {
      const params = {
        page,
        size: 12,
        sortBy: filters.sortBy,
        sortDirection: filters.sortDirection,
        ...(searchTerm.trim() && { searchTerm: searchTerm.trim() }),
        ...(filters.genreId && { genreId: filters.genreId }),
        ...(filters.availableOnly && { availableOnly: true }),
      };
      const res = await searchBooks(params);
      setBooks(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
      setTotalElements(res.data.totalElements || 0);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (key, value) => {
    setFilters({ ...filters, [key]: value });
    setPage(0);
  };

  return (
    <div className="page-container">
      <div className="page-header">
        <h1>Browse <span className="text-gradient">Books</span></h1>
        <p>Discover your next favorite read from our collection</p>
      </div>

      <div className="discovery-section">
        {renderCarousel("🔥 Trending Books", trendingBooks, false, 'trending')}
        {renderCarousel("⭐ Highly Rated", topRatedBooks, false, 'topRated')}
        {renderCarousel("🆕 New Arrivals", newArrivals, true, 'newArrivals')}
        {renderCarousel("📚 Most Read Books", randomBooks, false, 'mostRead')}
      </div>

      <div className="browse-toolbar" style={{ marginTop: '30px' }}>
        <form className="browse-search" onSubmit={handleSearch} style={{ display: 'flex', flex: '1', maxWidth: '500px' }}>
          <div style={{ position: 'relative', flex: '1', display: 'flex', alignItems: 'center' }}>
            <IoSearchOutline className="search-icon" style={{ position: 'absolute', left: '15px' }} />
            <input
              type="text"
              placeholder="Search books by title, author..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{ width: '100%', paddingLeft: '40px', borderTopRightRadius: '0', borderBottomRightRadius: '0' }}
            />
          </div>
          <button type="submit" className="btn btn-primary" style={{ borderTopLeftRadius: '0', borderBottomLeftRadius: '0' }}>
            Search
          </button>
        </form>

        <div className="browse-filters">
          <select
            className="form-select"
            value={filters.genreId}
            onChange={(e) => handleFilterChange('genreId', e.target.value)}
            style={{ width: '180px' }}
          >
            <option value="">All Genres</option>
            {genres.map((g) => (
              <option key={g.id} value={g.id}>{g.name}</option>
            ))}
          </select>

          <select
            className="form-select"
            value={filters.sortBy}
            onChange={(e) => handleFilterChange('sortBy', e.target.value)}
            style={{ width: '160px' }}
          >
            <option value="createdAt">Newest First</option>
            <option value="averageRating">Highly Rated</option>
            <option value="title">Title A-Z</option>
            <option value="author">Author A-Z</option>
          </select>

          <label className="checkbox-label" style={{ whiteSpace: 'nowrap' }}>
            <input
              type="checkbox"
              checked={filters.availableOnly}
              onChange={(e) => handleFilterChange('availableOnly', e.target.checked)}
            />
            <span>Available Only</span>
          </label>

          <div className="view-toggle">
            <button
              className={`btn-icon ${viewMode === 'grid' ? 'active' : ''}`}
              onClick={() => setViewMode('grid')}
              title="Grid view"
            >
              <IoGridOutline />
            </button>
            <button
              className={`btn-icon ${viewMode === 'list' ? 'active' : ''}`}
              onClick={() => setViewMode('list')}
              title="List view"
            >
              <IoListOutline />
            </button>
          </div>
        </div>
      </div>

      <p className="browse-count">
        Showing <strong>{books.length}</strong> of <strong>{totalElements}</strong> books
      </p>

      {loading ? (
        <div className="spinner-container"><div className="spinner"></div></div>
      ) : books.length > 0 ? (
        <>
          <div className={viewMode === 'grid' ? 'content-grid' : 'content-list'}>
            {books.map((book) => (
              <BookCard key={book.id} book={book} />
            ))}
          </div>
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      ) : (
        <div className="empty-state">
          <div className="empty-state-icon"><IoSearchOutline /></div>
          <h3 className="empty-state-title">No Books Found</h3>
          <p className="empty-state-text">Try adjusting your filters or search terms</p>
        </div>
      )}
    </div>
  );
}
