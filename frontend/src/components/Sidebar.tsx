import React from 'react';
import {
  MessageSquare,
  HelpCircle,
  FolderOpen,
  Calculator,
  Bookmark,
  History,
  FileText,
  ExternalLink,
} from 'lucide-react';

interface SidebarProps {
  activeTab: string;
  onTabChange: (tab: string) => void;
  onNewChat: () => void;
  onOpenPdf: (page?: number) => void;
  onOpenExamples: () => void;
  onOpenCategories: () => void;
  onOpenCalculators: () => void;
  onOpenSaved: () => void;
  savedCount: number;
}

export const Sidebar: React.FC<SidebarProps> = ({
  activeTab,
  onTabChange,
  onNewChat,
  onOpenPdf,
  onOpenExamples,
  onOpenCategories,
  onOpenCalculators,
  onOpenSaved,
}) => {
  return (
    <aside className="w-64 flex-shrink-0 bg-[#0e3a2f] text-emerald-50 flex flex-col justify-between p-3.5 select-none relative z-20">
      {/* Top Nav actions */}
      <div className="space-y-1.5">
        {/* New Chat Button */}
        <button
          type="button"
          onClick={onNewChat}
          className="w-full flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-semibold bg-[#155243] hover:bg-[#1a5f4f] text-white border border-emerald-500/30 transition-all shadow-2xs cursor-pointer group"
        >
          <MessageSquare className="w-4 h-4 text-emerald-200 group-hover:scale-105 transition-transform" />
          <span>New Chat</span>
        </button>

        <div className="pt-2 space-y-1">
          {/* Example Questions */}
          <button
            type="button"
            onClick={onOpenExamples}
            className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-xs font-medium transition-all text-left cursor-pointer ${
              activeTab === 'examples'
                ? 'bg-emerald-950/70 text-white font-semibold'
                : 'text-emerald-100/80 hover:bg-[#12483b] hover:text-white'
            }`}
          >
            <HelpCircle className="w-4 h-4 text-emerald-300/80" />
            <span>Example Questions</span>
          </button>

          {/* Rule Categories */}
          <button
            type="button"
            onClick={onOpenCategories}
            className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-xs font-medium transition-all text-left cursor-pointer ${
              activeTab === 'categories'
                ? 'bg-emerald-950/70 text-white font-semibold'
                : 'text-emerald-100/80 hover:bg-[#12483b] hover:text-white'
            }`}
          >
            <FolderOpen className="w-4 h-4 text-emerald-300/80" />
            <span>Rule Categories</span>
          </button>

          {/* Calculators */}
          <button
            type="button"
            onClick={onOpenCalculators}
            className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-xs font-medium transition-all text-left cursor-pointer ${
              activeTab === 'calculators'
                ? 'bg-emerald-950/70 text-white font-semibold'
                : 'text-emerald-100/80 hover:bg-[#12483b] hover:text-white'
            }`}
          >
            <Calculator className="w-4 h-4 text-emerald-300/80" />
            <span>Calculators</span>
          </button>

          {/* Saved Answers */}
          <button
            type="button"
            onClick={onOpenSaved}
            className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-xs font-medium transition-all text-left cursor-pointer ${
              activeTab === 'saved'
                ? 'bg-emerald-950/70 text-white font-semibold'
                : 'text-emerald-100/80 hover:bg-[#12483b] hover:text-white'
            }`}
          >
            <Bookmark className="w-4 h-4 text-emerald-300/80" />
            <span>Saved Answers</span>
          </button>

          {/* Recent Chats */}
          <button
            type="button"
            onClick={() => onTabChange('recent')}
            className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-xs font-medium transition-all text-left cursor-pointer ${
              activeTab === 'recent'
                ? 'bg-emerald-950/70 text-white font-semibold'
                : 'text-emerald-100/80 hover:bg-[#12483b] hover:text-white'
            }`}
          >
            <History className="w-4 h-4 text-emerald-300/80" />
            <span>Recent Chats</span>
          </button>
        </div>
      </div>

      {/* Bottom Architectural Illustration & Document Metadata Card */}
      <div className="mt-8 pt-4 border-t border-emerald-900/50 relative">
        {/* Architectural building / dome illustration in watermark */}
        <div className="absolute inset-x-0 bottom-28 h-32 opacity-20 pointer-events-none flex items-end justify-center overflow-hidden">
          <svg viewBox="0 0 200 130" className="w-48 h-auto text-emerald-200" fill="currentColor">
            {/* Dome and finial */}
            <path d="M100 5 Q102 0 104 5 L104 15 Q125 18 135 45 L65 45 Q75 18 96 15 Z" />
            <rect x="60" y="45" width="80" height="8" rx="1" />
            {/* Pillars and colonnade */}
            <rect x="52" y="53" width="96" height="65" fillOpacity="0.4" />
            <rect x="62" y="60" width="10" height="40" rx="3" fill="#0e3a2f" />
            <rect x="82" y="60" width="10" height="40" rx="3" fill="#0e3a2f" />
            <rect x="102" y="60" width="10" height="40" rx="3" fill="#0e3a2f" />
            <rect x="122" y="60" width="10" height="40" rx="3" fill="#0e3a2f" />
            <rect x="45" y="118" width="110" height="12" rx="2" fillOpacity="0.6" />
          </svg>
        </div>

        {/* Document Card */}
        <div className="relative z-10 bg-[#092b23] rounded-xl p-3.5 border border-emerald-800/70 shadow-inner">
          <div className="flex items-start gap-2.5">
            <div className="p-1.5 rounded-lg bg-[#145344] text-emerald-300 mt-0.5">
              <FileText className="w-4 h-4" />
            </div>
            <div className="text-[11px] leading-snug">
              <div className="font-bold text-white text-xs">Document</div>
              <div className="text-emerald-200 font-medium mt-0.5">MP Bhumi Vikas Niyam</div>
              <div className="text-emerald-400/80 text-[10px]">Source: Official Gazette</div>
              <div className="text-emerald-400/80 text-[10px]">Pages: 108</div>
              <div className="text-emerald-400/80 text-[10px]">Version: 2025-03-06</div>
            </div>
          </div>

          <button
            type="button"
            onClick={() => onOpenPdf(1)}
            className="mt-3 w-full flex items-center justify-center gap-1.5 py-1.5 px-3 rounded-lg bg-[#12493c] hover:bg-[#175b4b] text-emerald-100 hover:text-white text-xs font-semibold border border-emerald-700/60 transition-all cursor-pointer"
          >
            <FileText className="w-3.5 h-3.5 text-emerald-300" />
            <span>View PDF</span>
            <ExternalLink className="w-3 h-3 text-emerald-400 ml-0.5" />
          </button>
        </div>
      </div>
    </aside>
  );
};
