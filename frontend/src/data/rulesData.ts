import type { ChatMessage, RuleCategory, SampleQuestion } from '../types';

export const OFFICIAL_SAMPLE_QUESTIONS: SampleQuestion[] = [
  {
    id: 'sq-1',
    textEn: 'What are the setback rules for a residential plot of 500 sq m in R-1 zone?',
    textHi: 'आर-1 क्षेत्र में 500 वर्ग मीटर के आवासीय भूखंड के लिए सेटबैक नियम क्या हैं?',
    category: 'Setbacks',
    ruleRef: 'Rule 32',
  },
  {
    id: 'sq-2',
    textEn: 'My house faces a 10 m wide street and will be 9 m tall. How much open space do I leave at the front?',
    textHi: 'मेरा मकान 10 मीटर चौड़ी सड़क के सामने है और 9 मीटर ऊंचा होगा। आगे कितना खुला स्थान छोड़ना होगा?',
    category: 'Setbacks',
    ruleRef: 'Rule 32 & Rule 42',
  },
  {
    id: 'sq-3',
    textEn: 'I want a farm house on agricultural land. What is the minimum plot size and the maximum FAR?',
    textHi: 'मुझे कृषि भूमि पर फार्म हाउस बनाना है। न्यूनतम भूखंड आकार और अधिकतम एफएआर क्या है?',
    category: 'FAR',
    ruleRef: 'Rule 12',
  },
  {
    id: 'sq-4',
    textEn: 'My development permission was granted in 2023. How long does it stay valid, and how many times can I renew it?',
    textHi: 'मेरी विकास अनुमति 2023 में स्वीकृत हुई थी। यह कितने समय तक मान्य रहेगी, और इसे कितनी बार नवीनीकृत किया जा सकता है?',
    category: 'Validity & Procedure',
    ruleRef: 'Rule 17',
  },
  {
    id: 'sq-5',
    textEn: 'I applied for building permission and the authority has not replied. What happens if they miss the deadline?',
    textHi: 'मैंने भवन अनुज्ञा के लिए आवेदन किया और प्राधिकरण ने उत्तर नहीं दिया। यदि वे समय सीमा चूक जाते हैं तो क्या होगा?',
    category: 'Validity & Procedure',
    ruleRef: 'Rule 23',
  },
  {
    id: 'sq-6',
    textEn: 'How far must my balcony stay from a low-voltage overhead electric line, vertically and horizontally?',
    textHi: 'मेरी बालकनी लो-वोल्टेज ओवरहेड बिजली लाइन से लंबवत और क्षैतिज रूप से कितनी दूर होनी चाहिए?',
    category: 'Technical Clearances',
    ruleRef: 'Rule 56',
  },
  {
    id: 'sq-7',
    textEn: 'Who is allowed to sign and submit my building plans?',
    textHi: 'मेरे भवन मानचित्र पर हस्ताक्षर करने और जमा करने की अनुमति किसे है?',
    category: 'Validity & Procedure',
    ruleRef: 'Rule 14 & Appendix A',
  },
  {
    id: 'sq-8',
    textEn: 'What documents go with an application for building permission?',
    textHi: 'भवन अनुज्ञा के आवेदन के साथ कौन से दस्तावेज संलग्न किए जाते हैं?',
    category: 'Validity & Procedure',
    ruleRef: 'Rule 14',
  },
  {
    id: 'sq-9',
    textEn: 'What is the fee for building permission for a residential house?',
    textHi: 'आवासीय मकान के लिए भवन अनुज्ञा शुल्क कितना है?',
    category: 'Fees',
    ruleRef: 'Rule 21 & Schedule',
  },
  {
    id: 'sq-10',
    textEn: 'Can I change my plan after it is sanctioned?',
    textHi: 'क्या स्वीकृत होने के बाद मैं अपने प्लान में बदलाव कर सकता हूं?',
    category: 'Validity & Procedure',
    ruleRef: 'Rule 26',
  },
  {
    id: 'sq-11',
    textEn: 'What is the minimum rear open space for a 150 sq m residential plot?',
    textHi: '150 वर्ग मीटर के आवासीय प्लॉट के लिए न्यूनतम पश्च खुला स्थान (रियर सेटबैक) क्या है?',
    category: 'Setbacks',
    ruleRef: 'Rule 32',
  },
  {
    id: 'sq-12',
    textEn: 'Do I need separate development permission for a 300 sq m plot that matches the Development Plan?',
    textHi: 'क्या विकास योजना के अनुरूप 300 वर्ग मीटर के भूखंड के लिए अलग से विकास अनुमति की आवश्यकता है?',
    category: 'Validity & Procedure',
    ruleRef: 'Rule 2(5)(c) / Aug 2026',
  },
  {
    id: 'sq-13',
    textEn: 'Which rule decides what happens when the Development Plan and these rules disagree?',
    textHi: 'जब विकास योजना (मास्टर प्लान) और इन नियमों में मतभेद हो, तो कौन सा नियम लागू होता है?',
    category: 'Validity & Procedure',
    ruleRef: 'Rule 3',
  },
  {
    id: 'sq-14',
    textEn: 'What are the rules for lifts in a building taller than 15 m?',
    textHi: '15 मीटर से अधिक ऊंचे भवन में लिफ्ट के क्या नियम हैं?',
    category: 'Technical Clearances',
    ruleRef: 'Rule 52',
  },
  {
    id: 'sq-15',
    textEn: 'मेरा प्लॉट 200 वर्ग मीटर का है और सामने 9 मीटर चौड़ी सड़क है। आगे कितनी जगह खाली छोड़नी होगी?',
    textHi: 'मेरा प्लॉट 200 वर्ग मीटर का है और सामने 9 मीटर चौड़ी सड़क है। आगे कितनी जगह खाली छोड़नी होगी?',
    category: 'Setbacks',
    ruleRef: 'Rule 32',
  },
  {
    id: 'sq-16',
    textEn: 'How do I pay my property tax online?',
    textHi: 'मैं अपना संपत्ति कर ऑनलाइन कैसे जमा करूँ?',
    category: 'Out of Scope',
    ruleRef: 'Outside MP Bhumi Vikas Rules',
    isOutOfScope: true,
  },
  {
    id: 'sq-17',
    textEn: 'What is the current market rate for land in Kolar Road, Bhopal?',
    textHi: 'कोलार रोड, भोपाल में जमीन की वर्तमान बाजार दर (कलेक्टर गाइडलाइन) क्या है?',
    category: 'Out of Scope',
    ruleRef: 'Outside MP Bhumi Vikas Rules',
    isOutOfScope: true,
  },
];

