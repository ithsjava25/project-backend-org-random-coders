import React, { useState } from 'react';
import { authService } from '../services/api';
import { PawPrint } from 'lucide-react'; // Importera tassen för loggan

const Login = ({ onLoginSuccess, onSwitchToRegister }) => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [rememberMe, setRememberMe] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const response = await authService.login(email, password);
            let token = null;
            if (response.data && typeof response.data.token === 'string') {
                token = response.data.token;
            } else if (typeof response.data === 'string') {
                token = response.data;
            } else if (response.data && typeof response.data.accessToken === 'string') {
                token = response.data.accessToken;
            }

            if (!token) {
                throw new Error('Giltig inloggningsnyckel saknas i svaret från servern.');
            }

            if (rememberMe) {
                localStorage.setItem('token', token);
            } else {
                sessionStorage.setItem('token', token);
                localStorage.removeItem('token');
            }

            onLoginSuccess();
        } catch (err) {
            console.error("Login error:", err);
            const errorMsg = err.response?.data?.message || 'Fel e-post eller lösenord. Försök igen.';
            setError(errorMsg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-md w-full mx-auto mt-10">
            {/* --- UPPDATERAD LOGO-SEKTION (Matchar Layout.jsx) --- */}
            <div className="flex flex-col items-center mb-10">
                <div className="flex items-center gap-3 mb-2">
                    <div className="bg-[#0ea5e9] p-2.5 rounded-xl shadow-lg shadow-[#0ea5e9]/20 rotate-12">
                        <PawPrint size={24} className="text-white" />
                    </div>
                    <h1 className="text-3xl font-black text-[#003f5a] tracking-tighter italic leading-none">
                        VET<span className="text-[#0ea5e9]">1177</span>
                    </h1>
                </div>
                <p className="text-[10px] text-slate-400 uppercase font-black tracking-[0.3em] ml-1">
                    Digital djurvård
                </p>
            </div>

            {/* Kort sektion */}
            <div className="bg-white rounded-[2.5rem] shadow-2xl shadow-slate-200/50 border border-slate-100 overflow-hidden">
                <div className="p-10 md:p-12 text-left">
                    {/* <h2 className="text-2xl font-black text-slate-900 mb-8 tracking-tight italic uppercase">Logga in</h2>*/}

                    <form onSubmit={handleSubmit} className="space-y-6">
                        {error && (
                            <div className="bg-red-50 text-red-600 p-4 rounded-2xl text-[10px] font-black uppercase tracking-widest border border-red-100 italic">
                                {error}
                            </div>
                        )}

                        <div>
                            <label className="block text-[10px] font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">
                                E-post eller användarnamn
                            </label>
                            <input
                                type="email"
                                required
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                className="w-full px-6 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-[#003f5a] focus:bg-white outline-none transition-all font-bold text-sm"
                                placeholder="namn@exempel.se"
                            />
                        </div>

                        <div>
                            <label className="block text-[10px] font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">
                                Lösenord
                            </label>
                            <input
                                type="password"
                                required
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                className="w-full px-6 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-[#003f5a] focus:bg-white outline-none transition-all font-bold text-sm"
                                placeholder="••••••••"
                            />
                        </div>

                        <div className="flex items-center justify-between px-1 pt-2">
                            <label className="flex items-center text-[10px] font-black text-slate-400 cursor-pointer uppercase tracking-widest italic">
                                <input
                                    type="checkbox"
                                    checked={rememberMe}
                                    onChange={(e) => setRememberMe(e.target.checked)}
                                    className="mr-3 w-4 h-4 rounded border-slate-300 text-[#003f5a] focus:ring-[#003f5a]"
                                />
                                Kom ihåg mig
                            </label>
                        </div>

                        <div className="pt-4">
                            <button
                                type="submit"
                                disabled={loading}
                                className={`w-full flex items-center justify-center bg-[#003f5a] text-white py-4 rounded-2xl font-black text-[11px] uppercase tracking-[0.2em] shadow-xl shadow-[#003f5a]/20 hover:bg-slate-800 transition-all transform hover:-translate-y-0.5 active:scale-[0.98] italic ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
                            >
                                {loading ? 'Validerar...' : 'Logga in'}
                            </button>
                        </div>
                    </form>
                </div>

                <div className="bg-slate-50 px-10 py-6 border-t border-slate-100 flex items-center justify-center gap-3">
                    <svg className="w-4 h-4 text-slate-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
                    </svg>
                    <span className="text-[9px] font-black text-slate-400 uppercase tracking-[0.2em] pt-0.5">
                        Säker krypterad inloggning
                    </span>
                </div>
            </div>

            <p className="mt-10 text-center text-[10px] font-black text-slate-400 uppercase tracking-widest italic">
                Saknar du konto? <button onClick={onSwitchToRegister} className="text-[#003f5a] hover:text-[#0ea5e9] ml-2 font-black transition-colors uppercase">Skapa profil här</button>
            </p>
        </div>
    );
};

export default Login;