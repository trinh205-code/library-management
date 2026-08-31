export default function StatusBadge({ status }) {
  const statusConfig = {
    CHECKED_OUT: { label: 'Checked Out', className: 'badge-info' },
    RETURNED: { label: 'Returned', className: 'badge-success' },
    OVERDUE: { label: 'Overdue', className: 'badge-danger' },
    LOST: { label: 'Lost', className: 'badge-danger' },
    DAMEGED: { label: 'Damaged', className: 'badge-warning' },
    PENDING: { label: 'Pending', className: 'badge-warning' },
    READY: { label: 'Ready', className: 'badge-success' },
    USER_CONFIRMED: { label: 'Confirmed', className: 'badge-info' },
    BORROWED: { label: 'Borrowed', className: 'badge-success' },
    REJECTED: { label: 'Rejected', className: 'badge-danger' },
    CANCELLED: { label: 'Cancelled', className: 'badge-neutral' },
    EXPIRED: { label: 'Expired', className: 'badge-neutral' },
    PAID: { label: 'Paid', className: 'badge-success' },
    PARTIALLY_PAID: { label: 'Partial', className: 'badge-warning' },
    WAIVED: { label: 'Waived', className: 'badge-info' },
  };

  const config = statusConfig[status] || { label: status, className: 'badge-neutral' };

  return (
    <span className={`badge ${config.className}`}>
      {config.label}
    </span>
  );
}
