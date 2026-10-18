import { useEffect } from 'react'
import { mesAtual } from '@/format/mes'

export function useAtalhoMesAtual(navegarPara: (referencia: string) => void) {
  useEffect(() => {
    function aoPressionarTecla(evento: KeyboardEvent) {
      if (!(evento.key === 'h' && evento.ctrlKey)) return

      const alvo = evento.target as HTMLElement
      const tag = alvo.tagName.toLowerCase()
      const editavel =
        alvo.isContentEditable || tag === 'input' || tag === 'textarea' || tag === 'select'

      if (editavel) return

      evento.preventDefault()
      navegarPara(mesAtual())
    }

    document.addEventListener('keydown', aoPressionarTecla)
    return () => document.removeEventListener('keydown', aoPressionarTecla)
  }, [navegarPara])
}
