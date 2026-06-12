import React, { useMemo } from 'react';

/**
 * Renders a deterministic faux-QR for a seat ID.
 * Real QR encoding is unnecessary for a demo — we just need something
 * that looks scannable and is reproducible per seat.
 */
export default function QRDisplay({ seed, size = 110 }) {
  const cells = useMemo(() => buildCells(seed), [seed]);
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 21 21"
      shapeRendering="crispEdges"
      xmlns="http://www.w3.org/2000/svg"
      role="img"
      aria-label={`QR code for seat ${seed}`}
    >
      {cells}
    </svg>
  );
}

function buildCells(seed) {
  let h = 0;
  for (let i = 0; i < seed.length; i++) h = (h * 31 + seed.charCodeAt(i)) >>> 0;
  const rand = () => { h = (h * 1664525 + 1013904223) >>> 0; return h / 0xffffffff; };

  const size = 21;
  const out = [];
  for (let y = 0; y < size; y++) {
    for (let x = 0; x < size; x++) {
      const corner =
        (x < 7 && y < 7) ||
        (x >= size - 7 && y < 7) ||
        (x < 7 && y >= size - 7);
      let on;
      if (corner) {
        const cx = x < 7 ? x : x - (size - 7);
        const cy = y < 7 ? y : y - (size - 7);
        on = (cx === 0 || cx === 6 || cy === 0 || cy === 6) ||
             (cx >= 2 && cx <= 4 && cy >= 2 && cy <= 4);
      } else {
        on = rand() > 0.5;
      }
      if (on) {
        out.push(<rect key={`${x}-${y}`} x={x} y={y} width="1" height="1" fill="#042c53" />);
      }
    }
  }
  return out;
}
