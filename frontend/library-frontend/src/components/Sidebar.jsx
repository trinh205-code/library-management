import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  IoLibrary, IoHome, IoBookOutline, IoReceiptOutline,
  IoCalendarOutline, IoAlertCircleOutline, IoHeartOutline,
  IoPersonOutline, IoSettingsOutline, IoLogOutOutline,
  IoGridOutline, IoPeopleOutline, IoBookmarksOutline,
  IoStatsChartOutline, IoCashOutline, IoMenuOutline,
  IoDesktopOutline, IoChevronBackOutline, IoChevronForwardOutline
} from 'react-icons/io5';

export default function Sidebar({ isOpen, onClose }) {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const [isCollapsed, setIsCollapsed] = React.useState(false);

  React.useEffect(() => {
    if (isCollapsed) {
      document.body.classList.add('sidebar-collapsed');
    } else {
      document.body.classList.remove('sidebar-collapsed');
    }
    return () => document.body.classList.remove('sidebar-collapsed');
  }, [isCollapsed]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const userLinks = [
    { to: '/books', icon: <IoBookOutline />, label: 'Browse Books' },
    { to: '/my-loans', icon: <IoCalendarOutline />, label: 'My Loans' },
    { to: '/my-reservations', icon: <IoCalendarOutline />, label: 'My Reservations' },
    { to: '/my-fines', icon: <IoReceiptOutline />, label: 'My Fines' },
    { to: '/my-payments', icon: <IoCashOutline />, label: 'Payment History' },
    { to: '/subscriptions', icon: <IoDesktopOutline />, label: 'Subscriptions' },
    { to: '/wishlist', icon: <IoHeartOutline />, label: 'Wishlist' },
  ];

  const adminLinks = [
    { to: '/admin', icon: <IoGridOutline />, label: 'Dashboard' },
    { to: '/admin/books', icon: <IoBookOutline />, label: 'Manage Books' },
    { to: '/admin/genres', icon: <IoBookmarksOutline />, label: 'Manage Genres' },
    { to: '/admin/loans', icon: <IoCalendarOutline />, label: 'Manage Loans' },
    { to: '/admin/reservations', icon: <IoCalendarOutline />, label: 'Reservations' },
    { to: '/admin/fines', icon: <IoReceiptOutline />, label: 'Manage Fines' },
    { to: '/admin/payments', icon: <IoCashOutline />, label: 'Payments' },
    { to: '/admin/users', icon: <IoPeopleOutline />, label: 'Manage Users' },
  ];

  const navLinks = isAdmin ? adminLinks : userLinks;

  return (
    <>
      <div className={`sidebar-overlay ${isOpen ? 'open' : ''}`} onClick={onClose} />
      <aside className={`sidebar ${isOpen ? 'open' : ''}`}>
        <button 
          className="sidebar-collapse-btn" 
          onClick={() => setIsCollapsed(!isCollapsed)}
          title={isCollapsed ? "Expand sidebar" : "Collapse sidebar"}
        >
          {isCollapsed ? <IoChevronForwardOutline /> : <IoChevronBackOutline />}
        </button>

        <div className="sidebar-brand">
          <div className="sidebar-brand-icon">
            <IoLibrary />
          </div>
          <div className="sidebar-brand-text">
            <h2>UTCBook</h2>
            <span>Library Hub</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          {navLinks.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.to === '/' || link.to === '/admin'}
              className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
              onClick={onClose}
            >
              <span className="sidebar-link-icon">{link.icon}</span>
              <span className="sidebar-link-text">{link.label}</span>
            </NavLink>
          ))}

          <NavLink
            to="/profile"
            className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
            onClick={onClose}
          >
            <span className="sidebar-link-icon"><IoPersonOutline /></span>
            <span className="sidebar-link-text">Profile</span>
          </NavLink>
        </nav>

        <div className="sidebar-footer">
          <div className="sidebar-logout-container">
            <button className="sidebar-logout" onClick={handleLogout}>
              <span className="sidebar-link-icon"><IoLogOutOutline /></span>
              <span className="sidebar-link-text">Logout</span>
            </button>
          </div>
        </div>
      </aside>
    </>
  );
}
