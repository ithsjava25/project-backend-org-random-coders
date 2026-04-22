import React from 'react';
import { STATUS_MAP } from '../utils/statusHelper';

const PetDetail = ({ pet, petRecords = [], onBack, onRegisterCase, onCaseClick }) => {

    if (!pet) {
        return <div className="p-10 text-center italic text-slate-400">Ingen djurdata hittades.</div>;
    }

    return (
        <div className="animate-in slide-in-from-right-4 duration-500 pb-20">
            {/* Navigering tillbaka */}
            <button
                onClick={onBack}
                className="flex items-center gap-2 text-slate-400 hover:text-slate-900 font-bold text-[10px] uppercase tracking-[0.2em] mb-8 transition group"
            >
                <span className="group-hover:-translate-x-1 transition-transform">← Tillbaka till mina djur</span>
            </button>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* VÄNSTERKOLUMN: DJURPROFIL */}
                <div className="lg:col-span-1">
                    <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-sm">
                        <div className="bg-slate-900 h-32 relative">
                            <div className="absolute -bottom-10 left-8 w-20 h-20 bg-white rounded-2xl shadow-lg border-4 border-white flex items-center justify-center text-3xl font-bold text-slate-900 uppercase italic">
                                {pet?.name?.charAt(0) || '?'}
                            </div>
                        </div>

                        <div className="pt-14 p-8 pb-8">
                            <h1 className="text-3xl font-black text-slate-900 italic tracking-tight uppercase">
                                {pet?.name}
                            </h1>
                            <p className="text-slate-400 text-xs font-bold uppercase tracking-widest mt-1">
                                {pet?.species} • {pet?.breed || 'Okänd ras'}
                            </p>

                            <div className="mt-8 space-y-4 border-t border-slate-100 pt-6">
                                <div className="flex justify-between items-center">
                                    <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Född</span>
                                    <span className="font-bold text-slate-700">{pet?.dateOfBirth || 'Ej angivet'}</span>
                                </div>
                                <div className="flex justify-between items-center">
                                    <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Vikt</span>
                                    <span className="font-bold text-slate-700">
                                        {pet?.weightKg ? `${pet.weightKg} kg` : 'Ej angivet'}
                                    </span>
                                </div>
                            </div>

                            <button
                                onClick={() => onRegisterCase(pet)}
                                className="w-full mt-8 bg-slate-900 text-white font-bold py-4 rounded-xl shadow-lg hover:bg-blue-900 transition transform active:scale-95 italic text-sm uppercase tracking-widest"
                            >
                                Sök vård för {pet?.name}
                            </button>
                        </div>
                    </div>
                </div>

                {/* HÖGERKOLUMN: JOURNALHISTORIK */}
                <div className="lg:col-span-2 space-y-6">
                    <h2 className="text-xl font-bold text-slate-900 flex items-center">
                        <span className="w-2 h-6 bg-slate-300 mr-3 rounded-full"></span>
                        Journalhistorik för {pet?.name}
                    </h2>

                    <div className="space-y-4">
                        {Array.isArray(petRecords) && petRecords.length > 0 ? (
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
                                        aria-label={`Visa detaljer för journal: ${record.title || 'Journalanteckning'}`}
                                        className="w-full text-left p-5 bg-white border border-slate-200 rounded-xl hover:shadow-md transition group focus:ring-2 focus:ring-blue-500 outline-none"
                                    >
                                        <div className="flex justify-between items-start">
                                            <div>
                                                <span className={`text-[10px] font-bold px-2 py-0.5 rounded uppercase border italic ${statusConfig.color}`}>
                                                    {statusConfig.label}
                                                </span>
                                                <h3 className="text-lg font-bold text-slate-900 mt-2 group-hover:text-blue-700">
                                                    {record.reasonForVisit || record.title || 'Journalanteckning'}
                                                </h3>
                                                <p className="text-sm text-slate-500 italic mt-1">
                                                    {record.diagnosis || 'Ingen diagnos ställd än.'}
                                                </p>
                                            </div>
                                            <div className="text-right text-xs text-slate-400 italic">
                                                {record.createdAt ? new Date(record.createdAt).toLocaleDateString('sv-SE') : ''}
                                            </div>
                                        </div>
                                    </button>
                                );
                            })
                        ) : (
                            <div className="bg-slate-50 border border-dashed border-slate-200 rounded-2xl p-12 text-center">
                                <h3 className="text-slate-900 font-bold italic uppercase tracking-tight text-sm">Ingen historik hittades</h3>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PetDetail;