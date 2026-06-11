import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Skull, ChevronRight, Flame } from 'lucide-react'
import { api } from '../api'
import ThemeCard from '../components/ThemeCard'
import type { Theme } from '../types'

export default function HomePage() {
  const [famous, setFamous] = useState<Theme[]>([])
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    api.themes.famous({ limit: 5 })
      .then(setFamous)
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="space-y-16">
      {/* Hero */}
      <section className="text-center py-20 relative">
        <div className="absolute inset-0 flex items-center justify-center opacity-5">
          <Skull size={400} className="text-escape-red" />
        </div>
        <div className="relative z-10 space-y-6">
          <p className="font-cinzel text-escape-red tracking-[0.5em] text-sm uppercase animate-flicker">
            — 당신의 탈출을 도전하라 —
          </p>
          <h1 className="font-cinzel font-black text-5xl sm:text-7xl text-white leading-tight">
            ESCAPE<br />
            <span className="text-escape-gold drop-shadow-[0_0_20px_rgba(255,215,0,0.5)]">
              ROOM
            </span>
          </h1>
          <p className="text-gray-400 max-w-xl mx-auto text-sm sm:text-base leading-relaxed">
            공포, 미스터리, 모험이 기다리는 방탈출 체험.
            <br />
            당신은 과연 탈출할 수 있을까?
          </p>
          <div className="flex gap-4 justify-center pt-4">
            <button className="btn-primary flex items-center gap-2" onClick={() => navigate('/themes')}>
              테마 둘러보기 <ChevronRight size={18} />
            </button>
            <button className="btn-ghost" onClick={() => navigate('/reservations')}>
              예약 확인
            </button>
          </div>
        </div>
      </section>

      {/* Popular themes */}
      <section>
        <div className="flex items-center gap-3 mb-8">
          <Flame className="text-escape-red" size={24} />
          <h2 className="section-title mb-0">인기 테마 TOP 5</h2>
        </div>

        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {[...Array(5)].map((_, i) => (
              <div key={i} className="escape-card h-64 animate-pulse bg-escape-card" />
            ))}
          </div>
        ) : famous.length === 0 ? (
          <p className="text-gray-500 text-center py-12">인기 테마 데이터가 없습니다.</p>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {famous.map((theme, i) => (
              <ThemeCard key={theme.id} theme={theme} rank={i + 1} />
            ))}
          </div>
        )}

        <div className="text-center mt-10">
          <button className="btn-ghost" onClick={() => navigate('/themes')}>
            전체 테마 보기 →
          </button>
        </div>
      </section>
    </div>
  )
}
