import React, { useState } from 'react';
import { X, Calculator, ArrowRight } from 'lucide-react';


interface CalculatorsModalProps {
  isOpen: boolean;
  onClose: () => void;
  onApplyResultToChat?: (resultSummary: string) => void;
}

export const CalculatorsModal: React.FC<CalculatorsModalProps> = ({
  isOpen,
  onClose,
  onApplyResultToChat,
}) => {
  const [activeCalc, setActiveCalc] = useState<'setback' | 'far' | 'fee' | 'height' | 'electric'>('setback');

  // Setback inputs
  const [plotArea, setPlotArea] = useState<number>(500);
  const [roadWidth, setRoadWidth] = useState<number>(12);
  const [zone, setZone] = useState<string>('R-1 Residential');

  // FAR inputs
  const [farPlotArea, setFarPlotArea] = useState<number>(300);
  const [farRoadWidth, setFarRoadWidth] = useState<number>(12);

  // Fee inputs
  const [builtUpArea, setBuiltUpArea] = useState<number>(250);
  const [feeCategory, setFeeCategory] = useState<'residential' | 'commercial'>('residential');

  // Height inputs
  const [heightRoadWidth, setHeightRoadWidth] = useState<number>(18);

  // Electric inputs
  const [voltageType, setVoltageType] = useState<'low' | 'high' | 'extra'>('low');

  if (!isOpen) return null;

  // Setback calculation logic
  const getSetbackResult = () => {
    let front = 6;
    let rear = 4;
    let side = 3;
    let ruleCitation = 'Rule 32(1) - Page 25';

    if (plotArea <= 150) {
      front = 3.0;
      rear = 2.0;
      side = 1.5;
      ruleCitation = 'Rule 32(1), Row 1 - Page 25';
    } else if (plotArea <= 300) {
      front = 4.5;
      rear = 3.0;
      side = 2.0;
      ruleCitation = 'Rule 32(1), Row 2 - Page 25';
    } else if (plotArea <= 1000) {
      front = 6.0;
      rear = 4.0;
      side = 3.0;
      ruleCitation = 'Rule 32(1), Row 3 - Page 25';
    } else {
      front = 9.0;
      rear = 6.0;
      side = 4.5;
      ruleCitation = 'Rule 32(1), Row 4 - Page 26';
    }

    const groundCoveragePercent = plotArea <= 300 ? 60 : plotArea <= 1000 ? 50 : 40;
    const maxGroundCoverage = Math.round((plotArea * groundCoveragePercent) / 100);

    return { front, rear, side, ruleCitation, groundCoveragePercent, maxGroundCoverage };
  };

  // FAR calculation logic
  const getFarResult = () => {
    let baseFar = 1.25;
    if (farRoadWidth >= 18) baseFar = 2.0;
    else if (farRoadWidth >= 12) baseFar = 1.5;
    else if (farRoadWidth >= 9) baseFar = 1.25;
    else baseFar = 1.0;

    const maxBua = Math.round(farPlotArea * baseFar);
    return { baseFar, maxBua };
  };

  // Fee calculation logic
  const getFeeResult = () => {
    let ratePerSqM = 15;
    if (feeCategory === 'residential') {
      if (builtUpArea <= 100) ratePerSqM = 15;
      else if (builtUpArea <= 300) ratePerSqM = 25;
      else ratePerSqM = 40;
    } else {
      ratePerSqM = 75; // commercial multiplier
    }

    const scrutinyFee = builtUpArea * ratePerSqM;
    const developmentCess = Math.round(scrutinyFee * 0.1);
    const totalFee = scrutinyFee + developmentCess;
    return { ratePerSqM, scrutinyFee, developmentCess, totalFee };
  };

  // Height calculation logic
  const getHeightResult = () => {
    // Rule 42: Max height = 1.5 * (road width + front setback) or capped by road width
    let maxHeight = Math.min(30, Math.round(1.5 * heightRoadWidth));
    const isHighRise = maxHeight > 15;
    const liftMandatory = maxHeight > 15;
    const fireNocRequired = maxHeight > 15;

    return { maxHeight, isHighRise, liftMandatory, fireNocRequired };
  };

  const setbackRes = getSetbackResult();
  const farRes = getFarResult();
  const feeRes = getFeeResult();
  const heightRes = getHeightResult();

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4 animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl max-w-3xl w-full max-h-[90vh] flex flex-col shadow-2xl border border-slate-200 overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200 bg-[#0c382e] text-white">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-emerald-700/80 rounded-lg text-emerald-200">
              <Calculator className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold">
                Deterministic Building Rules Calculators
              </h3>
              <p className="text-xs text-emerald-300">
                Grounded in MP Bhumi Vikas Rules 2012 statutory tables & formulas
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

        {/* Navigation tabs */}
        <div className="flex border-b border-slate-200 bg-slate-50 px-6 pt-2 overflow-x-auto gap-2">
          {[
            { id: 'setback', label: 'Rule 32: Setbacks' },
            { id: 'far', label: 'Rule 42: FAR & BUA' },
            { id: 'fee', label: 'Rule 21: Permission Fee' },
            { id: 'height', label: 'Rule 42: Height Limit' },
            { id: 'electric', label: 'Rule 56: Electric Clearance' },
          ].map((tab) => (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveCalc(tab.id as any)}
              className={`pb-2.5 px-3 text-xs font-semibold border-b-2 transition-all whitespace-nowrap cursor-pointer ${
                activeCalc === tab.id
                  ? 'border-emerald-700 text-emerald-800'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Calculator Body */}
        <div className="p-6 overflow-y-auto flex-1">
          {activeCalc === 'setback' && (
            <div className="space-y-6">
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                    Plot Area (sq m / वर्ग मीटर)
                  </label>
                  <input
                    type="number"
                    value={plotArea}
                    onChange={(e) => setPlotArea(Math.max(1, parseInt(e.target.value) || 0))}
                    className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-600 focus:outline-hidden"
                  />
                  <span className="text-[11px] text-slate-500 mt-1 block">e.g. 150, 300, 500, 1000</span>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                    Road Width (meters / मीटर)
                  </label>
                  <input
                    type="number"
                    value={roadWidth}
                    onChange={(e) => setRoadWidth(Math.max(3, parseInt(e.target.value) || 0))}
                    className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-600 focus:outline-hidden"
                  />
                  <span className="text-[11px] text-slate-500 mt-1 block">e.g. 9m, 12m, 18m</span>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                    Zoning Category
                  </label>
                  <select
                    value={zone}
                    onChange={(e) => setZone(e.target.value)}
                    className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-600 focus:outline-hidden bg-white"
                  >
                    <option value="R-1 Residential">R-1 Residential (आवासीय)</option>
                    <option value="R-2 Mixed Residential">R-2 Mixed Residential</option>
                    <option value="Commercial">Commercial (व्यावसायिक)</option>
                  </select>
                </div>
              </div>

              {/* Result card */}
              <div className="bg-emerald-50 border border-emerald-200 rounded-xl p-5">
                <div className="flex items-center justify-between mb-3">
                  <span className="text-xs font-bold text-emerald-900 uppercase tracking-wider">
                    Statutory Setback Requirement
                  </span>
                  <span className="text-xs bg-white text-emerald-800 font-semibold px-2.5 py-0.5 rounded-full border border-emerald-300">
                    {setbackRes.ruleCitation}
                  </span>
                </div>

                <div className="grid grid-cols-3 gap-3 text-center my-3">
                  <div className="bg-white p-3 rounded-lg border border-emerald-200 shadow-2xs">
                    <span className="text-xs text-slate-500 block">Front Setback (अग्रभाग)</span>
                    <span className="text-lg font-bold text-slate-900">{setbackRes.front} meters</span>
                  </div>
                  <div className="bg-white p-3 rounded-lg border border-emerald-200 shadow-2xs">
                    <span className="text-xs text-slate-500 block">Rear Setback (पश्च भाग)</span>
                    <span className="text-lg font-bold text-slate-900">{setbackRes.rear} meters</span>
                  </div>
                  <div className="bg-white p-3 rounded-lg border border-emerald-200 shadow-2xs">
                    <span className="text-xs text-slate-500 block">Side (each / पार्श्व)</span>
                    <span className="text-lg font-bold text-slate-900">{setbackRes.side} meters</span>
                  </div>
                </div>

                <div className="mt-3 pt-3 border-t border-emerald-200/60 flex items-center justify-between text-xs text-emerald-800">
                  <span>Max Ground Coverage: <strong>{setbackRes.groundCoveragePercent}%</strong> ({setbackRes.maxGroundCoverage} sq m)</span>
                  <button
                    type="button"
                    onClick={() => {
                      onApplyResultToChat?.(
                        `What are the setback rules for a residential plot of ${plotArea} sq m in R-1 zone?`
                      );
                      onClose();
                    }}
                    className="text-xs font-bold text-emerald-800 hover:text-emerald-950 flex items-center gap-1 cursor-pointer"
                  >
                    <span>Ask in chat</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            </div>
          )}

          {activeCalc === 'far' && (
            <div className="space-y-6">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                    Plot Area (sq m)
                  </label>
                  <input
                    type="number"
                    value={farPlotArea}
                    onChange={(e) => setFarPlotArea(Math.max(1, parseInt(e.target.value) || 0))}
                    className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-600 focus:outline-hidden"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                    Road Width in Front (meters)
                  </label>
                  <input
                    type="number"
                    value={farRoadWidth}
                    onChange={(e) => setFarRoadWidth(Math.max(3, parseInt(e.target.value) || 0))}
                    className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-600 focus:outline-hidden"
                  />
                </div>
              </div>

              <div className="bg-blue-50 border border-blue-200 rounded-xl p-5">
                <div className="text-xs font-bold text-blue-900 uppercase tracking-wider mb-2">
                  Permissible Floor Area Ratio (Rule 42)
                </div>
                <div className="grid grid-cols-2 gap-3 text-center my-3">
                  <div className="bg-white p-3 rounded-lg border border-blue-200">
                    <span className="text-xs text-slate-500 block">Permissible FAR</span>
                    <span className="text-xl font-bold text-blue-900">{farRes.baseFar} : 1</span>
                  </div>
                  <div className="bg-white p-3 rounded-lg border border-blue-200">
                    <span className="text-xs text-slate-500 block">Max Total Built-up Area</span>
                    <span className="text-xl font-bold text-slate-900">{farRes.maxBua} sq m</span>
                  </div>
                </div>
                <p className="text-xs text-blue-800 mt-2">
                  * Basements and stilt parking are exempt from FAR computation under Rule 42(4).
                </p>
              </div>
            </div>
          )}

          {activeCalc === 'fee' && (
            <div className="space-y-6">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                    Proposed Total Built-up Area (sq m)
                  </label>
                  <input
                    type="number"
                    value={builtUpArea}
                    onChange={(e) => setBuiltUpArea(Math.max(1, parseInt(e.target.value) || 0))}
                    className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-600 focus:outline-hidden"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                    Building Use Category
                  </label>
                  <select
                    value={feeCategory}
                    onChange={(e) => setFeeCategory(e.target.value as any)}
                    className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-600 focus:outline-hidden bg-white"
                  >
                    <option value="residential">Residential (आवासीय)</option>
                    <option value="commercial">Commercial / Office (व्यावसायिक)</option>
                  </select>
                </div>
              </div>

              <div className="bg-amber-50 border border-amber-200 rounded-xl p-5">
                <div className="text-xs font-bold text-amber-900 uppercase tracking-wider mb-2">
                  Building Permission Scrutiny Fee (Rule 21, Schedule II)
                </div>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between text-slate-700">
                    <span>Applicable Scrutiny Rate:</span>
                    <span className="font-semibold">₹{feeRes.ratePerSqM} / sq m</span>
                  </div>
                  <div className="flex justify-between text-slate-700">
                    <span>Scrutiny Fee ({builtUpArea} m² × ₹{feeRes.ratePerSqM}):</span>
                    <span className="font-semibold">₹{feeRes.scrutinyFee.toLocaleString('en-IN')}</span>
                  </div>
                  <div className="flex justify-between text-slate-700">
                    <span>Urban Development Cess (10%):</span>
                    <span className="font-semibold">₹{feeRes.developmentCess.toLocaleString('en-IN')}</span>
                  </div>
                  <div className="border-t border-amber-200 pt-2 flex justify-between font-bold text-slate-900 text-base">
                    <span>Total Statutory Deposit:</span>
                    <span className="text-emerald-800">₹{feeRes.totalFee.toLocaleString('en-IN')}</span>
                  </div>
                </div>
              </div>
            </div>
          )}

          {activeCalc === 'height' && (
            <div className="space-y-6">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                  Abutting Road Width (meters)
                </label>
                <input
                  type="number"
                  value={heightRoadWidth}
                  onChange={(e) => setHeightRoadWidth(Math.max(3, parseInt(e.target.value) || 0))}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-600 focus:outline-hidden"
                />
              </div>

              <div className="bg-slate-50 border border-slate-200 rounded-xl p-5">
                <div className="text-xs font-bold text-slate-900 uppercase tracking-wider mb-3">
                  Permissible Height & Clearances (Rule 42)
                </div>
                <div className="grid grid-cols-2 gap-3 text-center mb-4">
                  <div className="bg-white p-3 rounded-lg border border-slate-200">
                    <span className="text-xs text-slate-500 block">Max Permissible Height</span>
                    <span className="text-xl font-bold text-slate-900">{heightRes.maxHeight} meters</span>
                  </div>
                  <div className="bg-white p-3 rounded-lg border border-slate-200">
                    <span className="text-xs text-slate-500 block">High-Rise Classification</span>
                    <span className={`text-base font-bold ${heightRes.isHighRise ? 'text-amber-700' : 'text-emerald-700'}`}>
                      {heightRes.isHighRise ? 'High-Rise (> 15m)' : 'Low-Rise (<= 15m)'}
                    </span>
                  </div>
                </div>

                <div className="space-y-1.5 text-xs text-slate-600">
                  <div className="flex items-center gap-2">
                    <span className={`w-2 h-2 rounded-full ${heightRes.liftMandatory ? 'bg-red-500' : 'bg-slate-300'}`}></span>
                    <span>Passenger Lift (Rule 52): <strong>{heightRes.liftMandatory ? 'MANDATORY' : 'Optional'}</strong></span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className={`w-2 h-2 rounded-full ${heightRes.fireNocRequired ? 'bg-red-500' : 'bg-slate-300'}`}></span>
                    <span>Fire Safety NOC (Rule 76): <strong>{heightRes.fireNocRequired ? 'MANDATORY' : 'Not Required'}</strong></span>
                  </div>
                </div>
              </div>
            </div>
          )}

          {activeCalc === 'electric' && (
            <div className="space-y-6">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1.5">
                  Overhead Transmission Voltage Class
                </label>
                <select
                  value={voltageType}
                  onChange={(e) => setVoltageType(e.target.value as any)}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-600 focus:outline-hidden bg-white"
                >
                  <option value="low">Low & Medium Voltage (&lt;= 650 V)</option>
                  <option value="high">High Voltage (11 kV to 33 kV)</option>
                  <option value="extra">Extra High Voltage (&gt; 33 kV)</option>
                </select>
              </div>

              <div className="bg-purple-50 border border-purple-200 rounded-xl p-5">
                <div className="text-xs font-bold text-purple-900 uppercase tracking-wider mb-2">
                  Clearance from Electric Lines (Rule 56, Table 22)
                </div>
                <div className="grid grid-cols-2 gap-3 text-center my-3">
                  <div className="bg-white p-3 rounded-lg border border-purple-200">
                    <span className="text-xs text-slate-500 block">Vertical Clearance (लंबवत)</span>
                    <span className="text-xl font-bold text-purple-900">
                      {voltageType === 'low' ? '2.5 meters' : voltageType === 'high' ? '3.7 meters' : '3.7m + 0.3m/33kV'}
                    </span>
                  </div>
                  <div className="bg-white p-3 rounded-lg border border-purple-200">
                    <span className="text-xs text-slate-500 block">Horizontal Clearance (क्षैतिज)</span>
                    <span className="text-xl font-bold text-purple-900">
                      {voltageType === 'low' ? '1.2 meters' : voltageType === 'high' ? '2.0 meters' : '2.0m + 0.3m/33kV'}
                    </span>
                  </div>
                </div>
                <p className="text-xs text-purple-800">
                  * Distance must be measured from any balcony edge, window, or projecting chhajja.
                </p>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-3 bg-slate-100 border-t border-slate-200 flex justify-end">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-1.5 bg-[#124d3a] hover:bg-[#165d46] text-white text-xs font-semibold rounded-lg transition-colors cursor-pointer"
          >
            Done
          </button>
        </div>
      </div>
    </div>
  );
};
