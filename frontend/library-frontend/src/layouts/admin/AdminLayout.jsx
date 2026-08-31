import { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from '../../components/Sidebar';
import Navbar from '../../components/Navbar';

const pageTitles = {
  '/admin': 'Admin Dashboard',
  '/admin/books': 'Manage Books',
  '/admin/genres': 'Manage Genres',
  '/admin/loans': 'Manage Loans',
  '/admin/reservations': 'Manage Reservations',
  '/admin/fines': 'Manage Fines',
  '/admin/users': 'Manage Users',
};

export default function AdminLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();

  const title = pageTitles[location.pathname] || 'Admin';

  return (
    <div className="app-layout">
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <main className="app-main">
        <Navbar title={title} onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} />
        <div className="app-content">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
