import React from 'react';
import QRDisplay from './QRDisplay.jsx';

const STATE_LABELS = {
  IDLE: '空閒', RESERVED: '已預約', OCCUPIED: '使用中', TEMP_AWAY: '暫離'
};

// Allowed actions per state — mirrors the backend State pattern.
const ALLOWED = {
  IDLE:      ['reserve', 'checkin'],
  RESERVED:  ['checkin', 'release'],
  OCCUPIED:  ['leaveTemp', 'release'],
  TEMP_AWAY: ['comeBack', 'release']
};

const ACTION_META = {
  reserve:   { label: '預約',           variant: 'default' },
  checkin:   { label: '掃 QR Check-in', variant: 'primary' },
  leaveTemp: { label: '暫時離開',       variant: 'default' },
  comeBack:  { label: '回來',           variant: 'primary' },
  release:   { label: '釋放',           variant: 'danger'  }
};

export default function ControlPanel({
  seat,
  qrOpenForSeatId,
  onAction,
  onConfirmCheckIn,
  onCancelCheckIn,
  busy
}) {
  if (!seat) {
    return (
      <div className="card">
        <p style={{ margin: 0, fontSize: 13, color: 'var(--text-secondary)' }}>
          點選一個座位以查看可用操作
        </p>
      </div>
    );
  }

  const allowed = ALLOWED[seat.state] || [];
  const showQR = qrOpenForSeatId === seat.id;

  return (
    <div className="card">
      <div className="control-panel-row">
        <div>
          <p style={{ margin: 0, fontSize: 12, color: 'var(--text-secondary)' }}>已選座位</p>
          <p style={{ margin: '2px 0 0', fontSize: 15, fontWeight: 500 }}>
            {seat.id} · {STATE_LABELS[seat.state]}
            {seat.currentUserId ? ` · 使用者 ${seat.currentUserId}` : ''}
            {seat.reservationOwner ? ` · 預約者 ${seat.reservationOwner}` : ''}
          </p>
        </div>
        <div className="actions">
          {allowed.map((a) => {
            const meta = ACTION_META[a];
            const cls =
              meta.variant === 'primary' ? 'btn btn-primary' :
              meta.variant === 'danger'  ? 'btn btn-danger'  :
              'btn';
            return (
              <button
                key={a}
                className={cls}
                disabled={busy}
                onClick={() => onAction(a)}
              >
                {meta.label}
              </button>
            );
          })}
        </div>
      </div>

      {showQR && (
        <div className="qr-zone">
          <div className="qr-card">
            <QRDisplay seed={seat.id} />
          </div>
          <div className="qr-info">
            <h3>掃描座位 {seat.id} 的 QR Code</h3>
            <p>
              系統會驗證您的 JWT,並透過 SeatState.checkIn() 將狀態切換為「使用中」。
              若該座位已被他人預約,後端會回傳 409 Conflict。
            </p>
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn btn-primary" disabled={busy} onClick={onConfirmCheckIn}>
                模擬掃描成功
              </button>
              <button className="btn" disabled={busy} onClick={onCancelCheckIn}>
                取消
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
