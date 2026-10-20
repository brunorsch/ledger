import { useEffect, type ReactNode } from 'react'
import {
  createBrowserRouter,
  Navigate,
  redirect,
  RouterProvider,
  useLocation,
  useNavigate,
} from 'react-router-dom'
import { AppLayout } from '@/layout/AppLayout'
import BalancoScreen from '@/screen/BalancoScreen'
import GastosFixosScreen from '@/screen/GastosFixosScreen'
import LoginScreen from '@/screen/LoginScreen'
import { TelaCarregando } from '@/components'
import { mesAtual } from '@/format/mes'
import { useAutenticacao } from '@/auth/useAutenticacao'
import { definirAoReceber401 } from '@/api/client'

const REFERENCIA_REGEX = /^\d{4}-(0[1-9]|1[0-2])$/

function referenciaValidaOuAtual(referencia: string | undefined): string {
  return referencia && REFERENCIA_REGEX.test(referencia) ? referencia : mesAtual()
}

function RequerAutenticacao({ children }: { children: ReactNode }) {
  const status = useAutenticacao((estado) => estado.status)
  const location = useLocation()
  const navigate = useNavigate()

  useEffect(() => {
    definirAoReceber401(() => navigate('/login', { replace: true }))
    return () => definirAoReceber401(null)
  }, [navigate])

  if (status === 'carregando') {
    return <TelaCarregando />
  }

  if (status === 'deslogado') {
    return <Navigate to="/login" state={{ de: location.pathname }} replace />
  }

  return <>{children}</>
}

const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginScreen />,
  },
  {
    path: '/',
    element: (
      <RequerAutenticacao>
        <AppLayout />
      </RequerAutenticacao>
    ),
    children: [
      {
        index: true,
        element: <Navigate to={`/meses/${mesAtual()}`} replace />,
      },
      {
        path: 'meses/:referencia',
        element: <BalancoScreen />,
        loader: ({ params }) => {
          const referencia = referenciaValidaOuAtual(params.referencia)
          if (referencia !== params.referencia) {
            throw redirect(`/meses/${referencia}`)
          }
          return { referencia }
        },
      },
      {
        path: 'gastos-fixos',
        element: <GastosFixosScreen />,
      },
      {
        path: '*',
        element: <Navigate to={`/meses/${mesAtual()}`} replace />,
      },
    ],
  },
])

export default function App() {
  return <RouterProvider router={router} />
}
