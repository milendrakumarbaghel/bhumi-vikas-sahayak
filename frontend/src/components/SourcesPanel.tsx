import React from 'react';
import type { Citation, RelatedRule } from '../types';
import { FileText, Scale, ExternalLink, ChevronRight, CheckCircle2 } from 'lucide-react';

interface SourcesPanelProps {
  citations: Citation[];
  relatedRules: RelatedRule[];
  onOpenPdf: (page: number) => void;
  onSelectRelatedRule?: (ruleNumber: string) => void;
}

export const SourcesPanel: React.FC<SourcesPanelProps> = ({
  citations,
  relatedRules,
  onOpenPdf,
  onSelectRelatedRule,
}) => {
  return (
    <aside className="w-80 flex-shrink-0 bg-transparent flex flex-col gap-4 p-4 select-none">
      {/* Sources Section */}
      <div className="bg-white rounded-xl border border-slate-200/90 shadow-xs p-4">
        {/* Header */}
        <div className="flex items-center justify-between mb-3.5">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-emerald-50 text-emerald-800">
              <FileText className="w-4 h-4" />
            </div>
            <h2 className="text-sm font-bold text-slate-900 tracking-tight">Sources</h2>
          </div>
          <span className="text-[11px] font-semibold text-emerald-800 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded-full">
            {citations.length} relevant sections
          </span>
        </div>

        {/* Source Cards */}
        <div className="space-y-3">
          {citations.length === 0 ? (
            <div className="text-xs text-slate-500 py-3 text-center italic">
              No citations required for this query.
            </div>
          ) : (
            citations.map((cite, index) => (
              <div
                key={`${cite.ruleNumber}-${index}`}
                className="bg-slate-50/70 hover:bg-slate-50 border border-slate-200 rounded-lg p-3 transition-colors"
              >
                <div className="flex items-center justify-between gap-2 mb-1">
                  <div className="flex items-center gap-1.5">
                    <span className="font-bold text-xs text-slate-900 font-sans">
                      {cite.ruleNumber}
                    </span>
                    {cite.validated && (
                      <span title="Statutory verified clause" className="inline-flex">
                        <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                      </span>
                    )}
                  </div>

                  <span className="text-[10px] font-semibold text-slate-600 bg-slate-200/80 px-1.5 py-0.5 rounded-sm">
                    Page {cite.pageStart}
                  </span>
                </div>

                <p className="text-[11px] text-slate-500 font-medium mb-2 leading-tight">
                  {cite.title}
                </p>

                {cite.hindiExcerpt && (
                  <div className="bg-white rounded-md p-2 border border-slate-200/70 text-[11px] text-slate-700 font-hindi italic leading-relaxed mb-2.5">
                    {cite.hindiExcerpt}
                  </div>
                )}

                {cite.excerpt && !cite.hindiExcerpt && (
                  <div className="bg-white rounded-md p-2 border border-slate-200/70 text-[11px] text-slate-700 leading-relaxed mb-2.5">
                    "{cite.excerpt}"
                  </div>
                )}

                <button
                  type="button"
                  onClick={() => onOpenPdf(cite.pageStart)}
                  className="w-full flex items-center justify-center gap-1.5 py-1 px-2.5 rounded-md text-[11px] font-medium text-slate-700 bg-white hover:bg-emerald-50 hover:text-emerald-800 border border-slate-200 hover:border-emerald-300 transition-all cursor-pointer"
                >
                  <span>View in PDF</span>
                  <ExternalLink className="w-3 h-3 text-slate-400 group-hover:text-emerald-700" />
                </button>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Related Rules Section */}
      <div className="bg-white rounded-xl border border-slate-200/90 shadow-xs p-4">
        {/* Header */}
        <div className="flex items-center gap-2 mb-3">
          <div className="p-1.5 rounded-lg bg-amber-50 text-amber-800">
            <Scale className="w-4 h-4" />
          </div>
          <h2 className="text-sm font-bold text-slate-900 tracking-tight">Related Rules</h2>
        </div>

        {/* List */}
        <div className="space-y-1.5">
          {relatedRules.map((rule, idx) => (
            <button
              key={`${rule.ruleNumber}-${idx}`}
              type="button"
              onClick={() => onSelectRelatedRule?.(rule.ruleNumber)}
              className={`w-full flex items-center justify-between p-2 rounded-lg text-left transition-all cursor-pointer border ${
                rule.isActive
                  ? 'bg-emerald-50/80 border-emerald-300 text-emerald-950 font-semibold'
                  : 'bg-transparent border-transparent hover:bg-slate-50 text-slate-700'
              }`}
            >
              <div className="leading-tight pr-2">
                <span className="text-xs font-bold mr-1.5">
                  {rule.ruleNumber}
                </span>
                <span className="text-xs font-normal text-slate-600 block sm:inline">
                  {rule.title}
                </span>
              </div>

              <div className="flex items-center gap-1 flex-shrink-0">
                <span className="text-[10px] text-slate-500 font-medium whitespace-nowrap bg-slate-100 px-1.5 py-0.5 rounded-xs">
                  {rule.pages}
                </span>
                <ChevronRight className="w-3.5 h-3.5 text-slate-400" />
              </div>
            </button>
          ))}
        </div>
      </div>
    </aside>
  );
};
