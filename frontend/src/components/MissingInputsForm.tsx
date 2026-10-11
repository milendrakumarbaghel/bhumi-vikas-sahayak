import React, { useState } from 'react';
import type { Language } from '../i18n';
import { i18n } from '../i18n';

interface MissingInputsFormProps {
  missingInputs: string[];
  originalQuestion: string;
  onSubmitRefinedQuestion: (newQuestion: string) => void;
  uiLang: Language;
}

export const MissingInputsForm: React.FC<MissingInputsFormProps> = ({
  missingInputs,
  originalQuestion,
  onSubmitRefinedQuestion,
  uiLang,
}) => {
  const t = i18n[uiLang];
  const [formValues, setFormValues] = useState<Record<string, string>>({});
  const [unitValues, setUnitValues] = useState<Record<string, string>>({});

  const handleInputChange = (field: string, val: string) => {
    setFormValues((prev) => ({ ...prev, [field]: val }));
  };

  const handleUnitChange = (field: string, unit: string) => {
    setUnitValues((prev) => ({ ...prev, [field]: unit }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const additions: string[] = [];

    for (const rawField of missingInputs) {
      const val = formValues[rawField];
      if (val && val.trim()) {
        const unit = unitValues[rawField] || getDefaultUnit(rawField);
        const readableName = formatFieldLabel(rawField);
        additions.push(`${readableName}: ${val.trim()} ${unit}`.trim());
      }
    }

    if (additions.length > 0) {
      const separator = originalQuestion.endsWith('.') || originalQuestion.endsWith('?') || originalQuestion.endsWith('।') ? ' ' : '. ';
      const appendedText = additions.join('. ') + '.';
      onSubmitRefinedQuestion(`${originalQuestion}${separator}${appendedText}`);
    }
  };

  function getDefaultUnit(fieldName: string): string {
    const fn = fieldName.toLowerCase();
    if (fn.includes('area') || fn.includes('क्षेत्र')) return 'sq m';
    if (fn.includes('width') || fn.includes('height') || fn.includes('चौड़ाई') || fn.includes('ऊंचाई')) return 'm';
    return '';
  }

  function formatFieldLabel(fieldName: string): string {
    return fieldName
      .replace(/([A-Z])/g, ' $1')
      .replace(/_/g, ' ')
      .replace(/^\w/, (c) => c.toUpperCase())
      .trim();
  }

  function getFieldType(fieldName: string): 'area' | 'length' | 'use' | 'text' {
    const fn = fieldName.toLowerCase();
    if (fn.includes('use') || fn.includes('उपयोग') || fn.includes('category') || fn.includes('type')) return 'use';
    if (fn.includes('area') || fn.includes('क्षेत्रफल') || fn.includes('size')) return 'area';
    if (fn.includes('width') || fn.includes('height') || fn.includes('setback') || fn.includes('road')) return 'length';
    return 'text';
  }

  return (
    <form onSubmit={handleSubmit} className="mt-3 p-4 bg-white/90 border border-blue-200 rounded-xl space-y-3">
      <div className="text-xs font-bold uppercase tracking-wider text-blue-900">
        {t.provideDetails}
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        {missingInputs.map((inputField) => {
          const type = getFieldType(inputField);
          const currentUnit = unitValues[inputField] || getDefaultUnit(inputField);

          if (type === 'use') {
            return (
              <div key={inputField}>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  {formatFieldLabel(inputField)}
                </label>
                <select
                  value={formValues[inputField] || 'Residential'}
                  onChange={(e) => handleInputChange(inputField, e.target.value)}
                  className="w-full text-sm border border-slate-300 rounded-lg p-2 bg-white text-slate-900 focus:outline-hidden focus:ring-2 focus:ring-[#0f2942]"
                >
                  <option value="Residential">{t.residential}</option>
                  <option value="Commercial">{t.commercial}</option>
                  <option value="Industrial">{t.industrial}</option>
                </select>
              </div>
            );
          }

          if (type === 'area') {
            return (
              <div key={inputField}>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  {formatFieldLabel(inputField)}
                </label>
                <div className="flex gap-2">
                  <input
                    type="number"
                    step="any"
                    required
                    placeholder="e.g. 200"
                    value={formValues[inputField] || ''}
                    onChange={(e) => handleInputChange(inputField, e.target.value)}
                    className="flex-1 text-sm border border-slate-300 rounded-lg p-2 text-slate-900 focus:outline-hidden focus:ring-2 focus:ring-[#0f2942]"
                  />
                  <select
                    value={currentUnit}
                    onChange={(e) => handleUnitChange(inputField, e.target.value)}
                    className="w-24 text-sm border border-slate-300 rounded-lg p-2 bg-white text-slate-900 focus:outline-hidden"
                  >
                    <option value="sq m">{t.sqm}</option>
                    <option value="sq ft">{t.sqft}</option>
                    <option value="gaj">{t.gaj}</option>
                  </select>
                </div>
              </div>
            );
          }

          if (type === 'length') {
            return (
              <div key={inputField}>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  {formatFieldLabel(inputField)}
                </label>
                <div className="flex gap-2">
                  <input
                    type="number"
                    step="any"
                    required
                    placeholder="e.g. 9"
                    value={formValues[inputField] || ''}
                    onChange={(e) => handleInputChange(inputField, e.target.value)}
                    className="flex-1 text-sm border border-slate-300 rounded-lg p-2 text-slate-900 focus:outline-hidden focus:ring-2 focus:ring-[#0f2942]"
                  />
                  <select
                    value={currentUnit}
                    onChange={(e) => handleUnitChange(inputField, e.target.value)}
                    className="w-24 text-sm border border-slate-300 rounded-lg p-2 bg-white text-slate-900 focus:outline-hidden"
                  >
                    <option value="m">{t.meters}</option>
                    <option value="ft">{t.feet}</option>
                  </select>
                </div>
              </div>
            );
          }

          return (
            <div key={inputField}>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                {formatFieldLabel(inputField)}
              </label>
              <input
                type="text"
                required
                value={formValues[inputField] || ''}
                onChange={(e) => handleInputChange(inputField, e.target.value)}
                className="w-full text-sm border border-slate-300 rounded-lg p-2 text-slate-900 focus:outline-hidden focus:ring-2 focus:ring-[#0f2942]"
              />
            </div>
          );
        })}
      </div>

      <div className="flex justify-end pt-2">
        <button
          type="submit"
          className="px-4 py-2 bg-[#0f2942] hover:bg-[#153a5c] text-white text-xs font-bold rounded-lg transition-colors cursor-pointer min-h-[40px]"
        >
          {t.appendAndAsk}
        </button>
      </div>
    </form>
  );
};
