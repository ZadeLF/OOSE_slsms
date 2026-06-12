import React, { useMemo } from 'react';
import SeatCell from './SeatCell.jsx';

export default function FloorMap({ seats, selectedId, onSelect }) {
  // Sort seats by row then col so the grid renders in reading order.
  const ordered = useMemo(
    () => [...seats].sort((a, b) =>
      (a.gridRow - b.gridRow) || (a.gridCol - b.gridCol)
    ),
    [seats]
  );

  return (
    <div className="floor-map-wrap">
      <p className="map-title">座位地圖 (點選座位 → 在下方面板進行操作)</p>
      <div className="floor-map">
        {ordered.map((seat) => (
          <SeatCell
            key={seat.id}
            seat={seat}
            selected={seat.id === selectedId}
            onSelect={onSelect}
          />
        ))}
      </div>
    </div>
  );
}
