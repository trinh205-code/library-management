import React, { Suspense } from 'react';
import {
  Link,
  createBrowserRouter,
  isRouteErrorResponse,
  RouterProvider,
  useRouteError,
  Navigate,
} from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import { Toaster } from 'react-hot-toast';
import './index.css';
import './components/components.css';

// Layouts
const UserLayout = React.lazy(() => import('./layouts/user/UserLayout'));
const AdminLayout = React.lazy(() => import('./layouts/admin/AdminLayout'));

// Auth
const LoginPage = React.lazy(() => import('./pages/auth/login'));
const RegisterPage = React.lazy(() => import('./pages/auth/register'));

// User Pages
const BrowseBooksPage = React.lazy(() => import('./pages/user/browseBooks'));
const BookDetailPage = React.lazy(() => import('./pages/user/bookDetail'));
const MyLoansPage = React.lazy(() => import('./pages/user/myLoans'));
const MyReservationsPage = React.lazy(() => import('./pages/user/myReservations'));
const MyFinesPage = React.lazy(() => import('./pages/user/myFines'));
const WishlistPage = React.lazy(() => import('./pages/user/wishlist'));
const ProfilePage = React.lazy(() => import('./pages/user/profile'));
const SubscriptionsPage = React.lazy(() => import('./pages/user/subscriptions'));

// Error Page
const ErrorPage = React.lazy(() => import('./pages/error/ErrorPage'));

// Admin Pages
const AdminDashboardPage = React.lazy(() => import('./pages/admin/dashboard'));
const ManageBooksPage = React.lazy(() => import('./pages/admin/manageBooks'));
const ManageGenresPage = React.lazy(() => import('./pages/admin/manageGenres'));
const ManageLoansPage = React.lazy(() => import('./pages/admin/manageLoans'));
const ManageFinesPage = React.lazy(() => import('./pages/admin/manageFines'));
const ManageUsersPage = React.lazy(() => import('./pages/admin/manageUsers'));
const ManageReservationsPage = React.lazy(() => import('./pages/admin/manageReservations'));
const PaymentLedgerPage = React.lazy(() => import('./pages/admin/paymentLedger'));

// User Payment Pages
const UserPaymentHistoryPage = React.lazy(() => import('./pages/user/paymentHistory'));

// Protected Route
import ProtectedRoute from './routes/ProtectedRoute';

const Loading = () => (
  <div className="spinner-container">
    <div className="spinner"></div>
  </div>
);

const router = createBrowserRouter([
  {
    path: '/',
    errorElement: <Suspense fallback={<Loading />}><ErrorPage /></Suspense>,
    children: [
      // Auth routes (no layout)
      {
        path: 'login',
        element: <Suspense fallback={<Loading />}><LoginPage /></Suspense>,
      },
      {
        path: 'register',
        element: <Suspense fallback={<Loading />}><RegisterPage /></Suspense>,
      },

      // User routes
      {
        path: '',
        element: (
          <ProtectedRoute>
            <Suspense fallback={<Loading />}>
              <UserLayout />
            </Suspense>
          </ProtectedRoute>
        ),
        children: [
          { index: true, element: <Navigate to="/books" replace /> },
          { path: 'books', element: <Suspense fallback={<Loading />}><BrowseBooksPage /></Suspense> },
          { path: 'books/:id', element: <Suspense fallback={<Loading />}><BookDetailPage /></Suspense> },
          { path: 'my-loans', element: <Suspense fallback={<Loading />}><MyLoansPage /></Suspense> },
          { path: 'my-reservations', element: <Suspense fallback={<Loading />}><MyReservationsPage /></Suspense> },
          { path: 'my-fines', element: <Suspense fallback={<Loading />}><MyFinesPage /></Suspense> },
          { path: 'wishlist', element: <Suspense fallback={<Loading />}><WishlistPage /></Suspense> },
          { path: 'profile', element: <Suspense fallback={<Loading />}><ProfilePage /></Suspense> },
          { path: 'subscriptions', element: <Suspense fallback={<Loading />}><SubscriptionsPage /></Suspense> },
          { path: 'my-payments', element: <Suspense fallback={<Loading />}><UserPaymentHistoryPage /></Suspense> },
        ],
      },

      // Admin routes
      {
        path: 'admin',
        element: (
          <ProtectedRoute adminOnly>
            <Suspense fallback={<Loading />}>
              <AdminLayout />
            </Suspense>
          </ProtectedRoute>
        ),
        children: [
          { index: true, element: <Suspense fallback={<Loading />}><AdminDashboardPage /></Suspense> },
          { path: 'books', element: <Suspense fallback={<Loading />}><ManageBooksPage /></Suspense> },
          { path: 'genres', element: <Suspense fallback={<Loading />}><ManageGenresPage /></Suspense> },
          { path: 'loans', element: <Suspense fallback={<Loading />}><ManageLoansPage /></Suspense> },
          { path: 'fines', element: <Suspense fallback={<Loading />}><ManageFinesPage /></Suspense> },
          { path: 'users', element: <Suspense fallback={<Loading />}><ManageUsersPage /></Suspense> },
          { path: 'reservations', element: <Suspense fallback={<Loading />}><ManageReservationsPage /></Suspense> },
          { path: 'payments', element: <Suspense fallback={<Loading />}><PaymentLedgerPage /></Suspense> },
        ],
      },
      
      // Catch-all 404
      {
        path: '*',
        element: <Suspense fallback={<Loading />}><ErrorPage /></Suspense>,
      },
    ],
  },
]);

function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <RouterProvider router={router} />
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 3000,
            style: {
              background: 'var(--bg-card)',
              color: 'var(--text-primary)',
              borderRadius: '10px',
              padding: '12px 16px',
              border: '1px solid var(--border-color)',
              fontSize: '0.875rem',
            },
            success: { iconTheme: { primary: '#00B894', secondary: '#fff' } },
            error: { iconTheme: { primary: '#E74C3C', secondary: '#fff' } },
          }}
        />
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
