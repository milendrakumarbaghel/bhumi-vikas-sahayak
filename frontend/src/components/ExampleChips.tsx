import React from 'react';

interface ExampleChipsProps {
  onSelectAndSubmit: (question: string) => void;
  disabled?: boolean;
}

export const EXACT_EXAMPLE_CHIPS = [
  'मेरा प्लॉट 200 वर्ग मीटर का है और सामने 9 मीटर चौड़ी सड़क है। आगे कितनी जगह खाली छोड़नी होगी?',
  'My house faces a 10 m wide street and will be 9 m tall. How much open space do I leave at the front?',
  'What is the fee for building permission for a residential house?',
  'I want a farm house on agricultural land. What is the minimum plot size and the maximum FAR?',
  'How do I pay my property tax online?',
];

export const ExampleChips: React.FC<ExampleChipsProps> = ({
  onSelectAndSubmit,
  disabled = false,
}) => {
  return (
    <div className="w-full space-y-2">
      <div className="text-xs font-semibold uppercase tracking-wider text-slate-500">
        Common Counter Queries / अक्सर पूछे जाने वाले प्रश्न:
      </div>
      <div className="flex flex-wrap gap-2">
        {EXACT_EXAMPLE_CHIPS.map((chipText, index) => {
          const isHindi = /[\u0900-\u097F]/.test(chipText);
          return (
            <button
              key={index}
              type="button"
              disabled={disabled}
              onClick={() => onSelectAndSubmit(chipText)}
              lang={isHindi ? 'hi' : 'en'}
              className={`text-left text-xs sm:text-sm px-3.5 py-2 rounded-lg bg-white hover:bg-slate-50 text-slate-800 border border-slate-300 hover:border-[#0f2942] disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-2xs min-h-[44px] cursor-pointer ${
                isHindi ? 'font-hindi leading-snug' : 'font-sans'
              }`}
            >
              {chipText}
            </button>
          );
        })}
      </div>
    </div>
  );
};
