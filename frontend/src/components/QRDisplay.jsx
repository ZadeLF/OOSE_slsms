import React from 'react';
import { QRCodeSVG } from 'qrcode.react';

/**
 * Renders a real, scannable QR code that encodes a check-in identifier
 * for the given seat (e.g. "SLSMS:CHECKIN:A1"). Scanning it with any QR
 * reader yields this same string — the "模擬掃描成功" button in the demo
 * simply skips the camera step and submits this identifier directly.
 */
export default function QRDisplay({ seed, size = 110 }) {
  const value = `SLSMS:CHECKIN:${seed}`;
  return (
    <QRCodeSVG
      value={value}
      size={size}
      role="img"
      aria-label={`QR code for seat ${seed}`}
    />
  );
}
