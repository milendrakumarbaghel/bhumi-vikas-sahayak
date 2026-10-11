import React, { useState } from 'react';
import { X, Lock, User, CheckCircle2, AlertCircle, LogOut } from 'lucide-react';
import { api, type UserPrincipal } from '../services/api';


interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
  currentUser: UserPrincipal | null;
  onLoginSuccess: (user: UserPrincipal) => void;
  onLogoutSuccess: () => void;
  isBackendOnline: boolean;
}

export const AuthModal: React.FC<AuthModalProps> = ({
  isOpen,
  onClose,
  currentUser,
  onLoginSuccess,
  onLogoutSuccess,
  isBackendOnline,
}) => {
  const [username, setUsername] = useState('bhopal-clerk');
  const [password, setPassword] = useState('clerk123456');
  const [role, setRole] = useState<'USER' | 'ADMIN'>('USER');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);

    try {
      if (isBackendOnline) {
        const resp = await api.login(username, password);
        onLoginSuccess({
          id: 1,
          username: resp.username,
          role: resp.role,
          enabled: true,
        });
      } else {
        // Mock offline clerk login
        api.setAuth('mock-token-clerk-2026', username, role);
        onLoginSuccess({
          id: 1,
          username,
          role,
          enabled: true,
        });
      }
      onClose();
    } catch (err: any) {
      setError(err?.message || 'Login failed. Please check credentials.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleQuickLogin = (selectedRole: 'USER' | 'ADMIN') => {
    const u = selectedRole === 'ADMIN' ? 'admin' : 'bhopal-clerk';
    const p = selectedRole === 'ADMIN' ? 'admin12345678' : 'clerk123456';
    setUsername(u);
    setPassword(p);
    setRole(selectedRole);

    api.setAuth('mock-token-2026', u, selectedRole);
    onLoginSuccess({
      id: selectedRole === 'ADMIN' ? 2 : 1,
      username: u,
      role: selectedRole,
      enabled: true,
    });
    onClose();
  };

  const handleLogout = async () => {
    await api.logout();
    onLogoutSuccess();
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4 animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl max-w-md w-full shadow-2xl border border-slate-200 overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200 bg-[#0c382e] text-white">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-emerald-700/80 rounded-lg text-emerald-200">
              <Lock className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold">
                Clerk & Officer Authentication
              </h3>
              <p className="text-xs text-emerald-300">
                Spring Boot Bearer Token Security (/api/v1/auth/login)
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1.5 text-emerald-300 hover:text-white rounded-lg cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Body */}
        <div className="p-6">
          {currentUser ? (
            <div className="space-y-4">
              <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-xl flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-emerald-800 text-white flex items-center justify-center font-bold">
                  {currentUser.username[0]?.toUpperCase()}
                </div>
                <div>
                  <div className="font-bold text-slate-900 text-sm">
                    {currentUser.username}
                  </div>
                  <div className="flex items-center gap-2 mt-0.5">
                    <span className="text-[10px] font-bold uppercase bg-emerald-200/80 text-emerald-900 px-2 py-0.2 rounded-xs">
                      {currentUser.role}
                    </span>
                    <span className="text-xs text-emerald-700 flex items-center gap-1">
                      <CheckCircle2 className="w-3.5 h-3.5" />
                      Session Active
                    </span>
                  </div>
                </div>
              </div>

              <div className="text-xs text-slate-600 bg-slate-50 p-3 rounded-lg border border-slate-200">
                You have authenticated access to the cited assistant APIs, deterministic calculators, and statutory rule records.
              </div>

              <button
                type="button"
                onClick={handleLogout}
                className="w-full flex items-center justify-center gap-2 py-2.5 bg-red-50 hover:bg-red-100 text-red-700 font-semibold text-xs rounded-xl border border-red-200 transition-colors cursor-pointer"
              >
                <LogOut className="w-4 h-4" />
                <span>Sign Out Current Session</span>
              </button>
            </div>
          ) : (
            <form onSubmit={handleLogin} className="space-y-4">
              {error && (
                <div className="p-3 bg-red-50 border border-red-200 rounded-lg text-xs text-red-700 flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 flex-shrink-0" />
                  <span>{error}</span>
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Username
                </label>
                <div className="relative">
                  <User className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                    className="w-full pl-9 pr-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-600 focus:outline-hidden"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Password
                </label>
                <div className="relative">
                  <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    className="w-full pl-9 pr-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-emerald-600 focus:outline-hidden"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={isLoading}
                className="w-full py-2.5 bg-[#0c382e] hover:bg-[#134d3f] disabled:opacity-50 text-white font-semibold text-xs rounded-xl shadow-xs transition-colors cursor-pointer"
              >
                {isLoading ? 'Verifying Credentials...' : 'Sign In as Officer / Clerk'}
              </button>

              <div className="border-t border-slate-200 pt-3">
                <span className="text-[11px] font-semibold text-slate-500 uppercase tracking-wider block mb-2 text-center">
                  Quick One-Click Demo Access
                </span>
                <div className="grid grid-cols-2 gap-2">
                  <button
                    type="button"
                    onClick={() => handleQuickLogin('USER')}
                    className="py-1.5 px-2 bg-emerald-50 hover:bg-emerald-100 text-emerald-800 text-xs font-semibold rounded-lg border border-emerald-200 transition-colors cursor-pointer text-center"
                  >
                    Municipal Clerk (USER)
                  </button>
                  <button
                    type="button"
                    onClick={() => handleQuickLogin('ADMIN')}
                    className="py-1.5 px-2 bg-purple-50 hover:bg-purple-100 text-purple-800 text-xs font-semibold rounded-lg border border-purple-200 transition-colors cursor-pointer text-center"
                  >
                    Administrator (ADMIN)
                  </button>
                </div>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};
