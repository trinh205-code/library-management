export default function StatCard({ icon, label, value, subText, color = 'purple' }) {
  const colorMap = {
    purple: { bg: 'var(--accent-purple-bg)', color: 'var(--accent-purple)' },
    green: { bg: 'var(--accent-green-bg)', color: 'var(--accent-green)' },
    orange: { bg: 'var(--accent-orange-bg)', color: 'var(--accent-orange)' },
    red: { bg: 'var(--accent-red-bg)', color: 'var(--accent-red)' },
    blue: { bg: 'var(--accent-blue-bg)', color: 'var(--accent-blue)' },
    pink: { bg: 'var(--accent-pink-bg)', color: 'var(--accent-pink)' },
  };

  const c = colorMap[color] || colorMap.purple;

  return (
    <div className="stat-card">
      <div className="stat-card-icon" style={{ background: c.bg, color: c.color }}>
        {icon}
      </div>
      <div className="stat-card-content">
        <div className="stat-card-label">{label}</div>
        <div className="stat-card-value" style={{ color: c.color }}>{value}</div>
        {subText && <div className="stat-card-sub">{subText}</div>}
      </div>
    </div>
  );
}
