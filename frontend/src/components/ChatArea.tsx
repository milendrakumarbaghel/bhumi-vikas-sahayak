import React, { useState, useRef, useEffect } from 'react';
import {
  Send,
  Paperclip,
  Mic,
  Copy,
  Download,
  Bookmark,
  ThumbsUp,
  ThumbsDown,
  RefreshCw,
  Lightbulb,
  Check,
  Layers,
  User,
} from 'lucide-react';
import type { ChatMessage, TableRow, Citation } from '../types';


interface ChatAreaProps {
  messages: ChatMessage[];
  onSendMessage: (text: string) => void;
  isLoading: boolean;
  onOpenDiagram: () => void;
  onOpenPrintMemo: (msg: ChatMessage) => void;
  onSaveMessage: (msg: ChatMessage) => void;
  isSaved: (msgId: string) => boolean;
  onSuggestionClick: (prompt: string) => void;
  language: 'en' | 'hi';
}

export const ChatArea: React.FC<ChatAreaProps> = ({
  messages,
  onSendMessage,
  isLoading,
  onOpenDiagram,
  onOpenPrintMemo,
  onSaveMessage,
  isSaved,
  onSuggestionClick,
  language,
}) => {
  const [inputText, setInputText] = useState('');
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<Record<string, 'up' | 'down'>>({});
  const [isRecording, setIsRecording] = useState(false);
  const chatBottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    chatBottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isLoading]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputText.trim() || isLoading) return;
    onSendMessage(inputText.trim());
    setInputText('');
  };

  const handleCopy = (msg: ChatMessage) => {
    let copyText = `${msg.answerSummary}\n\n`;
    if (msg.tableData) {
      copyText += msg.tableData.map((r: TableRow) => `${r.side}: ${r.minimumSetback} (${r.ruleReference})`).join('\n') + '\n\n';
    }
    if (msg.explanation) copyText += `${msg.explanation}\n\n`;
    if (msg.importantNotes) {
      copyText += `Important Notes:\n` + msg.importantNotes.map((n: string) => `• ${n}`).join('\n') + '\n\n';
    }
    if (msg.citations) {
      copyText += `Sources:\n` + msg.citations.map((c: Citation) => `• ${c.ruleNumber} (Page ${c.pageStart}): ${c.title}`).join('\n');
    }

    navigator.clipboard.writeText(copyText);
    setCopiedId(msg.id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const handleVoiceInput = () => {
    if (!('webkitSpeechRecognition' in window || 'SpeechRecognition' in window)) {
      alert('Speech recognition is not supported in this browser. Please type your question.');
      return;
    }

    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    const recognition = new SpeechRecognition();
    recognition.lang = language === 'hi' ? 'hi-IN' : 'en-IN';
    recognition.interimResults = false;

    recognition.onstart = () => setIsRecording(true);
    recognition.onend = () => setIsRecording(false);
    recognition.onerror = () => setIsRecording(false);
    recognition.onresult = (event: any) => {
      const speechText = event.results[0][0].transcript;
      setInputText(speechText);
    };

    recognition.start();
  };

  // Quick prompt suggestions above input bar (matching reference)
  const quickSuggestions = [
    'Show a diagram of setbacks',
    'What is the maximum FAR in R-1 zone?',
    'Rules for corner plots',
    'Can I build 4 floors in R-1 zone?',
  ];

  // Bottom example pills
  const bottomExamples = [
    { label: 'Setback rules', prompt: 'What are the setback rules for a residential plot of 500 sq m in R-1 zone?' },
    { label: 'FAR calculation', prompt: 'What is the maximum FAR for a plot on a 12m road in R-1 zone?' },
    { label: 'Height restrictions', prompt: 'What is the maximum building height permitted on an 18m road?' },
    { label: 'Parking requirements', prompt: 'What are the stilt and basement parking rules under MP Bhumi Vikas Rules?' },
    { label: 'Commercial building rules', prompt: 'What are the setback and FAR rules for commercial buildings under Rule 33?' },
  ];

  return (
    <div className="flex-1 flex flex-col justify-between h-full bg-[#f8fafc] overflow-hidden">
      {/* Scrollable messages container */}
      <div className="flex-1 overflow-y-auto px-4 lg:px-8 py-6 space-y-6">
        {messages.map((msg) => {
          if (msg.role === 'user') {
            return (
              <div key={msg.id} className="flex items-start gap-3 max-w-3xl">
                {/* User avatar */}
                <div className="w-8 h-8 rounded-full bg-slate-900 text-white flex items-center justify-center flex-shrink-0 mt-0.5 shadow-2xs">
                  <User className="w-4 h-4" />
                </div>

                {/* Message bubble */}
                <div className="flex-1">
                  <div className="bg-white rounded-2xl px-4 py-3 border border-slate-200/90 shadow-2xs text-sm font-semibold text-slate-800">
                    {msg.question || msg.rawText}
                  </div>
                  <div className="text-[11px] text-slate-400 mt-1 pl-1">
                    {msg.timestamp}
                  </div>
                </div>
              </div>
            );
          }

          // AI Response card
          return (
            <div key={msg.id} className="flex items-start gap-3 max-w-3xl">
              {/* AI avatar */}
              <div className="w-8 h-8 rounded-full bg-[#114b3d] text-white flex items-center justify-center font-bold text-xs flex-shrink-0 mt-0.5 shadow-2xs">
                Ai
              </div>

              {/* Main AI Card */}
              <div className="flex-1 bg-white rounded-2xl p-5 border border-slate-200/90 shadow-xs space-y-4">
                {/* Answer section */}
                <div>
                  <h3 className="text-sm font-bold text-slate-900 mb-1 tracking-tight">
                    Answer
                  </h3>
                  <p className="text-xs text-slate-700 leading-relaxed">
                    {msg.answerSummary}
                  </p>
                </div>

                {/* Structured Table (e.g. Setback table) */}
                {msg.tableData && msg.tableData.length > 0 && (
                  <div className="overflow-hidden border border-slate-200 rounded-lg">
                    <table className="w-full text-xs text-left border-collapse">
                      <thead>
                        <tr className="bg-slate-100/80 text-slate-700 font-semibold border-b border-slate-200">
                          <th className="py-2.5 px-3.5">Side</th>
                          <th className="py-2.5 px-3.5">Minimum Setback</th>
                          <th className="py-2.5 px-3.5">Rule Reference</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100 text-slate-700">
                        {msg.tableData.map((row: TableRow, i: number) => (
                          <tr key={i} className="hover:bg-slate-50/70 transition-colors">
                            <td className="py-2.5 px-3.5 font-medium">{row.side}</td>
                            <td className="py-2.5 px-3.5 font-bold text-slate-900">{row.minimumSetback}</td>
                            <td className="py-2.5 px-3.5 text-slate-600">{row.ruleReference}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}

                {/* Explanation */}
                {msg.explanation && (
                  <div>
                    <h4 className="text-xs font-bold text-slate-900 mb-1 tracking-tight">
                      Explanation
                    </h4>
                    <p className="text-xs text-slate-600 leading-relaxed font-normal">
                      {msg.explanation}
                    </p>
                  </div>
                )}

                {/* Important Notes Callout Box */}
                {msg.importantNotes && msg.importantNotes.length > 0 && (
                  <div className="bg-[#fffbeb] border border-amber-200/90 rounded-xl p-3.5 text-xs text-amber-950 space-y-1.5">
                    <div className="flex items-center gap-1.5 font-bold text-amber-900">
                      <Lightbulb className="w-4 h-4 text-amber-600 fill-amber-400" />
                      <span>Important Notes</span>
                    </div>
                    <ul className="space-y-1 pl-4 text-slate-700 list-disc">
                      {msg.importantNotes.map((note: string, index: number) => (
                        <li key={index} className="leading-relaxed">
                          {note}
                        </li>
                      ))}
                    </ul>
                  </div>
                )}

                {/* Action button: Show Diagram toggle (if diagramData present) */}
                {msg.diagramData && (
                  <div className="pt-1">
                    <button
                      type="button"
                      onClick={onOpenDiagram}
                      className="inline-flex items-center gap-2 px-3 py-1.5 rounded-lg bg-emerald-50 hover:bg-emerald-100/90 text-emerald-800 text-xs font-semibold border border-emerald-300 transition-all cursor-pointer"
                    >
                      <Layers className="w-3.5 h-3.5 text-emerald-700" />
                      <span>📐 View Technical Setback Blueprint Diagram</span>
                    </button>
                  </div>
                )}

                {/* Card Action Footer */}
                <div className="pt-2 border-t border-slate-100 flex flex-wrap items-center justify-between gap-3 text-xs text-slate-500">
                  {/* Left utility buttons */}
                  <div className="flex items-center gap-2">
                    {/* Copy Answer */}
                    <button
                      type="button"
                      onClick={() => handleCopy(msg)}
                      className="flex items-center gap-1 px-2.5 py-1 rounded-md hover:bg-slate-100 text-slate-600 hover:text-slate-900 transition-colors cursor-pointer border border-transparent hover:border-slate-200"
                    >
                      {copiedId === msg.id ? (
                        <>
                          <Check className="w-3.5 h-3.5 text-emerald-600" />
                          <span className="text-emerald-700 font-semibold">Copied!</span>
                        </>
                      ) : (
                        <>
                          <Copy className="w-3.5 h-3.5" />
                          <span>Copy Answer</span>
                        </>
                      )}
                    </button>

                    {/* Download as PDF / Memo */}
                    <button
                      type="button"
                      onClick={() => onOpenPrintMemo(msg)}
                      className="flex items-center gap-1 px-2.5 py-1 rounded-md hover:bg-slate-100 text-slate-600 hover:text-slate-900 transition-colors cursor-pointer border border-transparent hover:border-slate-200"
                    >
                      <Download className="w-3.5 h-3.5" />
                      <span>Download as PDF</span>
                    </button>

                    {/* Save Answer */}
                    <button
                      type="button"
                      onClick={() => onSaveMessage(msg)}
                      className={`flex items-center gap-1 px-2.5 py-1 rounded-md transition-colors cursor-pointer border ${
                        isSaved(msg.id)
                          ? 'bg-amber-50 text-amber-800 border-amber-300'
                          : 'hover:bg-slate-100 text-slate-600 border-transparent hover:border-slate-200'
                      }`}
                    >
                      <Bookmark className={`w-3.5 h-3.5 ${isSaved(msg.id) ? 'fill-amber-500 text-amber-600' : ''}`} />
                      <span>{isSaved(msg.id) ? 'Saved' : 'Save'}</span>
                    </button>
                  </div>

                  {/* Right feedback & time */}
                  <div className="flex items-center gap-3">
                    <div className="flex items-center gap-1">
                      <button
                        type="button"
                        onClick={() => setFeedback((f) => ({ ...f, [msg.id]: 'up' }))}
                        className={`p-1 rounded-sm hover:bg-slate-100 transition-colors cursor-pointer ${
                          feedback[msg.id] === 'up' ? 'text-emerald-600' : 'text-slate-400'
                        }`}
                        title="Helpful"
                      >
                        <ThumbsUp className="w-3.5 h-3.5" />
                      </button>
                      <button
                        type="button"
                        onClick={() => setFeedback((f) => ({ ...f, [msg.id]: 'down' }))}
                        className={`p-1 rounded-sm hover:bg-slate-100 transition-colors cursor-pointer ${
                          feedback[msg.id] === 'down' ? 'text-red-500' : 'text-slate-400'
                        }`}
                        title="Incorrect"
                      >
                        <ThumbsDown className="w-3.5 h-3.5" />
                      </button>
                    </div>

                    <span className="text-[11px] text-slate-400">
                      {msg.timestamp}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          );
        })}

        {/* Loading skeleton */}
        {isLoading && (
          <div className="flex items-start gap-3 max-w-3xl">
            <div className="w-8 h-8 rounded-full bg-[#114b3d] text-white flex items-center justify-center font-bold text-xs animate-pulse">
              Ai
            </div>
            <div className="bg-white rounded-2xl p-5 border border-slate-200 shadow-xs flex-1 space-y-3">
              <div className="flex items-center gap-2 text-xs font-semibold text-emerald-800">
                <span className="w-2 h-2 rounded-full bg-emerald-600 animate-ping"></span>
                <span>Searching MP Bhumi Vikas Niyam, 2012 Gazette & Evaluating Rules...</span>
              </div>
              <div className="h-3 bg-slate-200 rounded-sm w-3/4 animate-pulse"></div>
              <div className="h-16 bg-slate-100 rounded-lg animate-pulse"></div>
            </div>
          </div>
        )}

        <div ref={chatBottomRef} />
      </div>

      {/* Suggestion Chips and Input Container */}
      <div className="px-4 lg:px-8 pb-4 pt-2 bg-gradient-to-t from-white via-white/95 to-transparent border-t border-slate-200/80">
        {/* Quick Suggestion Chips */}
        <div className="flex items-center gap-2 mb-3 overflow-x-auto py-1 no-scrollbar">
          {quickSuggestions.map((suggestion, idx) => (
            <button
              key={idx}
              type="button"
              onClick={() => {
                if (suggestion.includes('diagram')) {
                  onOpenDiagram();
                } else {
                  onSuggestionClick(suggestion);
                }
              }}
              className="text-xs font-medium text-slate-700 bg-white hover:bg-emerald-50 hover:text-emerald-900 px-3 py-1.5 rounded-full border border-slate-200/90 hover:border-emerald-300 shadow-2xs transition-all whitespace-nowrap cursor-pointer"
            >
              {suggestion}
            </button>
          ))}
          <button
            type="button"
            onClick={() => onSuggestionClick('What are the setback rules for a residential plot of 300 sq m in R-1 zone?')}
            className="p-1.5 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-full transition-colors cursor-pointer"
            title="Refresh questions"
          >
            <RefreshCw className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* Input Bar Form */}
        <form onSubmit={handleSubmit} className="relative flex items-center">
          <div className="relative w-full flex items-center bg-white rounded-2xl border border-slate-300 focus-within:border-emerald-600 focus-within:ring-2 focus-within:ring-emerald-600/20 shadow-xs transition-all">
            {/* Attachment icon */}
            <button
              type="button"
              onClick={() => alert('Site Plan / Layout map attachment is enabled for clerk scrutiny.')}
              className="pl-3.5 pr-2 py-3 text-slate-400 hover:text-slate-700 transition-colors cursor-pointer"
              title="Attach Site Plan or Blueprint"
            >
              <Paperclip className="w-4 h-4" />
            </button>

            {/* Input field */}
            <input
              type="text"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder="Ask a question about MP Building Rules..."
              className="w-full py-3 px-2 text-sm text-slate-900 placeholder:text-slate-400 bg-transparent focus:outline-hidden"
              disabled={isLoading}
            />

            {/* Mic voice input */}
            <button
              type="button"
              onClick={handleVoiceInput}
              className={`p-2 transition-colors cursor-pointer ${
                isRecording ? 'text-red-600 animate-pulse' : 'text-slate-400 hover:text-slate-700'
              }`}
              title="Voice Input (Hindi/English)"
            >
              <Mic className="w-4 h-4" />
            </button>

            {/* Send button */}
            <div className="pr-2">
              <button
                type="submit"
                disabled={!inputText.trim() || isLoading}
                className="w-8 h-8 rounded-lg bg-[#0c382e] hover:bg-[#134d3f] disabled:opacity-40 disabled:cursor-not-allowed text-white flex items-center justify-center transition-all cursor-pointer shadow-xs"
                title="Send Question"
              >
                <Send className="w-3.5 h-3.5 ml-0.5" />
              </button>
            </div>
          </div>
        </form>

        {/* Bottom "Try examples:" */}
        <div className="mt-2.5 flex items-center gap-2 text-xs text-slate-500 overflow-x-auto py-0.5 no-scrollbar">
          <span className="font-semibold text-slate-600 whitespace-nowrap">
            Try examples:
          </span>
          <div className="flex items-center gap-1.5 flex-wrap sm:flex-nowrap">
            {bottomExamples.map((ex, idx) => (
              <button
                key={idx}
                type="button"
                onClick={() => onSuggestionClick(ex.prompt)}
                className="px-2.5 py-0.5 rounded-md bg-slate-100 hover:bg-slate-200/80 text-slate-600 hover:text-slate-900 text-[11px] font-medium transition-colors whitespace-nowrap cursor-pointer"
              >
                {ex.label}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
