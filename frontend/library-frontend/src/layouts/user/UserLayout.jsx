import { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from '../../components/Sidebar';
import Navbar from '../../components/Navbar';

const pageTitles = {
  '/': 'Dashboard',
  '/books': 'Browse Books',
  '/my-loans': 'My Loans',
  '/my-reservations': 'My Reservations',
  '/wishlist': 'Wishlist',
  '/my-fines': 'My Fines',
  '/profile': 'Profile',
  '/settings': 'Settings',
};

export default function UserLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();

  const getTitle = () => {
    if (location.pathname.startsWith('/books/')) return 'Book Details';
    return pageTitles[location.pathname] || 'UTCBook';
  };

  return (
    <div className="app-layout">
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <main className="app-main">
        <Navbar title={getTitle()} onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} />
        <div className="app-content">
          <Outlet />
        </div>
      </main>
    </div>
  );
}