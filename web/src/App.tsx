import { createBrowserRouter, Navigate, redirect, RouterProvider } from 'react-router-dom'
import { AppLayout } from '@/layout/AppLayout'
import BalancoScreen from '@/screen/BalancoScreen'
import GastosFixosScreen from '@/screen/GastosFixosScreen'
import { mesAtual } from '@/format/mes'

const REFERENCIA_REGEX = /^\d{4}-(0[1-9]|1[0-2])$/

function referenciaValidaOuAtual(referencia: string | undefined): string {
  return referencia && REFERENCIA_REGEX.test(referencia) ? referencia : mesAtual()
}

const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
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
