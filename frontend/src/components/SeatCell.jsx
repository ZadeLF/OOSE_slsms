import React from 'react';

const STATE_LABELS = {
  IDLE: '空閒',
  RESERVED: '已預約',
  OCCUPIED: '使用中',
  TEMP_AWAY: '暫離'
};

export default function SeatCell({ seat, selected, onSelect }) {
  return (
    <div
      className={`seat-cell s-${seat.state} ${selected ? 'selected' : ''}`}
      onClick={() => onSelect(seat.id)}
      role="button"
      tabIndex={0}
      aria-label={`座位 ${seat.id} 狀態 ${STATE_LABELS[seat.state]}`}
      onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') onSelect(seat.id); }}
    >
      <span className="label">{seat.id}</span>
      <span className="state">{STATE_LABELS[seat.state]}</span>
    </div>
  );
}
