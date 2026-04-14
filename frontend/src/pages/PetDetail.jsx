import React from 'react';

const PetDetail = ({ pet, onBack }) => {
    // Om vi inte har ett djur (för säkerhets skull)
    if (!pet) return null;

    return (
        <div className="max-w-4xl mx-auto space-y-8 animate-in fade-in slide-in-from-left-4 duration-500">
            {/* TILLBAKA-KNAPP */}
            <button
                onClick={onBack}
                className="flex items-center gap-2 text-slate-400 hover:text-vet-navy font-bold text-[10px] uppercase tracking-[0.2em] transition group"
            >
                <span className="group-hover:-translate-x-1 transition-transform">←</span> Tillbaka till mina djur
            </button>

            {/* DJURKORT - HEADER */}
            <section className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                <div className="p-8 flex flex-col md:flex-row gap-8 items-center md:items-start">
                    <div className="h-32 w-32 bg-slate-50 rounded-2xl flex items-center justify-center text-6xl shadow-inner border border-slate-100 relative group">
                        {pet.species || '🐕'}
                        <button className="absolute -bottom-2 -right-2 bg-white p-2 rounded-lg shadow-md border border-slate-100 opacity-0 group-hover:opacity-100 transition text-xs">📸</button>
                    </div>

                    <div className="flex-1 text-center md:text-left">
                        <div className="flex flex-col md:flex-row md:items-center gap-3 mb-2">
                            <h1 className="text-3xl font-extrabold text-slate-900 italic tracking-tight">{pet.name}</h1>
                            <span className="px-3 py-1 rounded-full bg-blue-50 text-vet-navy text-[10px] font-bold uppercase border border-blue-100 mx-auto md:mx-0">
                                ID: #VET-{pet.id || '8821'}
                            </span>
                        </div>

                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-6">
                            <div>
                                <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Ras</p>
                                <p className="font-bold text-slate-700 italic">{pet.breed}</p>
                            </div>
                            <div>
                                <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Ålder</p>
                                <p className="font-bold text-slate-700 italic">5 år (2019-04-12)</p>
                            </div>
                            <div>
                                <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Vikt</p>
                                <p className="font-bold text-slate-700 italic">28.5 kg</p>
                            </div>
                            <div>
                                <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Kön</p>
                                <p className="font-bold text-slate-700 italic">Tik (Kastrerad)</p>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="bg-slate-50 px-8 py-4 border-t border-slate-100 flex gap-4">
                    <button className="text-xs font-bold text-vet-navy hover:text-vet-accent transition uppercase tracking-widest flex items-center gap-2">
                        <span>+</span> Nytt ärende för {pet.name}
                    </button>
                    <button className="text-xs font-bold text-slate-400 hover:text-slate-600 transition uppercase tracking-widest ml-auto">
                        Redigera profil
                    </button>
                </div>
            </section>

            {/* JOURNALHISTORIK */}
            <section className="space-y-6">
                <div className="flex items-center justify-between px-2">
                    <h2 className="text-sm font-bold text-slate-400 uppercase tracking-[0.2em]">Journalhistorik</h2>
                    <button className="p-2 bg-white border border-slate-200 rounded-lg hover:bg-slate-50 transition shadow-sm">
                        <svg className="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
                        </svg>
                    </button>
                </div>

                <div className="relative pl-8 space-y-6 before:absolute before:left-3 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-100">
                    {/* Pågående ärende */}
                    <div className="relative">
                        <div className="absolute -left-8 top-5 w-4 h-4 rounded-full bg-vet-navy border-4 border-white shadow-sm ring-4 ring-blue-50"></div>
                        <div className="bg-white p-6 rounded-xl border-2 border-vet-navy/10 shadow-sm hover:border-vet-accent transition cursor-pointer group">
                            <div className="flex justify-between items-start mb-2">
                                <span className="text-[10px] font-bold text-vet-accent uppercase italic">Pågående Ärende</span>
                                <span className="text-[10px] text-slate-400 font-bold">Inlett: 2024-03-25</span>
                            </div>
                            <h3 className="text-xl font-bold text-slate-900 italic group-hover:text-vet-navy transition">Hälta vänster framben</h3>
                            <p className="text-sm text-slate-500 mt-2 italic leading-relaxed">Status: Väntar på kompletterande bild. Handläggs av Dr. Erik Berg.</p>
                            <div className="mt-4 flex items-center gap-2 text-vet-navy font-bold text-xs uppercase tracking-tighter">
                                Visa konversation och ladda upp filer →
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    );
};

export default PetDetail;