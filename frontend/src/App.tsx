import { useState, useEffect } from 'react';
import { Header } from './components/Header';
import { HeroBanner } from './components/HeroBanner';
import { Sidebar } from './components/Sidebar';
import { SourcesPanel } from './components/SourcesPanel';
import { ChatArea } from './components/ChatArea';
import { PlotDiagramModal } from './components/PlotDiagramModal';
import { PdfViewerModal } from './components/PdfViewerModal';
import { CalculatorsModal } from './components/CalculatorsModal';
import { RuleExplorerModal } from './components/RuleExplorerModal';
import { ExampleQuestionsModal } from './components/ExampleQuestionsModal';
import { SavedAnswersModal } from './components/SavedAnswersModal';
import { PrintMemoModal } from './components/PrintMemoModal';
import { AuthModal } from './components/AuthModal';
import { AmendmentsModal } from './components/AmendmentsModal';
import {
  INITIAL_ASSISTANT_MESSAGE,
  getGroundedAnswer,
} from './data/rulesData';
import type { ChatMessage, Citation, RelatedRule, TableRow } from './types';
import { api, type UserPrincipal, type BackendAskResponse } from './services/api';

export function App() {
  const [language, setLanguage] = useState<'en' | 'hi'>('en');
  const [isDark, setIsDark] = useState(false);
  const [selectedCity, setSelectedCity] = useState('Indore Municipal Corporation');
  const [activeTab, setActiveTab] = useState('chat');
  const [asOfDate, setAsOfDate] = useState('2026-10-11');

  // Backend Integration State
  const [isBackendOnline, setIsBackendOnline] = useState(false);
  const [currentUser, setCurrentUser] = useState<UserPrincipal | null>(null);

  // Messages thread initialized with user question + AI answer from reference design
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'msg-default-user',
      role: 'user',
      timestamp: '10:24 AM',
      question: 'What are the setback rules for a residential plot of 500 sq m iin R-1 zone?',
    },
    INITIAL_ASSISTANT_MESSAGE,
  ]);

  // Current active citations & related rules displayed on the right sidebar
  const [activeCitations, setActiveCitations] = useState<Citation[]>(
    INITIAL_ASSISTANT_MESSAGE.citations || []
  );
  const [activeRelatedRules, setActiveRelatedRules] = useState<RelatedRule[]>(
    INITIAL_ASSISTANT_MESSAGE.relatedRules || []
  );

  const [isLoading, setIsLoading] = useState(false);
  const [savedMessages, setSavedMessages] = useState<ChatMessage[]>([
    INITIAL_ASSISTANT_MESSAGE,
  ]);

  // Modals state
  const [isDiagramOpen, setIsDiagramOpen] = useState(false);
  const [isPdfOpen, setIsPdfOpen] = useState(false);
  const [pdfPage, setPdfPage] = useState<number>(25);
  const [isCalculatorsOpen, setIsCalculatorsOpen] = useState(false);
  const [isCategoriesOpen, setIsCategoriesOpen] = useState(false);
  const [isExamplesOpen, setIsExamplesOpen] = useState(false);
  const [isSavedOpen, setIsSavedOpen] = useState(false);
  const [isAuthOpen, setIsAuthOpen] = useState(false);
  const [isAmendmentsOpen, setIsAmendmentsOpen] = useState(false);
  const [printMemoMessage, setPrintMemoMessage] = useState<ChatMessage | null>(null);

  // Active diagram data (defaults to the 500 sq m plot from reference)
  const [activeDiagramData, setActiveDiagramData] = useState(
    INITIAL_ASSISTANT_MESSAGE.diagramData!
  );

  // Health check and session check on initial mount
  useEffect(() => {
    checkBackendHealth();
    checkExistingAuth();
  }, []);

  const checkBackendHealth = async () => {
    const health = await api.checkHealth();
    setIsBackendOnline(health.isOnline);
  };

  const checkExistingAuth = async () => {
    const user = await api.getMe();
    if (user) {
      setCurrentUser(user);
    } else {
      const savedU = api.getSavedUsername();
      const savedR = api.getSavedRole() as 'USER' | 'ADMIN' | null;
      if (savedU && savedR) {
        setCurrentUser({
          id: 1,
          username: savedU,
          role: savedR,
          enabled: true,
        });
      }
    }
  };

  /**
   * Helper to parse backend answers into rich structured table & explanation
   */
  const adaptBackendResponseToMessage = (
    queryText: string,
    data: BackendAskResponse
  ): ChatMessage => {
    const tableRows: TableRow[] = [];

    let plotArea = 500;
    const matchArea = queryText.match(/(\d+)\s*(?:sq\s*m|वर्ग\s*मीटर|sqm)/i);
    if (matchArea) {
      plotArea = parseInt(matchArea[1], 10);
    }

    if (data.calculation && typeof data.calculation === 'object') {
      const calc = data.calculation;
      if (calc.frontSetback || calc.rearSetback || calc.sideSetback) {
        tableRows.push({
          side: 'Front',
          minimumSetback: `${calc.frontSetback} meters`,
          ruleReference: 'Rule 32(1) – Page 25',
        });
        tableRows.push({
          side: 'Rear',
          minimumSetback: `${calc.rearSetback} meters`,
          ruleReference: 'Rule 32(2) – Page 26',
        });
        tableRows.push({
          side: 'Side (each)',
          minimumSetback: `${calc.sideSetback} meters`,
          ruleReference: 'Rule 32(3) – Page 26',
        });
      }
    }

    if (tableRows.length === 0 && (queryText.toLowerCase().includes('setback') || queryText.toLowerCase().includes('open space') || queryText.includes('जगह'))) {
      const f = plotArea <= 150 ? '3.0 meters' : plotArea <= 300 ? '4.5 meters' : plotArea <= 1000 ? '6 meters' : '9 meters';
      const r = plotArea <= 150 ? '2.0 meters' : plotArea <= 300 ? '3.0 meters' : plotArea <= 1000 ? '4 meters' : '6 meters';
      const s = plotArea <= 150 ? '1.5 meters' : plotArea <= 300 ? '2.0 meters' : plotArea <= 1000 ? '3 meters' : '4.5 meters';
      tableRows.push({ side: 'Front', minimumSetback: f, ruleReference: 'Rule 32(1) – Page 25' });
      tableRows.push({ side: 'Rear', minimumSetback: r, ruleReference: 'Rule 32(2) – Page 26' });
      tableRows.push({ side: 'Side (each)', minimumSetback: s, ruleReference: 'Rule 32(3) – Page 26' });
    }

    const citations: Citation[] = (data.citations || []).map((c) => ({
      ruleNumber: `Rule ${c.ruleNumber}`,
      title: c.title || 'Statutory Requirement',
      passageLabel: c.passageLabel || '',
      pageStart: c.pageStart || 25,
      pageEnd: c.pageEnd || 26,
      excerpt: c.excerpt || '',
      hindiExcerpt: language === 'hi' ? c.excerpt : undefined,
      validated: c.validated !== undefined ? c.validated : true,
    }));

    const notes = [...(data.warnings || [])];
    if (data.disclaimer) {
      notes.push(data.disclaimer);
    }

    let summary = language === 'hi'
      ? `म.प्र. भूमि विकास नियमों के तहत ${plotArea} वर्ग मीटर भूखंड के लिए सेटबैक नियम निम्नवत हैं:`
      : `For a residential plot of ${plotArea} sq m in an R-1 zone, the setback rules are as follows:`;

    if (data.status === 'NOT_FOUND_IN_RULES') {
      summary = language === 'hi'
        ? 'यह विषय म.प्र. भूमि विकास नियम, 2012 के अंतर्गत उपलब्ध नहीं है।'
        : 'This query is outside the scope of MP Bhumi Vikas Rules, 2012.';
    }

    return {
      id: data.answerId || `ai-${Date.now()}`,
      role: 'assistant',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      question: queryText,
      answerSummary: summary,
      tableData: tableRows.length > 0 ? tableRows : undefined,
      explanation: data.answer,
      importantNotes: notes,
      citations: citations.length > 0 ? citations : INITIAL_ASSISTANT_MESSAGE.citations,
      relatedRules: activeRelatedRules,
      isNotFound: data.status === 'NOT_FOUND_IN_RULES',
      calculationDetails: data.calculation,
      diagramData: {
        plotAreaSqM: plotArea,
        plotWidthM: plotArea <= 300 ? 12 : 20,
        plotDepthM: plotArea <= 300 ? 25 : Math.round(plotArea / 20),
        frontSetbackM: plotArea <= 300 ? 4.5 : 6,
        rearSetbackM: plotArea <= 300 ? 3 : 4,
        sideSetbackM: plotArea <= 300 ? 2 : 3,
        roadWidthM: 12,
        zone: 'R-1 Residential',
      },
    };
  };

  // Handle sending new questions to backend
  const handleSendMessage = async (queryText: string) => {
    const userTimestamp = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const userMsg: ChatMessage = {
      id: `user-${Date.now()}`,
      role: 'user',
      timestamp: userTimestamp,
      question: queryText,
    };

    setMessages((prev) => [...prev, userMsg]);
    setIsLoading(true);

    let backendSuccess = false;

    try {
      const response = await api.askQuestion(queryText, language, asOfDate);
      if (response && response.answer) {
        backendSuccess = true;
        setIsBackendOnline(true);
        const aiMsg = adaptBackendResponseToMessage(queryText, response);
        setMessages((prev) => [...prev, aiMsg]);
        if (aiMsg.citations && aiMsg.citations.length > 0) {
          setActiveCitations(aiMsg.citations);
        }
      }
    } catch {
      // Backend request fallback
    }

    if (!backendSuccess) {
      setTimeout(() => {
        const groundedAnswer = getGroundedAnswer(queryText, language);
        setMessages((prev) => [...prev, groundedAnswer]);

        if (groundedAnswer.citations && groundedAnswer.citations.length > 0) {
          setActiveCitations(groundedAnswer.citations);
        }
        if (groundedAnswer.relatedRules && groundedAnswer.relatedRules.length > 0) {
          setActiveRelatedRules(groundedAnswer.relatedRules);
        }
        if (groundedAnswer.diagramData) {
          setActiveDiagramData(groundedAnswer.diagramData);
        }
        setIsLoading(false);
      }, 400);
      return;
    }

    setIsLoading(false);
  };

  const handleOpenPdf = (page: number = 1) => {
    setPdfPage(page);
    setIsPdfOpen(true);
  };

  const handleSaveMessage = (msg: ChatMessage) => {
    if (savedMessages.some((m) => m.id === msg.id)) {
      setSavedMessages((prev) => prev.filter((m) => m.id !== msg.id));
    } else {
      setSavedMessages((prev) => [...prev, msg]);
    }
  };

  const isSaved = (msgId: string) => savedMessages.some((m) => m.id === msgId);

  const handleNewChat = () => {
    setMessages([
      INITIAL_ASSISTANT_MESSAGE,
    ]);
    setActiveCitations(INITIAL_ASSISTANT_MESSAGE.citations || []);
    setActiveRelatedRules(INITIAL_ASSISTANT_MESSAGE.relatedRules || []);
  };

  return (
    <div className={`min-h-screen flex flex-col ${isDark ? 'dark bg-slate-950 text-white' : 'bg-[#f4f6f8] text-slate-800'}`}>
      {/* 1. Full-Width Top Header */}
      <Header
        language={language}
        onLanguageChange={setLanguage}
        isDark={isDark}
        onToggleDark={() => setIsDark(!isDark)}
        selectedCity={selectedCity}
        onSelectCity={setSelectedCity}
        isBackendOnline={isBackendOnline}
        onCheckHealth={checkBackendHealth}
        asOfDate={asOfDate}
        onOpenAmendments={() => setIsAmendmentsOpen(true)}
        currentUser={currentUser}
        onOpenAuth={() => setIsAuthOpen(true)}
      />

      {/* 2. Main Body Grid: Left Sidebar + (Top Hero Banner + Split Content) */}
      <div className="flex-1 flex w-full overflow-hidden">
        {/* Left Sidebar runs all the way down */}
        <Sidebar
          activeTab={activeTab}
          onTabChange={setActiveTab}
          onNewChat={handleNewChat}
          onOpenPdf={handleOpenPdf}
          onOpenExamples={() => setIsExamplesOpen(true)}
          onOpenCategories={() => setIsCategoriesOpen(true)}
          onOpenCalculators={() => setIsCalculatorsOpen(true)}
          onOpenSaved={() => setIsSavedOpen(true)}
          savedCount={savedMessages.length}
        />

        {/* Right Area: Hero Banner on top, then Main Chat + Sources Panel */}
        <div className="flex-1 flex flex-col min-w-0 overflow-y-auto">
          {/* Top Heritage Banner spanning across the chat and right sources */}
          <HeroBanner />

          {/* Chat and Sources Panel row */}
          <div className="flex-1 flex flex-col lg:flex-row overflow-hidden">
            {/* Center Main Chat / Interaction Area */}
            <main className="flex-1 flex flex-col border-r border-slate-200/80 bg-[#f8fafc] min-w-0">
              <ChatArea
                messages={messages}
                onSendMessage={handleSendMessage}
                isLoading={isLoading}
                onOpenDiagram={() => setIsDiagramOpen(true)}
                onOpenPrintMemo={(msg) => setPrintMemoMessage(msg)}
                onSaveMessage={handleSaveMessage}
                isSaved={isSaved}
                onSuggestionClick={(prompt) => handleSendMessage(prompt)}
                language={language}
              />
            </main>

            {/* Right Sources & Related Rules Panel */}
            <SourcesPanel
              citations={activeCitations}
              relatedRules={activeRelatedRules}
              onOpenPdf={handleOpenPdf}
              onSelectRelatedRule={(ruleNo) => {
                handleSendMessage(`What are the regulations under ${ruleNo}?`);
              }}
            />
          </div>
        </div>
      </div>

      {/* Modals & Technical Drawers */}
      <PlotDiagramModal
        isOpen={isDiagramOpen}
        onClose={() => setIsDiagramOpen(false)}
        data={activeDiagramData}
      />

      <PdfViewerModal
        isOpen={isPdfOpen}
        onClose={() => setIsPdfOpen(false)}
        initialPage={pdfPage}
      />

      <CalculatorsModal
        isOpen={isCalculatorsOpen}
        onClose={() => setIsCalculatorsOpen(false)}
        onApplyResultToChat={handleSendMessage}
      />

      <RuleExplorerModal
        isOpen={isCategoriesOpen}
        onClose={() => setIsCategoriesOpen(false)}
        onOpenPdf={handleOpenPdf}
      />

      <ExampleQuestionsModal
        isOpen={isExamplesOpen}
        onClose={() => setIsExamplesOpen(false)}
        onSelectQuestion={handleSendMessage}
        language={language}
      />

      <SavedAnswersModal
        isOpen={isSavedOpen}
        onClose={() => setIsSavedOpen(false)}
        savedMessages={savedMessages}
        onRemoveSaved={(id) => setSavedMessages((prev) => prev.filter((m) => m.id !== id))}
        onOpenPrintMemo={(msg) => setPrintMemoMessage(msg)}
      />

      <AuthModal
        isOpen={isAuthOpen}
        onClose={() => setIsAuthOpen(false)}
        currentUser={currentUser}
        onLoginSuccess={(user) => setCurrentUser(user)}
        onLogoutSuccess={() => setCurrentUser(null)}
        isBackendOnline={isBackendOnline}
      />

      <AmendmentsModal
        isOpen={isAmendmentsOpen}
        onClose={() => setIsAmendmentsOpen(false)}
        asOfDate={asOfDate}
        onSelectAsOfDate={(date) => setAsOfDate(date)}
      />

      {printMemoMessage && (
        <PrintMemoModal
          isOpen={!!printMemoMessage}
          onClose={() => setPrintMemoMessage(null)}
          message={printMemoMessage}
          city={selectedCity}
        />
      )}
    </div>
  );
}

export default App;
