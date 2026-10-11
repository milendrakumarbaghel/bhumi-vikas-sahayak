import React, { useRef, useEffect } from 'react';
import { Send, XCircle } from 'lucide-react';
import type { Language } from '../i18n';
import { i18n } from '../i18n';
import type { ValidationError } from '../types';

interface QuestionBoxProps {
  question: string;
  onQuestionChange: (val: string) => void;
  onSubmit: () => void;
  isLoading: boolean;
  onCancel: () => void;
  languageMode: 'auto' | 'en' | 'hi';
  onLanguageModeChange: (mode: 'auto' | 'en' | 'hi') => void;
  uiLang: Language;
  validationErrors: ValidationError[];
}

export const QuestionBox: React.FC<QuestionBoxProps> = ({
  question,
  onQuestionChange,
  onSubmit,
  isLoading,
  onCancel,
  languageMode,
  onLanguageModeChange,
  uiLang,
  validationErrors,
}) => {
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const t = i18n[uiLang];

  // Auto resize height
  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      textareaRef.current.style.height = `${Math.max(110, textareaRef.current.scrollHeight)}px`;
    }
  }, [question]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      if (!isLoading && question.trim().length >= 3) {
        onSubmit();
      }
    }
  };

  const charCount = question.length;
  const isOverLimit = charCount > 2000;
  const isTooShort = charCount > 0 && charCount < 3;

  return (
    <div className="w-full bg-white rounded-xl border border-slate-300 shadow-2xs p-4 sm:p-5 transition-all focus-within:border-[#0f2942] focus-within:ring-2 focus-within:ring-[#0f2942]/15">
      {/* Top Bar: Language mode toggle & instructions */}
      <div className="flex flex-wrap items-center justify-between gap-2 pb-2.5 border-b border-slate-100 mb-3">
        <label htmlFor="clerk-question-input" className="text-sm font-semibold text-slate-700">
          Question / प्रश्न
        </label>

        {/* Language Toggle: Auto | English | हिन्दी */}
        <div className="inline-flex rounded-lg bg-slate-100 p-0.5 border border-slate-200">
          <button
            type="button"
            onClick={() => onLanguageModeChange('auto')}
            className={`px-3 py-1 text-xs font-semibold rounded-md transition-colors min-h-[32px] cursor-pointer ${
              languageMode === 'auto'
                ? 'bg-[#0f2942] text-white shadow-2xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            {t.langAuto}
          </button>
          <button
            type="button"
            onClick={() => onLanguageModeChange('en')}
            className={`px-3 py-1 text-xs font-semibold rounded-md transition-colors min-h-[32px] cursor-pointer ${
              languageMode === 'en'
                ? 'bg-[#0f2942] text-white shadow-2xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            English
          </button>
          <button
            type="button"
            onClick={() => onLanguageModeChange('hi')}
            className={`px-3 py-1 text-xs font-semibold rounded-md transition-colors font-hindi min-h-[32px] cursor-pointer ${
              languageMode === 'hi'
                ? 'bg-[#0f2942] text-white shadow-2xs'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            हिन्दी
          </button>
        </div>
      </div>

      {/* Multiline textarea (min 3 rows, 18px+ text) */}
      <textarea
        id="clerk-question-input"
        ref={textareaRef}
        rows={3}
        value={question}
        onChange={(e) => onQuestionChange(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder={t.placeholder}
        disabled={isLoading}
        lang={uiLang}
        className="w-full min-h-[100px] max-h-[260px] overflow-y-auto text-lg sm:text-[19px] leading-relaxed text-slate-900 placeholder:text-slate-400 bg-transparent resize-none border-0 p-0 focus:outline-hidden font-sans"
      />

      {/* 422 Field Validation Errors */}
      {validationErrors.length > 0 && (
        <div className="mt-2 text-sm text-red-600 space-y-0.5">
          {validationErrors.map((err, idx) => (
            <div key={idx} className="flex items-center gap-1.5">
              <span className="font-semibold">•</span>
              <span>{err.field ? `${err.field}: ` : ''}{err.message}</span>
            </div>
          ))}
        </div>
      )}

      {/* Action and counter footer */}
      <div className="flex flex-wrap items-center justify-between gap-3 pt-3 border-t border-slate-100 mt-2">
        {/* Character Counter & Helper text */}
        <div className="flex items-center gap-3 text-xs">
          <span className={`font-mono ${isOverLimit ? 'text-red-600 font-bold' : isTooShort ? 'text-amber-600' : 'text-slate-500'}`}>
            {charCount} / 2000
          </span>
          <span className="text-slate-400 hidden sm:inline">
            (Enter to ask, Shift+Enter for newline)
          </span>
        </div>

        {/* Action Buttons: Ask / Cancel */}
        <div className="flex items-center gap-2">
          {isLoading ? (
            <button
              type="button"
              onClick={onCancel}
              className="flex items-center gap-1.5 px-4 py-2.5 rounded-lg text-sm font-semibold text-red-700 bg-red-50 hover:bg-red-100 border border-red-200 transition-colors cursor-pointer min-h-[44px]"
            >
              <XCircle className="w-4 h-4" />
              <span>{t.cancelButton}</span>
            </button>
          ) : (
            <button
              type="button"
              onClick={onSubmit}
              disabled={isLoading || charCount < 3 || isOverLimit}
              className="flex items-center justify-center gap-2 px-6 py-2.5 rounded-lg text-base font-semibold text-white bg-[#0f2942] hover:bg-[#153a5c] disabled:opacity-40 disabled:cursor-not-allowed transition-all shadow-xs cursor-pointer min-h-[44px]"
            >
              <span>{t.askButton}</span>
              <Send className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
