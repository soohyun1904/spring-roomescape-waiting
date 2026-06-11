import type { Reservation, ReservationTime, Theme } from '../types';

const BASE = 'http://localhost:8080';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    let message = text || `HTTP ${res.status}`;
    try { const json = JSON.parse(text); message = json.detail ?? json.message ?? message; } catch {}
    throw new Error(message);
  }
  const text = await res.text();
  if (!text) return undefined as T;
  return JSON.parse(text);
}

export const api = {
  themes: {
    list: () => request<{ themes: Theme[] }>('/themes').then(res => res.themes),
    get: (id: number) => request<Theme>(`/themes/${id}`),
    famous: (params?: { days?: number; limit?: number; date?: string }) => {
      const q = new URLSearchParams();
      if (params?.days) q.set('days', String(params.days));
      if (params?.limit) q.set('limit', String(params.limit));
      if (params?.date) q.set('date', params.date);
      return request<{ themes: Theme[] }>(`/themes/famous?${q}`).then(res => res.themes);
    },
    create: (body: Omit<Theme, 'id'>) =>
      request<Theme>('/admin/themes', { method: 'POST', body: JSON.stringify(body) }),
    delete: (id: number) => request<void>(`/admin/themes/${id}`, { method: 'DELETE' }),
  },

  times: {
    list: () => request<{ times: ReservationTime[] }>('/times').then(res => res.times),
    available: (date: string, themeId: number) =>
      request<{ times: ReservationTime[] }>(`/times/available?date=${date}&themeId=${themeId}`).then(res => res.times),
    create: (startAt: string) =>
      request<ReservationTime>('/admin/times', { method: 'POST', body: JSON.stringify({ startAt }) }),
    delete: (id: number) => request<void>(`/admin/times/${id}`, { method: 'DELETE' }),
  },

  reservations: {
    list: (name?: string) => {
      const path = name ? `/reservations?name=${encodeURIComponent(name)}` : '/reservations'
      return request<{ reservations: Reservation[] }>(path).then(res => res.reservations)
    },
    get: (id: number) => request<Reservation>(`/reservations/${id}`),
    create: (body: { name: string; themeId: number; date: string; timeId: number }) =>
      request<Reservation>('/reservations', { method: 'POST', body: JSON.stringify(body) }),
    update: (id: number, body: { name: string; date: string; timeId: number; themeId: number }) =>
      request<Reservation>(`/reservations/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
    delete: (id: number, name: string) =>
      request<void>(`/reservations/${id}?name=${encodeURIComponent(name)}`, { method: 'DELETE' }),
  },
};
