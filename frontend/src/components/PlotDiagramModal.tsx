import React from 'react';
import type { DiagramData } from '../types';
import { X, Printer, Layers } from 'lucide-react';


interface PlotDiagramModalProps {
  isOpen: boolean;
  onClose: () => void;
  data: DiagramData;
}

export const PlotDiagramModal: React.FC<PlotDiagramModalProps> = ({
  isOpen,
  onClose,
  data,
}) => {
  if (!isOpen) return null;

  const {
    plotAreaSqM,
    plotWidthM = 20,
    plotDepthM = 25,
    frontSetbackM = 6,
    rearSetbackM = 4,
    sideSetbackM = 3,
    roadWidthM = 12,
    zone = 'R-1 Residential',
  } = data;

  const buildableWidth = Math.max(0, plotWidthM - 2 * sideSetbackM);
  const buildableDepth = Math.max(0, plotDepthM - frontSetbackM - rearSetbackM);
  const groundCoverage = buildableWidth * buildableDepth;
  const coveragePercent = Math.round((groundCoverage / plotAreaSqM) * 100);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4 animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl max-w-3xl w-full max-h-[90vh] flex flex-col shadow-2xl border border-slate-200 overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200 bg-slate-50">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-emerald-100 text-emerald-800 rounded-lg">
              <Layers className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900">
                Statutory Setback & Plot Margin Diagram (Rule 32)
              </h3>
              <p className="text-xs text-slate-500">
                Plot: {plotAreaSqM} sq m ({plotWidthM}m × {plotDepthM}m) • Zone: {zone}
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => window.print()}
              className="p-2 text-slate-500 hover:text-slate-800 hover:bg-slate-200 rounded-lg transition-colors cursor-pointer"
              title="Print Diagram"
            >
              <Printer className="w-4 h-4" />
            </button>
            <button
              type="button"
              onClick={onClose}
              className="p-2 text-slate-500 hover:text-slate-800 hover:bg-slate-200 rounded-lg transition-colors cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Diagram Canvas Body */}
        <div className="p-6 overflow-y-auto flex-1 flex flex-col items-center justify-center bg-[#fcfbfa]">
          {/* SVG Technical Blueprint */}
          <div className="w-full max-w-xl bg-white p-4 rounded-xl border border-slate-300 shadow-inner flex flex-col items-center">
            <svg
              viewBox="0 0 500 460"
              className="w-full h-auto select-none"
              xmlns="http://www.w3.org/2000/svg"
            >
              {/* Grid backdrop */}
              <defs>
                <pattern id="grid" width="20" height="20" patternUnits="userSpaceOnUse">
                  <path d="M 20 0 L 0 0 0 20" fill="none" stroke="#f1f5f9" strokeWidth="1" />
                </pattern>
              </defs>
              <rect width="500" height="460" fill="url(#grid)" />

              {/* Public Road at Front */}
              <rect x="50" y="20" width="400" height="60" fill="#e2e8f0" stroke="#cbd5e1" strokeWidth="2" />
              {/* Road center dashed line */}
              <line x1="50" y1="50" x2="450" y2="50" stroke="#94a3b8" strokeWidth="2" strokeDasharray="8 6" />
              <text x="250" y="46" textAnchor="middle" fill="#475569" fontSize="12" fontWeight="bold">
                PUBLIC ROAD ({roadWidthM} METERS WIDE)
              </text>
              <text x="250" y="66" textAnchor="middle" fill="#64748b" fontSize="10">
                Front Property Line / Right of Way
              </text>

              {/* Plot Boundary */}
              <rect
                x="80"
                y="100"
                width="340"
                height="300"
                fill="#f8fafc"
                stroke="#0f766e"
                strokeWidth="3"
                rx="2"
              />
              <text x="250" y="115" textAnchor="middle" fill="#0f766e" fontSize="11" fontWeight="bold">
                FRONT BOUNDARY ({plotWidthM}m)
              </text>

              {/* Front Setback Clearance Zone (Amber/Yellow soft wash) */}
              <rect
                x="80"
                y="100"
                width="340"
                height="70"
                fill="#fef3c7"
                opacity="0.8"
                stroke="#f59e0b"
                strokeDasharray="4 4"
              />
              <text x="250" y="142" textAnchor="middle" fill="#92400e" fontSize="11" fontWeight="bold">
                FRONT SETBACK: {frontSetbackM} METERS (MANDATORY OPEN)
              </text>
              <text x="250" y="157" textAnchor="middle" fill="#b45309" fontSize="10">
                Rule 32(1) • No permanent construction permitted
              </text>

              {/* Rear Setback Clearance Zone */}
              <rect
                x="80"
                y="350"
                width="340"
                height="50"
                fill="#fee2e2"
                opacity="0.8"
                stroke="#ef4444"
                strokeDasharray="4 4"
              />
              <text x="250" y="380" textAnchor="middle" fill="#991b1b" fontSize="11" fontWeight="bold">
                REAR SETBACK: {rearSetbackM} METERS
              </text>
              <text x="250" y="393" textAnchor="middle" fill="#b91c1c" fontSize="9">
                Rule 32(2) • Rear Open Space
              </text>

              {/* Left Side Setback */}
              <rect
                x="80"
                y="170"
                width="50"
                height="180"
                fill="#ecfdf5"
                opacity="0.9"
                stroke="#10b981"
                strokeDasharray="4 4"
              />
              <text x="105" y="260" textAnchor="middle" fill="#065f46" fontSize="10" fontWeight="bold" transform="rotate(-90 105 260)">
                SIDE: {sideSetbackM}m
              </text>

              {/* Right Side Setback */}
              <rect
                x="370"
                y="170"
                width="50"
                height="180"
                fill="#ecfdf5"
                opacity="0.9"
                stroke="#10b981"
                strokeDasharray="4 4"
              />
              <text x="395" y="260" textAnchor="middle" fill="#065f46" fontSize="10" fontWeight="bold" transform="rotate(90 395 260)">
                SIDE: {sideSetbackM}m
              </text>

              {/* Permissible Buildable Footprint */}
              <rect
                x="130"
                y="170"
                width="240"
                height="180"
                fill="#064e3b"
                opacity="0.9"
                rx="4"
              />
              <text x="250" y="240" textAnchor="middle" fill="#ffffff" fontSize="13" fontWeight="bold">
                PERMISSIBLE BUILDABLE ENVELOPE
              </text>
              <text x="250" y="260" textAnchor="middle" fill="#a7f3d0" fontSize="11">
                {buildableWidth}m width × {buildableDepth}m depth
              </text>
              <text x="250" y="280" textAnchor="middle" fill="#6ee7b7" fontSize="11" fontWeight="bold">
                Max Ground Coverage: {groundCoverage} sq m ({coveragePercent}%)
              </text>

              {/* Dimension markers */}
              <text x="440" y="250" fill="#475569" fontSize="11" fontWeight="bold">
                {plotDepthM}m
              </text>
              <line x1="430" y1="100" x2="430" y2="400" stroke="#64748b" strokeWidth="1.5" />
              <line x1="425" y1="100" x2="435" y2="100" stroke="#64748b" strokeWidth="1.5" />
              <line x1="425" y1="400" x2="435" y2="400" stroke="#64748b" strokeWidth="1.5" />
            </svg>
          </div>

          {/* Metric calculation breakdown cards */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 w-full mt-4">
            <div className="p-3 bg-white rounded-lg border border-slate-200 text-center">
              <span className="text-[11px] text-slate-500 font-medium block">Total Plot Area</span>
              <span className="text-base font-bold text-slate-900">{plotAreaSqM} m²</span>
            </div>
            <div className="p-3 bg-white rounded-lg border border-slate-200 text-center">
              <span className="text-[11px] text-slate-500 font-medium block">Front Setback</span>
              <span className="text-base font-bold text-amber-700">{frontSetbackM} meters</span>
            </div>
            <div className="p-3 bg-white rounded-lg border border-slate-200 text-center">
              <span className="text-[11px] text-slate-500 font-medium block">Rear Setback</span>
              <span className="text-base font-bold text-red-600">{rearSetbackM} meters</span>
            </div>
            <div className="p-3 bg-white rounded-lg border border-slate-200 text-center">
              <span className="text-[11px] text-slate-500 font-medium block">Side Setbacks</span>
              <span className="text-base font-bold text-emerald-700">{sideSetbackM}m (each)</span>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-3.5 bg-slate-100 border-t border-slate-200 flex items-center justify-between">
          <span className="text-xs text-slate-600 font-medium">
            * Generated in accordance with Table 14 of MP Bhumi Vikas Rules 2012
          </span>
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-1.5 bg-[#124d3a] hover:bg-[#165d46] text-white text-xs font-semibold rounded-lg transition-colors cursor-pointer"
          >
            Close Diagram
          </button>
        </div>
      </div>
    </div>
  );
};
