import React, { useState } from 'react';
import type { AskResponse } from '../types';
import type { Language } from '../i18n';
import { i18n } from '../i18n';
import { CalculationPanel } from './CalculationPanel';
import { CitationList } from './CitationList';
import { MissingInputsForm } from './MissingInputsForm';
import {
  CheckCircle2,
  AlertCircle,
  HelpCircle,
  AlertTriangle,
  Copy,
  Check,
  ExternalLink,
  Info,
} from 'lucide-react';

interface AnswerCardProps {
  response: AskResponse;
  originalQuestion: string;
  onRefineQuestion: (newQuestion: string) => void;
  uiLang: Language;
}

export const AnswerCard: React.FC<AnswerCardProps> = ({
  response,
  originalQuestion,
  onRefineQuestion,
  uiLang,
}) => {
  const t = i18n[uiLang];
  const [copied, setCopied] = useState(false);

  const responseLang = response.language || 'en';
  const isHindiResponse = responseLang === 'hi';

  // Parse missing inputs for NEEDS_MORE_INFORMATION
  const missingInputs: string[] = [];
  if (response.calculation?.missingInputs && Array.isArray(response.calculation.missingInputs)) {
    missingInputs.push(...response.calculation.missingInputs);
  }
  if (response.warnings && Array.isArray(response.warnings)) {
    for (const w of response.warnings) {
      if (w.toLowerCase().startsWith('required inputs:')) {
        const parts = w.substring('required inputs:'.length).split(',');
        for (const p of parts) {
          const trimmed = p.trim();
          if (trimmed && !missingInputs.includes(trimmed)) {
            missingInputs.push(trimmed);
          }
        }
      }
    }
  }

  // Check for amendment warnings (Rule 2(5)(c) or amendment)
  const amendmentWarning = response.warnings?.find(
    (w) =>
      w.toLowerCase().includes('2(5)(c)') ||
      w.toLowerCase().includes('amendment') ||
      w.includes('2(5)(ग)')
  );

  // Filter regular warnings (exclude "required inputs:" and amendment if shown separately)
  const displayWarnings = (response.warnings || []).filter(
    (w) => !w.toLowerCase().startsWith('required inputs:')
  );

  // Review reasons for NEEDS_REVIEW
  const reviewReasons = (response.warnings || []).filter(
    (w) =>
      w.toLowerCase().includes('amendment') ||
      w.toLowerCase().includes('review') ||
      w.toLowerCase().includes('officer') ||
      w.toLowerCase().includes('verify')
  );

  const handleCopy = async () => {
    const lines: string[] = [];
    lines.push(response.answer);
    lines.push('');

    if (
      response.citations &&
      response.citations.length > 0 &&
      response.status !== 'NOT_FOUND_IN_RULES'
    ) {
      for (const c of response.citations) {
        const page =
          c.pageEnd && c.pageEnd !== c.pageStart
            ? `${c.pageStart}-${c.pageEnd}`
            : `${c.pageStart}`;
        const label = c.passageLabel || c.ruleNumber;
        if (c.validated && c.excerpt) {
          lines.push(`Rule ${label}, p.${page}: "${c.excerpt}"`);
        } else {
          lines.push(`Rule ${label}, p.${page}`);
        }
      }
      lines.push('');
    }

    const calc = response.calculation;
    if (calc?.calculations && calc.calculations.length > 0) {
      lines.push('Calculation Results:');
      for (const item of calc.calculations) {
        const valStr = item.result.text || `${item.result.value} ${item.result.unit}`.trim();
        lines.push(`• ${item.result.name || item.code}: ${valStr} (Rule ${item.rule}, p.${item.pdfPage})`);
      }
      lines.push('');
    } else if (calc && (calc.frontOpenSpaceMeters !== undefined || calc.code)) {
      lines.push('Calculation Results:');
      if (calc.frontOpenSpaceMeters !== undefined) {
        lines.push(`• Front Open Space: ${calc.frontOpenSpaceMeters} m`);
      }
      if (calc.matchedBand) {
        lines.push(`• Matched Band: ${calc.matchedBand}`);
      }
      lines.push('');
    }

    if (response.sourceVersion) {
      lines.push(`Source: ${response.sourceVersion}`);
    }
    if (response.disclaimer) {
      lines.push(`Disclaimer: ${response.disclaimer}`);
    }

    const textToCopy = lines.join('\n');
    try {
      await navigator.clipboard.writeText(textToCopy);
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
    } catch {
      // Fallback if clipboard API is restricted
      const textarea = document.createElement('textarea');
      textarea.value = textToCopy;
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);
      setCopied(true);
      setTimeout(() => setCopied(false), 2500);
    }
  };

  // Distinct styling & headers per status
  const getStatusConfig = () => {
    switch (response.status) {
      case 'ANSWERED':
        return {
          containerBorder: 'border-emerald-300 bg-emerald-50/30',
          headerBg: 'bg-emerald-100/70 border-emerald-200 text-emerald-950',
          icon: <CheckCircle2 className="w-5 h-5 text-emerald-700 shrink-0" />,
          title: t.statusAnswered,
        };
      case 'NOT_FOUND_IN_RULES':
        return {
          containerBorder: 'border-amber-300 bg-amber-50/30',
          headerBg: 'bg-amber-100/70 border-amber-200 text-amber-950',
          icon: <AlertCircle className="w-5 h-5 text-amber-700 shrink-0" />,
          title: t.statusNotFound,
        };
      case 'NEEDS_MORE_INFORMATION':
        return {
          containerBorder: 'border-blue-300 bg-blue-50/30',
          headerBg: 'bg-blue-100/70 border-blue-200 text-blue-950',
          icon: <HelpCircle className="w-5 h-5 text-blue-700 shrink-0" />,
          title: t.statusNeedsMoreInfo,
        };
      case 'NEEDS_REVIEW':
        return {
          containerBorder: 'border-orange-400 bg-orange-50/30',
          headerBg: 'bg-orange-100/70 border-orange-200 text-orange-950',
          icon: <AlertTriangle className="w-5 h-5 text-orange-700 shrink-0" />,
          title: t.statusNeedsReview,
        };
      default:
        return {
          containerBorder: 'border-slate-300 bg-white',
          headerBg: 'bg-slate-100 border-slate-200 text-slate-800',
          icon: <Info className="w-5 h-5 text-slate-700 shrink-0" />,
          title: 'Official Answer',
        };
    }
  };

  const statusConfig = getStatusConfig();
  const showCitations = response.status !== 'NOT_FOUND_IN_RULES';
  const hasNoCitations =
    response.status === 'ANSWERED' && (!response.citations || response.citations.length === 0);

  return (
    <div
      className={`w-full rounded-2xl border ${statusConfig.containerBorder} shadow-sm overflow-hidden bg-white transition-all`}
    >
      {/* Top Status Header */}
      <div
        className={`px-4 sm:px-6 py-3 border-b flex flex-wrap items-center justify-between gap-3 ${statusConfig.headerBg}`}
      >
        <div className="flex items-center gap-2.5">
          {statusConfig.icon}
          <h2 className="text-base sm:text-lg font-bold tracking-tight">
            {statusConfig.title}
          </h2>
        </div>

        {/* Copy for citizen button */}
        <button
          type="button"
          onClick={handleCopy}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-white text-slate-800 border border-slate-300 hover:bg-slate-50 transition-colors shadow-2xs cursor-pointer min-h-[38px]"
        >
          {copied ? (
            <>
              <Check className="w-3.5 h-3.5 text-emerald-600" />
              <span className="text-emerald-700 font-bold">{t.copiedButton}</span>
            </>
          ) : (
            <>
              <Copy className="w-3.5 h-3.5 text-slate-600" />
              <span>{t.copyButton}</span>
            </>
          )}
        </button>
      </div>

      <div className="p-4 sm:p-6 space-y-5">
        {/* NEEDS_REVIEW Reason Banner */}
        {response.status === 'NEEDS_REVIEW' && reviewReasons.length > 0 && (
          <div className="p-3.5 bg-orange-100/80 border border-orange-300 rounded-xl text-orange-950 text-xs sm:text-sm font-semibold flex items-start gap-2.5">
            <AlertTriangle className="w-4 h-4 text-orange-700 shrink-0 mt-0.5" />
            <div className="space-y-1">
              <div>Reason for officer review:</div>
              <ul className="list-disc pl-4 text-xs font-normal space-y-0.5">
                {reviewReasons.map((r, i) => (
                  <li key={i}>{r}</li>
                ))}
              </ul>
            </div>
          </div>
        )}

        {/* Short answer FIRST, in large text (20-22px), lang set */}
        <div className="space-y-1.5">
          <div className="text-xs font-bold uppercase tracking-wider text-slate-500">
            Summary / मुख्य उत्तर:
          </div>
          <div
            lang={responseLang}
            className={`text-xl sm:text-[22px] font-medium leading-relaxed text-slate-950 ${
              isHindiResponse ? 'font-hindi leading-[1.7]' : 'font-sans'
            }`}
          >
            {response.answer}
          </div>
        </div>

        {/* Rule 2(5)(c) / Amendment Prominent Banner */}
        {amendmentWarning && (
          <div className="p-4 bg-amber-50 border-2 border-amber-400 rounded-xl text-amber-950 text-sm shadow-2xs space-y-2">
            <div className="flex items-start gap-2.5">
              <AlertTriangle className="w-5 h-5 text-amber-700 shrink-0 mt-0.5" />
              <div className="flex-1 font-semibold leading-relaxed">
                {t.amendmentBannerText}
              </div>
            </div>
            <div className="pl-7">
              <a
                href="https://teamleaseregtech.com/updates/article/59469/government-of-madhya-pradesh-amendment-to-land-development-rules"
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center gap-1.5 text-xs font-bold text-amber-900 underline hover:text-amber-950 cursor-pointer min-h-[36px]"
              >
                <span>{t.amendmentLinkText}</span>
                <ExternalLink className="w-3.5 h-3.5" />
              </a>
            </div>
          </div>
        )}

        {/* NEEDS_MORE_INFORMATION: Inline Mini-Form */}
        {response.status === 'NEEDS_MORE_INFORMATION' && (
          <MissingInputsForm
            missingInputs={missingInputs.length > 0 ? missingInputs : ['roadWidth', 'plotArea']}
            originalQuestion={originalQuestion}
            onSubmitRefinedQuestion={onRefineQuestion}
            uiLang={uiLang}
          />
        )}

        {/* Calculation Panel (Shape a, b, or c) */}
        {response.calculation && (
          <CalculationPanel payload={response.calculation} uiLang={uiLang} />
        )}

        {/* General Warnings (Shown above citations) */}
        {displayWarnings.length > 0 && (
          <div className="p-3.5 bg-amber-50/70 border border-amber-200 rounded-xl space-y-1 text-xs text-amber-900">
            <div className="font-bold flex items-center gap-1.5 text-amber-950 uppercase tracking-wider text-[11px]">
              <AlertCircle className="w-3.5 h-3.5 text-amber-600" />
              <span>Notices / सूचनाएं:</span>
            </div>
            <ul className="list-disc pl-4 space-y-0.5">
              {displayWarnings.map((warn, wIdx) => (
                <li key={wIdx} className="leading-relaxed">
                  {warn}
                </li>
              ))}
            </ul>
          </div>
        )}

        {/* Warning: Empty citations on ANSWERED */}
        {hasNoCitations && (
          <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg text-amber-900 text-xs flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 text-amber-600 shrink-0" />
            <span>{t.noVerifiedCitation}</span>
          </div>
        )}

        {/* Citations List (Hidden for NOT_FOUND_IN_RULES) */}
        {showCitations && response.citations && response.citations.length > 0 && (
          <CitationList citations={response.citations} uiLang={uiLang} />
        )}
      </div>
    </div>
  );
};
