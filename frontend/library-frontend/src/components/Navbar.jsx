import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { IoSearchOutline, IoNotificationsOutline, IoMenuOutline, IoMoonOutline, IoSunnyOutline } from 'react-icons/io5';

export default function Navbar({ title, onToggleSidebar }) {
  const { user } = useAuth();
  const { theme, toggleTheme } = useTheme();

  const getInitials = (name) => {
    if (!name) return 'U';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
  };

  return (
    <header className="navbar">
      <div className="navbar-left">
        <button className="mobile-toggle" onClick={onToggleSidebar}>
          <IoMenuOutline />
        </button>
        <h1 className="navbar-title">{title || 'Dashboard'}</h1>
      </div>

      <div className="navbar-right">
        <div className="navbar-search">
          <IoSearchOutline className="search-icon" />
          <input type="text" placeholder="Search..." />
        </div>

        <button className="navbar-icon-btn" onClick={toggleTheme}>
          {theme === 'dark' ? <IoSunnyOutline /> : <IoMoonOutline />}
        </button>

        <button className="navbar-icon-btn">
          <IoNotificationsOutline />
          <span className="notification-dot"></span>
        </button>

        <div className="navbar-user-menu">
          <div className="navbar-user-info">
            <div className="navbar-user-name">{user?.fullName || 'User'}</div>
            <div className="navbar-user-role">
              {user?.role === 'ROLE_ADMIN' ? 'Admin' : 'Member'}
            </div>
          </div>
          <div className="navbar-avatar">
            {getInitials(user?.fullName)}
          </div>
        </div>
      </div>
    </header>
  );
}