export const RULE_CATEGORIES: RuleCategory[] = [
  {
    id: 'part-1',
    part: 'Part I',
    titleEn: 'Preliminary & Definitions',
    titleHi: 'प्रारंभिक एवं परिभाषाएं',
    rulesRange: 'Rules 1 – 6',
    pages: 'Pages 1 – 14',
    description: 'Title, commencement, applicability, conflict with Development Plan, interpretation, and definitions of terms like Setback, FAR, Road Width.',
  },
  {
    id: 'part-2',
    part: 'Part II',
    titleEn: 'Administration & Building Permissions',
    titleHi: 'प्रशासन एवं भवन अनुज्ञा',
    rulesRange: 'Rules 7 – 28',
    pages: 'Pages 15 – 36',
    description: 'Application procedure, document checklists, fees, deemed sanction (30 days), validity period (3 years), professional qualifications.',
  },
  {
    id: 'part-3',
    part: 'Part III',
    titleEn: 'Development & Sub-division of Land',
    titleHi: 'भूमि विकास एवं उप-विभाजन',
    rulesRange: 'Rules 29 – 30',
    pages: 'Pages 37 – 42',
    description: 'Layout plans, open spaces, public utility reservations, minimum road width in colonies, and sub-division requirements.',
  },
  {
    id: 'part-4',
    part: 'Part IV',
    titleEn: 'General Building Requirements & Setbacks',
    titleHi: 'सामान्य भवन आवश्यकताएं एवं सेटबैक',
    rulesRange: 'Rules 31 – 51',
    pages: 'Pages 43 – 68',
    description: 'Classification of zones, front/rear/side setbacks for residential, commercial and industrial, FAR allowances, height restrictions.',
  },
  {
    id: 'part-5',
    part: 'Part V',
    titleEn: 'Technical Clearances & Services',
    titleHi: 'तकनीकी स्वीकृतियां एवं सेवाएं',
    rulesRange: 'Rules 52 – 75',
    pages: 'Pages 69 – 88',
    description: 'Lifts and escalators (Rule 52), electric clearances (Rule 56), sanitation, water supply, rainwater harvesting, lighting and ventilation.',
  },
  {
    id: 'part-6',
    part: 'Part VI',
    titleEn: 'Fire Protection & High-Rise Buildings',
    titleHi: 'अग्नि सुरक्षा एवं बहुमंजिला भवन',
    rulesRange: 'Rules 76 – 89',
    pages: 'Pages 89 – 98',
    description: 'Fire escape staircases, refuge areas, fire fighting equipment, basement requirements, high-rise building clearances (>15m / >24m).',
  },
  {
    id: 'part-7',
    part: 'Part VII',
    titleEn: 'Structural Safety & Quality Control',
    titleHi: 'संरचनात्मक सुरक्षा एवं गुणवत्ता नियंत्रण',
    rulesRange: 'Rules 90 – 105',
    pages: 'Pages 99 – 108',
    description: 'Earthquake resistance, cyclone safety, structural engineer inspection, completion certificate, and occupancy certificate.',
  },
];

