import { useNavigate } from 'react-router-dom'
import { ChevronRight, KeyRound } from 'lucide-react'
import type { Theme } from '../types'

interface Props {
  theme: Theme
  rank?: number
}

export default function ThemeCard({ theme, rank }: Props) {
  const navigate = useNavigate()

  return (
    <div
      className="escape-card overflow-hidden cursor-pointer group"
      onClick={() => navigate(`/themes/${theme.id}`)}
    >
      <div className="relative h-48 overflow-hidden bg-gray-900">
        {theme.thumbnailUrl ? (
          <img
            src={theme.thumbnailUrl}
            alt={theme.name}
            className="w-full h-full object-cover opacity-70 group-hover:opacity-90 transition-opacity duration-300"
            onError={(e) => {
              (e.target as HTMLImageElement).style.display = 'none'
            }}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center">
            <KeyRound size={48} className="text-escape-red opacity-50" />
          </div>
        )}
        {rank !== undefined && (
          <div className="absolute top-3 left-3 bg-escape-red text-white font-cinzel font-black text-lg w-10 h-10 flex items-center justify-center rounded-full shadow-red-glow">
            {rank}
          </div>
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-escape-card to-transparent" />
      </div>

      <div className="p-5">
        <h3 className="font-cinzel font-bold text-white text-lg mb-2 group-hover:text-escape-gold transition-colors duration-200">
          {theme.name}
        </h3>
        <p className="text-gray-400 text-sm leading-relaxed line-clamp-2 mb-4">
          {theme.description}
        </p>
        <div className="flex items-center text-escape-red text-sm font-semibold tracking-wider">
          <span>예약하기</span>
          <ChevronRight size={16} className="ml-1 group-hover:translate-x-1 transition-transform duration-200" />
        </div>
      </div>
    </div>
  )
}
