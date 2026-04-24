import React, { useEffect } from 'react';
import { X, ShieldCheck, Info, User as UserIcon, MapPin, Activity, Clock, Hash } from 'lucide-react';

const LogDetailsModal = ({ isOpen, onClose, log }) => {
    // --- NYTT: Keyboard handler & Scroll-lock ---
    useEffect(() => {
        const handleEscape = (e) => {
            if (e.key === 'Escape') onClose();
        };

        if (isOpen) {
            window.addEventListener('keydown', handleEscape);
            document.body.style.overflow = 'hidden'; // Lås bakgrundsscroll
        }

        return () => {
            window.removeEventListener('keydown', handleEscape);
            document.body.style.overflow = 'unset'; // Återställ scroll
        };
    }, [isOpen, onClose]);

    if (!isOpen || !log) return null;

    const fullDate = new Date(log.createdAt).toLocaleString('sv-SE', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });

    return (
        <div
            className="fixed inset-0 z-[70] flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200"
            // --- NYTT: Stäng vid klick på bakgrunden ---
            onClick={onClose}
        >
            <div
                className="bg-white w-full max-w-lg rounded-[2.5rem] shadow-2xl overflow-hidden border border-slate-200"
                // --- NYTT: Dialog semantik ---
                role="dialog"
                aria-modal="true"
                aria-labelledby="log-details-title"
                // --- NYTT: Stoppa klick-bubbling ---
                onClick={(e) => e.stopPropagation()}
            >
                {/* HEADER */}
                <div className="bg-purple-50/50 px-8 pt-10 pb-8 relative text-left">
                    <button
                        onClick={onClose}
                        className="absolute top-6 right-6 p-2 hover:bg-white rounded-full transition-all text-slate-400 shadow-sm"
                        aria-label="Stäng"
                    >
                        <X size={20} />
                    </button>
                    <div className="flex items-center gap-5">
                        <div className="w-20 h-20 bg-white rounded-3xl border border-slate-200 shadow-sm flex items-center justify-center text-purple-500">
                            <ShieldCheck size={40} />
                        </div>
                        <div>
                            <p className="text-[10px] font-black text-purple-400 uppercase tracking-widest italic mb-1">Systemlogg / Detaljer</p>
                            <h2
                                // --- NYTT: ID för aria-labelledby ---
                                id="log-details-title"
                                className="text-2xl font-black text-slate-900 italic tracking-tight uppercase leading-none"
                            >
                                {log.action.replace('_', ' ')}
                            </h2>
                        </div>
                    </div>
                </div>

                {/* INNEHÅLL */}
                <div className="p-8 space-y-8">
                    <div className="bg-slate-50 p-6 rounded-[2rem] border border-slate-100 relative text-left">
                        <Info size={16} className="absolute top-4 right-4 text-slate-300" />
                        <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic mb-2">Beskrivning</p>
                        <p className="text-slate-700 font-bold leading-relaxed">{log.description}</p>
                    </div>

                    <div className="grid grid-cols-2 gap-6">
                        <div className="space-y-1 text-left">
                            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Utförd av</p>
                            <div className="flex items-center gap-2 text-slate-700 font-bold text-sm">
                                <UserIcon size={16} className="text-purple-400" />
                                {log.performedByName}
                            </div>
                        </div>
                        <div className="space-y-1 text-left">
                            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Klinik</p>
                            <div className="flex items-center gap-2 text-slate-700 font-bold text-sm">
                                <MapPin size={16} className="text-purple-400" />
                                {log.clinicName || 'System'}
                            </div>
                        </div>
                        <div className="space-y-1 text-left">
                            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Patient</p>
                            <div className="flex items-center gap-2 text-amber-700 font-bold text-sm">
                                <Activity size={16} className="text-amber-500" />
                                {log.petName || 'N/A'}
                            </div>
                        </div>
                        <div className="space-y-1 text-left">
                            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Tidpunkt</p>
                            <div className="flex items-center gap-2 text-slate-700 font-bold text-sm">
                                <Clock size={16} className="text-purple-400" />
                                {fullDate}
                            </div>
                        </div>
                    </div>

                    <div className="pt-6 border-t border-slate-100 flex justify-between items-center">
                        <div className="flex items-center gap-3 text-left">
                            <div className="p-2 bg-slate-50 rounded-xl text-slate-400"><Hash size={18} /></div>
                            <div>
                                <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Referens-ID</p>
                                <p className="font-mono text-xs font-bold text-slate-600">{log.recordId}</p>
                            </div>
                        </div>
                        <button
                            onClick={onClose}
                            className="px-8 py-3 bg-slate-900 text-white text-[10px] font-black uppercase tracking-widest rounded-xl hover:bg-purple-600 transition-all italic"
                        >
                            Stäng
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LogDetailsModal;