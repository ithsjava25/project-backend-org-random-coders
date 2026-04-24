import React, { useState, useEffect } from 'react';
import { STATUS_MAP } from '../utils/statusHelper';
import { medicalRecordService, petService } from '../services/api';
import { Trash2, Info, AlertCircle } from 'lucide-react';

const PetDetail = ({ pet, onBack, onRegisterCase, onCaseClick, onDelete }) => {
    const [petRecords, setPetRecords] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isDeleting, setIsDeleting] = useState(false);

    useEffect(() => {
        if (!pet?.id) {
            setLoading(false);
            return;
        }
        let cancelled = false;
        setLoading(true);
        medicalRecordService.getRecordsByPet(pet.id)
            .then(res => { if (!cancelled) setPetRecords(res.data); })
            .catch(() => { if (!cancelled) setPetRecords([]); })
            .finally(() => { if (!cancelled) setLoading(false); });
        return () => { cancelled = true; };
    }, [pet?.id]);

    // Strikt kontroll: Vi tillåter bara borttagning om noll journaler finns (matchar backend)
    const hasHistory = Array.isArray(petRecords) && petRecords.length > 0;

    const handleDeletePet = async () => {
        if (hasHistory) return;

        const confirmMessage = `Är du helt säker på att du vill ta bort ${pet.name}? Detta går inte att ångra.`;
        if (window.confirm(confirmMessage)) {
            setIsDeleting(true);
            try {
                await petService.deletePet(pet.id);
                if (onDelete) {
                    await onDelete();
                }
                onBack(); // Navigera tillbaka till listan
            } catch (err) {
                console.error("Fel vid borttagning:", err);
                alert(err.response?.data?.message || "Kunde inte ta bort djurprofilen.");
            } finally {
                setIsDeleting(false);
            }
        }
    };

    if (!pet) {
        return <div className="p-10 text-center italic text-slate-400">Ingen djurdata hittades.</div>;
    }

    return (
        <div className="animate-in slide-in-from-right-4 duration-500 pb-20">
            {/* Navigering tillbaka */}
            <button
                onClick={onBack}
                className="flex items-center gap-2 text-slate-400 hover:text-slate-900 font-black text-[10px] uppercase tracking-[0.2em] mb-8 transition group"
            >
                <span className="group-hover:-translate-x-1 transition-transform">← Tillbaka till mina djur</span>
            </button>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 text-left">
                {/* VÄNSTERKOLUMN: DJURPROFIL */}
                <div className="lg:col-span-1">
                    <div className="bg-white rounded-[2.5rem] border border-slate-200 overflow-hidden shadow-sm">
                        <div className="bg-slate-900 h-32 relative">
                            <div className="absolute -bottom-10 left-8 w-20 h-20 bg-white rounded-3xl shadow-lg border-4 border-white flex items-center justify-center text-3xl font-black text-slate-900 uppercase italic">
                                {pet?.name?.charAt(0) || '?'}
                            </div>
                        </div>

                        <div className="pt-14 p-8 pb-8">
                            <h1 className="text-3xl font-black text-slate-900 italic tracking-tight uppercase leading-none">
                                {pet?.name}
                            </h1>
                            <p className="text-slate-400 text-[10px] font-black uppercase tracking-[0.2em] mt-2">
                                {pet?.species} • {pet?.breed || 'Okänd ras'}
                            </p>

                            <div className="mt-8 space-y-4 border-t border-slate-100 pt-6">
                                <div className="flex justify-between items-center">
                                    <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Född</span>
                                    <span className="font-bold text-slate-700">{pet?.dateOfBirth || 'Ej angivet'}</span>
                                </div>
                                <div className="flex justify-between items-center">
                                    <span className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Vikt</span>
                                    <span className="font-bold text-slate-700">
                                        {pet?.weightKg ? `${pet.weightKg} kg` : 'Ej angivet'}
                                    </span>
                                </div>
                            </div>

                            <div className="mt-8 space-y-3">
                                <button
                                    onClick={() => onRegisterCase(pet)}
                                    className="w-full bg-slate-900 text-white font-black py-4 rounded-2xl shadow-lg hover:bg-blue-600 transition transform active:scale-95 italic text-[11px] uppercase tracking-[0.2em]"
                                >
                                    Sök vård för {pet?.name}
                                </button>

                                {/* TA BORT-KNAPP */}
                                <button
                                    onClick={handleDeletePet}
                                    disabled={hasHistory || isDeleting}
                                    className={`w-full flex items-center justify-center gap-2 py-3 rounded-xl font-black text-[10px] uppercase tracking-widest transition-all italic
                                        ${hasHistory
                                        ? 'bg-slate-50 text-slate-300 cursor-not-allowed'
                                        : 'text-red-500 hover:bg-red-50'}`}
                                >
                                    <Trash2 size={14} />
                                    {isDeleting ? 'Tar bort...' : 'Ta bort djurprofil'}
                                </button>

                                {hasHistory && (
                                    <div className="flex items-start gap-3 p-4 bg-slate-50 rounded-2xl border border-slate-100 mt-2">
                                        <Info size={16} className="text-slate-400 shrink-0 mt-0.5" />
                                        <p className="text-[9px] font-black text-slate-500 uppercase leading-relaxed italic tracking-wide">
                                            Profilen kan ej raderas då den är kopplad till medicinsk historik i systemet.
                                        </p>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                </div>

                {/* HÖGERKOLUMN: JOURNALHISTORIK */}
                <div className="lg:col-span-2 space-y-6">
                    <h2 className="text-xl font-black text-slate-900 flex items-center italic uppercase tracking-tight">
                        <span className="w-2 h-6 bg-slate-900 mr-3 rounded-full"></span>
                        Journalhistorik för {pet?.name}
                    </h2>

                    <div className="space-y-4">
                        {loading ? (
                            <div className="flex items-center gap-3 text-slate-400 py-8">
                                <div className="w-5 h-5 border-2 border-slate-200 border-t-blue-500 rounded-full animate-spin"></div>
                                <span className="text-[10px] font-black uppercase tracking-widest italic">Hämtar journaler...</span>
                            </div>
                        ) : Array.isArray(petRecords) && petRecords.length > 0 ? (
                            petRecords.map(record => {
                                const statusKey = record?.status;
                                const statusConfig = (STATUS_MAP && STATUS_MAP[statusKey]) || {
                                    label: statusKey || 'Okänd',
                                    color: 'bg-slate-100 text-slate-600 border-slate-200'
                                };

                                return (
                                    <button
                                        type="button"
                                        key={record.id}
                                        onClick={() => onCaseClick && onCaseClick(record)}
                                        className="w-full text-left p-6 bg-white border border-slate-200 rounded-[2rem] hover:shadow-xl hover:border-blue-100 transition-all group outline-none"
                                    >
                                        <div className="flex justify-between items-start">
                                            <div className="flex-1">
                                                <span className={`text-[9px] font-black px-3 py-1 rounded-full border uppercase italic ${statusConfig.color}`}>
                                                    {statusConfig.label}
                                                </span>
                                                <h3 className="text-xl font-black text-slate-900 mt-3 group-hover:text-blue-600 transition-colors italic tracking-tight">
                                                    {record.reasonForVisit || record.title || 'Journalanteckning'}
                                                </h3>
                                                <p className="text-sm text-slate-500 font-medium italic mt-1 line-clamp-1">
                                                    {record.diagnosis || 'Väntar på medicinsk bedömning...'}
                                                </p>
                                            </div>
                                            <div className="text-right flex flex-col items-end gap-2">
                                                <div className="text-[10px] font-black text-slate-300 uppercase italic tracking-widest">
                                                    {record.createdAt ? new Date(record.createdAt).toLocaleDateString('sv-SE') : ''}
                                                </div>
                                            </div>
                                        </div>
                                    </button>
                                );
                            })
                        ) : (
                            <div className="bg-white border-2 border-dashed border-slate-200 rounded-[2.5rem] p-16 text-center">
                                <div className="w-16 h-16 bg-slate-50 rounded-2xl flex items-center justify-center mx-auto mb-4 text-slate-200">
                                    <AlertCircle size={32} />
                                </div>
                                <h3 className="text-slate-400 font-black italic uppercase tracking-widest text-xs">Ingen historik hittades</h3>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PetDetail;