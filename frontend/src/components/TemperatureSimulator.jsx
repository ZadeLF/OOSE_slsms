import React, { useEffect, useState } from 'react';
import { api } from '../api/slsmsApi.js';

const ZONE_ID = 'A';
const MIN_C = 5;
const MAX_C = 40;

export default function TemperatureSimulator({ onAlertChange }) {
  const [temperature, setTemperature] = useState(22);
  const [lowerBound, setLowerBound] = useState(18);
  const [upperBound, setUpperBound] = useState(28);
  const [alertActive, setAlertActive] = useState(false);
  const [signageMessage, setSignageMessage] = useState('');
  const [busy, setBusy] = useState(false);

  // Tell the parent when alert state flips so the global banner can pulse.
  useEffect(() => { onAlertChange?.(alertActive); }, [alertActive, onAlertChange]);

  const sync = async (celsius) => {
    setBusy(true);
    try {
      const res = await api.pushTemperature(ZONE_ID, celsius);
      setLowerBound(res.lowerBound);
      setUpperBound(res.upperBound);
      setAlertActive(res.alertActive);
      const sig = await api.getSignage(ZONE_ID);
      setSignageMessage(sig.message || '');
    } catch (err) {
      console.error('temperature push failed', err);
    } finally {
      setBusy(false);
    }
  };

  const adjust = (delta) => {
    const next = Math.max(MIN_C, Math.min(MAX_C, temperature + delta));
    setTemperature(next);
    sync(next);
  };

  const reset = () => {
    setTemperature(22);
    sync(22);
  };

  const pct = Math.round(((temperature - MIN_C) / (MAX_C - MIN_C)) * 100);
  const color =
    temperature > upperBound || temperature < lowerBound ? '#e24b4a' :
    temperature > upperBound - 2 || temperature < lowerBound + 2 ? '#ef9f27' :
    '#1d9e75';

  return (
    <div className="card">
      <div className="control-panel-row" style={{ marginBottom: 10 }}>
        <div>
          <p style={{ margin: 0, fontSize: 14, fontWeight: 500 }}>A 區溫度感測器</p>
          <p style={{ margin: '2px 0 0', fontSize: 12, color: 'var(--text-secondary)' }}>
            Observer pattern · 範圍 {lowerBound}–{upperBound} °C
          </p>
        </div>
        <p style={{ margin: 0, fontSize: 22, fontWeight: 500, color }}>
          {Math.round(temperature)} °C
        </p>
      </div>

      <div className="noise-bar-track">
        <div className="noise-bar-fill" style={{ width: `${pct}%`, background: color }} />
      </div>

      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        <button className="btn" disabled={busy} onClick={() => adjust(-5)}>−5 °C</button>
        <button className="btn" disabled={busy} onClick={() => adjust(5)}>+5 °C</button>
        <button className="btn" disabled={busy} onClick={() => adjust(10)}>+10 °C (過熱)</button>
        <button className="btn" disabled={busy} onClick={() => adjust(-10)}>−10 °C (過冷)</button>
        <button className="btn" disabled={busy} onClick={reset}>重置</button>
      </div>

      {alertActive && (
        <div className="alert-banner pulsing">
          <strong>⚠ Observer 觸發 ·</strong>{' '}
          A 區溫度超出範圍,後端已推送通知至管理員 + 數位看板
          {signageMessage ? `:「${signageMessage}」` : ''}
        </div>
      )}
    </div>
  );
}
