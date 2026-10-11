import React from 'react';
import { X, Printer, CheckCircle2 } from 'lucide-react';
import type { ChatMessage, TableRow, Citation } from '../types';


interface PrintMemoModalProps {
  isOpen: boolean;
  onClose: () => void;
  message: ChatMessage;
  city: string;
}

export const PrintMemoModal: React.FC<PrintMemoModalProps> = ({
  isOpen,
  onClose,
  message,
  city,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4 animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl max-w-3xl w-full max-h-[92vh] flex flex-col shadow-2xl border border-slate-300 overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-3 border-b border-slate-200 bg-slate-900 text-white">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-5 h-5 text-emerald-400" />
            <span className="text-sm font-bold">
              Official Rule Reference Memo (Print / PDF)
            </span>
          </div>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => window.print()}
              className="flex items-center gap-1.5 px-3 py-1 bg-emerald-700 hover:bg-emerald-600 rounded-lg text-xs font-semibold cursor-pointer"
            >
              <Printer className="w-3.5 h-3.5" />
              <span>Print Memo</span>
            </button>
            <button
              type="button"
              onClick={onClose}
              className="p-1.5 text-slate-400 hover:text-white rounded-lg cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Printable Official Memo Sheet */}
        <div className="p-8 overflow-y-auto flex-1 bg-white" id="official-memo-content">
          <div className="border-4 border-double border-slate-800 p-8 relative">
            {/* Watermark */}
            <div className="absolute inset-0 flex items-center justify-center pointer-events-none opacity-5">
              <span className="text-7xl font-bold font-hindi">म.प्र. शासन</span>
            </div>

            {/* Letterhead */}
            <div className="text-center pb-4 border-b-2 border-slate-800">
              <div className="text-xs font-bold tracking-widest uppercase text-slate-600">
                Government of Madhya Pradesh • Urban Development & Housing Department
              </div>
              <div className="text-xl font-bold text-slate-900 mt-1 uppercase">
                {city}
              </div>
              <div className="text-xs font-semibold text-slate-700">
                Building Permission & Scrutiny Counter • Information Memo
              </div>
              <div className="text-[11px] text-slate-500 mt-0.5">
                Grounded in MP Bhumi Vikas Niyam, 2012 (Consolidated to March 2025)
              </div>
            </div>

            {/* Memo Details Meta */}
            <div className="flex justify-between items-center text-xs text-slate-600 py-3 border-b border-slate-200">
              <div>
                <strong>Memo Ref:</strong> BVS/CLERK/2026/
                {Math.floor(1000 + Math.random() * 9000)}
              </div>
              <div>
                <strong>Date & Time:</strong> {new Date().toLocaleDateString('en-IN')} | {message.timestamp}
              </div>
            </div>

            {/* Citizen Question */}
            <div className="my-4">
              <div className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1">
                Citizen Inquiry:
              </div>
              <div className="p-2.5 bg-slate-50 border border-slate-200 rounded-md text-sm font-semibold text-slate-900">
                "{message.question}"
              </div>
            </div>

            {/* Answer Content */}
            <div className="my-4">
              <div className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-1">
                Statutory Determination:
              </div>
              <p className="text-xs text-slate-800 leading-relaxed mb-3">
                {message.answerSummary}
              </p>

              {message.tableData && message.tableData.length > 0 && (
                <table className="w-full text-xs border-collapse border border-slate-300 mb-4">
                  <thead>
                    <tr className="bg-slate-100 text-slate-800">
                      <th className="border border-slate-300 px-3 py-2 text-left font-bold">Side / Requirement</th>
                      <th className="border border-slate-300 px-3 py-2 text-left font-bold">Minimum Setback</th>
                      <th className="border border-slate-300 px-3 py-2 text-left font-bold">Rule Reference</th>
                    </tr>
                  </thead>
                  <tbody>
                    {message.tableData.map((row: TableRow, i: number) => (
                      <tr key={i} className={i % 2 === 0 ? 'bg-white' : 'bg-slate-50/50'}>
                        <td className="border border-slate-300 px-3 py-1.5 font-semibold">{row.side}</td>
                        <td className="border border-slate-300 px-3 py-1.5 text-slate-900 font-bold">{row.minimumSetback}</td>
                        <td className="border border-slate-300 px-3 py-1.5 text-slate-700">{row.ruleReference}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}

              <div className="text-xs font-bold text-slate-800 mb-1">Detailed Explanation:</div>
              <p className="text-xs text-slate-700 leading-relaxed mb-3">
                {message.explanation}
              </p>

              {message.importantNotes && message.importantNotes.length > 0 && (
                <div className="bg-amber-50/80 border border-amber-200 p-3 rounded-md text-[11px] text-amber-900 mb-3">
                  <strong className="block font-bold mb-1">Important Statutory Notes:</strong>
                  <ul className="list-disc pl-4 space-y-0.5">
                    {message.importantNotes.map((note: string, idx: number) => (
                      <li key={idx}>{note}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>

            {/* Citations Footer */}
            {message.citations && message.citations.length > 0 && (
              <div className="my-4 pt-3 border-t border-slate-200">
                <div className="text-xs font-bold text-slate-800 mb-1.5">
                  Verified Gazette Citations:
                </div>
                {message.citations.map((c: Citation, i: number) => (
                  <div key={i} className="text-[11px] text-slate-600 mb-1">
                    • <strong>{c.ruleNumber}:</strong> {c.title} (Gazette Page {c.pageStart})
                    {c.hindiExcerpt && <span className="italic block text-slate-500 font-hindi pl-3">{c.hindiExcerpt}</span>}
                  </div>
                ))}
              </div>
            )}

            {/* Signature Block */}
            <div className="pt-8 mt-6 border-t border-slate-300 flex justify-between items-end text-xs">
              <div>
                <div className="text-[10px] text-slate-500 max-w-xs">
                  * Disclaimer: This informational slip references MP Bhumi Vikas Niyam 2012. Formal building sanction is subject to site inspection, fee verification and competent authority approval.
                </div>
              </div>
              <div className="text-center">
                <div className="w-36 border-b border-slate-400 mb-1"></div>
                <div className="font-bold text-slate-800">Scrutiny Clerk Signature</div>
                <div className="text-[10px] text-slate-500">{city}</div>
              </div>
            </div>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="px-6 py-3 bg-slate-100 border-t border-slate-200 flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-1.5 bg-slate-200 hover:bg-slate-300 text-slate-800 text-xs font-semibold rounded-lg transition-colors cursor-pointer"
          >
            Close
          </button>
          <button
            type="button"
            onClick={() => window.print()}
            className="px-4 py-1.5 bg-[#124d3a] hover:bg-[#165d46] text-white text-xs font-semibold rounded-lg transition-colors cursor-pointer flex items-center gap-1.5"
          >
            <Printer className="w-3.5 h-3.5" />
            <span>Print Official Slip</span>
          </button>
        </div>
      </div>
    </div>
  );
};
