export interface ReservationTime {
  id: number;
  startAt: string;
}

export interface Theme {
  id: number;
  name: string;
  description: string;
  thumbnailUrl: string;
}

export interface Reservation {
  id: number;
  name: string;
  date: string;
  state: string;
  rank: number | null;
  time: ReservationTime;
  theme: Theme;
}
