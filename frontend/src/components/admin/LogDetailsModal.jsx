import React from 'react';
import {
    X,
    Clock,
    User as UserIcon,
    MapPin,
    Activity,
    Hash,
    Info,
    ShieldCheck
} from 'lucide-react';

const LogDetailsModal = ({ isOpen, onClose, log }) => {
    if (!isOpen || !log) return null;

    const date = new Date(log.createdAt).toLocaleString('sv-SE', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });

    return (
        <div className="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="bg-white w-full max-w-lg rounded-[2.5rem] shadow-2xl overflow-hidden border border-slate-200">

                {/* HEADER - Lila Audit-tema */}
                <div className="bg-purple-50/50 px-8 pt-10 pb-8 relative text-left">
                    <button
                        onClick={onClose}
                        className="absolute top-6 right-6 p-2 hover:bg-white rounded-full transition-all text-slate-400 shadow-sm"
                    >
                        <X size={20} />
                    </button>

                    <div className="flex items-center gap-5">
                        <div className="w-20 h-20 bg-white rounded-3xl border border-slate-200 shadow-sm flex items-center justify-center text-purple-500">
                            <ShieldCheck size={40} />
                        </div>
                        <div>
                            <p className="text-[10px] font-black text-purple-400 uppercase tracking-widest italic mb-1">Systemlogg / Händelse</p>
                            <h2 className="text-2xl font-black text-slate-900 italic tracking-tight uppercase leading-none">
                                {log.action.replace('_', ' ')}
                            </h2>
                        </div>
                    </div>
                </div>

                {/* INNEHÅLL */}
                <div className="p-8 space-y-8">

                    {/* HUVUDBESKRIVNING */}
                    <div className="bg-slate-50 p-6 rounded-[2rem] border border-slate-100 relative">
                        <Info size={16} className="absolute top-4 right-4 text-slate-300" />
                        <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic mb-2 text-left">Händelseförlopp</p>
                        <p className="text-slate-700 font-bold leading-relaxed text-left">
                            {log.description}
                        </p>
                    </div>

                    {/* DETALJERAD INFO GRID */}
                    <div className="grid grid-cols-2 gap-6">
                        <div className="space-y-1 text-left">
                            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Utförd av</p>
                            <div className="flex items-center gap-2 text-slate-700 font-bold text-sm">
                                <UserIcon size={16} className="text-purple-400" />
                                {log.performedByName}
                                <span className="text-[9px] bg-purple-50 text-purple-600 px-1.5 rounded border border-purple-100">
                                    {log.performedByRole?.replace('ROLE_', '')}
                                </span>
                            </div>
                        </div>
                        <div className="space-y-1 text-left">
                            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Klinik</p>
                            <div className="flex items-center gap-2 text-slate-700 font-bold text-sm">
                                <MapPin size={16} className="text-purple-400" />
                                {log.clinicName || 'Systemnivå'}
                            </div>
                        </div>
                        <div className="space-y-1 text-left">
                            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Patient (Referens)</p>
                            <div className="flex items-center gap-2 text-amber-700 font-bold text-sm">
                                <Activity size={16} className="text-amber-500" />
                                {log.petName || 'N/A'}
                            </div>
                        </div>
                        <div className="space-y-1 text-left">
                            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Tidstämpel</p>
                            <div className="flex items-center gap-2 text-slate-700 font-bold text-sm">
                                <Clock size={16} className="text-purple-400" />
                                {date}
                            </div>
                        </div>
                    </div>

                    {/* TEKNISK INFO */}
                    <div className="pt-6 border-t border-slate-100">
                        <div className="flex items-center justify-between">
                            <div className="flex items-center gap-3 text-left">
                                <div className="p-2 bg-slate-50 rounded-xl text-slate-400"><Hash size={18} /></div>
                                <div>
                                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Ärende ID (Record)</p>
                                    <p className="font-mono text-xs font-bold text-slate-600">
                                        {log.recordId}
                                    </p>
                                </div>
                            </div>
                            <div className="flex items-center gap-3 text-right">
                                <div>
                                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Logg-ID</p>
                                    <p className="font-mono text-[10px] text-slate-300">
                                        {log.id}
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <button
                        onClick={onClose}
                        className="w-full flex items-center justify-center gap-2 py-4 bg-slate-900 text-white text-[10px] font-black uppercase tracking-[0.2em] rounded-2xl hover:bg-purple-600 transition-all shadow-xl active:scale-95 italic"
                    >
                        Stäng detaljvy
                    </button>
                </div>

                <div className="h-2 bg-purple-500/20 w-full" />
            </div>
        </div>
    );
};

export default LogDetailsModal;