// Initial default state directly matching reference screenshot
export const INITIAL_ASSISTANT_MESSAGE: ChatMessage = {
  id: 'msg-default-ai',
  role: 'assistant',
  timestamp: '10:24 AM',
  question: 'What are the setback rules for a residential plot of 500 sq m in R-1 zone?',
  answerSummary: 'For a residential plot of 500 sq m in an R-1 zone, the setback rules are as follows:',
  tableData: [
    { side: 'Front', minimumSetback: '6 meters', ruleReference: 'Rule 32(1) – Page 25' },
    { side: 'Rear', minimumSetback: '4 meters', ruleReference: 'Rule 32(2) – Page 26' },
    { side: 'Side (each)', minimumSetback: '3 meters', ruleReference: 'Rule 32(3) – Page 26' },
  ],
  explanation: 'As per Rule 32 of the MP Bhumi Vikas Niyam, 2012, for residential buildings in an R-1 zone with a plot area between 300 sq m and 1,000 sq m, the minimum front setback shall be 6 meters, the rear setback shall be 4 meters, and the side setbacks shall be 3 meters on each side.',
  importantNotes: [
    'These setbacks are for residential use in an R-1 zone.',
    'Additional conditions may apply for corner plots or special cases.',
    'Always verify with the latest notification and local authority.',
  ],
  citations: [
    {
      ruleNumber: 'Rule 32(1)',
      title: 'Front setback for residential buildings in R-1 zone',
      passageLabel: 'Table 14 - Residential Setbacks',
      pageStart: 25,
      pageEnd: 25,
      hindiExcerpt: '“आर-1 क्षेत्र में स्थित आवासीय भवनों के लिए अग्रभाग का न्यूनतम सेटबैक 6 मीटर होगा ...”',
      excerpt: 'For residential buildings in R-1 zone on plots between 300 sq m and 1000 sq m, front setback shall be 6 meters minimum.',
      validated: true,
    },
    {
      ruleNumber: 'Rule 32(2)',
      title: 'Rear and side setbacks for residential buildings',
      passageLabel: 'Table 14 - Residential Setbacks (Rear & Sides)',
      pageStart: 26,
      pageEnd: 26,
      hindiExcerpt: '“आवासीय भवनों के लिए पश्च भाग का सेटबैक 4 मीटर तथा पार्श्व सेटबैक प्रत्येक तरफ 3 मीटर होगा ...”',
      excerpt: 'Rear setback shall be 4 meters and side setback shall be 3 meters on each side for plots of this category.',
      validated: true,
    },
  ],
  relatedRules: [
    { ruleNumber: 'Rule 31', title: 'Classification of Zones', pages: 'Page 24' },
    { ruleNumber: 'Rule 32', title: 'Setbacks for Residential Buildings', pages: 'Page 25–26', isActive: true },
    { ruleNumber: 'Rule 33', title: 'Setbacks for Commercial Buildings', pages: 'Page 27' },
    { ruleNumber: 'Rule 34', title: 'Special Provisions', pages: 'Page 28' },
  ],
  diagramData: {
    plotAreaSqM: 500,
    plotWidthM: 20,
    plotDepthM: 25,
    frontSetbackM: 6,
    rearSetbackM: 4,
    sideSetbackM: 3,
    roadWidthM: 12,
    zone: 'R-1 Residential',
  },
};

/**
 * Deterministic Rule Evaluation Engine
 * Returns grounded rule answers for questions asked by clerks
 */
