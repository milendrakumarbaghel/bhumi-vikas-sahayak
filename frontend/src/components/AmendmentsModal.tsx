import React, { useState } from 'react';
import { X, CheckCircle2, AlertTriangle, Calendar, ShieldCheck } from 'lucide-react';


interface AmendmentsModalProps {
  isOpen: boolean;
  onClose: () => void;
  asOfDate: string;
  onSelectAsOfDate: (date: string) => void;
}

export const AmendmentsModal: React.FC<AmendmentsModalProps> = ({
  isOpen,
  onClose,
  asOfDate,
  onSelectAsOfDate,
}) => {
  const [activeTab, setActiveTab] = useState<'timeline' | 'august2026'>('timeline');

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4 animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl max-w-3xl w-full max-h-[90vh] flex flex-col shadow-2xl border border-slate-200 overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200 bg-[#0c382e] text-white">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-emerald-700/80 rounded-lg text-emerald-200">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold">
                MP Bhumi Vikas Rules – Amendments & Legal Regime
              </h3>
              <p className="text-xs text-emerald-300">
                Effective-Dated Amendments Management (Spring Boot /api/v1/admin/amendments)
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1.5 text-emerald-300 hover:text-white rounded-lg cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tab selector */}
        <div className="flex border-b border-slate-200 bg-slate-50 px-6 pt-2 gap-3">
          <button
            type="button"
            onClick={() => setActiveTab('timeline')}
            className={`pb-2.5 text-xs font-semibold border-b-2 transition-all cursor-pointer ${
              activeTab === 'timeline'
                ? 'border-emerald-700 text-emerald-800'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            Legal Regime & As-Of Date Selector
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('august2026')}
            className={`pb-2.5 text-xs font-semibold border-b-2 transition-all cursor-pointer ${
              activeTab === 'august2026'
                ? 'border-emerald-700 text-emerald-800'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            28 August 2026 Notification (Rule 2(5)(c))
          </button>
        </div>

        {/* Modal body */}
        <div className="p-6 overflow-y-auto flex-1 space-y-5">
          {activeTab === 'timeline' && (
            <div className="space-y-4">
              <div className="p-4 bg-emerald-50/70 border border-emerald-200 rounded-xl">
                <div className="flex items-center gap-2 mb-1.5">
                  <Calendar className="w-4 h-4 text-emerald-700" />
                  <span className="text-xs font-bold text-emerald-950 uppercase tracking-wider">
                    Select Target Evaluation Date (As-Of Date)
                  </span>
                </div>
                <p className="text-xs text-emerald-900 mb-3">
                  Under the MP Bhumi Vikas Niyam statutory lifecycle, rules apply based on the date of submission or effective notification date.
                </p>

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                  {[
                    {
                      date: '2026-10-11',
                      title: 'Current Date (11 Oct 2026)',
                      desc: 'Post-August 2026 amendment. Plots <= 500 sq m in approved layouts exempted.',
                      isLatest: true,
                    },
                    {
                      date: '2025-03-06',
                      title: 'March 2025 Consolidated',
                      desc: 'Official 108-page Gazette baseline published by Town & Country Planning.',
                    },
                    {
                      date: '2023-01-01',
                      title: 'Historical (2023 Sanction)',
                      desc: 'Check validity of sanctions granted in 2023 under Rule 17 (3-year validity).',
                    },
                  ].map((option) => (
                    <button
                      key={option.date}
                      type="button"
                      onClick={() => onSelectAsOfDate(option.date)}
                      className={`text-left p-3 rounded-lg border transition-all cursor-pointer ${
                        asOfDate === option.date
                          ? 'bg-white border-emerald-600 ring-2 ring-emerald-600/20 shadow-xs'
                          : 'bg-white/80 border-slate-200 hover:border-slate-300'
                      }`}
                    >
                      <div className="flex items-center justify-between mb-1">
                        <span className="text-xs font-bold text-slate-900">
                          {option.title}
                        </span>
                        {option.isLatest && (
                          <span className="text-[9px] bg-emerald-100 text-emerald-800 font-bold px-1.5 py-0.2 rounded-xs">
                            Active
                          </span>
                        )}
                      </div>
                      <span className="text-[11px] text-slate-500 block leading-tight">
                        {option.desc}
                      </span>
                    </button>
                  ))}
                </div>
              </div>

              {/* Amendments list */}
              <div className="space-y-3">
                <h4 className="text-xs font-bold text-slate-900 uppercase tracking-wider">
                  Tracked Statutory Gazette Notifications
                </h4>

                <div className="border border-slate-200 rounded-xl p-4 bg-white">
                  <div className="flex items-center justify-between mb-1.5">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold text-purple-900 bg-purple-50 px-2 py-0.5 rounded-sm border border-purple-200">
                        Notification F-1-18/2026/32
                      </span>
                      <span className="text-xs font-bold text-slate-900">
                        Rule 2(5)(c) – Plots up to 500 sq m
                      </span>
                    </div>
                    <span className="text-xs font-semibold text-emerald-700 flex items-center gap-1">
                      <CheckCircle2 className="w-3.5 h-3.5" />
                      Effective: 28 Aug 2026
                    </span>
                  </div>
                  <p className="text-xs text-slate-600 leading-relaxed mb-2">
                    Exemption from separate development permission for residential plots up to 500 sq m in approved Town & Country Planning layouts.
                  </p>
                  <div className="text-[11px] text-slate-400">
                    Source: Urban Development & Housing Department, MP Gazette (Extraordinary)
                  </div>
                </div>

                <div className="border border-slate-200 rounded-xl p-4 bg-white">
                  <div className="flex items-center justify-between mb-1.5">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold text-slate-800 bg-slate-100 px-2 py-0.5 rounded-sm">
                        Gazette Consolidated
                      </span>
                      <span className="text-xs font-bold text-slate-900">
                        105 Core Rules + Rule 26-B
                      </span>
                    </div>
                    <span className="text-xs font-semibold text-slate-600">
                      Baseline: 06 March 2025
                    </span>
                  </div>
                  <p className="text-xs text-slate-600 leading-relaxed mb-2">
                    Consolidated 108-page official English & Hindi gazette covering Setbacks (Rule 32), FAR (Rule 42), Lifts (Rule 52), Electric Clearance (Rule 56).
                  </p>
                </div>
              </div>
            </div>
          )}

          {activeTab === 'august2026' && (
            <div className="space-y-4">
              <div className="p-4 bg-amber-50 border border-amber-200 rounded-xl">
                <div className="flex items-center gap-2 text-amber-900 font-bold text-xs mb-1">
                  <AlertTriangle className="w-4 h-4 text-amber-600" />
                  <span>August 2026 Statutory Amendment Note (Official Guide Check)</span>
                </div>
                <p className="text-xs text-amber-950 leading-relaxed">
                  On 28 August 2026, the Government of Madhya Pradesh notified an important amendment to Rule 2(5)(c). For residential plots up to 500 sq m in approved layouts, separate development permission is exempted, speeding up sanction for small homeowners.
                </p>
              </div>

              <div className="border border-slate-200 rounded-xl p-4 bg-white space-y-2 text-xs">
                <div className="font-bold text-slate-900">
                  Impact on Municipal Permission Counter:
                </div>
                <ul className="list-disc pl-5 space-y-1 text-slate-700">
                  <li>
                    Clerks no longer demand separate layout development NOC for plots &le; 500 sq m if colony is already approved.
                  </li>
                  <li>
                    Building permission (map scrutiny and fee deposit) is still required.
                  </li>
                  <li>
                    The Spring Boot assistant automatically checks this date when answering Sample Question #11!
                  </li>
                </ul>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-3 bg-slate-100 border-t border-slate-200 flex items-center justify-between">
          <span className="text-xs text-slate-500">
            Active As-Of Date: <strong>{asOfDate}</strong>
          </span>
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-1.5 bg-[#124d3a] hover:bg-[#165d46] text-white text-xs font-semibold rounded-lg transition-colors cursor-pointer"
          >
            Apply & Close
          </button>
        </div>
      </div>
    </div>
  );
};
