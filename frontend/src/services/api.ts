import type { AskResponse, RuleDetail, ValidationError } from '../types';

export const API_BASE_URL =
  (import.meta.env.VITE_API_BASE_URL as string) || 'http://localhost:8080';

export class ApiError extends Error {
  public status: number;
  public validationErrors?: ValidationError[];

  constructor(message: string, status: number, validationErrors?: ValidationError[]) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.validationErrors = validationErrors;
  }
}

export interface UserPrincipal {
  id: number;
  username: string;
  role: 'USER' | 'ADMIN';
  enabled: boolean;
}

export type BackendAskResponse = AskResponse;

export const api = {
  checkHealth: async () => {
    try {
      const res = await fetch(`${API_BASE_URL}/api/v1/health/live`);
      return { isOnline: res.ok };
    } catch {
      return { isOnline: false };
    }
  },
  getMe: async (): Promise<UserPrincipal | null> => null,
  getSavedUsername: () => null,
  getSavedRole: () => null,
  setAuth: (_u?: string, _p?: string, _r?: string) => {},
  login: async (username: string, _p?: string, role?: string): Promise<UserPrincipal> => ({
    id: 1,
    username,
    role: (role as 'USER' | 'ADMIN') || 'USER',
    enabled: true,
  }),
  logout: async () => {},
  askQuestion,
};

export async function askQuestion(
  question: string,
  language: 'auto' | 'en' | 'hi' = 'auto',
  signalOrDate?: AbortSignal | string,
  maybeDateOrSignal?: string | AbortSignal
): Promise<AskResponse> {
  const signal =
    signalOrDate instanceof AbortSignal
      ? signalOrDate
      : maybeDateOrSignal instanceof AbortSignal
      ? maybeDateOrSignal
      : undefined;

  const asOfDate =
    typeof signalOrDate === 'string'
      ? signalOrDate
      : typeof maybeDateOrSignal === 'string'
      ? maybeDateOrSignal
      : undefined;

  const url = `${API_BASE_URL}/api/v1/questions:answer`;
  const body: Record<string, string> = {
    question: question.trim(),
    language,
  };
  if (asOfDate) {
    body.asOfDate = asOfDate;
  }

  let response: Response;
  try {
    response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
      signal,
    });
  } catch (err: any) {
    if (err?.name === 'AbortError') {
      throw err;
    }
    throw new ApiError('Service unavailable, network connection failed', 503);
  }

  if (!response.ok) {
    if (response.status === 422) {
      try {
        const problem = await response.json();
        const errors: ValidationError[] = [];
        if (Array.isArray(problem.errors)) {
          for (const e of problem.errors) {
            errors.push({ field: e.field, message: e.message || e.defaultMessage || 'Invalid input' });
          }
        } else if (problem.detail) {
          errors.push({ message: problem.detail });
        }
        throw new ApiError(problem.title || 'Validation error', 422, errors);
      } catch (e: any) {
        if (e instanceof ApiError) throw e;
        throw new ApiError('Validation error', 422);
      }
    }

    if (response.status === 429) {
      throw new ApiError('Too many questions, wait a minute', 429);
    }

    throw new ApiError(`Service unavailable (Status ${response.status})`, response.status);
  }

  return await response.json();
}

/**
 * Fetch a single rule's complete text and passages for highlighting
 */
export async function getRuleDetail(
  ruleNumber: string,
  signal?: AbortSignal
): Promise<RuleDetail> {
  const cleanNum = ruleNumber.replace(/^Rule\s*/i, '').trim();
  const url = `${API_BASE_URL}/api/v1/rules/${encodeURIComponent(cleanNum)}`;

  const response = await fetch(url, {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
    signal,
  });

  if (!response.ok) {
    throw new ApiError(`Failed to fetch Rule ${ruleNumber}`, response.status);
  }

  return await response.json();
}

/**
 * Construct direct PDF page URL
 */
export function getPdfUrl(page: number): string {
  return `${API_BASE_URL}/api/v1/documents/rules.pdf#page=${page}`;
}

/**
 * Collapses whitespace for reliable excerpt matching
 */
export function normalizeWhitespace(str: string): string {
  return str.replace(/\s+/g, ' ').trim();
}

/**
 * Highlights an excerpt inside a full passage/rule text
 * Returns chunks: [{ text, isHighlight }]
 */
export function highlightExcerpt(
  fullText: string,
  excerpt: string
): { text: string; isHighlight: boolean }[] {
  if (!fullText || !excerpt) return [{ text: fullText, isHighlight: false }];

  const normFull = normalizeWhitespace(fullText);
  const normExcerpt = normalizeWhitespace(excerpt);

  const idx = normFull.toLowerCase().indexOf(normExcerpt.toLowerCase());
  if (idx === -1) {
    // If no direct normalized match, return full text un-highlighted
    return [{ text: fullText, isHighlight: false }];
  }

  // Find approximate start and end in original text
  // Simple heuristic: search substring or return highlighted block
  return [
    { text: normFull.substring(0, idx), isHighlight: false },
    { text: normFull.substring(idx, idx + normExcerpt.length), isHighlight: true },
    { text: normFull.substring(idx + normExcerpt.length), isHighlight: false },
  ];
}
