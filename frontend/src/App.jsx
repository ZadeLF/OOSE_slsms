import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { api } from './api/slsmsApi.js';
import FloorMap from './components/FloorMap.jsx';
import ControlPanel from './components/ControlPanel.jsx';
import NoiseSimulator from './components/NoiseSimulator.jsx';
import RecentAlerts from './components/RecentAlerts.jsx';

const FLOOR_ID = '1';
const POLL_MS = 2000; // re-poll seats every 2 s — meets FR-01 (≤2 s update)
const FAKE_USER_ID = 'demo-user-001';

export default function App() {
  const [seats, setSeats] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [qrOpenForSeatId, setQrOpenForSeatId] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);
  const [connected, setConnected] = useState(false);
  const [alertTick, setAlertTick] = useState(0);

  // Poll seats periodically to reflect timeouts and admin actions.
  useEffect(() => {
    let cancelled = false;
    const refresh = async () => {
      try {
        const list = await api.listSeatsByFloor(FLOOR_ID);
        if (cancelled) return;
        setSeats(list);
        setConnected(true);
        setError(null);
      } catch (err) {
        if (cancelled) return;
        setConnected(false);
        setError('無法連線到後端 (http://localhost:8080) — 請確認 Spring Boot 已啟動');
      }
    };
    refresh();
    const id = setInterval(refresh, POLL_MS);
    return () => { cancelled = true; clearInterval(id); };
  }, []);

  const selected = useMemo(
    () => seats.find((s) => s.id === selectedId) || null,
    [seats, selectedId]
  );

  const counts = useMemo(() => {
    const c = { IDLE: 0, RESERVED: 0, OCCUPIED: 0, TEMP_AWAY: 0 };
    seats.forEach((s) => { c[s.state] = (c[s.state] || 0) + 1; });
    return c;
  }, [seats]);

  const refreshSeat = useCallback(async (seatId) => {
    const fresh = await api.getSeat(seatId);
    setSeats((prev) => prev.map((s) => (s.id === seatId ? fresh : s)));
  }, []);

  const handleAction = useCallback(async (action) => {
    if (!selected) return;
    if (action === 'checkin') {
      // Show the QR overlay; actual API call happens on confirmation.
      setQrOpenForSeatId(selected.id);
      return;
    }
    setBusy(true);
    setError(null);
    try {
      let updated;
      switch (action) {
        case 'reserve':   updated = await api.reserve(selected.id, FAKE_USER_ID);   break;
        case 'leaveTemp': updated = await api.leaveTemp(selected.id, FAKE_USER_ID); break;
        case 'comeBack':  updated = await api.comeBack(selected.id, FAKE_USER_ID);  break;
        case 'release':   updated = await api.release(selected.id, FAKE_USER_ID);   break;
        default: return;
      }
      setSeats((prev) => prev.map((s) => (s.id === updated.id ? updated : s)));
    } catch (err) {
      setError(err.body?.message || err.message);
    } finally {
      setBusy(false);
    }
  }, [selected]);

  const confirmCheckIn = useCallback(async () => {
    if (!selected) return;
    setBusy(true);
    try {
      const updated = await api.checkIn(selected.id, FAKE_USER_ID);
      setSeats((prev) => prev.map((s) => (s.id === updated.id ? updated : s)));
      setQrOpenForSeatId(null);
    } catch (err) {
      setError(err.body?.message || err.message);
    } finally {
      setBusy(false);
    }
  }, [selected]);

  return (
    <div className="app-shell">
      <div className="page-header">
        <div>
          <h1>智慧圖書館 · 1F 閱覽 A 區</h1>
          <p>Smart Library Space Management System · State + Observer pattern demo</p>
        </div>
        <div style={{ textAlign: 'right' }}>
          <span className={`status-badge ${connected ? '' : 'disconnected'}`}>
            {connected ? '● 後端已連線' : '○ 後端未連線'}
          </span>
          <div className="legend" style={{ marginTop: 8 }}>
            <span className="legend-dot" style={{ background: '#1d9e75' }} />空閒
            <span className="legend-dot" style={{ background: '#ef9f27', marginLeft: 8 }} />已預約
            <span className="legend-dot" style={{ background: '#d85a30', marginLeft: 8 }} />使用中
            <span className="legend-dot" style={{ background: '#7f77dd', marginLeft: 8 }} />暫離
          </div>
        </div>
      </div>

      <div className="stats">
        <div className="stat-card"><p className="label">空閒</p><p className="value">{counts.IDLE}</p></div>
        <div className="stat-card"><p className="label">已預約</p><p className="value">{counts.RESERVED}</p></div>
        <div className="stat-card"><p className="label">使用中</p><p className="value">{counts.OCCUPIED}</p></div>
        <div className="stat-card"><p className="label">暫離</p><p className="value">{counts.TEMP_AWAY}</p></div>
      </div>

      <FloorMap seats={seats} selectedId={selectedId} onSelect={(id) => {
        setSelectedId(id);
        setQrOpenForSeatId(null);
        setError(null);
      }} />

      {error && (
        <div className="card" style={{ borderColor: '#e24b4a', color: '#791f1f', background: '#fcebeb' }}>
          {error}
        </div>
      )}

      <ControlPanel
        seat={selected}
        qrOpenForSeatId={qrOpenForSeatId}
        onAction={handleAction}
        onConfirmCheckIn={confirmCheckIn}
        onCancelCheckIn={() => setQrOpenForSeatId(null)}
        busy={busy}
      />

      <NoiseSimulator onAlertChange={() => setAlertTick((t) => t + 1)} />

      <RecentAlerts tick={alertTick} />
    </div>
  );
}
