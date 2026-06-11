import { Outlet, NavLink } from 'react-router-dom'
import { Lock, Home, TrendingUp, CalendarCheck, Settings } from 'lucide-react'

export default function Layout() {
  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `flex items-center gap-2 px-4 py-2 rounded transition-all duration-200 text-sm tracking-widest uppercase ${
      isActive
        ? 'text-escape-gold border-b-2 border-escape-gold'
        : 'text-gray-400 hover:text-escape-gold'
    }`

  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b border-escape-border bg-escape-card/80 backdrop-blur sticky top-0 z-50">
        <div className="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
          <NavLink to="/" className="flex items-center gap-3">
            <Lock className="text-escape-red animate-pulse_red" size={28} />
            <span className="font-cinzel font-black text-xl text-white tracking-widest">
              ESCAPE ROOM
            </span>
          </NavLink>
          <nav className="flex items-center gap-2">
            <NavLink to="/" end className={linkClass}>
              <Home size={16} />
              <span className="hidden sm:inline">홈</span>
            </NavLink>
            <NavLink to="/themes" className={linkClass}>
              <TrendingUp size={16} />
              <span className="hidden sm:inline">인기 테마</span>
            </NavLink>
            <NavLink to="/reservations" className={linkClass}>
              <CalendarCheck size={16} />
              <span className="hidden sm:inline">예약 내역</span>
            </NavLink>
            <NavLink to="/admin" className={linkClass}>
              <Settings size={16} />
              <span className="hidden sm:inline">관리</span>
            </NavLink>
          </nav>
        </div>
      </header>

      <main className="flex-1 max-w-6xl w-full mx-auto px-6 py-10">
        <Outlet />
      </main>

      <footer className="border-t border-escape-border py-6 text-center text-gray-600 text-xs tracking-widest">
        © 2024 ESCAPE ROOM — 당신의 탈출을 기다립니다
      </footer>
    </div>
  )
}
