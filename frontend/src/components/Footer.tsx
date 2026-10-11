import React from 'react';
import type { Language } from '../i18n';
import { i18n } from '../i18n';

interface FooterProps {
  sourceVersion?: string;
  uiLang: Language;
}

export const Footer: React.FC<FooterProps> = ({ sourceVersion, uiLang }) => {
  const t = i18n[uiLang];
  const displayVersion = sourceVersion || t.defaultSourceVersion;

  return (
    <footer className="fixed bottom-0 left-0 right-0 h-12 bg-slate-900 text-slate-300 px-4 sm:px-8 border-t border-slate-800 text-xs z-50 flex items-center shadow-lg">
      <div className="w-full max-w-5xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-1 sm:gap-4 text-center sm:text-left">
        <div className="font-medium text-slate-300 truncate max-w-full">
          <span className="text-slate-400 font-semibold mr-1.5">Source:</span>
          <span className="text-slate-200">{displayVersion}</span>
        </div>
        <div className="text-[11px] text-slate-400 truncate max-w-full sm:max-w-xl">
          {t.footerDisclaimer}
        </div>
      </div>
    </footer>
  );
};
