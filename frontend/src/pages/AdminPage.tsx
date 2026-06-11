import { useEffect, useState } from 'react'
import { Settings, Plus, Trash2, Clock, Image, Search, X, CalendarCheck } from 'lucide-react'
import { api } from '../api'
import TimePicker from '../components/TimePicker'
import type { Theme, ReservationTime, Reservation } from '../types'

type Tab = 'themes' | 'times' | 'reservations'

export default function AdminPage() {
  const [tab, setTab] = useState<Tab>('themes')

  return (
    <div>
      <div className="flex items-center gap-3 mb-8">
        <Settings className="text-escape-red" size={24} />
        <h1 className="section-title mb-0">관리자</h1>
      </div>

      <div className="flex gap-2 mb-8 border-b border-escape-border">
        {(['themes', 'times', 'reservations'] as Tab[]).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`px-6 py-3 font-cinzel text-sm tracking-widest uppercase transition-all duration-200 border-b-2 -mb-px ${
              tab === t
                ? 'border-escape-red text-escape-gold'
                : 'border-transparent text-gray-500 hover:text-gray-300'
            }`}
          >
            {t === 'themes' ? '테마 관리' : t === 'times' ? '시간 관리' : '예약 관리'}
          </button>
        ))}
      </div>

      {tab === 'themes' ? <ThemeAdmin /> : tab === 'times' ? <TimeAdmin /> : <ReservationAdmin />}
    </div>
  )
}

