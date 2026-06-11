import { useState } from 'react'
import { ChevronLeft, ChevronRight } from 'lucide-react'

interface Props {
  value: string
  onChange: (date: string) => void
  minDate?: string
}

const DAY_NAMES = ['일', '월', '화', '수', '목', '금', '토']

function toYMD(date: Date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export default function DatePicker({ value, onChange, minDate }: Props) {
  const today = toYMD(new Date())
  const min = minDate ?? today

  const [cursor, setCursor] = useState(() => {
    const base = value || today
    const [y, m] = base.split('-').map(Number)
    return { year: y, month: m - 1 }
  })

  const prevMonth = () =>
    setCursor(({ year, month }) =>
      month === 0 ? { year: year - 1, month: 11 } : { year, month: month - 1 }
    )

  const nextMonth = () =>
    setCursor(({ year, month }) =>
      month === 11 ? { year: year + 1, month: 0 } : { year, month: month + 1 }
    )

  const firstDay = new Date(cursor.year, cursor.month, 1).getDay()
  const daysInMonth = new Date(cursor.year, cursor.month + 1, 0).getDate()

  const cells: (number | null)[] = [
    ...Array(firstDay).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
  ]

  const monthLabel = `${cursor.year}년 ${cursor.month + 1}월`

  return (
    <div className="bg-escape-bg border border-escape-border rounded-lg p-4 w-full">
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <button
          type="button"
          onClick={prevMonth}
          className="p-1 text-gray-400 hover:text-escape-gold transition-colors duration-150"
        >
          <ChevronLeft size={18} />
        </button>
        <span className="font-cinzel font-bold text-white text-sm tracking-wider">{monthLabel}</span>
        <button
          type="button"
          onClick={nextMonth}
          className="p-1 text-gray-400 hover:text-escape-gold transition-colors duration-150"
        >
          <ChevronRight size={18} />
        </button>
      </div>

      {/* Day names */}
      <div className="grid grid-cols-7 mb-1">
        {DAY_NAMES.map((d, i) => (
          <div
            key={d}
            className={`text-center text-xs py-1 font-cinzel ${
              i === 0 ? 'text-red-400' : i === 6 ? 'text-blue-400' : 'text-gray-500'
            }`}
          >
            {d}
          </div>
        ))}
      </div>

      {/* Date grid */}
      <div className="grid grid-cols-7 gap-y-1">
        {cells.map((day, i) => {
          if (day === null) return <div key={`empty-${i}`} />

          const dateStr = `${cursor.year}-${String(cursor.month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
          const isDisabled = dateStr < min
          const isSelected = dateStr === value
          const isToday = dateStr === today
          const isSun = (i % 7) === 0
          const isSat = (i % 7) === 6

          return (
            <button
              key={dateStr}
              type="button"
              disabled={isDisabled}
              onClick={() => onChange(dateStr)}
              className={[
                'text-center text-sm py-1.5 rounded transition-all duration-150 font-cinzel',
                isDisabled
                  ? 'text-gray-700 cursor-not-allowed'
                  : isSelected
                  ? 'bg-escape-red text-white shadow-red-glow'
                  : isToday
                  ? 'border border-escape-gold text-escape-gold hover:bg-escape-gold/10'
                  : isSun
                  ? 'text-red-300 hover:bg-escape-border'
                  : isSat
                  ? 'text-blue-300 hover:bg-escape-border'
                  : 'text-gray-300 hover:bg-escape-border',
              ].join(' ')}
            >
              {day}
            </button>
          )
        })}
      </div>
    </div>
  )
}
