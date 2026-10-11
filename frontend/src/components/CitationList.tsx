import React, { useState } from 'react';
import type { Citation, RuleDetail } from '../types';
import { getRuleDetail, getPdfUrl, highlightExcerpt } from '../services/api';
import type { Language } from '../i18n';
import { i18n } from '../i18n';
import { CheckCircle, AlertTriangle, ExternalLink, ChevronDown, ChevronUp } from 'lucide-react';

interface CitationListProps {
  citations: Citation[];
  uiLang: Language;
}

export const CitationList: React.FC<CitationListProps> = ({ citations, uiLang }) => {
  const t = i18n[uiLang];
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [ruleDetails, setRuleDetails] = useState<Record<string, RuleDetail>>({});
  const [loadingRules, setLoadingRules] = useState<Record<string, boolean>>({});
  const [fetchError, setFetchError] = useState<Record<string, string>>({});

  if (!citations || citations.length === 0) return null;

  const handleToggle = async (cite: Citation) => {
    const key = `${cite.ruleNumber}-${cite.passageLabel}-${cite.pageStart}`;
    if (expandedId === key) {
      setExpandedId(null);
      return;
    }

    setExpandedId(key);

    // Fetch full rule details if not already loaded and validated
    const ruleNum = cite.ruleNumber;
    if (cite.validated && !ruleDetails[ruleNum] && !loadingRules[ruleNum]) {
      setLoadingRules((prev) => ({ ...prev, [ruleNum]: true }));
      try {
        const detail = await getRuleDetail(ruleNum);
        setRuleDetails((prev) => ({ ...prev, [ruleNum]: detail }));
      } catch (err: any) {
        setFetchError((prev) => ({ ...prev, [ruleNum]: err?.message || 'Failed to load rule' }));
      } finally {
        setLoadingRules((prev) => ({ ...prev, [ruleNum]: false }));
      }
    }
  };

  return (
    <div className="w-full mt-4 space-y-2">
      <div className="text-xs font-bold uppercase tracking-wider text-slate-500">
        Statutory Citations / नियम उद्धरण:
      </div>

      <div className="space-y-2">
        {citations.map((cite, index) => {
          const key = `${cite.ruleNumber}-${cite.passageLabel}-${cite.pageStart}`;
          const isExpanded = expandedId === key;
          const pageRangeText = cite.pageEnd && cite.pageEnd !== cite.pageStart
            ? `p.${cite.pageStart}-${cite.pageEnd}`
            : `p.${cite.pageStart}`;
          const chipLabel = `Rule ${cite.passageLabel || cite.ruleNumber} · ${pageRangeText}`;

          const ruleNum = cite.ruleNumber;
          const detail = ruleDetails[ruleNum];
          const isLoadingRule = loadingRules[ruleNum];

          return (
            <div
              key={key || index}
              className="border border-slate-200 rounded-xl bg-white overflow-hidden transition-all shadow-2xs"
            >
              {/* Citation Chip Trigger */}
              <button
                type="button"
                onClick={() => handleToggle(cite)}
                className="w-full flex items-center justify-between p-3 sm:px-4 text-left hover:bg-slate-50 transition-colors cursor-pointer min-h-[44px]"
              >
                <div className="flex items-center gap-2.5 flex-wrap">
                  <span className="text-sm font-bold text-slate-900 font-mono">
                    {chipLabel}
                  </span>
                  <span className="text-xs text-slate-600 font-medium hidden sm:inline">
                    {cite.title}
                  </span>

                  {/* Verification Badge */}
                  {cite.validated ? (
                    <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-emerald-800 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded-sm">
                      <CheckCircle className="w-3 h-3 text-emerald-600" />
                      <span>{t.citationVerified}</span>
                    </span>
                  ) : (
                    <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-amber-800 bg-amber-50 border border-amber-200 px-2 py-0.5 rounded-sm">
                      <AlertTriangle className="w-3 h-3 text-amber-600" />
                      <span>{t.citationUnverified}</span>
                    </span>
                  )}
                </div>

                <div className="p-1 text-slate-400">
                  {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                </div>
              </button>

              {/* Expanded Details */}
              {isExpanded && (
                <div className="p-4 bg-slate-50/70 border-t border-slate-200 space-y-3 text-xs">
                  {/* Verified Excerpt or Unverified Warning */}
                  {cite.validated ? (
                    <div className="space-y-2">
                      <div className="font-semibold text-slate-700">
                        Exact Quoted Excerpt:
                      </div>
                      <blockquote className="border-l-4 border-[#0f2942] pl-3 py-1 italic bg-white rounded-r-md text-slate-800 leading-relaxed font-serif text-[13px]">
                        "{cite.excerpt}"
                      </blockquote>

                      {/* Full Passage / Rule Text with Highlighted Excerpt */}
                      {isLoadingRule && (
                        <div className="text-slate-500 italic py-1">
                          {t.loadingRuleText}
                        </div>
                      )}

                      {fetchError[ruleNum] && (
                        <div className="text-slate-500 italic py-1 text-xs">
                          {fetchError[ruleNum]}
                        </div>
                      )}

                      {detail && (
                        <div className="mt-3 pt-3 border-t border-slate-200 space-y-1.5">
                          <div className="font-semibold text-slate-700">
                            Complete Statutory Rule Text ({detail.title}):
                          </div>
                          <div className="p-3 bg-white border border-slate-200 rounded-lg max-h-60 overflow-y-auto text-slate-700 leading-relaxed font-sans text-xs whitespace-pre-line">
                            {highlightExcerpt(detail.text, cite.excerpt).map((chunk, cIdx) => (
                              <span
                                key={cIdx}
                                className={chunk.isHighlight ? 'bg-amber-200 font-semibold text-slate-900 px-0.5 rounded-xs' : ''}
                              >
                                {chunk.text}
                              </span>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                  ) : (
                    <div className="p-3 bg-amber-50 border border-amber-200 rounded-lg text-amber-900 font-medium">
                      {t.citationUnverified}
                    </div>
                  )}

                  {/* Open PDF link */}
                  <div className="flex justify-end pt-1">
                    <a
                      href={getPdfUrl(cite.pageStart)}
                      target="_blank"
                      rel="noreferrer"
                      className="inline-flex items-center gap-1.5 text-xs font-bold text-blue-700 hover:text-blue-900 hover:underline min-h-[44px] items-center cursor-pointer"
                    >
                      <span>{t.openPdfPage} (p.{cite.pageStart})</span>
                      <ExternalLink className="w-3.5 h-3.5" />
                    </a>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};
