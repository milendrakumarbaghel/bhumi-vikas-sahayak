import React, { useState } from 'react';
import { X, FolderOpen, BookOpen, ExternalLink, Search } from 'lucide-react';
import { RULE_CATEGORIES } from '../data/rulesData';

interface RuleExplorerModalProps {
  isOpen: boolean;
  onClose: () => void;
  onOpenPdf: (page: number) => void;
  onSelectCategory?: (category: string) => void;
}

export const RuleExplorerModal: React.FC<RuleExplorerModalProps> = ({
  isOpen,
  onClose,
  onOpenPdf,
}) => {
  const [searchTerm, setSearchTerm] = useState('');

  if (!isOpen) return null;

  const filteredCategories = RULE_CATEGORIES.filter(
    (c) =>
      c.titleEn.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.titleHi.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.rulesRange.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4 animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl max-w-4xl w-full max-h-[90vh] flex flex-col shadow-2xl border border-slate-200 overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200 bg-[#0c382e] text-white">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-emerald-700/80 rounded-lg text-emerald-200">
              <FolderOpen className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold">
                MP Bhumi Vikas Rules – Parts & Categories
              </h3>
              <p className="text-xs text-emerald-300">
                105 Statutory Rules organized under 7 major parts
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

        {/* Search */}
        <div className="p-4 border-b border-slate-200 bg-slate-50">
          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search rule parts, topics, setbacks, fire safety, fees..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-9 pr-4 py-2 bg-white text-xs border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-600 focus:outline-hidden"
            />
          </div>
        </div>

        {/* Categories list */}
        <div className="p-6 overflow-y-auto flex-1 space-y-3.5">
          {filteredCategories.map((cat) => (
            <div
              key={cat.id}
              className="border border-slate-200 rounded-xl p-4 hover:border-emerald-500/50 hover:bg-emerald-50/20 transition-all bg-white"
            >
              <div className="flex items-center justify-between gap-3 mb-1.5">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-bold text-emerald-800 bg-emerald-100/70 px-2 py-0.5 rounded-sm">
                    {cat.part}
                  </span>
                  <h4 className="text-sm font-bold text-slate-900">
                    {cat.titleEn}
                  </h4>
                  <span className="text-xs text-slate-500 font-hindi">
                    ({cat.titleHi})
                  </span>
                </div>
                <div className="flex items-center gap-2 flex-shrink-0">
                  <span className="text-xs font-semibold text-slate-700 bg-slate-100 px-2 py-0.5 rounded-sm">
                    {cat.rulesRange}
                  </span>
                  <span className="text-xs text-slate-500">
                    {cat.pages}
                  </span>
                </div>
              </div>

              <p className="text-xs text-slate-600 leading-relaxed mb-3">
                {cat.description}
              </p>

              <div className="flex items-center justify-end gap-2">
                <button
                  type="button"
                  onClick={() => {
                    const startPage = parseInt(cat.pages.match(/\d+/)?.[0] || '1', 10);
                    onOpenPdf(startPage);
                  }}
                  className="flex items-center gap-1.5 text-xs font-semibold text-emerald-800 hover:text-emerald-950 bg-emerald-50 hover:bg-emerald-100/80 px-2.5 py-1 rounded-md transition-colors cursor-pointer"
                >
                  <BookOpen className="w-3.5 h-3.5" />
                  <span>Open in Gazette PDF</span>
                  <ExternalLink className="w-3 h-3 ml-0.5" />
                </button>
              </div>
            </div>
          ))}
        </div>

        {/* Footer */}
        <div className="px-6 py-3 bg-slate-100 border-t border-slate-200 flex justify-end">
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
