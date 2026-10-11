import React from 'react';
import { FileCheck, ShieldCheck, Calculator, Users } from 'lucide-react';

export const HeroBanner: React.FC = () => {
  return (
    <div className="w-full bg-[#f8f5ec] border-b border-amber-200/60 overflow-hidden relative shadow-2xs">
      {/* Heritage Architectural Skyline Backdrop (Maheshwar / Indore / Gwalior sandstone heritage) */}
      <div className="relative w-full py-5 px-6 sm:px-10 bg-gradient-to-r from-[#faeed4] via-[#f7e6bd] to-[#eed3a1] overflow-hidden min-h-[110px] flex items-center justify-between">
        {/* Subtle traditional jaali pattern on the left */}
        <div className="absolute left-6 top-1/2 -translate-y-1/2 opacity-15 pointer-events-none hidden sm:block">
          <svg width="90" height="90" viewBox="0 0 100 100">
            <circle cx="50" cy="50" r="42" fill="none" stroke="#854d0e" strokeWidth="2" strokeDasharray="3 3" />
            <circle cx="50" cy="50" r="32" fill="none" stroke="#854d0e" strokeWidth="1.5" />
            <path d="M50 8 L50 92 M8 50 L92 50 M20 20 L80 80 M20 80 L80 20" stroke="#854d0e" strokeWidth="1" opacity="0.6" />
          </svg>
        </div>

        {/* Central Titles */}
        <div className="relative z-10 mx-auto text-center pl-4 sm:pl-0">
          <h1 className="text-3xl sm:text-4xl lg:text-[42px] font-black text-slate-900 tracking-tight font-hindi leading-tight">
            भूमि विकास सहायक
          </h1>
          <p className="mt-1 text-sm sm:text-base font-semibold text-slate-700 tracking-wide font-sans">
            Ask. Understand. Build Right.
          </p>
        </div>

        {/* Authentic Maheshwar Ghat / Palace Skyline on the Right */}
        <div className="absolute right-0 top-0 bottom-0 w-2/5 max-w-[420px] pointer-events-none opacity-45 hidden md:block">
          <svg viewBox="0 0 400 120" className="w-full h-full object-cover object-right" preserveAspectRatio="none">
            {/* Distant river / sky horizon */}
            <path d="M0 120 L400 120 L400 60 Q340 70 300 55 T180 65 T60 70 L0 75 Z" fill="#b45309" opacity="0.15" />
            {/* Temples and Shikhara spires */}
            <path d="M120 120 L120 70 Q130 35 140 70 L140 120 Z" fill="#92400e" opacity="0.3" />
            <path d="M135 35 Q140 20 145 35 Z" fill="#78350f" opacity="0.4" />
            <path d="M170 120 L170 55 Q185 15 200 55 L200 120 Z" fill="#78350f" opacity="0.35" />
            <path d="M182 15 Q185 0 188 15 Z" fill="#78350f" opacity="0.5" />
            {/* Grand Fort / Palace Pavilion with Chhatris & Arches */}
            <rect x="230" y="45" width="170" height="75" fill="#78350f" opacity="0.3" />
            <path d="M220 45 L400 45 L390 35 L230 35 Z" fill="#78350f" opacity="0.45" />
            {/* Chhatri Domes */}
            <path d="M245 35 Q255 12 265 35 Z" fill="#78350f" opacity="0.5" />
            <path d="M295 35 Q310 8 325 35 Z" fill="#78350f" opacity="0.55" />
            <path d="M308 8 Q310 0 312 8 Z" fill="#78350f" opacity="0.6" />
            <path d="M355 35 Q365 12 375 35 Z" fill="#78350f" opacity="0.5" />
            {/* Arch cutouts in fortress wall */}
            <rect x="250" y="60" width="14" height="22" rx="7" fill="#faeed4" opacity="0.7" />
            <rect x="275" y="60" width="14" height="22" rx="7" fill="#faeed4" opacity="0.7" />
            <rect x="300" y="60" width="14" height="22" rx="7" fill="#faeed4" opacity="0.7" />
            <rect x="325" y="60" width="14" height="22" rx="7" fill="#faeed4" opacity="0.7" />
            <rect x="350" y="60" width="14" height="22" rx="7" fill="#faeed4" opacity="0.7" />
            <rect x="375" y="60" width="14" height="22" rx="7" fill="#faeed4" opacity="0.7" />
            {/* River ghat steps */}
            <line x1="100" y1="95" x2="400" y2="95" stroke="#78350f" strokeWidth="2" opacity="0.4" />
            <line x1="80" y1="105" x2="400" y2="105" stroke="#78350f" strokeWidth="2" opacity="0.5" />
            <line x1="60" y1="115" x2="400" y2="115" stroke="#78350f" strokeWidth="2.5" opacity="0.6" />
          </svg>
        </div>
      </div>

      {/* Trust Badges Bar */}
      <div className="bg-[#fefdfa] border-t border-amber-200/50 px-4 sm:px-8 py-2">
        <div className="flex items-center justify-between max-w-full overflow-x-auto gap-4 sm:gap-6 text-xs text-slate-700 no-scrollbar">
          {/* Badge 1 */}
          <div className="flex items-center gap-2 flex-shrink-0">
            <span className="p-1 rounded-sm bg-amber-100/80 text-amber-800">
              <FileCheck className="w-3.5 h-3.5" />
            </span>
            <div className="leading-tight">
              <div className="font-semibold text-slate-800">Based on Official</div>
              <div className="text-[11px] text-slate-500">MP Building Rules</div>
            </div>
          </div>

          <div className="hidden sm:block w-px h-6 bg-slate-200"></div>

          {/* Badge 2 */}
          <div className="flex items-center gap-2 flex-shrink-0">
            <span className="p-1 rounded-sm bg-emerald-100/80 text-emerald-800">
              <ShieldCheck className="w-3.5 h-3.5" />
            </span>
            <div className="leading-tight">
              <div className="font-semibold text-slate-800">Accurate Rule</div>
              <div className="text-[11px] text-slate-500">References</div>
            </div>
          </div>

          <div className="hidden sm:block w-px h-6 bg-slate-200"></div>

          {/* Badge 3 (Aa Hindi & English Support) */}
          <div className="flex items-center gap-2 flex-shrink-0">
            <span className="w-6 h-6 flex items-center justify-center font-bold text-xs bg-blue-50 text-blue-800 rounded-sm font-serif">
              Aa
            </span>
            <div className="leading-tight">
              <div className="font-semibold text-slate-800">Hindi & English</div>
              <div className="text-[11px] text-slate-500">Support</div>
            </div>
          </div>

          <div className="hidden sm:block w-px h-6 bg-slate-200"></div>

          {/* Badge 4 */}
          <div className="flex items-center gap-2 flex-shrink-0">
            <span className="p-1 rounded-sm bg-purple-100/80 text-purple-800">
              <Calculator className="w-3.5 h-3.5" />
            </span>
            <div className="leading-tight">
              <div className="font-semibold text-slate-800">Calculations &</div>
              <div className="text-[11px] text-slate-500">Drawings</div>
            </div>
          </div>

          <div className="hidden md:block w-px h-6 bg-slate-200"></div>

          {/* Badge 5 */}
          <div className="flex items-center gap-2 flex-shrink-0">
            <span className="p-1 rounded-sm bg-slate-100 text-slate-800">
              <Users className="w-3.5 h-3.5" />
            </span>
            <div className="leading-tight">
              <div className="font-semibold text-slate-800">Trusted for</div>
              <div className="text-[11px] text-slate-500">Municipal Clerks</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
