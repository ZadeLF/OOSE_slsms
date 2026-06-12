import React, { useEffect, useState } from 'react';
import { api } from '../api/slsmsApi.js';

const ZONE_ID = 'A';
const MIN_DB = 20;
const MAX_DB = 110;

export default function NoiseSimulator({ onAlertChange }) {
  const [noise, setNoise] = useState(42);
  const [threshold, setThreshold] = useState(65);
  const [alertActive, setAlertActive] = useState(false);
  const [signageMessage, setSignageMessage] = useState('');
  const [busy, setBusy] = useState(false);

  // Tell the parent when alert state flips so the global banner can pulse.
  useEffect(() => { onAlertChange?.(alertActive); }, [alertActive, onAlertChange]);

  const sync = async (dB) => {
    setBusy(true);
    try {
      const res = await api.pushNoise(ZONE_ID, dB);
      setThreshold(res.threshold);
      setAlertActive(res.alertActive);
      const sig = await api.getSignage(ZONE_ID);
      setSignageMessage(sig.message || '');
    } catch (err) {
      console.error('noise push failed', err);
    } finally {
      setBusy(false);
    }
  };

  const adjust = (delta) => {
    const next = Math.max(MIN_DB, Math.min(MAX_DB, noise + delta));
    setNoise(next);
    sync(next);
  };

  const reset = () => {
    setNoise(42);
    sync(42);
  };

  const pct = Math.round(((noise - MIN_DB) / (MAX_DB - MIN_DB)) * 100);
  const color =
    noise > threshold ? '#e24b4a' :
    noise > threshold - 10 ? '#ef9f27' :
    '#1d9e75';

  return (
    <div className="card">
      <div className="control-panel-row" style={{ marginBottom: 10 }}>
        <div>
          <p style={{ margin: 0, fontSize: 14, fontWeight: 500 }}>A 區噪音感測器</p>
          <p style={{ margin: '2px 0 0', fontSize: 12, color: 'var(--text-secondary)' }}>
            Observer pattern · 門檻 {threshold} dB
          </p>
        </div>
        <p style={{ margin: 0, fontSize: 22, fontWeight: 500, color }}>
          {Math.round(noise)} dB
        </p>
      </div>

      <div className="noise-bar-track">
        <div className="noise-bar-fill" style={{ width: `${pct}%`, background: color }} />
      </div>

      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        <button className="btn" disabled={busy} onClick={() => adjust(-5)}>−5 dB</button>
        <button className="btn" disabled={busy} onClick={() => adjust(5)}>+5 dB</button>
        <button className="btn" disabled={busy} onClick={() => adjust(20)}>+20 dB (突發)</button>
        <button className="btn" disabled={busy} onClick={reset}>重置</button>
      </div>

      {alertActive && (
        <div className="alert-banner pulsing">
          <strong>⚠ Observer 觸發 ·</strong>{' '}
          A 區噪音超標,後端已推送通知至管理員 + 數位看板
          {signageMessage ? `:「${signageMessage}」` : ''}
        </div>
      )}
    </div>
  );
}
