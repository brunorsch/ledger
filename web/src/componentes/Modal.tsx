import { useEffect, useRef, type ReactNode } from 'react'

type ModalProps = {
  aberto: boolean
  titulo: string
  children: ReactNode
  onFechar: () => void
}

export function Modal({ aberto, titulo, children, onFechar }: ModalProps) {
  const painelRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!aberto) return

    function aoPressionarTecla(evento: KeyboardEvent) {
      if (evento.key === 'Escape') {
        onFechar()
      }
    }

    document.addEventListener('keydown', aoPressionarTecla)
    painelRef.current?.focus()

    return () => document.removeEventListener('keydown', aoPressionarTecla)
  }, [aberto, onFechar])

  if (!aberto) return null

  return (
    <div
      className="fixed inset-0 z-50 flex items-start justify-center bg-ink/40 p-4 backdrop-blur-sm md:items-center"
      onClick={onFechar}
      role="presentation"
    >
      <div
        ref={painelRef}
        tabIndex={-1}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-titulo"
        onClick={(evento) => evento.stopPropagation()}
        className="w-full max-w-md overflow-hidden rounded-2xl border border-line bg-paper shadow-xl outline-none"
      >
        <div className="border-b border-line px-5 py-4 md:px-6">
          <h2 id="modal-titulo" className="font-display text-lg font-medium">
            {titulo}
          </h2>
        </div>
        <div className="px-5 py-5 md:px-6 md:py-6">{children}</div>
      </div>
    </div>
  )
}
