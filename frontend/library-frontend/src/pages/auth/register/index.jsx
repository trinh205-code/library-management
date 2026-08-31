import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { signup } from '../../../api/authApi';
import toast from 'react-hot-toast';
import { IoLibrary, IoMailOutline, IoLockClosedOutline, IoPersonOutline, IoCallOutline, IoEyeOutline, IoEyeOffOutline } from 'react-icons/io5';
import './register.css';

export default function Register() {
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    phone: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    if (errors[e.target.name]) {
      setErrors({ ...errors, [e.target.name]: '' });
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!form.fullName.trim()) newErrors.fullName = 'Full name is required';
    if (!form.email.trim()) newErrors.email = 'Email is required';
    else if (!/\S+@\S+\.\S+/.test(form.email)) newErrors.email = 'Invalid email format';
    if (!form.password) newErrors.password = 'Password is required';
    else if (form.password.length < 6) newErrors.password = 'Password must be at least 6 characters';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    setLoading(true);
    try {
      await signup(form);
      toast.success('Account created successfully!');
      navigate('/login');
    } catch (error) {
      const msg = error.response?.data?.message || 'Registration failed';
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="register-page">
      <div className="register-left">
        <div className="register-left-content">
          <div className="login-brand">
            <div className="login-brand-icon"><IoLibrary /></div>
            <h1>UTCBook</h1>
          </div>
          <h2>Start Your<br />Reading Journey</h2>
          <p>Join our community of book lovers. Access thousands of books, track your reading habits, and discover new favorites.</p>
          <div className="login-features">
            <div className="login-feature">
              <span className="login-feature-dot"></span>
              Free membership access
            </div>
            <div className="login-feature">
              <span className="login-feature-dot"></span>
              Borrow up to 5 books at once
            </div>
            <div className="login-feature">
              <span className="login-feature-dot"></span>
              Personalized recommendations
            </div>
          </div>
        </div>
      </div>

      <div className="register-right">
        <div className="register-form-container">
          <div className="login-form-header">
            <h2>Create Account</h2>
            <p>Fill in your details to get started</p>
          </div>

          <form onSubmit={handleSubmit} className="login-form">
            <div className="form-group">
              <label className="form-label">Full Name</label>
              <div className="input-with-icon">
                <IoPersonOutline className="input-icon" />
                <input
                  type="text"
                  name="fullName"
                  className={`form-input ${errors.fullName ? 'error' : ''}`}
                  placeholder="John Doe"
                  value={form.fullName}
                  onChange={handleChange}
                />
              </div>
              {errors.fullName && <span className="form-error">{errors.fullName}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">Email Address</label>
              <div className="input-with-icon">
                <IoMailOutline className="input-icon" />
                <input
                  type="email"
                  name="email"
                  className={`form-input ${errors.email ? 'error' : ''}`}
                  placeholder="you@example.com"
                  value={form.email}
                  onChange={handleChange}
                />
              </div>
              {errors.email && <span className="form-error">{errors.email}</span>}
            </div>

            <div className="form-group">
              <label className="form-label">Phone (Optional)</label>
              <div className="input-with-icon">
                <IoCallOutline className="input-icon" />
                <input
                  type="tel"
                  name="phone"
                  className="form-input"
                  placeholder="+84 xxx xxx xxx"
                  value={form.phone}
                  onChange={handleChange}
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <div className="input-with-icon">
                <IoLockClosedOutline className="input-icon" />
                <input
                  type={showPassword ? 'text' : 'password'}
                  name="password"
                  className={`form-input ${errors.password ? 'error' : ''}`}
                  placeholder="Min 6 characters"
                  value={form.password}
                  onChange={handleChange}
                  autoComplete="new-password"
                />
                <button type="button" className="input-toggle" onClick={() => setShowPassword(!showPassword)}>
                  {showPassword ? <IoEyeOffOutline /> : <IoEyeOutline />}
                </button>
              </div>
              {errors.password && <span className="form-error">{errors.password}</span>}
            </div>

            <button type="submit" className="btn btn-primary btn-lg w-full" disabled={loading} style={{ marginTop: '8px' }}>
              {loading ? 'Creating Account...' : 'Create Account'}
            </button>

            <p className="login-register-text" style={{ marginTop: '20px' }}>
              Already have an account? <Link to="/login" className="register-link">Sign In</Link>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
}
