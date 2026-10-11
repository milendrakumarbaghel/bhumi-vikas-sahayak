export type Status = "ANSWERED" | "NEEDS_MORE_INFORMATION" | "NEEDS_REVIEW" | "NOT_FOUND_IN_RULES";

export interface Citation {
  ruleNumber: string;
  title: string;
  passageLabel?: string;
  pageStart: number;
  pageEnd: number;
  sourceUrl?: string;
  excerpt: string;
  validated: boolean;
  hindiExcerpt?: string;
}

export interface FloorIllustration {
  groundCoveragePercent: number;
  footprintSqm: number;
  floors: number;
}

export interface CalcInput {
  name: string;
  value: number | string | boolean;
  unit: string;
}

export interface CalcResult {
  name: string;
  value: number;
  unit: string;
  text?: string;
  floorIllustration?: FloorIllustration[];
  floorIllustrationLabel?: string;
  [k: string]: unknown;
}

export interface Calc {
  code: string;
  rule: string;
  pdfPage: number;
  matchedBand: string;
  formula: string;
  inputs: CalcInput[];
  result: CalcResult;
  assumptions: string[];
  notes: string[];
}

export interface CalculationPayload {
  calculations?: Calc[];
  missingInputs?: string[];
  notApplicable?: string[];
  // Java fallback fields (shape c):
  code?: string;
  rule?: string;
  matchedBand?: string;
  roadWidthMeters?: number;
  frontOpenSpaceMeters?: number;
  [key: string]: unknown;
}

export interface AskResponse {
  answerId: string;
  status: Status;
  language: "en" | "hi";
  asOfDate: string;
  sourceVersion: string;
  sourceDocumentUrl: string;
  answer: string;
  citations: Citation[];
  calculation: CalculationPayload;
  warnings: string[];
  disclaimer: string;
}

export interface RulePassage {
  id?: number;
  label: string;
  pageStart: number;
  pageEnd: number;
  text: string;
}

export interface RuleDetail {
  id?: number;
  ruleNumber: string;
  title: string;
  part: string;
  pageStart: number;
  pageEnd: number;
  text: string;
  passages: RulePassage[];
}

export interface ValidationError {
  field?: string;
  message: string;
}

// Supplementary types for components / legacy modals
export interface TableRow {
  side: string;
  minimumSetback: string;
  ruleReference: string;
}

export interface RelatedRule {
  ruleNumber: string;
  title: string;
  pages: string;
  isActive?: boolean;
}

export interface DiagramData {
  plotAreaSqM: number;
  plotWidthM: number;
  plotDepthM: number;
  frontSetbackM: number;
  rearSetbackM: number;
  sideSetbackM: number;
  roadWidthM: number;
  zone: string;
}

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  timestamp: string;
  question: string;
  answerSummary?: string;
  rawText?: string;
  isNotFound?: boolean;
  tableData?: TableRow[];
  explanation?: string;
  importantNotes?: string[];
  citations?: Citation[];
  relatedRules?: RelatedRule[];
  diagramData?: DiagramData;
  isOutOfScope?: boolean;
  calculationDetails?: unknown;
}

export interface SampleQuestion {
  id: string;
  textEn: string;
  textHi: string;
  category: string;
  ruleRef: string;
  isOutOfScope?: boolean;
}

export interface RuleCategory {
  id: string;
  part: string;
  titleEn: string;
  titleHi: string;
  rulesRange: string;
  pages: string;
  description: string;
}