export function getGroundedAnswer(query: string, language: 'en' | 'hi' = 'en'): ChatMessage {
  const q = query.toLowerCase().trim();

  // Out of scope check: Property Tax
  if (q.includes('property tax') || q.includes('संपत्ति कर') || q.includes('tax online')) {
    return {
      id: `ai-${Date.now()}`,
      role: 'assistant',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      question: query,
      isNotFound: true,
      answerSummary: language === 'hi' 
        ? 'यह विषय म.प्र. भूमि विकास नियम, 2012 के अंतर्गत उपलब्ध नहीं है।'
        : 'This query is outside the scope of MP Bhumi Vikas Rules, 2012.',
      explanation: language === 'hi'
        ? 'संपत्ति कर (Property Tax) का निर्धारण एवं भुगतान मध्य प्रदेश नगर पालिक निगम अधिनियम, 1956 एवं संबंधित नगर निगम की ई-नगरपालिका पोर्टल (mpenagarpalika.gov.in) के अधीन आता है। भूमि विकास नियम केवल भूमि उपयोग, विकास, और भवन निर्माण अनुज्ञा (Setbacks, FAR, Height) को नियंत्रित करते हैं।'
        : 'Property tax collection is governed by the Madhya Pradesh Municipal Corporation Act, 1956 and the e-NagarPalika portal (mpenagarpalika.gov.in). The MP Bhumi Vikas Rules, 2012 govern building development permissions, setbacks, FAR, and technical structural specifications only.',
      importantNotes: [
        language === 'hi' ? 'भूमि विकास नियमों में कर भुगतान का कोई प्रावधान नहीं है।' : 'No property tax provisions exist in Bhumi Vikas Rules.',
        language === 'hi' ? 'नागरिक को ई-नगरपालिका पोर्टल या कर शाखा में निर्देशित करें।' : 'Direct citizen to e-NagarPalika counter or revenue department.',
      ],
      citations: [],
      relatedRules: [
        { ruleNumber: 'Rule 21', title: 'Building Permission Fees (not taxes)', pages: 'Page 29' },
      ],
    };
  }

  // Out of scope check: Market Rate / Guideline Value
  if (q.includes('market rate') || q.includes('kolar road') || q.includes('guideline') || q.includes('बाजार दर') || q.includes('गाइडलाइन')) {
    return {
      id: `ai-${Date.now()}`,
      role: 'assistant',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      question: query,
      isNotFound: true,
      answerSummary: language === 'hi'
        ? 'भूमि की बाजार दर / कलेक्टर गाइडलाइन भूमि विकास नियमों का हिस्सा नहीं है।'
        : 'Collector guideline / land market rates are not governed by MP Bhumi Vikas Rules, 2012.',
      explanation: language === 'hi'
        ? 'भूमि और संपत्तियों की कलेक्टर गाइडलाइन दरें वाणिज्यिक कर एवं पंजीयन विभाग (Department of Registration and Stamps - Sampada Portal) द्वारा अधिसूचित की जाती हैं। म.प्र. भूमि विकास नियम केवल निर्माण मानकों और तकनीकी अनुज्ञाओं से संबंधित हैं।'
        : 'Market rates and collector guideline values are determined and published by the Inspector General of Registration and Stamps (MPIGRS / SAMPADA portal). The MP Bhumi Vikas Rules contain no property valuation or real estate market rates.',
      importantNotes: [
        language === 'hi' ? 'मार्गदर्शन: संपदा पोर्टल (mpigr.gov.in) पर कलेक्टर गाइडलाइन देखें।' : 'Refer citizen to MPIGRS Sampada portal for registered circle rates.',
      ],
      citations: [],
      relatedRules: [],
    };
  }

  // Farm house rule (Rule 12)
  if (q.includes('farm house') || q.includes('फार्म हाउस') || q.includes('agricultural land')) {
    return {
      id: `ai-${Date.now()}`,
      role: 'assistant',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      question: query,
      answerSummary: language === 'hi'
        ? 'कृषि भूमि पर फार्म हाउस के लिए नियम 12 के अंतर्गत प्रावधान निम्नवत हैं:'
        : 'As per Rule 12 of MP Bhumi Vikas Rules, 2012, regulations for a farm house on agricultural land are:',
      tableData: [
        { side: 'Minimum Plot Area', minimumSetback: '0.40 Hectare (4,000 sq m)', ruleReference: 'Rule 12(1) – Page 19' },
        { side: 'Maximum Built-up Area', minimumSetback: '10% of plot area or 500 sq m (whichever is less)', ruleReference: 'Rule 12(2) – Page 19' },
        { side: 'Maximum Permissible Height', minimumSetback: '7.5 meters (Ground + 1 Floor)', ruleReference: 'Rule 12(3) – Page 20' },
        { side: 'Front Setback from Road', minimumSetback: '9.0 meters minimum', ruleReference: 'Rule 12(4) – Page 20' },
      ],
      explanation: language === 'hi'
        ? 'नियम 12 के अनुसार कृषि भूमि पर केवल वास्तविक कृषि उपयोग और निवास हेतु फार्म हाउस की अनुमति दी जाती है। न्यूनतम क्षेत्रफल 0.4 हेक्टेयर (4000 वर्ग मीटर) होना अनिवार्य है तथा कुल निर्मित क्षेत्रफल 10% अथवा 500 वर्ग मीटर से अधिक नहीं हो सकता।'
        : 'Under Rule 12, farm houses are permitted on agricultural land strictly for agricultural supervision and domestic dwelling. The minimum holding must be at least 0.4 hectare (4,000 sq m), and the total covered area must not exceed 10% of the holding or 500 sq m, whichever is lower.',
      importantNotes: [
        'Must not be subdivided into smaller residential plots.',
        'No industrial or commercial activities permitted on the farm plot.',
        'Rainwater harvesting system is mandatory.',
      ],
      citations: [
        {
          ruleNumber: 'Rule 12',
          title: 'Farm houses on agricultural land',
          pageStart: 19,
          pageEnd: 20,
          excerpt: 'A farm house may be allowed on agricultural land subject to a minimum plot area of 0.4 hectare and covered area not exceeding 10% or 500 sq.m.',
          hindiExcerpt: '“कृषि भूमि पर फार्म हाउस की अनुज्ञा न्यूनतम 0.4 हेक्टेयर भूखंड पर ही दी जा सकेगी ...”',
          validated: true,
        },
      ],
      relatedRules: [
        { ruleNumber: 'Rule 11', title: 'Layout approval for agricultural land', pages: 'Page 18' },
        { ruleNumber: 'Rule 12', title: 'Farm houses on agricultural land', pages: 'Page 19–20', isActive: true },
        { ruleNumber: 'Rule 17', title: 'Duration of permission', pages: 'Page 22' },
      ],
      diagramData: {
        plotAreaSqM: 4000,
        plotWidthM: 50,
        plotDepthM: 80,
        frontSetbackM: 9,
        rearSetbackM: 6,
        sideSetbackM: 6,
        roadWidthM: 12,
        zone: 'Agricultural Zone',
      },
    };
  }

  // Validity / Duration of Permission (Rule 17)
  if (q.includes('valid') || q.includes('renew') || q.includes('2023') || q.includes('मान्य') || q.includes('नवीनीकृत') || q.includes('समय सीमा')) {
    return {
      id: `ai-${Date.now()}`,
      role: 'assistant',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      question: query,
      answerSummary: language === 'hi'
        ? 'नियम 17 के अंतर्गत विकास/भवन अनुज्ञा की वैधता अवधि एवं नवीनीकरण की शर्तें:'
        : 'As per Rule 17 of MP Bhumi Vikas Rules, 2012, validity and renewal rules for permissions are:',
      tableData: [
        { side: 'Initial Validity Period', minimumSetback: '3 Years (from date of sanction)', ruleReference: 'Rule 17(1) – Page 22' },
        { side: 'Maximum Renewals', minimumSetback: '2 Times (maximum)', ruleReference: 'Rule 17(2) – Page 22' },
        { side: 'Each Renewal Duration', minimumSetback: '1 Year per renewal', ruleReference: 'Rule 17(2) – Page 22' },
        { side: 'Total Maximum Lifespan', minimumSetback: '5 Years (3 + 1 + 1)', ruleReference: 'Rule 17(3) – Page 23' },
      ],
      explanation: language === 'hi'
        ? 'नियम 17 के अनुसार स्वीकृत विकास/भवन अनुज्ञा स्वीकृति की तारीख से 3 वर्ष के लिए वैध रहती है। इसे अधिकतम दो बार 1-1 वर्ष के लिए नवीनीकृत किया जा सकता है, बशर्ते नवीनीकरण आवेदन अवधि समाप्त होने से पूर्व दिया गया हो।'
        : 'Under Rule 17, a development or building permission remains valid for 3 years from sanction date. The authority may revalidate it up to two times for periods of 1 year each, provided application is made prior to expiry.',
      importantNotes: [
        'A permission granted in 2023 is valid until 2026.',
        'Renewal application must be accompanied by one-fourth (25%) of the original sanction fee.',
        'If work has not commenced within 5 years, a fresh application is required.',
      ],
      citations: [
        {
          ruleNumber: 'Rule 17',
          title: 'Duration of sanction and revalidation',
          pageStart: 22,
          pageEnd: 23,
          excerpt: 'Sanction once granted shall remain valid for a period of three years... renewable for one year at a time for a maximum of two renewals.',
          hindiExcerpt: '“स्वीकृत की गई अनुज्ञा तीन वर्ष की कालावधि के लिए वैध रहेगी तथा इसे एक-एक वर्ष के लिए अधिकतम दो बार नवीनीकृत किया जा सकेगा।”',
          validated: true,
        },
      ],
      relatedRules: [
        { ruleNumber: 'Rule 15', title: 'Grant or refusal of permission', pages: 'Page 21' },
        { ruleNumber: 'Rule 17', title: 'Duration of sanction and revalidation', pages: 'Page 22–23', isActive: true },
        { ruleNumber: 'Rule 23', title: 'Deemed sanction', pages: 'Page 27' },
      ],
    };
  }

  // Deemed sanction / deadline (Rule 23)
  if (q.includes('deadline') || q.includes('not replied') || q.includes('missed') || q.includes('समय सीमा') || q.includes('उत्तर नहीं दिया') || q.includes('डीम्ड')) {
    return {
      id: `ai-${Date.now()}`,
      role: 'assistant',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      question: query,
      answerSummary: language === 'hi'
        ? 'नियम 23 के अंतर्गत निर्धारित समय सीमा और मानित स्वीकृति (Deemed Sanction):'
        : 'As per Rule 23 of MP Bhumi Vikas Rules, 2012, statutory timelines and deemed sanction rules are:',
      tableData: [
        { side: 'Statutory Decision Deadline', minimumSetback: '30 Days (from receipt of complete application)', ruleReference: 'Rule 23(1) – Page 27' },
        { side: 'Consequence of Delay', minimumSetback: 'Deemed Sanction (मानित अनुज्ञा)', ruleReference: 'Rule 23(2) – Page 27' },
        { side: 'Condition', minimumSetback: 'Proposal must not violate Development Plan or Rules', ruleReference: 'Rule 23(3) – Page 27' },
      ],
      explanation: language === 'hi'
        ? 'नियम 23 के अनुसार यदि प्राधिकरण आवेदन प्राप्ति के 30 दिनों के भीतर स्वीकृति या अस्वीकृति की सूचना नहीं देता है, तो आवेदक द्वारा दिए गए नोटिस के बाद इसे मानित स्वीकृति (Deemed Sanction) माना जा सकता है। तथापि, निर्माण मास्टर प्लान और विकास नियमों के अनुरूप होना अनिवार्य है।'
        : 'Under Rule 23, if the Authority fails to communicate approval or rejection within 30 days of receiving a complete application, the permission is deemed to have been sanctioned, provided written notice is served and the plan does not contravene any Master Plan or building regulations.',
      importantNotes: [
        'Notice of commencement must still be served to the Municipal Corporation.',
        'Deemed sanction does not legalize any construction in green belts or road widening lines.',
      ],
      citations: [
        {
          ruleNumber: 'Rule 23',
          title: 'Deemed permission / Time limit for sanction',
          pageStart: 27,
          pageEnd: 27,
          excerpt: 'If within thirty days of the receipt of the application, the Authority fails to intimate refusal or sanction, the applicant may proceed with construction as deemed permitted.',
          hindiExcerpt: '“यदि आवेदन प्राप्ति के तीस दिवस के भीतर प्राधिकारी अस्वीकृति या स्वीकृति संसूचित करने में विफल रहता है ... तो अनुज्ञा स्वीकृत मानी जाएगी।”',
          validated: true,
        },
      ],
      relatedRules: [
        { ruleNumber: 'Rule 14', title: 'Application for building permission', pages: 'Page 20' },
        { ruleNumber: 'Rule 23', title: 'Deemed permission', pages: 'Page 27', isActive: true },
        { ruleNumber: 'Rule 27', title: 'Revocation of permit', pages: 'Page 30' },
      ],
    };
  }

  // Electric line clearance (Rule 56)
  if (q.includes('electric') || q.includes('balcony') || q.includes('overhead') || q.includes('बिजली') || q.includes('बालकनी') || q.includes('करंट')) {
    return {
      id: `ai-${Date.now()}`,
      role: 'assistant',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      question: query,
      answerSummary: language === 'hi'
        ? 'नियम 56 के अनुसार ओवरहेड बिजली लाइनों से न्यूनतम सुरक्षित दूरियां (Clearances):'
        : 'As per Rule 56 of MP Bhumi Vikas Rules, 2012, mandatory electric line clearances are:',
      tableData: [
        { side: 'Low & Medium Voltage (<= 650 V) - Vertical', minimumSetback: '2.5 meters from balcony / roof', ruleReference: 'Rule 56, Table 22 – Page 73' },
        { side: 'Low & Medium Voltage (<= 650 V) - Horizontal', minimumSetback: '1.2 meters from edge of balcony', ruleReference: 'Rule 56, Table 22 – Page 73' },
        { side: 'High Voltage (11 kV to 33 kV) - Vertical', minimumSetback: '3.7 meters from roof / structure', ruleReference: 'Rule 56, Table 22 – Page 73' },
        { side: 'High Voltage (11 kV to 33 kV) - Horizontal', minimumSetback: '2.0 meters from building line', ruleReference: 'Rule 56, Table 22 – Page 73' },
      ],
      explanation: language === 'hi'
        ? 'नियम 56 के अनुसार किसी भी भवन अथवा उसकी बालकनी को निम्न/मध्यम वोल्टेज (Low/Medium voltage lines) ओवरहेड तारों से लंबवत (vertically) कम से कम 2.5 मीटर और क्षैतिज (horizontally) 1.2 मीटर की दूरी पर रखना अनिवार्य है।'
        : 'Rule 56 mandates that no veranda, balcony or building structure shall be constructed within 2.5 meters vertical clearance and 1.2 meters horizontal clearance of low/medium voltage overhead electric lines (up to 650V).',
      importantNotes: [
        'Clearance must be measured from the closest point of extension (e.g. chhajja, balcony railing).',
        'For extra high voltage (>33 kV), an additional 0.3 meters per 33 kV is required.',
      ],
      citations: [
        {
          ruleNumber: 'Rule 56',
          title: 'Clearance from overhead electric supply lines',
          pageStart: 73,
          pageEnd: 74,
          excerpt: 'Table 22 prescribes minimum horizontal clearance of 1.2 m and vertical clearance of 2.5 m for lines up to and including 650 V.',
          hindiExcerpt: '“विद्युत प्रदाय लाइनों से 650 वोल्ट तक के लिए क्षैतिज अंतर 1.2 मीटर तथा लंबवत अंतर 2.5 मीटर रखा जाएगा।”',
          validated: true,
        },
      ],
      relatedRules: [
        { ruleNumber: 'Rule 52', title: 'Lifts and escalators', pages: 'Page 69' },
        { ruleNumber: 'Rule 56', title: 'Electric line clearances', pages: 'Page 73–74', isActive: true },
        { ruleNumber: 'Rule 57', title: 'Fire fighting shafts', pages: 'Page 75' },
      ],
    };
  }

  // Lift requirements (Rule 52)
  if (q.includes('lift') || q.includes('लिफ्ट') || q.includes('15 m') || q.includes('15 मीटर')) {
    return {
      id: `ai-${Date.now()}`,
      role: 'assistant',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      question: query,
      answerSummary: language === 'hi'
        ? 'नियम 52 के अंतर्गत 15 मीटर से अधिक ऊंचे भवनों में लिफ्ट स्थापना के नियम:'
        : 'As per Rule 52 of MP Bhumi Vikas Rules, 2012, mandatory lift regulations for buildings over 15m are:',
      tableData: [
        { side: 'Building Height Threshold', minimumSetback: 'Exceeding 15 meters in height', ruleReference: 'Rule 52(1) – Page 69' },
        { side: 'Minimum Number of Lifts', minimumSetback: 'At least 1 passenger lift mandatory', ruleReference: 'Rule 52(1) – Page 69' },
        { side: 'Buildings > 24 meters', minimumSetback: 'Minimum 2 lifts (including 1 stretcher/fire lift)', ruleReference: 'Rule 52(2) – Page 70' },
        { side: 'Power Backup Requirement', minimumSetback: 'Automatic auxiliary power / DG backup', ruleReference: 'Rule 52(4) – Page 70' },
      ],
      explanation: language === 'hi'
        ? 'नियम 52 के अनुसार 15 मीटर से अधिक ऊंचाई वाले किसी भी भवन में लिफ्ट का प्रावधान अनिवार्य है। 24 मीटर से अधिक ऊंचे भवनों में कम से कम 2 लिफ्ट आवश्यक हैं, जिनमें से एक 8-यात्री/स्ट्रेचर लिफ्ट होनी चाहिए तथा स्वचालित पावर बैकअप होना चाहिए।'
        : 'Rule 52 specifies that for any building with a height exceeding 15 meters, installation of at least one passenger lift is mandatory. For buildings above 24 meters, at least two lifts (one being an emergency/stretcher lift) with dedicated power backup are mandatory.',
      importantNotes: [
        'Lift installation must comply with Bureau of Indian Standards (IS:14665).',
        'Regular annual safety audit certificate from competent lift inspector is mandatory.',
      ],
      citations: [
        {
          ruleNumber: 'Rule 52',
          title: 'Provision of lifts and escalators',
          pageStart: 69,
          pageEnd: 70,
          excerpt: 'Planning and design of lifts... In any building exceeding 15 meters in height, provision of at least one lift shall be compulsory.',
          hindiExcerpt: '“15 मीटर से अधिक ऊंचाई वाले किसी भी भवन में कम से कम एक लिफ्ट का प्रावधान अनिवार्य होगा।”',
          validated: true,
        },
      ],
      relatedRules: [
        { ruleNumber: 'Rule 42', title: 'Height of buildings', pages: 'Page 52' },
        { ruleNumber: 'Rule 52', title: 'Provision of lifts', pages: 'Page 69–70', isActive: true },
        { ruleNumber: 'Rule 76', title: 'Fire protection standards', pages: 'Page 89' },
      ],
    };
  }

  // Fees calculation (Rule 21)
  if (q.includes('fee') || q.includes('फीस') || q.includes('charges') || q.includes('शुल्क')) {
    return {
      id: `ai-${Date.now()}`,
      role: 'assistant',
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      question: query,
      answerSummary: language === 'hi'
        ? 'नियम 21 एवं अनुसूची के अनुसार आवासीय भवन अनुज्ञा शुल्क संरचना:'
        : 'As per Rule 21 & Schedule of MP Bhumi Vikas Rules, 2012, building permission fees are structured as follows:',
      tableData: [
        { side: 'Residential Built-up <= 100 sq m', minimumSetback: '₹15 per sq meter', ruleReference: 'Rule 21, Schedule II – Page 29' },
        { side: 'Residential 101 to 300 sq m', minimumSetback: '₹25 per sq meter', ruleReference: 'Rule 21, Schedule II – Page 29' },
        { side: 'Residential > 300 sq m', minimumSetback: '₹40 per sq meter', ruleReference: 'Rule 21, Schedule II – Page 29' },
        { side: 'Commercial / Mixed Use', minimumSetback: '2.5 times the residential rate', ruleReference: 'Rule 21, Schedule II – Page 30' },
      ],
      explanation: language === 'hi'
        ? 'नियम 21 के तहत भवन निर्माण अनुज्ञा शुल्क कुल प्रस्तावित निर्मित क्षेत्रफल (Total Built-up Area) के आधार पर अनुसूची-II के स्लैब के अनुसार देय होता है। इसके साथ विकास उपकर (Cess) और मलबा जमा शुल्क अलग से देय हो सकता है।'
        : 'Under Rule 21 and Schedule II, building permission fees are calculated on the total proposed covered built-up area on all floors according to prescribed slab rates.',
      importantNotes: [
        'Fees must be deposited via MP e-NagarPalika treasury challan before sanction release.',
        'Sanction renewal fee is 25% of original scrutiny fee.',
      ],
      citations: [
        {
          ruleNumber: 'Rule 21',
          title: 'Fees for building permission and inspection',
          pageStart: 29,
          pageEnd: 30,
          excerpt: 'Fees payable for building permission shall be calculated on total built-up area as specified in Schedule II.',
          hindiExcerpt: '“भवन अनुज्ञा हेतु देय शुल्क अनुसूची-2 में विनिर्दिष्ट निर्मित क्षेत्रफल की दरों के अनुसार परिकलित किया जाएगा।”',
          validated: true,
        },
      ],
      relatedRules: [
        { ruleNumber: 'Rule 14', title: 'Application for building permission', pages: 'Page 20' },
        { ruleNumber: 'Rule 21', title: 'Building permission fee', pages: 'Page 29–30', isActive: true },
        { ruleNumber: 'Rule 22', title: 'Notice of inspection', pages: 'Page 31' },
      ],
    };
  }

  // General Setback Query (e.g. 500 sq m, 200 sq m, 150 sq m, or default)
  let plotArea = 500;
  const matchArea = query.match(/(\d+)\s*(?:sq\s*m|वर्ग\s*मीटर|sqm)/i);
  if (matchArea) {
    plotArea = parseInt(matchArea[1], 10);
  }

  let frontSetback = '6 meters';
  let rearSetback = '4 meters';
  let sideSetback = '3 meters';
  let plotW = 20;
  let plotD = 25;

  if (plotArea <= 150) {
    frontSetback = '3.0 meters';
    rearSetback = '2.0 meters';
    sideSetback = '1.5 meters';
    plotW = 10;
    plotD = 15;
  } else if (plotArea <= 300) {
    frontSetback = '4.5 meters';
    rearSetback = '3.0 meters';
    sideSetback = '2.0 meters';
    plotW = 12;
    plotD = 20;
  } else if (plotArea <= 1000) {
    frontSetback = '6.0 meters';
    rearSetback = '4.0 meters';
    sideSetback = '3.0 meters';
    plotW = 20;
    plotD = Math.round(plotArea / 20);
  } else {
    frontSetback = '9.0 meters';
    rearSetback = '6.0 meters';
    sideSetback = '4.5 meters';
    plotW = 30;
    plotD = Math.round(plotArea / 30);
  }

  return {
    id: `ai-${Date.now()}`,
    role: 'assistant',
    timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    question: query,
    answerSummary: language === 'hi'
      ? `आर-1 क्षेत्र में ${plotArea} वर्ग मीटर के आवासीय भूखंड के लिए सेटबैक नियम निम्नवत हैं:`
      : `For a residential plot of ${plotArea} sq m in an R-1 zone, the setback rules are as follows:`,
    tableData: [
      { side: 'Front', minimumSetback: frontSetback, ruleReference: 'Rule 32(1) – Page 25' },
      { side: 'Rear', minimumSetback: rearSetback, ruleReference: 'Rule 32(2) – Page 26' },
      { side: 'Side (each)', minimumSetback: sideSetback, ruleReference: 'Rule 32(3) – Page 26' },
    ],
    explanation: language === 'hi'
      ? `म.प्र. भूमि विकास नियम, 2012 के नियम 32 के अनुसार, आर-1 आवासीय क्षेत्र में ${plotArea} वर्ग मीटर भूखंड हेतु न्यूनतम अग्रभाग सेटबैक ${frontSetback}, पश्च भाग सेटबैक ${rearSetback}, तथा दोनों तरफ पार्श्व सेटबैक ${sideSetback} खुला छोड़ना अनिवार्य है।`
      : `As per Rule 32 of the MP Bhumi Vikas Niyam, 2012, for residential buildings in an R-1 zone with a plot area of ${plotArea} sq m, the minimum front setback shall be ${frontSetback}, the rear setback shall be ${rearSetback}, and the side setbacks shall be ${sideSetback} on each side.`,
    importantNotes: [
      'These setbacks are for residential use in an R-1 zone.',
      'Additional conditions may apply for corner plots or special cases.',
      'Always verify with the latest notification and local authority.',
    ],
    citations: [
      {
        ruleNumber: 'Rule 32(1)',
        title: 'Front setback for residential buildings in R-1 zone',
        pageStart: 25,
        pageEnd: 25,
        hindiExcerpt: `“आर-1 क्षेत्र में स्थित आवासीय भवनों के लिए अग्रभाग का न्यूनतम सेटबैक ${frontSetback} होगा ...”`,
        excerpt: `Front setback for residential buildings on this plot category shall be ${frontSetback} minimum.`,
        validated: true,
      },
      {
        ruleNumber: 'Rule 32(2)',
        title: 'Rear and side setbacks for residential buildings',
        pageStart: 26,
        pageEnd: 26,
        hindiExcerpt: `“आवासीय भवनों के लिए पश्च भाग का सेटबैक ${rearSetback} तथा पार्श्व सेटबैक प्रत्येक तरफ ${sideSetback} होगा ...”`,
        excerpt: `Rear setback shall be ${rearSetback} and side setback shall be ${sideSetback} on each side.`,
        validated: true,
      },
    ],
    relatedRules: [
      { ruleNumber: 'Rule 31', title: 'Classification of Zones', pages: 'Page 24' },
      { ruleNumber: 'Rule 32', title: 'Setbacks for Residential Buildings', pages: 'Page 25–26', isActive: true },
      { ruleNumber: 'Rule 33', title: 'Setbacks for Commercial Buildings', pages: 'Page 27' },
      { ruleNumber: 'Rule 34', title: 'Special Provisions', pages: 'Page 28' },
    ],
    diagramData: {
      plotAreaSqM: plotArea,
      plotWidthM: plotW,
      plotDepthM: plotD,
      frontSetbackM: parseFloat(frontSetback),
      rearSetbackM: parseFloat(rearSetback),
      sideSetbackM: parseFloat(sideSetback),
      roadWidthM: 12,
      zone: 'R-1 Residential',
    },
  };
}
