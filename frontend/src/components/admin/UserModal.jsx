import React, { useState, useEffect } from 'react';
import { X, User, Mail, Lock, Shield, Hospital, Award, Loader2, PlusCircle } from 'lucide-react';
import { vetService } from '../../services/api';

const UserModal = ({ isOpen, onClose, onSave, initialData = null, clinics = [] }) => {
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
        role: 'OWNER',
        clinicId: '',
        licenseId: '',
        specialization: '',
        bookingInfo: ''
    });

    useEffect(() => {
        if (isOpen) {
            if (initialData) {
                setFormData({ ...initialData, password: '' });
            } else {
                setFormData({
                    name: '', email: '', password: '',
                    role: 'OWNER', clinicId: '',
                    licenseId: '', specialization: '', bookingInfo: ''
                });
            }
        }
    }, [isOpen, initialData]);

    if (!isOpen) return null;

    const isVet = formData.role === 'VET';

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            // 1. Spara användaren (returnerar response med ID)
            const response = await onSave(formData);

            // 2. Om veterinär, spara detaljer (använd ID från response)
            if (isVet && response?.data?.id) {
                await vetService.create({
                    userId: response.data.id,
                    licenseId: formData.licenseId,
                    specialization: formData.specialization,
                    bookingInfo: formData.bookingInfo
                });
            }
            onClose();
        } catch (err) {
            alert(err.response?.data?.message || "Ett fel uppstod vid sparandet.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="bg-white w-full max-w-md rounded-[2.5rem] shadow-2xl overflow-hidden border border-slate-200 max-h-[95vh] flex flex-col">

                {/* HEADER */}
                <div className="px-8 pt-8 pb-4 flex justify-between items-center">
                    <div>
                        <h2 className="text-2xl font-black text-slate-900 italic tracking-tight text-left">
                            {initialData ? 'Redigera Profil' : 'Ny Användare'}
                        </h2>
                        <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mt-1 text-left">
                            {initialData ? 'Uppdatera behörigheter' : 'Registrera ny systemanvändare'}
                        </p>
                    </div>
                    <button onClick={onClose} className="p-2 hover:bg-slate-100 rounded-full transition-colors text-slate-400">
                        <X size={20} />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="p-8 space-y-5 overflow-y-auto custom-scrollbar text-left">
                    {/* NAMN */}
                    <div className="space-y-1.5">
                        <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest ml-1 italic block text-left">Namn</label>
                        <div className="relative">
                            <User className="absolute left-4 top-3 text-slate-400" size={18} />
                            <input
                                required
                                className="w-full pl-11 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-slate-900 focus:border-transparent outline-none transition-all font-medium text-slate-700"
                                placeholder="Namn Namnsson"
                                value={formData.name}
                                onChange={(e) => setFormData({...formData, name: e.target.value})}
                            />
                        </div>
                    </div>

                    {/* E-POST */}
                    <div className="space-y-1.5">
                        <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest ml-1 italic block text-left">E-postadress</label>
                        <div className="relative">
                            <Mail className="absolute left-4 top-3 text-slate-400" size={18} />
                            <input
                                required
                                type="email"
                                className="w-full pl-11 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-slate-900 focus:border-transparent outline-none transition-all font-medium text-slate-700"
                                placeholder="namn@epost.se"
                                value={formData.email}
                                onChange={(e) => setFormData({...formData, email: e.target.value})}
                            />
                        </div>
                    </div>

                    {/* LÖSENORD (Endast vid skapande) */}
                    {!initialData && (
                        <div className="space-y-1.5">
                            <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest ml-1 italic block text-left">Lösenord</label>
                            <div className="relative">
                                <Lock className="absolute left-4 top-3 text-slate-400" size={18} />
                                <input
                                    required
                                    type="password"
                                    className="w-full pl-11 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-slate-900 focus:border-transparent outline-none transition-all font-medium text-slate-700"
                                    placeholder="••••••••"
                                    value={formData.password}
                                    onChange={(e) => setFormData({...formData, password: e.target.value})}
                                />
                            </div>
                        </div>
                    )}

                    {/* ROLL */}
                    <div className="space-y-1.5">
                        <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest ml-1 italic block text-left">Systemroll</label>
                        <div className="relative">
                            <Shield className="absolute left-4 top-3 text-slate-400" size={18} />
                            <select
                                className="w-full pl-11 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-slate-900 focus:border-transparent outline-none transition-all font-medium text-slate-700 appearance-none bg-white"
                                value={formData.role}
                                onChange={(e) => setFormData({...formData, role: e.target.value})}
                            >
                                <option value="OWNER">Djurägare</option>
                                <option value="VET">Veterinär</option>
                                <option value="ADMIN">Administratör</option>
                            </select>
                        </div>
                    </div>

                    {/* KLINIK (Om veterinär) */}
                    {isVet && (
                        <div className="space-y-1.5 animate-in slide-in-from-top-2">
                            <label className="text-[10px] font-black text-slate-500 uppercase tracking-widest ml-1 italic block text-left">Ansluten Klinik</label>
                            <div className="relative">
                                <Hospital className="absolute left-4 top-3 text-slate-400" size={18} />
                                <select
                                    required
                                    className="w-full pl-11 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-slate-900 focus:border-transparent outline-none transition-all font-medium text-slate-700 appearance-none bg-white"
                                    value={formData.clinicId}
                                    onChange={(e) => setFormData({...formData, clinicId: e.target.value})}
                                >
                                    <option value="">Välj klinik...</option>
                                    {clinics.map(c => (
                                        <option key={c.id} value={c.id}>{c.name}</option>
                                    ))}
                                </select>
                            </div>
                        </div>
                    )}

                    {/* VETERINÄR-DETALJER */}
                    {isVet && (
                        <div className="pt-4 space-y-4 border-t border-slate-100">
                            <div className="space-y-1.5">
                                <label className="text-[10px] font-black text-blue-500 uppercase tracking-widest ml-1 italic block text-left">Legitimations-ID</label>
                                <div className="relative">
                                    <Award className="absolute left-4 top-3 text-blue-400" size={18} />
                                    <input
                                        required
                                        className="w-full pl-11 pr-4 py-3 bg-blue-50/30 border border-blue-100 rounded-2xl focus:ring-2 focus:ring-blue-500 outline-none transition-all font-medium text-slate-700"
                                        placeholder="VET-123456"
                                        value={formData.licenseId}
                                        onChange={(e) => setFormData({...formData, licenseId: e.target.value})}
                                    />
                                </div>
                            </div>
                            <div className="space-y-1.5">
                                <label className="text-[10px] font-black text-blue-500 uppercase tracking-widest ml-1 italic block text-left">Specialisering</label>
                                <input
                                    className="w-full px-4 py-3 bg-blue-50/30 border border-blue-100 rounded-2xl focus:ring-2 focus:ring-blue-500 outline-none transition-all font-medium text-slate-700"
                                    placeholder="T.ex. Kirurgi"
                                    value={formData.specialization}
                                    onChange={(e) => setFormData({...formData, specialization: e.target.value})}
                                />
                            </div>
                        </div>
                    )}

                    {/* ACTION BUTTONS */}
                    <div className="pt-4 flex gap-3">
                        <button
                            type="button"
                            onClick={onClose}
                            className="flex-1 px-6 py-4 border border-slate-200 text-slate-400 text-[10px] font-black uppercase tracking-[0.2em] rounded-2xl hover:bg-slate-50 transition-all italic"
                        >
                            Avbryt
                        </button>
                        <button
                            disabled={loading}
                            type="submit"
                            className={`flex-2 px-10 py-4 text-white text-[10px] font-black uppercase tracking-[0.2em] rounded-2xl transition-all italic shadow-lg flex items-center justify-center gap-2 ${
                                initialData
                                    ? 'bg-blue-600 hover:bg-blue-700 shadow-blue-900/20'
                                    : 'bg-emerald-600 hover:bg-emerald-700 shadow-emerald-900/20'
                            }`}
                        >
                            {loading ? (
                                <Loader2 className="animate-spin" size={16} />
                            ) : (
                                initialData ? 'Spara Ändringar' : 'Skapa Användare'
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default UserModal;