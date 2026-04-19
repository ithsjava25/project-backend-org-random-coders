import React from 'react';
import { STATUS_MAP } from '../utils/statusHelper';

const OwnerDashboard = ({ userName, pets, records, onAddPet, onPetClick, onRegisterCase, onCaseClick, viewMode = 'dashboard' }) => {
    const isPetsView = viewMode === 'my-pets';
    const isCasesView = viewMode === 'my-cases';
    const isDashboard = viewMode === 'dashboard';

    return (
        <div className="animate-in fade-in duration-500">
            {/* HEADER */}
            <header className="mb-10 flex flex-col md:flex-row md:items-end justify-between gap-4">
                <div>
                    <h1 className="text-4xl font-extrabold text-slate-900 italic tracking-tight">
                        {isPetsView ? 'Mina djur' :
                            isCasesView ? 'Min Journal' :
                                `Välkommen, ${userName ? userName.split(' ')[0] : 'Vän'}!`}
                    </h1>
                    <p className="text-slate-500 mt-2 font-semibold uppercase tracking-wider text-sm italic">
                        {isPetsView ? 'Översikt över dina registrerade vänner' :
                            isCasesView ? 'Historik och aktiva vårdärenden' :
                                'Vet1177 Portalen'}
                    </p>
                </div>

                {(isDashboard || isCasesView) && (
                    <button
                        onClick={onRegisterCase}
                        className="px-6 py-3 bg-slate-900 text-white rounded-lg font-bold shadow-lg hover:bg-blue-900 transition-all uppercase text-xs tracking-widest italic"
                    >
                        Sök vård för ditt djur
                    </button>
                )}
            </header>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-10">

                {/* JOURNAL & ÄRENDEN */}
                {(isDashboard || isCasesView) && (
                    <div className={`${isCasesView ? 'lg:col-span-3' : 'lg:col-span-2'} space-y-8`}>
                        <section>
                            <h2 className="text-xl font-bold text-slate-900 flex items-center mb-6">
                                <span className="w-2 h-6 bg-slate-900 mr-3 rounded-full"></span>
                                Min Journal & Ärenden
                            </h2>
                            <div className={`${isCasesView ? 'grid grid-cols-1 md:grid-cols-2 gap-4' : 'space-y-4'}`}>
                                {records && records.length > 0 ? (
                                    records.map(record => {
                                        const statusConfig = STATUS_MAP[record.status] || { label: record.status, color: 'bg-slate-100 text-slate-600 border-slate-200' };
                                        return (
                                            <button
                                                type="button"
                                                key={record.id}
                                                onClick={() => onCaseClick(record)}
                                                className="w-full text-left p-5 bg-white border border-slate-200 rounded-xl hover:shadow-md transition group focus:ring-2 focus:ring-blue-500 outline-none"
                                            >
                                                <div className="flex justify-between items-start">
                                                    <div>
                                                        <span className={`text-[10px] font-bold px-2 py-0.5 rounded uppercase border italic ${statusConfig.color}`}>
                                                            {statusConfig.label}
                                                        </span>
                                                        <h3 className="text-lg font-bold text-slate-900 mt-2 group-hover:text-blue-700">
                                                            {record.reasonForVisit || record.title || 'Ingen rubrik'}
                                                        </h3>
                                                        <p className="text-sm text-slate-500 italic mt-1">
                                                            Patient: <span className="font-semibold">{record.petName || 'Okänt djur'}</span>
                                                        </p>
                                                    </div>
                                                    <div className="text-right text-xs text-slate-400 italic">
                                                        {record.createdAt ? new Date(record.createdAt).toLocaleDateString('sv-SE') : 'Datum saknas'}
                                                    </div>
                                                </div>
                                            </button>
                                        );
                                    })
                                ) : (
                                    <div className="text-center p-10 bg-white rounded-xl border border-dashed border-slate-200 col-span-full italic text-slate-400">
                                        Inga aktiva ärenden eller journalanteckningar hittades.
                                    </div>
                                )}
                            </div>
                        </section>
                    </div>
                )}

                {/* MINA DJUR */}
                {(isDashboard || isPetsView) && (
                    <div className={`${isPetsView ? 'lg:col-span-3' : 'space-y-8'}`}>
                        <section>
                            <h2 className="text-xl font-bold text-slate-900 flex items-center mb-6">
                                <span className="w-2 h-6 bg-slate-300 mr-3 rounded-full"></span>
                                Mina djur
                            </h2>
                            <div className={`${isPetsView ? 'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6' : 'space-y-3'}`}>
                                {pets && pets.length > 0 && pets.map(pet => (
                                    <button
                                        type="button"
                                        key={pet.id}
                                        onClick={() => onPetClick(pet)}
                                        className="w-full flex items-center p-4 bg-white border border-slate-200 rounded-xl hover:bg-slate-50 transition shadow-sm group focus:ring-2 focus:ring-blue-500 outline-none"
                                    >
                                        <div className="w-12 h-12 bg-slate-200 rounded-lg flex items-center justify-center text-slate-500 font-bold mr-4 group-hover:bg-slate-900 group-hover:text-white transition uppercase italic">
                                            {pet.name ? pet.name.charAt(0) : '?'}
                                        </div>
                                        <div className="flex-1 text-left">
                                            <span className="block font-bold text-slate-900 italic">
                                                {pet.name || 'Namnlös'}
                                            </span>
                                            <span className="block text-[10px] text-slate-400 uppercase font-bold tracking-tighter">
                                                {pet.species || 'Okänd art'} • {pet.breed || 'Okänd ras'}
                                            </span>
                                        </div>
                                    </button>
                                ))}

                                <button
                                    onClick={onAddPet}
                                    className={`flex flex-col items-center justify-center p-6 border-2 border-dashed border-slate-200 rounded-xl hover:border-blue-400 hover:bg-blue-50 transition group ${isPetsView ? 'h-full' : 'w-full mt-4'}`}
                                >
                                    <span className="text-2xl mb-1 text-slate-300 group-hover:text-blue-500">+</span>
                                    <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest group-hover:text-blue-600">Registrera nytt djur</span>
                                </button>
                            </div>
                        </section>
                    </div>
                )}
            </div>
        </div>
    );
};

export default OwnerDashboard;