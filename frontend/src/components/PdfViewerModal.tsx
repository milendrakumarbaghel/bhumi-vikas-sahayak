import React, { useState } from 'react';
import { X, Download, FileText, ChevronLeft, ChevronRight, ExternalLink } from 'lucide-react';

interface PdfViewerModalProps {
  isOpen: boolean;
  onClose: () => void;
  initialPage?: number;
}

export const PdfViewerModal: React.FC<PdfViewerModalProps> = ({
  isOpen,
  onClose,
  initialPage = 1,
}) => {
  const [currentPage, setCurrentPage] = useState<number>(initialPage);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-xs p-3 sm:p-6 animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl max-w-5xl w-full h-[92vh] flex flex-col shadow-2xl border border-slate-300 overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-5 py-3 border-b border-slate-200 bg-slate-900 text-white">
          <div className="flex items-center gap-3">
            <div className="p-1.5 bg-emerald-700 rounded-lg text-white">
              <FileText className="w-5 h-5" />
            </div>
            <div>
              <div className="text-sm font-bold tracking-tight">
                MP Bhumi Vikas Rules, 2012 (Consolidated Official Gazette)
              </div>
              <div className="text-[11px] text-slate-300">
                108 Pages • Town & Country Planning Department, Govt. of Madhya Pradesh
              </div>
            </div>
          </div>

          <div className="flex items-center gap-3">
            {/* Quick page jumper */}
            <div className="flex items-center gap-1.5 bg-slate-800 px-2.5 py-1 rounded-lg border border-slate-700 text-xs">
              <span className="text-slate-400">Page</span>
              <input
                type="number"
                min={1}
                max={108}
                value={currentPage}
                onChange={(e) => {
                  const val = parseInt(e.target.value, 10);
                  if (val >= 1 && val <= 108) setCurrentPage(val);
                }}
                className="w-12 bg-slate-700 text-white text-center font-bold px-1 py-0.5 rounded-sm border border-slate-600 focus:outline-hidden"
              />
              <span className="text-slate-400">/ 108</span>
              <button
                type="button"
                onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                disabled={currentPage <= 1}
                className="p-1 hover:bg-slate-700 rounded-sm disabled:opacity-30 cursor-pointer ml-1"
              >
                <ChevronLeft className="w-3.5 h-3.5" />
              </button>
              <button
                type="button"
                onClick={() => setCurrentPage((p) => Math.min(108, p + 1))}
                disabled={currentPage >= 108}
                className="p-1 hover:bg-slate-700 rounded-sm disabled:opacity-30 cursor-pointer"
              >
                <ChevronRight className="w-3.5 h-3.5" />
              </button>
            </div>

            {/* Direct download */}
            <a
              href="/MP_Bhumi_Vikas_Rules_2012.pdf"
              download="MP_Bhumi_Vikas_Rules_2012.pdf"
              className="p-2 text-slate-300 hover:text-white hover:bg-slate-800 rounded-lg transition-colors cursor-pointer"
              title="Download Original Gazette PDF"
            >
              <Download className="w-4 h-4" />
            </a>

            {/* Open in new tab */}
            <a
              href={`/MP_Bhumi_Vikas_Rules_2012.pdf#page=${currentPage}`}
              target="_blank"
              rel="noreferrer"
              className="p-2 text-slate-300 hover:text-white hover:bg-slate-800 rounded-lg transition-colors cursor-pointer"
              title="Open PDF in new browser tab"
            >
              <ExternalLink className="w-4 h-4" />
            </a>

            {/* Close */}
            <button
              type="button"
              onClick={onClose}
              className="p-2 text-slate-300 hover:text-white hover:bg-slate-800 rounded-lg transition-colors cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* PDF viewer iframe */}
        <div className="flex-1 w-full bg-slate-100 relative">
          <iframe
            src={`/MP_Bhumi_Vikas_Rules_2012.pdf#page=${currentPage}&toolbar=1&navpanes=1`}
            title="MP Bhumi Vikas Rules PDF"
            className="w-full h-full border-none"
          />
        </div>
      </div>
    </div>
  );
};
