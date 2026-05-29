import { Moon, Sun } from 'lucide-react';
import { useEffect, useState } from 'react';
import { AppTabs } from './components/app-shell/AppTabs';
import { CardBillsSection } from './components/CardBillsSection';
import { LoadingScreen } from './components/app-shell/LoadingScreen';
import { ExportButton } from './components/ExportButton';
import { ExpensesSection, InstallmentsSection, RevenuesSection } from './components/sections/index';
import { SummaryDashboard } from './components/summary/SummaryDashboard';
import { useFinance } from './context/FinanceContext';
import { type PieMode, OVERRIDE_TYPES } from './domain/constants';
import { useCardDeleteReasons } from './hooks/useCardDeleteReasons';
import { useCharts } from './hooks/useCharts';
import { useFinanceActions } from './hooks/useFinanceActions';
import { useFinanceSettings } from './hooks/useFinanceData';
import { useHashImport } from './hooks/useHashImport';
import { useMonthOverridesActions } from './hooks/useMonthOverridesActions';
import { prefetchChartModule } from './lib/chartLoader';
import { useI18n } from './lib/i18n';
import { monthLabel } from './lib/utils';
import { TABS } from './ui/constants';

export default function FinanceApp() {
  const { t, normalizeCardName } = useI18n();
  const finance = useFinance();
  const { monthView, currentKey, currentDate } = finance;
  const { fixedExpenses, revenues, monthOverrides } = finance;
  const settings = useFinanceSettings();
  const ThemeIcon = settings.theme === 'premium' ? Sun : Moon;
  const { isReady } = finance;
  const actions = useFinanceActions();
  const [activeTab, setActiveTab] = useState('resumo');
  const [pieMode, setPieMode] = useState<PieMode>('categories');
  const { pieChartRef, barChartRef } = useCharts(monthView, pieMode, activeTab);
  const {
    monthCardBills,
    monthRevenueAmounts,
    setMonthCardBill,
    setMonthFixedExpenseAmount,
    setMonthRevenueAmount,
    toggleMonthPaid,
  } = useMonthOverridesActions({
    monthOverrides,
    monthView,
    currentKey,
    ...actions,
  });

  const handleSetCardList = (nextCardList: typeof settings.cardBills) => {
    actions.setCardBills(nextCardList);
  };

  const cardBillsList = settings.cardBills;
  const cardListMapped = settings.cardBills?.map((cb) => ({
    key: cb.id,
    label: normalizeCardName(cb.name),
  }));

  const cardDeleteReasons = useCardDeleteReasons({
    cardBills: settings.cardBills || [],
    fixedExpenses,
    monthViewVariableExpenses: monthView.variableExpenses,
    monthViewInstallments: monthView.installments,
    monthCardBills,
    currentKey,
  });

  useEffect(() => {
    prefetchChartModule();
  }, []);

  useHashImport({ onImport: actions.importFinanceState });

  const renderTabContent = (tabId: string) => {
    if (tabId === 'resumo') {
      return (
        <SummaryDashboard
          monthView={monthView}
          monthCardBills={monthCardBills}
          monthOverrides={monthOverrides}
          currentMonthKey={currentKey}
          pieMode={pieMode}
          setPieMode={setPieMode}
          pieChartRef={pieChartRef}
          barChartRef={barChartRef}
          onToggleMonthPaid={toggleMonthPaid}
          cardList={cardListMapped}
          onOpenCards={() => setActiveTab('parcelas')}
        />
      );
    }

    if (tabId === 'gastos') {
      return (
        <ExpensesSection
          items={fixedExpenses}
          variableItems={monthView.variableExpenses}
          currentMonthKey={currentKey}
          monthOverrides={monthOverrides}
          cardList={cardBillsList}
          onAdd={(payload) => {
            const { ...rest } = payload;
            actions.addFixedExpense(rest as any);
          }}
          onEdit={(id, payload) => {
            const { ...rest } = payload;
            actions.updateFixedExpense(id, rest as any);
          }}
          onDelete={actions.removeFixedExpense}
          onAddVariable={actions.addVariableExpense}
          onEditVariable={actions.updateVariableExpense}
          onDeleteVariable={actions.removeVariableExpense}
          onMonthFixedExpenseAmount={setMonthFixedExpenseAmount}
          onTogglePaid={(itemId, paid) =>
            toggleMonthPaid(OVERRIDE_TYPES.FIXED_EXPENSE_PAYMENT, itemId, paid)
          }
        />
      );
    }

    if (tabId === 'parcelas') {
      return (
        <section className="cards-section">
          <CardBillsSection
            cardBills={monthCardBills}
            onSetCardBill={setMonthCardBill}
            cardList={cardBillsList}
            onSetCardList={handleSetCardList}
            cardDeleteReasons={cardDeleteReasons}
          />

          <InstallmentsSection
            items={monthView.installments as any}
            currentMonthKey={currentKey}
            monthOverrides={monthOverrides}
            cardList={cardBillsList}
            onAdd={actions.addInstallment}
            onEdit={actions.updateInstallment}
            onDelete={actions.removeInstallment}
            onTogglePaid={(itemId, paid) =>
              toggleMonthPaid(OVERRIDE_TYPES.INSTALLMENT_PAYMENT, itemId, paid)
            }
          />
        </section>
      );
    }

    return (
      <RevenuesSection
        items={revenues}
        currentMonthKey={currentKey}
        monthRevenueAmounts={monthRevenueAmounts}
        onAdd={actions.addRevenue}
        onEdit={actions.updateRevenue}
        onDelete={actions.removeRevenue}
        onMonthRevenueAmount={setMonthRevenueAmount}
      />
    );
  };

  if (!isReady) {
    return (
      <div className="app">
        <LoadingScreen />
      </div>
    );
  }

  return (
    <div className="app">
      <h2 className="sr-only">Painel de controle financeiro</h2>

      <header className="app-topbar">
        <div className="app-brand" aria-label="Ledger">
          <span className="app-brand-mark" aria-hidden="true">
            L
          </span>
          <span>Ledger</span>
        </div>

        <div className="app-month-stepper" role="group" aria-label="Navegacao de meses">
          <button
            className="month-step-btn month-step-btn--icon"
            type="button"
            onClick={() => actions.changeMonth(-1)}
            aria-label="Mes anterior"
          >
            &lt;
          </button>
          <h2>{monthLabel(currentDate)}</h2>
          <button
            className="month-step-btn month-step-btn--icon"
            type="button"
            onClick={() => actions.changeMonth(1)}
            aria-label="Proximo mes"
          >
            &gt;
          </button>
        </div>

        <div className="app-topbar-actions">
          <ExportButton />
          <button
            className="theme-btn"
            onClick={() => actions.setTheme(settings.theme === 'premium' ? 'default' : 'premium')}
            aria-label={`Mudar para tema ${settings.theme === 'premium' ? 'Claro' : 'Escuro'}`}
            title={`Mudar para tema ${settings.theme === 'premium' ? 'Claro' : 'Escuro'}`}
          >
            <ThemeIcon aria-hidden="true" className="theme-btn-icon" size={15} strokeWidth={2} />
          </button>
        </div>
      </header>

      <div className="dashboard-shell">
        <section className="dashboard-controls">
          <AppTabs tabs={TABS} activeTab={activeTab} translate={t} onChange={setActiveTab} />
        </section>

        <main className="app-content" aria-live="polite">
          <div className="app-screen app-screen--active" key={activeTab}>
            {renderTabContent(activeTab)}
          </div>
        </main>
      </div>
    </div>
  );
}
