import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, KeyRound, Clock, User, CalendarDays } from 'lucide-react'
import { api } from '../api'
import DatePicker from '../components/DatePicker'
import type { Theme, ReservationTime } from '../types'

const d = new Date()
const today = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`

export default function ThemeDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const [theme, setTheme] = useState<Theme | null>(null)
  const [times, setTimes] = useState<ReservationTime[]>([])
  const [availableIds, setAvailableIds] = useState<Set<number> | null>(null)
  const [availableLoading, setAvailableLoading] = useState(false)
  const [selectedDate, setSelectedDate] = useState(today)
  const [selectedTimeId, setSelectedTimeId] = useState<number | null>(null)
  const [name, setName] = useState('')
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null)

  // 테마 + 전체 시간 목록 로드
  useEffect(() => {
    if (!id) return
    Promise.all([api.themes.get(Number(id)), api.times.list()])
      .then(([t, ts]) => { setTheme(t); setTimes(ts) })
      .catch(() => navigate('/themes'))
      .finally(() => setLoading(false))
  }, [id, navigate])

  // 날짜가 바뀔 때 예약 가능 시간 조회 → 비활성화 계산에만 사용
  useEffect(() => {
    if (!id || !selectedDate) return
    setAvailableLoading(true)
    setAvailableIds(null)
    api.times.available(selectedDate, Number(id))
      .then((available) => setAvailableIds(new Set(available.map((t) => t.id))))
      .catch(() => setAvailableIds(new Set()))
      .finally(() => setAvailableLoading(false))
  }, [id, selectedDate])

  const handleDateChange = (date: string) => {
    setSelectedDate(date)
    setSelectedTimeId(null)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedTimeId || !selectedDate || !name.trim() || !id) return
    setSubmitting(true)
    setMessage(null)
    try {
      const reservation = await api.reservations.create({
        name: name.trim(),
        themeId: Number(id),
        date: selectedDate,
        timeId: selectedTimeId,
      })
      navigate(`/reservations/${reservation.id}`)
    } catch (err) {
      setMessage({ type: 'error', text: err instanceof Error ? err.message : '예약에 실패했습니다.' })
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center py-40">
        <div className="animate-pulse_red w-12 h-12 rounded-full border-2 border-escape-red" />
      </div>
    )
  }

  if (!theme) return null

  return (
    <div>
      <button
        onClick={() => navigate('/themes')}
        className="flex items-center gap-2 text-gray-400 hover:text-escape-gold mb-8 transition-colors duration-200"
      >
        <ArrowLeft size={18} /> 테마 목록으로
      </button>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-10">
        {/* Theme info */}
        <div className="escape-card overflow-hidden">
          {theme.thumbnailUrl ? (
            <img
              src={theme.thumbnailUrl}
              alt={theme.name}
              className="w-full h-64 object-cover opacity-80"
              onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
            />
          ) : (
            <div className="w-full h-64 flex items-center justify-center bg-escape-bg">
              <KeyRound size={64} className="text-escape-red opacity-30" />
            </div>
          )}
          <div className="p-6">
            <h1 className="font-cinzel font-black text-3xl text-white mb-4">{theme.name}</h1>
            <p className="text-gray-300 leading-relaxed">{theme.description}</p>
          </div>
        </div>

        {/* Reservation form */}
        <div className="escape-card p-6">
          <h2 className="font-cinzel font-bold text-escape-gold text-xl tracking-widest uppercase mb-6">
            예약하기
          </h2>

          {message && (
            <div className={`p-4 rounded mb-6 text-sm border ${
              message.type === 'success'
                ? 'bg-green-900/30 border-green-700 text-green-300'
                : 'bg-red-900/30 border-escape-red text-red-300'
            }`}>
              {message.text}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="flex items-center gap-2 text-gray-400 text-xs tracking-widest uppercase mb-2">
                <User size={14} /> 예약자 이름
              </label>
              <input
                type="text"
                className="input-dark"
                placeholder="이름을 입력하세요 (1~20자)"
                value={name}
                onChange={(e) => setName(e.target.value)}
                maxLength={20}
                required
              />
            </div>

            <div>
              <label className="flex items-center gap-2 text-gray-400 text-xs tracking-widest uppercase mb-2">
                <CalendarDays size={14} /> 날짜 선택
              </label>
              <DatePicker value={selectedDate} onChange={handleDateChange} minDate={today} />
            </div>

            <div>
              <label className="flex items-center gap-2 text-gray-400 text-xs tracking-widest uppercase mb-2">
                <Clock size={14} /> 시간 선택
                {availableLoading && (
                  <span className="text-gray-600 text-xs normal-case tracking-normal ml-1">조회 중...</span>
                )}
              </label>
              {times.length === 0 ? (
                <p className="text-gray-500 text-sm py-2">등록된 시간이 없습니다.</p>
              ) : (
                <div className="grid grid-cols-3 gap-2">
                  {times.map((t) => {
                    const isBooked = availableIds !== null && !availableIds.has(t.id)
                    const isSelected = selectedTimeId === t.id

                    return (
                      <button
                        key={t.id}
                        type="button"
                        disabled={availableLoading}
                        onClick={() => setSelectedTimeId(t.id)}
                        className={`py-2 px-3 rounded text-sm font-cinzel border transition-all duration-200 ${
                          isSelected
                            ? 'bg-escape-red border-escape-red text-white shadow-red-glow'
                            : isBooked
                            ? 'border-yellow-800 text-yellow-600 hover:border-yellow-500 hover:text-yellow-400'
                            : 'border-escape-border text-gray-400 hover:border-escape-red hover:text-white'
                        }`}
                      >
                        <span className="block">{t.startAt.slice(0, 5)}</span>
                        {isBooked && <span className="block text-xs opacity-75">대기</span>}
                      </button>
                    )
                  })}
                </div>
              )}
            </div>

            <button
              type="submit"
              disabled={!name.trim() || !selectedDate || !selectedTimeId || submitting}
              className="btn-primary w-full disabled:opacity-40 disabled:cursor-not-allowed"
            >
              {submitting
                ? '처리 중...'
                : selectedTimeId && availableIds !== null && !availableIds.has(selectedTimeId)
                ? '대기 신청'
                : '예약 확정'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
