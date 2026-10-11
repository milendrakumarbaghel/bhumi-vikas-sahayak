import React, { useState } from 'react';
import { X, HelpCircle, ArrowRight, AlertTriangle } from 'lucide-react';
import { OFFICIAL_SAMPLE_QUESTIONS } from '../data/rulesData';
import type { SampleQuestion } from '../types';


interface ExampleQuestionsModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSelectQuestion: (questionText: string) => void;
  language: 'en' | 'hi';
}

export const ExampleQuestionsModal: React.FC<ExampleQuestionsModalProps> = ({
  isOpen,
  onClose,
  onSelectQuestion,
  language,
}) => {
  const [selectedCategory, setSelectedCategory] = useState<string>('All');

  if (!isOpen) return null;

  const categories = ['All', 'Setbacks', 'FAR', 'Validity & Procedure', 'Technical Clearances', 'Fees', 'Out of Scope'];

  const filtered = selectedCategory === 'All'
    ? OFFICIAL_SAMPLE_QUESTIONS
    : OFFICIAL_SAMPLE_QUESTIONS.filter((q) => q.category === selectedCategory);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4 animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl max-w-3xl w-full max-h-[90vh] flex flex-col shadow-2xl border border-slate-200 overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200 bg-[#0c382e] text-white">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-emerald-700/80 rounded-lg text-emerald-200">
              <HelpCircle className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold">
                Official Municipal Clerk Benchmark Questions
              </h3>
              <p className="text-xs text-emerald-300">
                17 Real-world queries tested at Bhopal & Indore Municipal Corporation counters
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-2 text-emerald-300 hover:text-white hover:bg-emerald-800 rounded-lg transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Category filters */}
        <div className="p-3 border-b border-slate-200 bg-slate-50 flex gap-1.5 overflow-x-auto">
          {categories.map((cat) => (
            <button
              key={cat}
              type="button"
              onClick={() => setSelectedCategory(cat)}
              className={`px-3 py-1 rounded-full text-xs font-semibold whitespace-nowrap transition-all cursor-pointer ${
                selectedCategory === cat
                  ? 'bg-[#124d3a] text-white'
                  : 'bg-white text-slate-600 hover:bg-slate-200 border border-slate-200'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* Questions list */}
        <div className="p-5 overflow-y-auto flex-1 space-y-2.5">
          {filtered.map((item: SampleQuestion, idx: number) => {
            const questionText = language === 'hi' ? item.textHi : item.textEn;
            return (
              <button
                key={item.id}
                type="button"
                onClick={() => {
                  onSelectQuestion(questionText);
                  onClose();
                }}
                className="w-full text-left p-3.5 rounded-xl border border-slate-200 hover:border-emerald-500/70 hover:bg-emerald-50/30 transition-all flex items-start justify-between gap-3 group bg-white cursor-pointer"
              >
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-[11px] font-bold text-slate-400">
                      #{idx + 1}
                    </span>
                    <span className="text-[11px] font-semibold text-emerald-800 bg-emerald-50 px-2 py-0.2 rounded-xs border border-emerald-200">
                      {item.category}
                    </span>
                    {item.isOutOfScope ? (
                      <span className="text-[10px] font-medium text-amber-700 bg-amber-50 px-1.5 py-0.2 rounded-xs flex items-center gap-1">
                        <AlertTriangle className="w-3 h-3" />
                        <span>Out of Scope (Correctly Handled)</span>
                      </span>
                    ) : (
                      <span className="text-[10px] text-slate-500 font-medium">
                        Ref: {item.ruleRef}
                      </span>
                    )}
                  </div>

                  <p className={`text-xs font-semibold text-slate-900 group-hover:text-emerald-950 ${language === 'hi' ? 'font-hindi text-sm' : ''}`}>
                    {questionText}
                  </p>
                </div>

                <div className="p-1.5 rounded-lg bg-slate-100 group-hover:bg-emerald-600 group-hover:text-white text-slate-400 transition-colors flex-shrink-0 mt-0.5">
                  <ArrowRight className="w-3.5 h-3.5" />
                </div>
              </button>
            );
          })}
        </div>

        {/* Footer */}
        <div className="px-6 py-3 bg-slate-100 border-t border-slate-200 flex items-center justify-between">
          <span className="text-xs text-slate-500">
            * Clicking any question immediately evaluates it with verified rule grounding
          </span>
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-1.5 bg-[#124d3a] hover:bg-[#165d46] text-white text-xs font-semibold rounded-lg transition-colors cursor-pointer"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
