import React from 'react';
import type { CalculationPayload } from '../types';
import { getPdfUrl } from '../services/api';
import type { Language } from '../i18n';
import { i18n } from '../i18n';
import { Calculator, ExternalLink } from 'lucide-react';

interface CalculationPanelProps {
  payload: CalculationPayload;
  uiLang: Language;
}

export const CalculationPanel: React.FC<CalculationPanelProps> = ({ payload, uiLang }) => {
  const t = i18n[uiLang];

  // Check if shape (a): empty object
  const isEmpty =
    (!payload.calculations || payload.calculations.length === 0) &&
    !payload.code &&
    !payload.roadWidthMeters &&
    !payload.frontOpenSpaceMeters &&
    (!payload.notApplicable || payload.notApplicable.length === 0);

  if (isEmpty) return null;

  // Handle shape (c): Java fallback single calc row
  const isJavaFallback = !payload.calculations && (payload.code || payload.rule || payload.frontOpenSpaceMeters !== undefined);

  return (
    <div className="w-full mt-4 bg-slate-50 border border-slate-300 rounded-xl p-4 sm:p-5 space-y-4">
      <div className="flex items-center gap-2 border-b border-slate-200 pb-2.5">
        <Calculator className="w-4 h-4 text-[#0f2942]" />
        <h3 className="text-sm font-bold uppercase tracking-wider text-[#0f2942]">
          {t.calculationTitle}
        </h3>
      </div>

      {/* notApplicable reasons as plain text */}
      {payload.notApplicable && payload.notApplicable.length > 0 && (
        <div className="bg-amber-50 border border-amber-200 rounded-lg p-3 text-xs text-amber-900 space-y-1">
          {payload.notApplicable.map((reason, idx) => (
            <div key={idx} className="leading-relaxed">
              • {reason}
            </div>
          ))}
        </div>
      )}

      {/* Shape (c): Java Fallback rendering */}
      {isJavaFallback && (
        <div className="bg-white border border-slate-200 rounded-xl p-4 space-y-3">
          <div className="flex flex-wrap items-baseline justify-between gap-2">
            <div>
              <span className="text-xs text-slate-500 font-semibold uppercase block">
                Result
              </span>
              <span className="text-2xl font-bold text-slate-900">
                {payload.frontOpenSpaceMeters !== undefined
                  ? `${payload.frontOpenSpaceMeters} m`
                  : payload.code || 'Evaluated'}
              </span>
            </div>
            {payload.rule && (
              <span className="text-xs font-semibold px-2 py-0.5 rounded-sm bg-slate-100 text-slate-700">
                Rule {payload.rule}
              </span>
            )}
          </div>

          {payload.matchedBand && (
            <div className="text-xs text-slate-700">
              <strong>{t.matchedBand}:</strong> {payload.matchedBand}
            </div>
          )}

          {payload.roadWidthMeters !== undefined && (
            <div className="text-xs text-slate-600">
              Road Width: <strong>{payload.roadWidthMeters} m</strong>
            </div>
          )}
        </div>
      )}

      {/* Shape (b): Calcs from ai-service */}
      {payload.calculations && payload.calculations.map((calc, idx) => {
        const resultDisplay = calc.result.text || `${calc.result.value} ${calc.result.unit}`.trim();

        return (
          <div key={calc.code || idx} className="bg-white border border-slate-200 rounded-xl p-4 sm:p-5 space-y-4 shadow-2xs">
            {/* Top: Big Result & Rule Link */}
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <span className="text-xs font-semibold uppercase tracking-wider text-slate-500 block">
                  {calc.result.name || 'Statutory Result'}
                </span>
                <span className="text-2xl sm:text-3xl font-extrabold text-[#0f2942]">
                  {resultDisplay}
                </span>
              </div>

              <div className="flex items-center gap-2">
                <span className="text-xs font-bold text-slate-700 bg-slate-100 px-2.5 py-1 rounded-md">
                  Rule {calc.rule}
                </span>
                {calc.pdfPage && (
                  <a
                    href={getPdfUrl(calc.pdfPage)}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-flex items-center gap-1 text-xs font-semibold text-blue-700 hover:text-blue-900 hover:underline cursor-pointer"
                  >
                    <span>p.{calc.pdfPage}</span>
                    <ExternalLink className="w-3 h-3" />
                  </a>
                )}
              </div>
            </div>

            {/* Matched Band & Formula */}
            <div className="space-y-1.5 text-xs text-slate-700 pt-1">
              {calc.matchedBand && (
                <div>
                  <span className="font-semibold text-slate-500">{t.matchedBand}: </span>
                  <span className="font-medium text-slate-900">{calc.matchedBand}</span>
                </div>
              )}
              {calc.formula && (
                <div>
                  <span className="font-semibold text-slate-500">{t.formula}: </span>
                  <code className="bg-slate-100 px-1.5 py-0.5 rounded-sm text-slate-800 font-mono text-[11px]">
                    {calc.formula}
                  </code>
                </div>
              )}
            </div>

            {/* Inputs Table */}
            {calc.inputs && calc.inputs.length > 0 && (
              <div className="pt-2">
                <span className="text-xs font-bold uppercase tracking-wider text-slate-500 block mb-1.5">
                  {t.inputs}
                </span>
                <div className="overflow-x-auto border border-slate-200 rounded-lg">
                  <table className="w-full text-xs text-left">
                    <thead className="bg-slate-100 text-slate-600 font-semibold border-b border-slate-200">
                      <tr>
                        <th className="py-1.5 px-3">Input Name</th>
                        <th className="py-1.5 px-3">Value</th>
                        <th className="py-1.5 px-3">{t.unit}</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 text-slate-800">
                      {calc.inputs.map((inp, i) => (
                        <tr key={i} className="hover:bg-slate-50">
                          <td className="py-1.5 px-3 font-medium">{inp.name}</td>
                          <td className="py-1.5 px-3 font-mono">{String(inp.value)}</td>
                          <td className="py-1.5 px-3 text-slate-500">{inp.unit}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {/* Assumptions */}
            {calc.assumptions && calc.assumptions.length > 0 && (
              <div className="text-xs text-slate-600 italic bg-slate-50 p-2.5 rounded-lg border border-slate-200/80">
                <span className="font-semibold text-slate-700 not-italic block mb-0.5">
                  {t.assumption}:
                </span>
                <ul className="list-disc pl-4 space-y-0.5">
                  {calc.assumptions.map((assump, aIdx) => (
                    <li key={aIdx}>{assump}</li>
                  ))}
                </ul>
              </div>
            )}

            {/* Notes */}
            {calc.notes && calc.notes.length > 0 && (
              <div className="text-xs text-slate-600 space-y-0.5">
                <span className="font-semibold text-slate-700 block">{t.notes}:</span>
                <ul className="list-disc pl-4 space-y-0.5">
                  {calc.notes.map((note, nIdx) => (
                    <li key={nIdx}>{note}</li>
                  ))}
                </ul>
              </div>
            )}

            {/* Separate Grey Box for floorIllustration (Never mixed with rule results!) */}
            {calc.result.floorIllustration && calc.result.floorIllustration.length > 0 && (
              <div className="mt-4 p-3.5 bg-slate-200/80 border border-slate-300 rounded-xl space-y-2">
                <div className="text-xs font-black tracking-wide text-slate-700 uppercase">
                  {calc.result.floorIllustrationLabel || t.illustrationBadge}
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
                  {calc.result.floorIllustration.map((illus, illIdx) => (
                    <div key={illIdx} className="bg-white p-2.5 rounded-lg border border-slate-300 text-xs text-center">
                      <span className="text-slate-500 block text-[11px]">Floors: {illus.floors}</span>
                      <span className="font-bold text-slate-900 block text-sm">
                        {illus.footprintSqm} sq m
                      </span>
                      <span className="text-slate-600 text-[10px]">
                        Coverage: {illus.groundCoveragePercent}%
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
};
