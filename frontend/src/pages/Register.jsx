import React, { useState } from 'react';
import api from '../services/api';

const Register = ({ onSwitchToLogin }) => {
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
        role: 'OWNER'
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const response = await api.post('/auth/register', formData);

            // --- CodeRabbit fix: Robust token-extraktion ---
            let token = null;

            if (response.data && typeof response.data.token === 'string') {
                token = response.data.token;
            } else if (typeof response.data === 'string') {
                token = response.data;
            }

            if (token) {
                // Om vi fick en giltig sträng-token, logga in direkt
                localStorage.setItem('token', token);
                window.location.reload();
            } else {
                // Om registreringen lyckades men ingen token returnerades
                // (t.ex. vid krav på e-postbekräftelse), skicka till login.
                alert("Kontot skapades framgångsrikt! Vänligen logga in.");
                onSwitchToLogin();
            }
            // -----------------------------------------------

        } catch (err) {
            console.error("Register error:", err);
            const errorMessage = err.response?.data?.message || 'Registreringen misslyckades. Kontrollera dina uppgifter.';
            setError(errorMessage);
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
                <div className="p-10 md:p-12">
                    <h2 className="text-2xl font-bold text-slate-900 mb-8 tracking-tight">Skapa konto</h2>

                    <form onSubmit={handleSubmit} className="space-y-6">
                        {error && (
                            <div className="bg-red-50 text-red-600 p-4 rounded-xl text-xs font-bold uppercase tracking-wide border border-red-100">
                                {error}
                            </div>
                        )}

                        {/* NAMN */}
                        <div>
                            <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-2 ml-1">
                                Fullständigt namn
                            </label>
                            <input
                                type="text"
                                required
                                value={formData.name}
                                onChange={(e) => setFormData({...formData, name: e.target.value})}
                                className="w-full px-5 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-[#003f5a] focus:bg-white outline-none transition-all font-medium text-sm"
                                placeholder="För- och efternamn"
                            />
                        </div>

                        {/* E-POST */}
                        <div>
                            <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-2 ml-1">
                                E-postadress
                            </label>
                            <input
                                type="email"
                                required
                                value={formData.email}
                                onChange={(e) => setFormData({...formData, email: e.target.value})}
                                className="w-full px-5 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-[#003f5a] focus:bg-white outline-none transition-all font-medium text-sm"
                                placeholder="namn@exempel.se"
                            />
                        </div>

                        {/* LÖSENORD */}
                        <div>
                            <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-2 ml-1">
                                Välj lösenord
                            </label>
                            <input
                                type="password"
                                required
                                minLength="6"
                                value={formData.password}
                                onChange={(e) => setFormData({...formData, password: e.target.value})}
                                className="w-full px-5 py-4 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-[#003f5a] focus:bg-white outline-none transition-all font-medium text-sm"
                                placeholder="••••••••"
                            />
                        </div>

                        <div className="pt-4">
                            <button
                                type="submit"
                                disabled={loading}
                                className={`w-full flex items-center justify-center bg-[#003f5a] text-white py-4 rounded-2xl font-bold text-sm shadow-lg shadow-[#003f5a]/20 hover:bg-slate-800 transition-all transform hover:-translate-y-0.5 active:scale-[0.98] ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
                            >
                                {loading ? 'Skapar konto...' : 'Skapa konto'}
                            </button>
                        </div>
                    </form>
                </div>

                {/* Footer sektion */}
                <div className="bg-slate-50 px-10 py-5 border-t border-slate-100 flex items-center justify-center gap-2">
                    <svg className="w-4 h-4 text-emerald-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/>
                    </svg>
                    <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest leading-none pt-0.5">
                        Din data skyddas enligt GDPR
                    </span>
                </div>
            </div>

            <p className="mt-8 text-center text-xs font-bold text-slate-400 uppercase tracking-tighter">
                Har du redan ett konto? <button onClick={onSwitchToLogin} className="text-[#003f5a] hover:underline ml-1 font-bold bg-transparent border-none p-0 cursor-pointer">Logga in här</button>
            </p>
        </div>
    );
};

export default Register;