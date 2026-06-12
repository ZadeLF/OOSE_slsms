/**
 * Thin REST client for the SLSMS backend.
 * Vite dev server proxies /api/** to http://localhost:8080.
 */

const json = async (res) => {
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    const err = new Error(body.message || `${res.status} ${res.statusText}`);
    err.status = res.status;
    err.body = body;
    throw err;
  }
  return res.json();
};

const post = (url, body) =>
  fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body || {})
  }).then(json);

const get = (url) => fetch(url).then(json);

export const api = {
  // Seats
  listSeatsByFloor: (floorId) => get(`/api/floors/${floorId}/seats`),
  getSeat: (id) => get(`/api/seats/${id}`),
  reserve: (id, userId) => post(`/api/seats/${id}/reserve`, { userId }),
  checkIn: (id, userId) => post(`/api/seats/${id}/checkin`, { userId }),
  leaveTemp: (id, userId) => post(`/api/seats/${id}/leave-temp`, { userId }),
  comeBack: (id, userId) => post(`/api/seats/${id}/come-back`, { userId }),
  release: (id, userId) => post(`/api/seats/${id}/release`, { userId }),

  // Sensors
  pushNoise: (zoneId, dB) => post('/api/sensors/noise', { zoneId, dB }),
  getNoise: (zoneId) => get(`/api/sensors/noise/${zoneId}`),

  // Alerts
  recentAlerts: () => get('/api/alerts/recent'),
  getSignage: (zoneId) => get(`/api/alerts/signage/${zoneId}`)
};
