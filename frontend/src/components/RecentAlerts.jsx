import React, { useEffect, useState } from 'react';
import { api } from '../api/slsmsApi.js';

export default function RecentAlerts({ tick }) {
  const [alerts, setAlerts] = useState([]);

  useEffect(() => {
    let cancelled = false;
    api.recentAlerts().then((list) => { if (!cancelled) setAlerts(list); }).catch(() => {});
    return () => { cancelled = true; };
  }, [tick]);

  if (!alerts.length) return null;

  return (
    <div className="card">
      <p style={{ margin: 0, fontSize: 14, fontWeight: 500 }}>近期警示 ({alerts.length})</p>
      <ul className="alerts-list">
        {alerts.slice(0, 8).map((a) => (
          <li key={a.id}>
            <span className="ts">
              {new Date(a.occurredAt).toLocaleTimeString('zh-TW', { hour12: false })}
            </span>
            {a.message}
          </li>
        ))}
      </ul>
    </div>
  );
}
