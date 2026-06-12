import React, { useEffect, useState } from 'react';
import { api } from '../api/slsmsApi.js';

const STATE_LABELS = {
  IDLE: '空閒',
  RESERVED: '已預約',
  OCCUPIED: '使用中',
  TEMP_AWAY: '暫離'
};

function SeatUsageTable({ report }) {
  return (
    <>
      <p style={{ margin: '0 0 8px', fontSize: 13 }}>
        總座位數 {report.totalSeats} · 整體使用率{' '}
        <strong>{(report.occupancyRate * 100).toFixed(1)}%</strong>
      </p>
      <table className="report-table">
        <thead>
          <tr>
            <th>分區</th>
            <th>座位數</th>
            {Object.keys(report.countByState).map((s) => (
              <th key={s}>{STATE_LABELS[s] || s}</th>
            ))}
            <th>使用率</th>
          </tr>
        </thead>
        <tbody>
          {report.byZone.map((z) => (
            <tr key={z.zoneId}>
              <td>{z.zoneId}</td>
              <td>{z.totalSeats}</td>
              {Object.keys(report.countByState).map((s) => (
                <td key={s}>{z.countByState[s] ?? 0}</td>
              ))}
              <td>{(z.occupancyRate * 100).toFixed(1)}%</td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
}

function EnvironmentAlertTable({ report }) {
  return (
    <>
      <p style={{ margin: '0 0 8px', fontSize: 13 }}>
        近期警示總數 <strong>{report.totalRecentAlerts}</strong>
      </p>
      <table className="report-table">
        <thead>
          <tr>
            <th>分區</th>
            <th>噪音 (dB)</th>
            <th>噪音門檻</th>
            <th>噪音警示</th>
            <th>溫度 (°C)</th>
            <th>溫度範圍</th>
            <th>溫度警示</th>
          </tr>
        </thead>
        <tbody>
          {report.byZone.map((z) => (
            <tr key={z.zoneId}>
              <td>{z.zoneName} ({z.zoneId})</td>
              <td>{z.latestNoiseDb.toFixed(1)}</td>
              <td>{z.noiseThreshold.toFixed(1)}</td>
              <td>{z.recentNoiseAlerts} 次{z.noiseAlertActive ? ' ⚠' : ''}</td>
              <td>{z.latestTemperature.toFixed(1)}</td>
              <td>{z.temperatureLowerBound.toFixed(1)}–{z.temperatureUpperBound.toFixed(1)}</td>
              <td>{z.recentTemperatureAlerts} 次{z.temperatureAlertActive ? ' ⚠' : ''}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
}

export default function ReportPanel() {
  const [types, setTypes] = useState([]);
  const [selectedType, setSelectedType] = useState('');
  const [report, setReport] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    api.listReportTypes()
      .then((list) => {
        setTypes(list);
        if (list.length) setSelectedType(list[0].type);
      })
      .catch(() => {});
  }, []);

  useEffect(() => {
    if (!selectedType) return;
    let cancelled = false;
    api.getReport(selectedType)
      .then((r) => { if (!cancelled) { setReport(r); setError(null); } })
      .catch((err) => { if (!cancelled) setError(err.body?.message || err.message); });
    return () => { cancelled = true; };
  }, [selectedType]);

  const refresh = () => {
    if (!selectedType) return;
    api.getReport(selectedType)
      .then((r) => { setReport(r); setError(null); })
      .catch((err) => setError(err.body?.message || err.message));
  };

  return (
    <div className="card">
      <div className="control-panel-row" style={{ marginBottom: 10 }}>
        <div>
          <p style={{ margin: 0, fontSize: 14, fontWeight: 500 }}>使用率報表</p>
          <p style={{ margin: '2px 0 0', fontSize: 12, color: 'var(--text-secondary)' }}>
            Strategy pattern · 可擴充多種報表
          </p>
        </div>
        <div style={{ display: 'flex', gap: 8 }}>
          <select className="btn" value={selectedType} onChange={(e) => setSelectedType(e.target.value)}>
            {types.map((t) => (
              <option key={t.type} value={t.type}>{t.title}</option>
            ))}
          </select>
          <button className="btn" onClick={refresh}>重新整理</button>
        </div>
      </div>

      {error && (
        <div className="alert-banner">{error}</div>
      )}

      {report?.type === 'seat-usage' && <SeatUsageTable report={report} />}
      {report?.type === 'environment-alerts' && <EnvironmentAlertTable report={report} />}
    </div>
  );
}
