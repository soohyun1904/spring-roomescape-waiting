import { useRef, useEffect, useCallback } from 'react'

interface Props {
  value: string        // "HH:mm"
  onChange: (val: string) => void
}

const ITEM_H = 44
const VISIBLE = 5     // 홀수여야 중앙이 선택값
const SPACER = Math.floor(VISIBLE / 2)

const HOURS = Array.from({ length: 24 }, (_, i) => String(i).padStart(2, '0'))
const MINUTES = Array.from({ length: 60 }, (_, i) => String(i).padStart(2, '0'))

interface ColProps {
  items: string[]
  selected: string
  onSelect: (val: string) => void
}

function ScrollColumn({ items, selected, onSelect }: ColProps) {
  const ref = useRef<HTMLDivElement>(null)
  const timerRef = useRef<ReturnType<typeof setTimeout>>()
  const isUserScrolling = useRef(false)

  // 외부에서 selected 바뀔 때만 스크롤 이동
  useEffect(() => {
    const idx = items.indexOf(selected)
    if (idx < 0 || !ref.current || isUserScrolling.current) return
    ref.current.scrollTo({ top: idx * ITEM_H, behavior: 'smooth' })
  }, [selected, items])

  const handleScroll = useCallback(() => {
    isUserScrolling.current = true
    clearTimeout(timerRef.current)
    timerRef.current = setTimeout(() => {
      isUserScrolling.current = false
      if (!ref.current) return
      const idx = Math.round(ref.current.scrollTop / ITEM_H)
      const clamped = Math.max(0, Math.min(items.length - 1, idx))
      // 정확한 위치로 snap
      ref.current.scrollTo({ top: clamped * ITEM_H, behavior: 'smooth' })
      onSelect(items[clamped])
    }, 120)
  }, [items, onSelect])

  const handleClick = (item: string, idx: number) => {
    ref.current?.scrollTo({ top: idx * ITEM_H, behavior: 'smooth' })
    onSelect(item)
  }

  return (
    <div
      ref={ref}
      onScroll={handleScroll}
      className="overflow-y-scroll hide-scrollbar"
      style={{
        height: ITEM_H * VISIBLE,
        scrollSnapType: 'y mandatory',
      }}
    >
      {/* 상단 여백 — 첫 번째 항목을 중앙에 오게 */}
      {Array.from({ length: SPACER }).map((_, i) => (
        <div key={`top-${i}`} style={{ height: ITEM_H }} />
      ))}

      {items.map((item, idx) => (
        <div
          key={item}
          onClick={() => handleClick(item, idx)}
          style={{ height: ITEM_H, scrollSnapAlign: 'center' }}
          className={`flex items-center justify-center cursor-pointer font-cinzel text-xl font-bold select-none transition-colors duration-150 ${
            item === selected
              ? 'text-white'
              : 'text-gray-600 hover:text-gray-400'
          }`}
        >
          {item}
        </div>
      ))}

      {/* 하단 여백 */}
      {Array.from({ length: SPACER }).map((_, i) => (
        <div key={`bot-${i}`} style={{ height: ITEM_H }} />
      ))}
    </div>
  )
}

export default function TimePicker({ value, onChange }: Props) {
  const [hh, mm] = value ? value.split(':') : ['10', '00']

  const setHour = (h: string) => onChange(`${h}:${mm}`)
  const setMin = (m: string) => onChange(`${hh}:${m}`)

  return (
    <div className="bg-escape-bg border border-escape-border rounded-lg overflow-hidden select-none">
      {/* 라벨 행 — relative 영역 밖에 위치해야 하이라이트 top이 정확히 맞음 */}
      <div className="flex">
        <div className="flex-1 text-center text-gray-600 text-xs tracking-widest uppercase py-2">시</div>
        <div className="px-4" />
        <div className="flex-1 text-center text-gray-600 text-xs tracking-widest uppercase py-2">분</div>
      </div>

      {/* 스크롤 영역 (relative는 여기서부터) */}
      <div className="relative">
        {/* 선택 영역 하이라이트 */}
        <div
          className="absolute inset-x-0 border-y border-escape-red/60 bg-escape-red/10 pointer-events-none z-10"
          style={{ top: ITEM_H * SPACER, height: ITEM_H }}
        />

        <div className="flex">
          <div className="flex-1 flex justify-center">
            <ScrollColumn items={HOURS} selected={hh} onSelect={setHour} />
          </div>

          <div className="flex items-center justify-center text-escape-red font-black text-2xl px-2 z-20">
            :
          </div>

          <div className="flex-1 flex justify-center">
            <ScrollColumn items={MINUTES} selected={mm} onSelect={setMin} />
          </div>
        </div>
      </div>
    </div>
  )
}
