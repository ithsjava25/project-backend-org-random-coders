import React from 'react';

const OwnerDashboard = ({ pets, records, onAddPet, onPetClick, onRegisterCase, onCaseClick }) => {
    return (
        <div className="animate-in fade-in duration-500">
            <header className="mb-10 flex flex-col md:flex-row md:items-end justify-between gap-4">
                <div>
                    <h1 className="text-4xl font-extrabold text-slate-900 italic tracking-tight">Välkommen</h1>
                    <p className="text-slate-500 mt-2 font-semibold uppercase tracking-wider text-sm italic">Vet1177 Portalen</p>
                </div>
                <button
                    onClick={onRegisterCase}
                    className="px-6 py-3 bg-vet-navy text-white rounded-lg font-bold shadow-lg hover:bg-vet-accent transition-all"
                >
                    Sök vård för ditt djur
                </button>
            </header>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-10">
                {/* VÄNSTER: JOURNAL */}
                <div className="lg:col-span-2 space-y-8">
                    <section>
                        <h2 className="text-xl font-bold text-slate-900 flex items-center mb-6">
                            <span className="w-2 h-6 bg-vet-navy mr-3 rounded-full"></span>
                            Min Journal & Ärenden
                        </h2>
                        <div className="space-y-4">
                            {records && records.length > 0 ? (
                                records.map(record => (
                                    <div
                                        key={record.id}
                                        onClick={() => onCaseClick(record)}
                                        className="p-5 bg-white border border-slate-200 rounded-xl hover:shadow-md transition cursor-pointer group"
                                    >
                                        <div className="flex justify-between items-start">
                                            <div>
                                                <span className={`text-[10px] font-bold px-2 py-1 rounded uppercase ${
                                                    record.status === 'OPEN' ? 'bg-green-100 text-green-700' : 'bg-slate-100 text-slate-600'
                                                }`}>
                                                    {record.status}
                                                </span>
                                                <h3 className="text-lg font-bold text-slate-900 mt-2 group-hover:text-vet-navy">{record.title}</h3>
                                                <p className="text-sm text-slate-500">Patient: <span className="font-semibold">{record.petName}</span></p>
                                            </div>
                                            <div className="text-right text-xs text-slate-400 italic">
                                                {new Date(record.createdAt).toLocaleDateString('sv-SE')}
                                            </div>
                                        </div>
                                    </div>
                                ))
                            ) : (
                                <p className="text-slate-400 italic bg-white p-6 rounded-xl border border-dashed border-slate-200">
                                    Inga aktiva ärenden eller journalanteckningar hittades.
                                </p>
                            )}
                        </div>
                    </section>
                </div>

                {/* HÖGER: MINA DJUR */}
                <div className="space-y-8">
                    <section>
                        <h2 className="text-xl font-bold text-slate-900 flex items-center mb-6">
                            <span className="w-2 h-6 bg-slate-300 mr-3 rounded-full"></span>
                            Mina djur
                        </h2>
                        <div className="space-y-3">
                            {pets && pets.map(pet => (
                                <div key={pet.id} onClick={() => onPetClick(pet)} className="flex items-center p-4 bg-white border border-slate-200 rounded-xl hover:bg-slate-50 transition shadow-sm group cursor-pointer">
                                    <div className="w-12 h-12 bg-slate-200 rounded-lg flex items-center justify-center text-slate-500 font-bold mr-4 group-hover:bg-vet-navy group-hover:text-white transition uppercase">
                                        {pet.name.charAt(0)}
                                    </div>
                                    <div className="flex-1">
                                        <span className="block font-bold text-slate-900">{pet.name}</span>
                                        <span className="block text-[10px] text-slate-400 uppercase font-bold">{pet.species} • {pet.breed}</span>
                                    </div>
                                </div>
                            ))}
                            <button onClick={onAddPet} className="w-full flex items-center justify-center p-4 border-2 border-dashed border-slate-200 rounded-xl hover:border-vet-accent hover:bg-blue-50 transition group">
                                <span className="text-xs font-bold text-slate-400 group-hover:text-vet-navy uppercase">+ Registrera nytt djur</span>
                            </button>
                        </div>
                    </section>
                </div>
            </div>
        </div>
    );
};

export default OwnerDashboard;