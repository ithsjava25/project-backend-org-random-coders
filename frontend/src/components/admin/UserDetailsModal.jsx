import React, { useState, useEffect } from 'react';
import { X, Mail, Shield, Hospital, Calendar, Activity, User as UserIcon, Edit2, Award } from 'lucide-react';
import { vetService } from '../../services/api';

const UserDetailsModal = ({ isOpen, onClose, user, clinics, onEdit }) => {
    const [vetDetails, setVetDetails] = useState(null);
    const [loadingVet, setLoadingVet] = useState(false);

    // --- KEYBOARD & SCROLL LOCK & RACE CONDITION PROTECTION ---
    useEffect(() => {
        let ignore = false;

        const handleEscape = (e) => {
            if (e.key === 'Escape') onClose();
        };

        if (isOpen) {
            window.addEventListener('keydown', handleEscape);
            document.body.style.overflow = 'hidden';

            // Om användaren är en veterinär, hämta extrauppgifter
            if (user?.role?.includes('VET')) {
                setLoadingVet(true);
                vetService.getAll()
                    .then(res => {
                        if (!ignore) {
                            const details = res.data.find(v => v.userId === user.id);
                            setVetDetails(details);
                        }
                    })
                    .catch(err => {
                        if (!ignore) console.error("Kunde inte hämta veterinärinfo:", err);
                    })
                    .finally(() => {
                        if (!ignore) setLoadingVet(false);
                    });
            }
        } else {
            setVetDetails(null);
        }

        return () => {
            ignore = true;
            window.removeEventListener('keydown', handleEscape);
            document.body.style.overflow = 'unset';
        };
    }, [isOpen, user, onClose]);

    if (!isOpen || !user) return null;

    const userClinic = clinics.find(c => c.id === user.clinicId);
    const roleName = user.role?.replace('ROLE_', '');
    const isVet = user.role?.includes('VET');

    return (
        <div
            className="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200"
            onClick={onClose}
        >
            <div
                className="bg-white w-full max-w-lg rounded-[2.5rem] shadow-2xl overflow-hidden border border-slate-200"
                role="dialog"
                aria-modal="true"
                aria-labelledby="user-modal-title"
                onClick={(e) => e.stopPropagation()}
            >

                {/* HEADER */}
                <div className="bg-slate-50 px-8 pt-10 pb-8 relative">
                    <button
                        onClick={onClose}
                        className="absolute top-6 right-6 p-2 hover:bg-white rounded-full transition-all text-slate-400 shadow-sm"
                        aria-label="Stäng modal"
                    >
                        <X size={20} />
                    </button>

                    <div className="flex items-center gap-5">
                        <div className="w-20 h-20 bg-white rounded-3xl border border-slate-200 shadow-sm flex items-center justify-center text-blue-500">
                            <UserIcon size={40} />
                        </div>
                        <div className="text-left">
                            <h2
                                id="user-modal-title"
                                className="text-3xl font-black text-slate-900 italic tracking-tight uppercase leading-none"
                            >
                                {user.name}
                            </h2>
                            <div className="flex items-center gap-2 mt-2">
                                <span className={`text-[10px] font-black px-3 py-1 rounded-full border uppercase ${
                                    user.role?.includes('ADMIN') ? 'bg-red-50 text-red-600 border-red-100' : 'bg-blue-50 text-blue-600 border-blue-100'
                                }`}>
                                    {roleName}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                {/* INNEHÅLL */}
                <div className="p-8 space-y-8">
                    {/* BASINFO KONTAKT */}
                    <div className="grid grid-cols-2 gap-6">
                        <div className="space-y-1 text-left">
                            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">E-postadress</p>
                            <div className="flex items-center gap-2 text-slate-700 font-bold text-sm">
                                <Mail size={16} className="text-slate-400" />
                                {user.email}
                            </div>
                        </div>

                        {user.clinicId && (
                            <div className="space-y-1 text-left">
                                <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Ansluten klinik</p>
                                <div className="flex items-center gap-2 text-slate-700 font-bold text-sm">
                                    <Hospital size={16} className="text-slate-400" />
                                    {userClinic?.name || 'Laddar...'}
                                </div>
                            </div>
                        )}
                    </div>

                    {/* VETERINÄR-SPECIFIKA FÄLT */}
                    {isVet && (
                        <div className="grid grid-cols-2 gap-6 pt-6 border-t border-slate-100 animate-in slide-in-from-top-2">
                            <div className="space-y-1 text-left">
                                <p className="text-[10px] font-black text-blue-500 uppercase tracking-widest italic">Vet-ID / Legitimation</p>
                                <div className="flex items-center gap-2 text-slate-700 font-bold text-sm">
                                    <Award size={16} className="text-blue-400" />
                                    {loadingVet ? (
                                        <span className="animate-pulse">Hämtar...</span>
                                    ) : (
                                        vetDetails?.licenseId || 'Ej angivet'
                                    )}
                                </div>
                            </div>
                            <div className="space-y-1 text-left">
                                <p className="text-[10px] font-black text-blue-500 uppercase tracking-widest italic">Specialisering</p>
                                <div className="flex items-center gap-2 text-slate-700 font-bold text-sm">
                                    <Shield size={16} className="text-blue-400" />
                                    {loadingVet ? (
                                        <span className="animate-pulse">...</span>
                                    ) : (
                                        vetDetails?.specialization || 'Allmänpraktiserande'
                                    )}
                                </div>
                            </div>
                        </div>
                    )}

                    {/* SYSTEMDETALJER */}
                    <div className="pt-6 border-t border-slate-100">
                        <div className="flex items-center justify-between">
                            <div className="flex items-center gap-3">
                                <div className="p-2 bg-slate-50 rounded-xl text-slate-400"><Calendar size={18} /></div>
                                <div className="text-left">
                                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Medlem sedan</p>
                                    <p className="font-bold text-slate-700 text-sm">
                                        {user.createdAt ? new Date(user.createdAt).toLocaleDateString('sv-SE') : '---'}
                                    </p>
                                </div>
                            </div>
                            <div className="flex items-center gap-3 text-right">
                                <div className="p-2 bg-slate-50 rounded-xl text-slate-400"><Activity size={18} /></div>
                                <div className="text-left">
                                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Användar-ID</p>
                                    <p className="font-mono text-[10px] text-slate-400">
                                        {user.id?.substring(0, 13)}...
                                    </p>
                                </div>
                            </div>
                        </div>

                        <button
                            onClick={() => onEdit(user)}
                            className="w-full mt-10 flex items-center justify-center gap-2 py-4 bg-slate-900 text-white text-[10px] font-black uppercase tracking-[0.2em] rounded-2xl hover:bg-blue-600 transition-all shadow-xl active:scale-95 italic"
                        >
                            <Edit2 size={14} />
                            Redigera användare
                        </button>
                    </div>
                </div>

                <div className="h-2 bg-slate-50 w-full" />
            </div>
        </div>
    );
};

export default UserDetailsModal;