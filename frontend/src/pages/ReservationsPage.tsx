import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { CalendarCheck, Search, X } from 'lucide-react'
import { api } from '../api'
import type { Reservation } from '../types'

export default function ReservationsPage() {
  const navigate = useNavigate()
  const [reservations, setReservations] = useState<Reservation[]>([])
  const [loading, setLoading] = useState(true)
  const [query, setQuery] = useState('')
  const [submittedQuery, setSubmittedQuery] = useState('')

  const fetchReservations = (name: string) => {
    setLoading(true)
    api.reservations.list(name || undefined)
      .then(setReservations)
      .catch(console.error)
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchReservations('') }, [])

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    setSubmittedQuery(query)
    fetchReservations(query)
  }

  const handleClear = () => {
    setQuery('')
    setSubmittedQuery('')
    fetchReservations('')
  }

  return (
    <div>
      <div className="flex items-center gap-3 mb-6">
        <CalendarCheck className="text-escape-red" size={24} />
        <h1 className="section-title mb-0">예약 내역</h1>
      </div>

      <form onSubmit={handleSearch} className="flex gap-2 mb-8">
        <div className="relative flex-1">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
          <input
            type="text"
            className="input-dark pl-9 pr-9 w-full"
            placeholder="예약자 이름으로 검색"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
          {query && (
            <button
              type="button"
              onClick={handleClear}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-300"
            >
              <X size={16} />
            </button>
          )}
        </div>
        <button type="submit" className="btn-primary px-5">검색</button>
      </form>

      {loading ? (
        <div className="flex flex-col gap-4">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="escape-card h-24 animate-pulse" />
          ))}
        </div>
      ) : reservations.length === 0 ? (
        <div className="text-center py-20 text-gray-500">
          <CalendarCheck size={48} className="mx-auto mb-4 opacity-30" />
          <p>{submittedQuery ? `"${submittedQuery}" 검색 결과가 없습니다.` : '예약 내역이 없습니다.'}</p>
        </div>
      ) : (
        <div className="flex flex-col gap-4">
          {reservations.map((reservation) => (
            <button
              key={reservation.id}
              onClick={() => navigate(`/reservations/${reservation.id}`)}
              className="escape-card flex items-center gap-4 p-4 w-full text-left hover:border-escape-gold transition-colors duration-200 cursor-pointer"
            >
              <img
                src={reservation.theme.thumbnailUrl}
                alt={reservation.theme.name}
                className="w-20 h-20 object-cover rounded-lg flex-shrink-0"
              />
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-0.5">
                  <p className="font-semibold text-lg truncate">{reservation.theme.name}</p>
                  {reservation.state === '대기'
                    ? <span className="flex-shrink-0 text-xs text-yellow-500 border border-yellow-800 px-2 py-0.5 rounded">대기 {reservation.rank}번</span>
                    : <span className="flex-shrink-0 text-xs text-green-400 border border-green-800 px-2 py-0.5 rounded">승인</span>
                  }
                </div>
                <p className="text-gray-500 text-sm">
                  {reservation.date} · {reservation.time.startAt.slice(0, 5)}
                </p>
                <p className="text-gray-400 text-sm">{reservation.name}</p>
              </div>
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
