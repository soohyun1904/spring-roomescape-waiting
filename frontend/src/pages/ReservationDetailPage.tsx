import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { CalendarCheck, ArrowLeft, KeyRound, CalendarDays, Clock, User, Pencil, Trash2 } from 'lucide-react'
import { api } from '../api'
import DatePicker from '../components/DatePicker'
import type { Reservation, ReservationTime } from '../types'

const d = new Date()
const today = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`

export default function ReservationDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [reservation, setReservation] = useState<Reservation | null>(null)
  const [loading, setLoading] = useState(true)

  // 수정 모드 state
  const [isEditing, setIsEditing] = useState(false)
  const [times, setTimes] = useState<ReservationTime[]>([])
  const [availableIds, setAvailableIds] = useState<Set<number> | null>(null)
  const [availableLoading, setAvailableLoading] = useState(false)
  const [editForm, setEditForm] = useState({ name: '', date: '', themeId: 0, timeId: 0 })
  const [saving, setSaving] = useState(false)
  const [editError, setEditError] = useState('')
  const [deleting, setDeleting] = useState(false)

  useEffect(() => {
    if (!id) return
    api.reservations.get(Number(id))
      .then(setReservation)
      .catch(() => navigate('/reservations'))
      .finally(() => setLoading(false))
  }, [id, navigate])

  const fetchAvailableTimes = (date: string, themeId: number) => {
    setAvailableLoading(true)
    setAvailableIds(null)
    api.times.available(date, themeId)
      .then((available) => setAvailableIds(new Set(available.map((t) => t.id))))
      .catch(() => setAvailableIds(new Set()))
      .finally(() => setAvailableLoading(false))
  }

  const startEdit = async () => {
    if (!reservation) return
    const form = {
      name: reservation.name,
      date: reservation.date,
      themeId: reservation.theme.id,
      timeId: 0,
    }
    setEditForm(form)
    setEditError('')
    setIsEditing(true)

    const fetchedTimes = await api.times.list()
    setTimes(fetchedTimes)
    fetchAvailableTimes(form.date, form.themeId)
  }

  const cancelEdit = () => {
    setIsEditing(false)
    setEditError('')
  }

  const handleDateChange = (date: string) => {
    setEditForm((prev) => ({ ...prev, date, timeId: 0 }))
    fetchAvailableTimes(date, editForm.themeId)
  }

  const handleDelete = async () => {
    if (!id || !reservation || !confirm('예약을 삭제하시겠습니까?')) return
    setDeleting(true)
    try {
      await api.reservations.delete(Number(id), reservation.name)
      navigate('/reservations')
    } catch (err) {
      alert(err instanceof Error ? err.message : '삭제에 실패했습니다.')
    } finally {
      setDeleting(false)
    }
  }

  const handleSave = async () => {
    if (!id || !editForm.timeId) return
    setSaving(true)
    setEditError('')
    try {
      const updated = await api.reservations.update(Number(id), {
        name: reservation!.name,
        date: editForm.date,
        themeId: editForm.themeId,
        timeId: editForm.timeId,
      })
      setReservation(updated)
      setIsEditing(false)
    } catch (err) {
      setEditError(err instanceof Error ? err.message : '수정에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center py-40">
        <div className="animate-pulse_red w-12 h-12 rounded-full border-2 border-escape-red" />
      </div>
    )
  }

  if (!reservation) return null

  return (
    <div className="max-w-lg mx-auto">
      <button
        onClick={() => navigate('/reservations')}
        className="flex items-center gap-2 text-gray-400 hover:text-escape-gold mb-8 transition-colors duration-200"
      >
        <ArrowLeft size={18} /> 예약 내역으로
      </button>

      <div className="escape-card overflow-hidden">
        {reservation.theme.thumbnailUrl ? (
          <img
            src={reservation.theme.thumbnailUrl}
            alt={reservation.theme.name}
            className="w-full h-56 object-cover opacity-80"
            onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
          />
        ) : (
          <div className="w-full h-56 flex items-center justify-center bg-escape-bg">
            <KeyRound size={64} className="text-escape-red opacity-30" />
          </div>
        )}

        <div className="p-6 space-y-5">
          <div className="flex items-center justify-between mb-2">
            <div className="flex items-center gap-3">
              <CalendarCheck className="text-escape-red" size={20} />
              <h1 className="font-cinzel font-black text-xl text-white tracking-widest">
                예약 확인
              </h1>
            </div>
            {!isEditing && (
              <div className="flex items-center gap-2">
                <button
                  onClick={startEdit}
                  className="flex items-center gap-2 px-3 py-1.5 text-sm text-gray-400 hover:text-escape-gold border border-escape-border hover:border-escape-gold rounded transition-colors duration-200"
                >
                  <Pencil size={14} /> 수정
                </button>
                <button
                  onClick={handleDelete}
                  disabled={deleting}
                  className="flex items-center gap-2 px-3 py-1.5 text-sm text-gray-400 hover:text-red-400 border border-escape-border hover:border-red-400 rounded transition-colors duration-200 disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  <Trash2 size={14} /> {deleting ? '삭제 중...' : '삭제'}
                </button>
              </div>
            )}
          </div>

          {isEditing ? (
            <div className="border-t border-escape-border pt-4 space-y-4">
              {editError && (
                <p className="text-red-400 text-sm">{editError}</p>
              )}

              <div>
                <label className="flex items-center gap-1 text-gray-500 text-xs tracking-widest uppercase mb-1">
                  <User size={12} /> 예약자
                </label>
                <p className="input-dark w-full text-gray-400 cursor-not-allowed select-none">{editForm.name}</p>
              </div>

              <div>
                <label className="flex items-center gap-1 text-gray-500 text-xs tracking-widest uppercase mb-1">
                  <CalendarDays size={12} /> 날짜
                </label>
                <DatePicker value={editForm.date} onChange={handleDateChange} minDate={today} />
              </div>

              <div>
                <label className="text-gray-500 text-xs tracking-widest uppercase mb-1 block">테마</label>
                <p className="input-dark w-full text-gray-400 cursor-not-allowed select-none">{reservation.theme.name}</p>
              </div>

              <div>
                <label className="flex items-center gap-1 text-gray-500 text-xs tracking-widest uppercase mb-1">
                  <Clock size={12} /> 시간
                  {availableLoading && <span className="text-gray-600 text-xs normal-case tracking-normal ml-1">조회 중...</span>}
                </label>
                {times.length === 0 ? (
                  <p className="text-gray-500 text-sm py-2">등록된 시간이 없습니다.</p>
                ) : (
                  <div className="grid grid-cols-3 gap-2">
                    {times.map((t) => {
                      const isBooked = availableIds !== null && !availableIds.has(t.id)
                      const isSelected = editForm.timeId === t.id
                      return (
                        <button
                          key={t.id}
                          type="button"
                          disabled={availableLoading}
                          onClick={() => setEditForm((prev) => ({ ...prev, timeId: t.id }))}
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

              <div className="flex gap-3 pt-2">
                <button
                  onClick={handleSave}
                  disabled={saving || !editForm.timeId}
                  className="btn-primary flex items-center gap-2 disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  {saving ? '저장 중...' : '저장'}
                </button>
                <button onClick={cancelEdit} className="btn-ghost">취소</button>
              </div>
            </div>
          ) : (
            <div className="border-t border-escape-border pt-4 space-y-4">
              <div>
                <p className="text-gray-500 text-xs tracking-widest uppercase mb-1">예약 상태</p>
                {reservation.state === '대기'
                  ? <p className="text-yellow-400 font-semibold">대기 중 ({reservation.rank}번째)</p>
                  : <p className="text-green-400 font-semibold">승인</p>
                }
              </div>

              <div>
                <p className="text-gray-500 text-xs tracking-widest uppercase mb-1">테마</p>
                <p className="text-white font-semibold text-lg">{reservation.theme.name}</p>
              </div>

              <div className="flex gap-6">
                <div>
                  <p className="flex items-center gap-1 text-gray-500 text-xs tracking-widest uppercase mb-1">
                    <CalendarDays size={12} /> 날짜
                  </p>
                  <p className="text-white">{reservation.date}</p>
                </div>
                <div>
                  <p className="flex items-center gap-1 text-gray-500 text-xs tracking-widest uppercase mb-1">
                    <Clock size={12} /> 시간
                  </p>
                  <p className="text-white">{reservation.time.startAt.slice(0, 5)}</p>
                </div>
              </div>

              <div>
                <p className="flex items-center gap-1 text-gray-500 text-xs tracking-widest uppercase mb-1">
                  <User size={12} /> 예약자
                </p>
                <p className="text-white">{reservation.name}</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
