import React from 'react';
import { Globe, Sun, Moon, UserCheck, ChevronDown } from 'lucide-react';
import type { UserPrincipal } from '../services/api';

interface HeaderProps {
  language: 'en' | 'hi';
  onLanguageChange: (lang: 'en' | 'hi') => void;
  isDark: boolean;
  onToggleDark: () => void;
  selectedCity: string;
  onSelectCity: (city: string) => void;
  isBackendOnline: boolean;
  onCheckHealth: () => void;
  asOfDate: string;
  onOpenAmendments: () => void;
  currentUser: UserPrincipal | null;
  onOpenAuth: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  language,
  onLanguageChange,
  isDark,
  onToggleDark,
  selectedCity,
  onSelectCity,
  isBackendOnline,
  onCheckHealth,
  currentUser,
  onOpenAuth,
}) => {
  return (
    <header className="sticky top-0 z-40 bg-white border-b border-slate-200/90 shadow-2xs px-4 lg:px-6 py-2">
      <div className="flex items-center justify-between gap-4 max-w-full">
        {/* Left branding matching reference */}
        <div className="flex items-center gap-3.5">
          {/* Ashoka Lion Emblem */}
          <div className="flex-shrink-0 w-10 h-11 flex items-center justify-center">
            <svg
              viewBox="0 0 100 115"
              className="w-9 h-11 text-amber-700"
              fill="currentColor"
              xmlns="http://www.w3.org/2000/svg"
              aria-label="National Emblem of India"
            >
              <circle cx="50" cy="18" r="7" fill="#b45309" />
              <path d="M42 26 C42 22, 58 22, 58 26 L56 46 C56 48, 44 48, 44 46 Z" fill="#b45309" />
              <path d="M28 28 C28 22, 38 24, 40 32 L39 48 C33 48, 27 42, 28 28 Z" fill="#b45309" opacity="0.9" />
              <path d="M72 28 C72 22, 62 24, 60 32 L61 48 C67 48, 73 42, 72 28 Z" fill="#b45309" opacity="0.9" />
              <rect x="24" y="50" width="52" height="12" rx="2" fill="#92400e" />
              <circle cx="50" cy="56" r="4.5" fill="#fef3c7" stroke="#92400e" strokeWidth="1" />
              <circle cx="34" cy="56" r="2" fill="#fef3c7" />
              <circle cx="66" cy="56" r="2" fill="#fef3c7" />
              <path d="M20 64 L80 64 L74 72 L26 72 Z" fill="#78350f" />
              <rect x="22" y="74" width="56" height="4" rx="1" fill="#92400e" />
              <text x="50" y="87" fontSize="8.5" fontFamily="serif" textAnchor="middle" fill="#78350f" fontWeight="bold">
                सत्यमेव जयते
              </text>
            </svg>
          </div>

          <div className="flex items-center gap-4">
            <div className="flex flex-col">
              <span className="text-[19px] font-bold tracking-tight text-slate-900 font-sans leading-none">
                Bhumi Vikas Sahayak
              </span>
              <span className="text-xs font-semibold text-slate-700 mt-0.5">
                MP Building Rules AI Assistant
              </span>
            </div>

            <div className="hidden lg:flex flex-col text-[11px] leading-tight text-slate-500 pl-4 border-l border-slate-300">
              <span className="font-medium text-slate-600">Madhya Pradesh Urban Development & Housing Department</span>
              <span className="text-slate-400">Your AI Assistant for Building Regulations</span>
            </div>
          </div>
        </div>

        {/* Right actions matching reference */}
        <div className="flex items-center gap-3">
          {/* Subtle live backend indicator */}
          <button
            type="button"
            onClick={onCheckHealth}
            className="hidden xl:flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10px] font-medium bg-slate-50 border border-slate-200 text-slate-600 hover:bg-slate-100 cursor-pointer"
            title="Spring Boot Backend Connectivity Check"
          >
            <span className={`w-1.5 h-1.5 rounded-full ${isBackendOnline ? 'bg-emerald-500 animate-pulse' : 'bg-amber-500'}`} />
            <span>{isBackendOnline ? 'Spring Boot 8080' : 'Local Engine'}</span>
          </button>

          {/* Language toggle: [Globe] [English] [हिंदी] */}
          <div className="flex items-center bg-slate-100 p-0.5 rounded-lg border border-slate-200">
            <div className="px-1.5 text-slate-500 flex items-center">
              <Globe className="w-3.5 h-3.5" />
            </div>
            <button
              type="button"
              onClick={() => onLanguageChange('en')}
              className={`px-3 py-1 text-xs font-semibold rounded-md transition-all cursor-pointer ${
                language === 'en'
                  ? 'bg-[#124d3a] text-white shadow-2xs'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              English
            </button>
            <button
              type="button"
              onClick={() => onLanguageChange('hi')}
              className={`px-3 py-1 text-xs font-semibold rounded-md transition-all font-hindi cursor-pointer ${
                language === 'hi'
                  ? 'bg-[#124d3a] text-white shadow-2xs'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              हिंदी
            </button>
          </div>

          {/* Theme toggle: [☀️] */}
          <button
            type="button"
            onClick={onToggleDark}
            className="p-1.5 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-lg border border-slate-200 transition-colors cursor-pointer"
            title="Toggle theme"
            aria-label="Toggle theme"
          >
            {isDark ? <Moon className="w-4 h-4" /> : <Sun className="w-4 h-4 text-amber-600" />}
          </button>

          {/* User profile: [👤] Municipal Clerk / Indore Municipal Corporation */}
          <div className="relative group">
            <div
              onClick={onOpenAuth}
              className="flex items-center gap-2 pl-2 py-1 pr-1 bg-slate-50 border border-slate-200 rounded-lg hover:bg-slate-100 transition-colors cursor-pointer"
            >
              <div className="w-8 h-8 rounded-full bg-slate-900 flex items-center justify-center text-white">
                <UserCheck className="w-4 h-4" />
              </div>
              <div className="text-left leading-tight hidden sm:block">
                <div className="text-xs font-bold text-slate-900">
                  {currentUser ? currentUser.username : 'Municipal Clerk'}
                </div>
                <div className="text-[11px] text-slate-500 flex items-center gap-1">
                  <span>{selectedCity}</span>
                  <ChevronDown className="w-3 h-3 text-slate-400" />
                </div>
              </div>
            </div>

            {/* Dropdown for city selection */}
            <div className="absolute right-0 top-full mt-1.5 w-60 bg-white border border-slate-200 rounded-lg shadow-lg py-1 hidden group-hover:block z-50">
              <div className="px-3 py-1.5 text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
                Select Municipal Corporation
              </div>
              {['Indore Municipal Corporation', 'Bhopal Municipal Corporation', 'Jabalpur Municipal Corporation', 'Gwalior Municipal Corporation'].map((corp) => (
                <button
                  key={corp}
                  type="button"
                  onClick={() => onSelectCity(corp)}
                  className={`w-full text-left px-3 py-1.5 text-xs flex items-center justify-between cursor-pointer ${
                    selectedCity === corp
                      ? 'bg-emerald-50 text-emerald-800 font-semibold'
                      : 'text-slate-700 hover:bg-slate-50'
                  }`}
                >
                  <span>{corp}</span>
                  {selectedCity === corp && <span className="w-1.5 h-1.5 rounded-full bg-emerald-600"></span>}
                </button>
              ))}
              <div className="border-t border-slate-100 my-1"></div>
              <button
                type="button"
                onClick={onOpenAuth}
                className="w-full text-left px-3 py-1.5 text-xs font-semibold text-emerald-800 hover:bg-emerald-50 cursor-pointer"
              >
                {currentUser ? 'Manage Session / Logout' : 'Officer Sign In'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
};
