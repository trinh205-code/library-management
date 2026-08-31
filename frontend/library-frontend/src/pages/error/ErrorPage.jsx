import React from 'react';
import { useRouteError, Link, useNavigate } from 'react-router-dom';
import { IoAlertCircleOutline, IoArrowBackOutline, IoHomeOutline } from 'react-icons/io5';

export default function ErrorPage() {
  const error = useRouteError();
  const navigate = useNavigate();
  
  // Determine if it's a 404 or a general error
  const is404 = error?.status === 404 || !error;

  return (
    <div className="error-page-container">
      <div className="error-card">
        <div className="error-icon">
          <IoAlertCircleOutline />
        </div>
        
        <h1 className="error-title">
          {is404 ? '404 - Page Not Found' : 'Oops! Something went wrong'}
        </h1>
        
        <p className="error-message">
          {is404 
            ? "The page you are looking for doesn't exist or has been moved."
            : (error?.message || error?.statusText || "An unexpected error occurred. Please try again later.")}
        </p>
        <div className="error-actions">
          <button onClick={() => navigate(-1)} className="btn btn-secondary">
            <IoArrowBackOutline /> Go Back
          </button>
          <Link to="/" className="btn btn-primary">
            <IoHomeOutline /> Back to Dashboard
          </Link>
        </div>
      </div>
    </div>
  );
}
