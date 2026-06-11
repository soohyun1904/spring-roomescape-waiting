import { useEffect, useState } from 'react'
import { TrendingUp, LayoutGrid } from 'lucide-react'
import { api } from '../api'
import ThemeCard from '../components/ThemeCard'
import type { Theme } from '../types'

const d = new Date()
const today = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`

export default function ThemesPage() {
  const [famous, setFamous] = useState<Theme[]>([])
  const [all, setAll] = useState<Theme[]>([])
  const [loadingFamous, setLoadingFamous] = useState(true)
  const [loadingAll, setLoadingAll] = useState(true)

  useEffect(() => {
    api.themes.famous({ limit: 10, days: 7, date: today })
      .then(setFamous)
      .catch(console.error)
      .finally(() => setLoadingFamous(false))

    api.themes.list()
      .then(setAll)
      .catch(console.error)
      .finally(() => setLoadingAll(false))
  }, [])

  return (
    <div className="space-y-16">
      {/* 인기 테마 */}
      <section>
        <div className="flex items-center gap-3 mb-8">
          <TrendingUp className="text-escape-red" size={24} />
          <h1 className="section-title mb-0">인기 테마</h1>
        </div>

        {loadingFamous ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {[...Array(6)].map((_, i) => (
              <div key={i} className="escape-card h-64 animate-pulse" />
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
      </section>

      {/* 전체 테마 */}
      <section>
        <div className="flex items-center gap-3 mb-8">
          <LayoutGrid className="text-escape-gold" size={24} />
          <h2 className="section-title mb-0">전체 테마</h2>
        </div>

        {loadingAll ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {[...Array(6)].map((_, i) => (
              <div key={i} className="escape-card h-64 animate-pulse" />
            ))}
          </div>
        ) : all.length === 0 ? (
          <p className="text-gray-500 text-center py-12">등록된 테마가 없습니다.</p>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {all.map((theme) => (
              <ThemeCard key={theme.id} theme={theme} />
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