function ThemeAdmin() {
  const [themes, setThemes] = useState<Theme[]>([])
  const [loading, setLoading] = useState(true)
  const [form, setForm] = useState({ name: '', description: '', thumbnailUrl: '' })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const load = () => {
    api.themes.list().then(setThemes).catch(console.error).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      const created = await api.themes.create(form)
      setThemes((prev) => [...prev, created])
      setForm({ name: '', description: '', thumbnailUrl: '' })
    } catch (err) {
      setError(err instanceof Error ? err.message : '처리에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('테마를 삭제하시겠습니까?')) return
    try {
      await api.themes.delete(id)
      setThemes((prev) => prev.filter((t) => t.id !== id))
    } catch (err) {
      alert(err instanceof Error ? err.message : '삭제에 실패했습니다.')
    }
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
      {/* Form */}
      <div className="escape-card p-6">
        <h3 className="font-cinzel font-bold text-white mb-6">새 테마 추가</h3>
        {error && <p className="text-red-400 text-sm mb-4">{error}</p>}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="text-gray-400 text-xs tracking-widest uppercase block mb-1">테마 이름</label>
            <input
              className="input-dark"
              placeholder="최대 20자"
              maxLength={20}
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              required
            />
          </div>
          <div>
            <label className="text-gray-400 text-xs tracking-widest uppercase block mb-1">설명</label>
            <textarea
              className="input-dark resize-none h-24"
              placeholder="테마 설명"
              maxLength={255}
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
          </div>
          <div>
            <label className="flex items-center gap-1 text-gray-400 text-xs tracking-widest uppercase mb-1">
              <Image size={12} /> 썸네일 URL
            </label>
            <input
              className="input-dark"
              placeholder="https://..."
              value={form.thumbnailUrl}
              onChange={(e) => setForm({ ...form, thumbnailUrl: e.target.value })}
            />
          </div>
          <div className="flex gap-3 pt-2">
            <button type="submit" className="btn-primary flex items-center gap-2" disabled={submitting}>
              <Plus size={16} /> {submitting ? '처리 중...' : '테마 추가'}
            </button>
          </div>
        </form>
      </div>

      {/* List */}
      <div className="space-y-3">
        <h3 className="font-cinzel font-bold text-white">테마 목록 ({themes.length})</h3>
        {loading ? (
          <div className="space-y-3">
            {[...Array(3)].map((_, i) => <div key={i} className="escape-card h-16 animate-pulse" />)}
          </div>
        ) : themes.map((theme) => (
          <div key={theme.id} className="escape-card p-4 flex items-center justify-between gap-4">
            <div className="flex items-center gap-3 min-w-0">
              {theme.thumbnailUrl && (
                <img
                  src={theme.thumbnailUrl}
                  alt=""
                  className="w-10 h-10 rounded object-cover opacity-70 flex-shrink-0"
                  onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
                />
              )}
              <div className="min-w-0">
                <p className="text-white font-semibold truncate">{theme.name}</p>
                <p className="text-gray-500 text-xs truncate">{theme.description}</p>
              </div>
            </div>
            <div className="flex gap-2 flex-shrink-0">
              <button className="btn-danger p-2" onClick={() => handleDelete(theme.id)}>
                <Trash2 size={14} />
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

function TimeAdmin() {
  const [times, setTimes] = useState<ReservationTime[]>([])
  const [loading, setLoading] = useState(true)
  const [startAt, setStartAt] = useState('10:00')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const load = () => {
    api.times.list().then(setTimes).catch(console.error).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!startAt) return
    setSubmitting(true)
    setError('')
    try {
      const created = await api.times.create(startAt)
      setTimes((prev) => [...prev, created])
      setStartAt('')
    } catch (err) {
      setError(err instanceof Error ? err.message : '추가에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('이 시간 슬롯을 삭제하시겠습니까?')) return
    try {
      await api.times.delete(id)
      setTimes((prev) => prev.filter((t) => t.id !== id))
    } catch (err) {
      alert(err instanceof Error ? err.message : '삭제에 실패했습니다.')
    }
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
      {/* Form */}
      <div className="escape-card p-6">
        <h3 className="font-cinzel font-bold text-white mb-6">시간 슬롯 추가</h3>
        {error && <p className="text-red-400 text-sm mb-4">{error}</p>}
        <form onSubmit={handleAdd} className="space-y-4">
          <div>
            <label className="flex items-center gap-1 text-gray-400 text-xs tracking-widest uppercase mb-2">
              <Clock size={12} /> 시작 시간
            </label>
            <div className="mb-2 text-center font-cinzel font-black text-3xl text-escape-gold tracking-widest">
              {startAt}
            </div>
            <TimePicker value={startAt} onChange={setStartAt} />
          </div>
          <button type="submit" className="btn-primary flex items-center gap-2" disabled={submitting}>
            <Plus size={16} /> {submitting ? '추가 중...' : '시간 추가'}
          </button>
        </form>
      </div>

      {/* List */}
      <div className="space-y-3">
        <h3 className="font-cinzel font-bold text-white">시간 목록 ({times.length})</h3>
        {loading ? (
          <div className="space-y-3">
            {[...Array(5)].map((_, i) => <div key={i} className="escape-card h-12 animate-pulse" />)}
          </div>
        ) : times.map((t) => (
          <div key={t.id} className="escape-card p-4 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Clock size={16} className="text-escape-red" />
              <span className="text-white font-cinzel font-semibold">{t.startAt}</span>
            </div>
            <button className="btn-danger p-2" onClick={() => handleDelete(t.id)}>
              <Trash2 size={14} />
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}

function ReservationAdmin() {
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

  const handleDelete = async (id: number) => {
    if (!confirm('예약을 삭제하시겠습니까?')) return
    const target = reservations.find((r) => r.id === id)
    if (!target) return
    setReservations((prev) => prev.filter((r) => r.id !== id))
    try {
      await api.reservations.delete(id, target.name)
    } catch (err) {
      alert(err instanceof Error ? err.message : '삭제에 실패했습니다.')
      fetchReservations(submittedQuery)
    }
  }

  return (
    <div className="space-y-6">
      <form onSubmit={handleSearch} className="flex gap-2">
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

      <div className="space-y-3">
        <h3 className="font-cinzel font-bold text-white">예약 목록 ({reservations.length})</h3>
        {loading ? (
          <div className="space-y-3">
            {[...Array(4)].map((_, i) => <div key={i} className="escape-card h-16 animate-pulse" />)}
          </div>
        ) : reservations.length === 0 ? (
          <div className="text-center py-12 text-gray-500">
            <CalendarCheck size={40} className="mx-auto mb-3 opacity-30" />
            <p>{submittedQuery ? `"${submittedQuery}" 검색 결과가 없습니다.` : '예약 내역이 없습니다.'}</p>
          </div>
        ) : reservations.map((r) => (
          <div key={r.id} className="escape-card p-4 flex items-center justify-between gap-4">
            <div className="flex-1 min-w-0 grid grid-cols-5 gap-2 text-sm">
              <p className="text-white font-semibold truncate">{r.theme.name}</p>
              <p className="text-gray-400">{r.date}</p>
              <p className="text-gray-400">{r.time.startAt.slice(0, 5)}</p>
              <p className="text-gray-400">{r.name}</p>
              {r.state === '대기'
                ? <span className="text-yellow-500 text-xs">대기 {r.rank}번</span>
                : <span className="text-green-400 text-xs">승인</span>
              }
            </div>
            <button className="btn-danger p-2 flex-shrink-0" onClick={() => handleDelete(r.id)}>
              <Trash2 size={14} />
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}
