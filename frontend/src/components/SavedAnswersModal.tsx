import React from 'react';
import { X, Bookmark, Trash2, Printer } from 'lucide-react';
import type { ChatMessage, Citation } from '../types';


interface SavedAnswersModalProps {
  isOpen: boolean;
  onClose: () => void;
  savedMessages: ChatMessage[];
  onRemoveSaved: (id: string) => void;
  onOpenPrintMemo: (msg: ChatMessage) => void;
}

export const SavedAnswersModal: React.FC<SavedAnswersModalProps> = ({
  isOpen,
  onClose,
  savedMessages,
  onRemoveSaved,
  onOpenPrintMemo,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4 animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl max-w-3xl w-full max-h-[90vh] flex flex-col shadow-2xl border border-slate-200 overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200 bg-[#0c382e] text-white">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-amber-500/80 rounded-lg text-white">
              <Bookmark className="w-5 h-5 fill-white" />
            </div>
            <div>
              <h3 className="text-base font-bold">
                Saved Answers & Verified Rule Determinations
              </h3>
              <p className="text-xs text-emerald-300">
                {savedMessages.length} bookmarked answers saved by clerk
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

        {/* List of saved answers */}
        <div className="p-6 overflow-y-auto flex-1 space-y-4">
          {savedMessages.length === 0 ? (
            <div className="text-center py-12 text-slate-500 text-xs">
              <Bookmark className="w-10 h-10 mx-auto text-slate-300 mb-2" />
              <p className="font-semibold text-slate-700">No saved answers yet.</p>
              <p className="mt-1">Click the "Save" button on any rule response to bookmark it here for quick reference.</p>
            </div>
          ) : (
            savedMessages.map((msg) => (
              <div
                key={msg.id}
                className="border border-slate-200 rounded-xl p-4 bg-white hover:shadow-xs transition-shadow"
              >
                <div className="flex items-start justify-between gap-3 mb-2">
                  <div>
                    <span className="text-[11px] font-bold text-slate-400 block mb-0.5">
                      {msg.timestamp}
                    </span>
                    <h4 className="text-sm font-bold text-slate-900">
                      {msg.question}
                    </h4>
                  </div>
                  <div className="flex items-center gap-1.5 flex-shrink-0">
                    <button
                      type="button"
                      onClick={() => onOpenPrintMemo(msg)}
                      className="p-1.5 text-slate-500 hover:text-emerald-700 hover:bg-emerald-50 rounded-lg transition-colors cursor-pointer"
                      title="Print Official Slip"
                    >
                      <Printer className="w-4 h-4" />
                    </button>
                    <button
                      type="button"
                      onClick={() => onRemoveSaved(msg.id)}
                      className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors cursor-pointer"
                      title="Remove from saved"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>

                <p className="text-xs text-slate-600 line-clamp-2 mb-3">
                  {msg.answerSummary}
                </p>

                {msg.citations && msg.citations.length > 0 && (
                  <div className="flex items-center gap-2 flex-wrap">
                    {msg.citations.map((c: Citation, i: number) => (
                      <span
                        key={i}
                        className="text-[10px] font-semibold text-emerald-800 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded-sm"
                      >
                        {c.ruleNumber} (p.{c.pageStart})
                      </span>
                    ))}
                  </div>
                )}
              </div>
            ))
          )}
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
