import React, { useState } from 'react';
import { authService } from '../services/api';

const Login = ({ onLoginSuccess, onSwitchToRegister }) => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [rememberMe, setRememberMe] = useState(false); // NY: State för checkbox
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleForgotPassword = () => {
        // Här kan du antingen navigera till en ny sida eller visa en alert
        alert("Funktionen för lösenordsåterställning kommer snart. Kontakta din administratör så länge.");
    };

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
            console.error("Login error:", {
                status: err.response?.status,
                message: err.message || "An error occurred during login"
            });
            const errorMsg = err.response?.data?.message || 'Fel e-post eller lösenord. Försök igen.';
            setError(errorMsg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-md w-full mx-auto mt-10">
            {/* Logo sektion */}
            <div className="text-center mb-8">
                <span className="text-4xl font-extrabold text-[#003f5a] tracking-tighter italic">
                    Vet<span className="text-[#0ea5e9]">1177</span>
                </span>
            </div>

            {/* Kort sektion */}
            <div className="bg-white rounded-[2rem] shadow-2xl shadow-slate-200/50 border border-slate-100 overflow-hidden">
                <div className="p-10 md:p-12 text-left">
                    <h2 className="text-2xl font-bold text-slate-900 mb-8 tracking-tight text-left">Logga in</h2>

                    <form onSubmit={handleSubmit} className="space-y-6">
                        {error && (
                            <div className="bg-red-50 text-red-600 p-4 rounded-xl text-xs font-bold uppercase tracking-wide border border-red-100 text-left">
                                {error}
                            </div>
                        )}

                        <div>
                            <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-2 ml-1 text-left">
                                E-post eller användarnamn
                            </label>
                            <input
                                type="email"
                                required
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                className="w-full px-5 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-[#003f5a] focus:bg-white outline-none transition-all font-medium text-sm"
                                placeholder="namn@exempel.se"
                            />
                        </div>

                        <div>
                            <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-2 ml-1 text-left">
                                Lösenord
                            </label>
                            <input
                                type="password"
                                required
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                className="w-full px-5 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-[#003f5a] focus:bg-white outline-none transition-all font-medium text-sm"
                                placeholder="••••••••"
                            />
                        </div>

                        <div className="flex items-center justify-between px-1 pt-2">
                            {/* FIX: Checkbox nu kopplad till state */}
                            <label className="flex items-center text-xs font-bold text-slate-400 cursor-pointer">
                                <input
                                    type="checkbox"
                                    checked={rememberMe}
                                    onChange={(e) => setRememberMe(e.target.checked)}
                                    className="mr-2 rounded border-slate-300 text-[#003f5a] focus:ring-[#003f5a]"
                                />
                                Kom ihåg mig
                            </label>
                            {/* FIX: Knapp nu kopplad till handler */}
                            <button
                                type="button"
                                onClick={handleForgotPassword}
                                className="text-xs font-bold text-[#0ea5e9] hover:underline bg-transparent border-none p-0 cursor-pointer"
                            >
                                Glömt lösenord?
                            </button>
                        </div>

                        <div className="pt-4">
                            <button
                                type="submit"
                                disabled={loading}
                                className={`w-full flex items-center justify-center bg-[#003f5a] text-white py-4 rounded-2xl font-bold text-sm shadow-lg shadow-[#003f5a]/20 hover:bg-slate-800 transition-all transform hover:-translate-y-0.5 active:scale-[0.98] ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
                            >
                                {loading ? 'Validerar...' : 'Logga in'}
                            </button>
                        </div>
                    </form>
                </div>

                {/* Footer sektion */}
                <div className="bg-slate-50 px-10 py-5 border-t border-slate-100 flex items-center justify-center gap-2">
                    <svg className="w-4 h-4 text-slate-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
                    </svg>
                    <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest leading-none pt-0.5">
                        Säker krypterad inloggning
                    </span>
                </div>
            </div>

            <p className="mt-8 text-center text-xs font-bold text-slate-400 uppercase tracking-tighter">
                Har du inget konto? <button onClick={onSwitchToRegister} className="text-[#003f5a] hover:underline ml-1 font-bold bg-transparent border-none p-0 cursor-pointer">Registrera dig här</button>
            </p>
        </div>
    );
};

export default Login;