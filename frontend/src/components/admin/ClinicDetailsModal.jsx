import React, { useEffect } from 'react';
import {
    X,
    MapPin,
    Phone,
    Hospital,
    Calendar,
    Activity,
    Edit2,
    Award
} from 'lucide-react';

const ClinicDetailsModal = ({ isOpen, onClose, clinic, users = [], onEdit }) => {

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

    if (!isOpen || !clinic) return null;

    const clinicStaff = users.filter(u =>
        u.clinicId === clinic.id &&
        u.role?.includes('VET')
    );

    return (
        <div
            className="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200"
            // --- NYTT: Stäng vid klick på bakgrunden ---
            onClick={onClose}
        >
            <div
                className="bg-white w-full max-w-lg rounded-[2.5rem] shadow-2xl overflow-hidden border border-slate-200"
                // --- NYTT: Dialog semantik ---
                role="dialog"
                aria-modal="true"
                aria-labelledby="clinic-modal-title"
                // --- NYTT: Stoppa klick-bubbling (så inte modalen stängs när man klickar inuti den) ---
                onClick={(e) => e.stopPropagation()}
            >

                {/* HEADER */}
                <div className="bg-emerald-50/50 px-8 pt-10 pb-8 relative text-left">
                    <button
                        onClick={onClose}
                        className="absolute top-6 right-6 p-2 hover:bg-white rounded-full transition-all text-slate-400 shadow-sm"
                        aria-label="Stäng modal"
                    >
                        <X size={20} />
                    </button>

                    <div className="flex items-center gap-5">
                        <div className="w-20 h-20 bg-white rounded-3xl border border-slate-200 shadow-sm flex items-center justify-center text-emerald-500">
                            <Hospital size={40} />
                        </div>
                        <div>
                            <h2
                                // --- NYTT: ID för ARIA-koppling ---
                                id="clinic-modal-title"
                                className="text-3xl font-black text-slate-900 italic tracking-tight uppercase leading-none"
                            >
                                {clinic.name}
                            </h2>
                        </div>
                    </div>
                </div>

                {/* INNEHÅLL */}
                <div className="p-8 space-y-8 max-h-[70vh] overflow-y-auto">
                    {/* KONTAKT & ADRESS */}
                    <div className="grid grid-cols-2 gap-6">
                        <div className="space-y-1 text-left">
                            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Adress</p>
                            <div className="flex items-start gap-2 text-slate-700 font-bold text-sm">
                                <MapPin size={16} className="text-slate-400 mt-0.5 shrink-0" />
                                <span>{clinic.address || 'Ingen adress angiven'}</span>
                            </div>
                        </div>
                        <div className="space-y-1 text-left">
                            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Telefonnummer</p>
                            <div className="flex items-center gap-2 text-slate-700 font-bold text-sm">
                                <Phone size={16} className="text-slate-400" />
                                {clinic.phoneNumber || 'Saknas'}
                            </div>
                        </div>
                    </div>

                    {/* VETERINÄR-LISTA */}
                    <div className="pt-6 border-t border-slate-100">
                        <div className="flex items-center gap-2 mb-4">
                            <Award size={18} className="text-emerald-500" />
                            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic italic">Anslutna Veterinärer ({clinicStaff.length})</p>
                        </div>

                        {clinicStaff.length > 0 ? (
                            <div className="grid grid-cols-1 gap-2">
                                {clinicStaff.map(member => (
                                    <div key={member.id} className="flex items-center justify-between p-3 bg-slate-50 rounded-xl border border-slate-100 group">
                                        <div className="text-left">
                                            <p className="text-sm font-bold text-slate-700 leading-none">{member.name}</p>
                                            <p className="text-[9px] font-black text-emerald-500 uppercase mt-1 italic tracking-wider">
                                                Legitimerad Veterinär
                                            </p>
                                        </div>
                                        <div className="text-[10px] font-mono text-slate-400 bg-white px-2 py-1 rounded-md border border-slate-100 shadow-sm">
                                            {member.email}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <div className="p-4 bg-slate-50 rounded-2xl border border-dashed border-slate-200">
                                <p className="text-[10px] font-bold text-slate-400 uppercase italic">Inga veterinärer kopplade till denna klinik</p>
                            </div>
                        )}
                    </div>

                    {/* SYSTEMDETALJER */}
                    <div className="pt-6 border-t border-slate-100">
                        <div className="flex items-center justify-between mb-8">
                            <div className="flex items-center gap-3 text-left">
                                <div className="p-2 bg-slate-50 rounded-xl text-slate-400"><Calendar size={18} /></div>
                                <div>
                                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Registrerad</p>
                                    <p className="font-bold text-slate-700 text-sm">
                                        {clinic.createdAt ? new Date(clinic.createdAt).toLocaleDateString('sv-SE') : '---'}
                                    </p>
                                </div>
                            </div>
                            <div className="flex items-center gap-3 text-left">
                                <div className="p-2 bg-slate-50 rounded-xl text-slate-400"><Activity size={18} /></div>
                                <div>
                                    <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Klinik-ID</p>
                                    <p className="font-mono text-[10px] text-slate-400">
                                        {clinic.id?.substring(0, 13)}...
                                    </p>
                                </div>
                            </div>
                        </div>

                        <button
                            onClick={() => onEdit(clinic)}
                            className="w-full flex items-center justify-center gap-2 py-4 bg-slate-900 text-white text-[10px] font-black uppercase tracking-[0.2em] rounded-2xl hover:bg-emerald-600 transition-all shadow-xl active:scale-95 italic"
                        >
                            <Edit2 size={14} />
                            Redigera klinikuppgifter
                        </button>
                    </div>
                </div>

                <div className="h-2 bg-emerald-500/20 w-full" />
            </div>
        </div>
    );
};

export default ClinicDetailsModal;