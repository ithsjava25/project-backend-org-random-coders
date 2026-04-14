import React, { useState } from 'react';

const PetForm = ({ onCancel, onSave }) => {
    const [formData, setFormData] = useState({
        name: '',
        species: 'Hund',
        dob: '',
        weight: ''
    });

    const handleSubmit = (e) => {
        e.preventDefault();
        // Här skickar vi senare data till Java-backend
        console.log("Sparar djur:", formData);
        onSave(); // Gå tillbaka till dashboard efter spara
    };

    return (
        <div className="max-w-2xl mx-auto animate-in slide-in-from-bottom-4 duration-500">
            <button
                onClick={onCancel}
                className="flex items-center gap-2 text-slate-400 hover:text-vet-navy font-bold text-[10px] uppercase tracking-[0.2em] mb-8 transition group"
            >
                <span className="group-hover:-translate-x-1 transition-transform">←</span> Tillbaka till översikt
            </button>

            <div className="bg-white rounded-xl shadow-xl shadow-slate-200/50 border border-slate-200 overflow-hidden">
                <div className="bg-vet-navy p-8 text-white relative">
                    <div className="relative z-10">
                        <h1 className="text-2xl font-extrabold italic tracking-tight">Registrera ny vän</h1>
                        <p className="text-slate-400 text-[10px] mt-1 uppercase font-black tracking-[0.2em]">Skapa en hälsoprofil</p>
                    </div>
                    <div className="absolute right-8 top-8 text-5xl opacity-20">🐾</div>
                </div>

                <form onSubmit={handleSubmit} className="p-8 space-y-8">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">
                        <div className="space-y-2">
                            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">Djurets Namn *</label>
                            <input
                                type="text"
                                value={formData.name}
                                onChange={(e) => setFormData({...formData, name: e.target.value})}
                                placeholder="t.ex. Buster"
                                className="w-full px-4 py-3 rounded-lg border border-slate-200 bg-slate-50 focus:ring-2 focus:ring-vet-accent focus:bg-white outline-none transition font-medium italic"
                                required
                            />
                        </div>

                        <div className="space-y-2">
                            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">Art *</label>
                            <div className="relative">
                                <select
                                    value={formData.species}
                                    onChange={(e) => setFormData({...formData, species: e.target.value})}
                                    className="w-full px-4 py-3 rounded-lg border border-slate-200 bg-slate-50 focus:ring-2 focus:ring-vet-accent focus:bg-white outline-none transition font-medium italic appearance-none cursor-pointer"
                                >
                                    <option>Hund</option>
                                    <option>Katt</option>
                                    <option>Smådjur / Gnagare</option>
                                    <option>Häst</option>
                                </select>
                                <div className="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400">▼</div>
                            </div>
                        </div>

                        <div className="space-y-2">
                            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">Födelsedatum</label>
                            <input
                                type="date"
                                value={formData.dob}
                                onChange={(e) => setFormData({...formData, dob: e.target.value})}
                                className="w-full px-4 py-3 rounded-lg border border-slate-200 bg-slate-50 focus:ring-2 focus:ring-vet-accent focus:bg-white outline-none transition font-medium italic"
                            />
                        </div>

                        <div className="space-y-2">
                            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">Vikt (kg)</label>
                            <input
                                type="number"
                                step="0.1"
                                value={formData.weight}
                                onChange={(e) => setFormData({...formData, weight: e.target.value})}
                                placeholder="0.0"
                                className="w-full px-4 py-3 rounded-lg border border-slate-200 bg-slate-50 focus:ring-2 focus:ring-vet-accent focus:bg-white outline-none transition font-medium italic"
                            />
                        </div>
                    </div>

                    <div className="pt-8 border-t border-slate-100 flex flex-col sm:flex-row gap-4">
                        <button type="submit" className="flex-1 bg-vet-navy text-white font-bold py-4 rounded-lg shadow-lg shadow-vet-navy/20 hover:bg-vet-accent transition transform active:scale-95 italic text-sm">
                            Bekräfta & Spara Profil
                        </button>
                        <button
                            type="button"
                            onClick={onCancel}
                            className="px-8 py-4 text-slate-400 font-bold text-[10px] uppercase tracking-widest hover:text-red-500 transition"
                        >
                            Avbryt
                        </button>
                    </div>
                </form>
            </div>

            <p className="mt-6 text-center text-[10px] text-slate-400 font-medium uppercase tracking-tighter">
                🔒 All data lagras krypterat enligt patientdatalagen
            </p>
        </div>
    );
};

export default PetForm;