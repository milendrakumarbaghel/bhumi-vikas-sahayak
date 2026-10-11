export type Language = 'en' | 'hi';

export interface Translations {
  appTitle: string;
  appSubtitle: string;
  placeholder: string;
  askButton: string;
  cancelButton: string;
  searching: string;
  langAuto: string;
  langEn: string;
  langHi: string;
  statusAnswered: string;
  statusNotFound: string;
  statusNeedsMoreInfo: string;
  statusNeedsReview: string;
  calculationTitle: string;
  formula: string;
  matchedBand: string;
  inputs: string;
  assumption: string;
  notes: string;
  illustrationBadge: string;
  citationVerified: string;
  citationUnverified: string;
  openPdfPage: string;
  noVerifiedCitation: string;
  loadingRuleText: string;
  amendmentBannerText: string;
  amendmentLinkText: string;
  copyButton: string;
  copiedButton: string;
  footerDisclaimer: string;
  defaultSourceVersion: string;
  error429: string;
  error5xx: string;
  retry: string;
  provideDetails: string;
  appendAndAsk: string;
  sqm: string;
  sqft: string;
  gaj: string;
  meters: string;
  feet: string;
  residential: string;
  commercial: string;
  industrial: string;
  unit: string;
}

export const i18n: Record<Language, Translations> = {
  en: {
    appTitle: 'Bhumi Vikas Sahayak / भूमि विकास सहायक',
    appSubtitle: 'MP Bhumi Vikas Rules 2012',
    placeholder: 'Ask in English or Hindi (e.g. My plot is 200 sq m on a 9 m road... / हिन्दी या अंग्रेजी में पूछें...)',
    askButton: 'Ask / पूछें',
    cancelButton: 'Cancel',
    searching: 'Searching the rules... / नियम खोजे जा रहे हैं...',
    langAuto: 'Auto',
    langEn: 'English',
    langHi: 'हिन्दी',
    statusAnswered: 'Answer from the rules / नियमों से उत्तर',
    statusNotFound: 'Not found in the rules. Check with the department. / नियमों में नहीं मिला। विभाग से पुष्टि करें।',
    statusNeedsMoreInfo: 'Need more information',
    statusNeedsReview: 'Needs officer review',
    calculationTitle: 'Calculation Breakdown',
    formula: 'Formula',
    matchedBand: 'Matched Band',
    inputs: 'Inputs',
    assumption: 'Assumption',
    notes: 'Notes',
    illustrationBadge: 'ILLUSTRATION ONLY, NOT FROM THE RULES',
    citationVerified: 'Verified against rule text',
    citationUnverified: 'Quote could not be verified - open the PDF page',
    openPdfPage: 'Open PDF page',
    noVerifiedCitation: 'No verified citation',
    loadingRuleText: 'Loading full rule text...',
    amendmentBannerText: 'Rule 2(5)(c) was amended on 28 Aug 2026 (plots up to 500 sq m). That change is not in the loaded rules - check the latest notification.',
    amendmentLinkText: 'View notification',
    copyButton: 'Copy for citizen',
    copiedButton: 'Copied!',
    footerDisclaimer: 'Points to the rule. The clerk decides. Not legal advice. / यह नियम की ओर संकेत करता है। निर्णय क्लर्क का है। यह कानूनी सलाह नहीं है।',
    defaultSourceVersion: 'MP Bhumi Vikas Rules 2012, consolidated to 6 March 2025',
    error429: 'Too many questions, wait a minute.',
    error5xx: 'Service unavailable, try again',
    retry: 'Retry',
    provideDetails: 'Please provide missing details:',
    appendAndAsk: 'Add Details & Ask Again',
    sqm: 'sq m',
    sqft: 'sq ft',
    gaj: 'gaj',
    meters: 'm',
    feet: 'ft',
    residential: 'Residential',
    commercial: 'Commercial',
    industrial: 'Industrial',
    unit: 'Unit',
  },
  hi: {
    appTitle: 'भूमि विकास सहायक / Bhumi Vikas Sahayak',
    appSubtitle: 'म.प्र. भूमि विकास नियम 2012',
    placeholder: 'हिन्दी या अंग्रेजी में पूछें (जैसे: मेरा प्लॉट 200 वर्ग मीटर का है और 9 मीटर सड़क है...)',
    askButton: 'पूछें / Ask',
    cancelButton: 'रद्द करें',
    searching: 'नियम खोजे जा रहे हैं... / Searching the rules...',
    langAuto: 'Auto',
    langEn: 'English',
    langHi: 'हिन्दी',
    statusAnswered: 'नियमों से उत्तर / Answer from the rules',
    statusNotFound: 'नियमों में नहीं मिला। विभाग से पुष्टि करें। / Not found in the rules. Check with the department.',
    statusNeedsMoreInfo: 'और जानकारी चाहिए / Need more information',
    statusNeedsReview: 'अधिकारी द्वारा समीक्षा आवश्यक / Needs officer review',
    calculationTitle: 'गणना विवरण',
    formula: 'सूत्र',
    matchedBand: 'लागू श्रेणी',
    inputs: 'प्रविष्टियां',
    assumption: 'अनुमान',
    notes: 'टिप्पणी',
    illustrationBadge: 'केवल चित्रण हेतु, नियमों से नहीं / ILLUSTRATION ONLY, NOT FROM THE RULES',
    citationVerified: 'नियम पाठ से सत्यापित',
    citationUnverified: 'उद्धरण सत्यापित नहीं किया जा सका - PDF पृष्ठ खोलें',
    openPdfPage: 'PDF पृष्ठ खोलें',
    noVerifiedCitation: 'कोई सत्यापित उद्धरण नहीं',
    loadingRuleText: 'नियम का संपूर्ण पाठ लोड हो रहा है...',
    amendmentBannerText: 'नियम 2(5)(c) को 28 अगस्त 2026 को संशोधित किया गया था (500 वर्ग मीटर तक के भूखंड)। यह परिवर्तन लोड किए गए नियमों में नहीं है - नवीनतम अधिसूचना देखें।',
    amendmentLinkText: 'अधिसूचना देखें',
    copyButton: 'नागरिक के लिए कॉपी करें',
    copiedButton: 'कॉपी हो गया!',
    footerDisclaimer: 'यह नियम की ओर संकेत करता है। निर्णय क्लर्क का है। यह कानूनी सलाह नहीं है। / Points to the rule. The clerk decides. Not legal advice.',
    defaultSourceVersion: 'म.प्र. भूमि विकास नियम 2012, 6 मार्च 2025 तक समेकित',
    error429: 'बहुत अधिक प्रश्न, कृपया एक मिनट प्रतीक्षा करें।',
    error5xx: 'सेवा अनुपलब्ध है, पुनः प्रयास करें',
    retry: 'पुनः प्रयास करें',
    provideDetails: 'कृपया आवश्यक विवरण भरें:',
    appendAndAsk: 'विवरण जोड़ें और पुनः पूछें',
    sqm: 'वर्ग मीटर',
    sqft: 'वर्ग फीट',
    gaj: 'गज',
    meters: 'मीटर',
    feet: 'फीट',
    residential: 'आवासीय',
    commercial: 'व्यावसायिक',
    industrial: 'औद्योगिक',
    unit: 'इकाई',
  },
};
