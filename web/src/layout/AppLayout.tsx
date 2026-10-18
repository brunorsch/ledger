import { NavLink, Outlet, useLocation } from 'react-router-dom'
import type { ReactNode } from 'react'
import { mesAtual } from '@/format/mes'

const LINK_BALANCO = `/meses/${mesAtual()}`
const LINK_GASTOS_FIXOS = '/gastos-fixos'

type ItemNavegacao = {
  rotulo: string
  destino: string
  icone: ReactNode
  ativoEm: (pathname: string) => boolean
}

const itens: ItemNavegacao[] = [
  {
    rotulo: 'Balanço',
    destino: LINK_BALANCO,
    icone: (
      <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
        />
      </svg>
    ),
    ativoEm: (pathname) => pathname.startsWith('/meses'),
  },
  {
    rotulo: 'Gastos fixos',
    destino: LINK_GASTOS_FIXOS,
    icone: (
      <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
        />
      </svg>
    ),
    ativoEm: (pathname) => pathname === '/gastos-fixos',
  },
]

function Marca() {
  return (
    <div className="flex items-center gap-2.5 px-2">
      <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-accent font-display text-base font-semibold text-white">
        L
      </span>
      <span className="font-display text-lg font-semibold tracking-tight text-ink">Ledger</span>
    </div>
  )
}

function Sidebar() {
  return (
    <aside className="fixed left-0 top-0 hidden h-screen w-60 flex-col border-r border-line bg-paper px-4 py-6 md:flex">
      <Marca />
      <nav className="mt-8 flex flex-col gap-1">
        {itens.map((item) => (
          <LinkNavegacao key={item.destino} item={item} />
        ))}
      </nav>
    </aside>
  )
}

function BottomBar() {
  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 flex items-center justify-around border-t border-line bg-paper px-2 pt-2 md:hidden">
      {itens.map((item) => (
        <LinkNavegacao key={item.destino} item={item} mobile />
      ))}
    </nav>
  )
}

function LinkNavegacao({ item, mobile = false }: { item: ItemNavegacao; mobile?: boolean }) {
  const location = useLocation()
  const ativo = item.ativoEm(location.pathname)

  return (
    <NavLink
      to={item.destino}
      end={false}
      className={
        `${mobile ? 'flex flex-1 flex-col items-center gap-1 rounded-lg px-3 py-2 text-xs' : 'flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm'} font-medium transition ${
          ativo ? 'bg-accent-soft text-accent' : 'text-muted hover:bg-fill hover:text-ink'
        }`
      }
    >
      {item.icone}
      <span>{item.rotulo}</span>
    </NavLink>
  )
}

export function AppLayout() {
  return (
    <div className="min-h-screen bg-fill text-ink md:pl-60">
      <Sidebar />
      <main className="px-4 py-6 pb-24 md:px-8 md:py-10 md:pb-10">
        <div className="mx-auto max-w-6xl">
          <Outlet />
        </div>
      </main>
      <BottomBar />
    </div>
  )
}
