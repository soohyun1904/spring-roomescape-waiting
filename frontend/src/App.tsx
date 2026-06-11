import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import HomePage from './pages/HomePage'
import ThemesPage from './pages/ThemesPage'
import ThemeDetailPage from './pages/ThemeDetailPage'
import ReservationsPage from './pages/ReservationsPage'
import ReservationDetailPage from './pages/ReservationDetailPage'
import AdminPage from './pages/AdminPage'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/themes" element={<ThemesPage />} />
          <Route path="/themes/:id" element={<ThemeDetailPage />} />
          <Route path="/reservations" element={<ReservationsPage />} />
          <Route path="/reservations/:id" element={<ReservationDetailPage />} />
          <Route path="/admin" element={<AdminPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
