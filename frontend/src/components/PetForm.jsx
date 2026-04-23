import React, { useState } from 'react';
import { petService } from '../services/api';

const PetForm = ({ onCancel, onSave }) => {
    const [formData, setFormData] = useState({
        name: '',
        species: 'Hund',
        breed: '', // Lagt till Ras
        dob: '',
        weight: ''
    });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [fieldErrors, setFieldErrors] = useState({});

    const today = new Date().toISOString().split('T')[0];


    const handleSubmit = async (e) => {
        e.preventDefault();
        setFieldErrors({});

                // Klient-validering: framtida datum
        if (formData.dob && formData.dob > today) {
            setFieldErrors({ dob: 'Födelsedatum kan inte vara i framtiden.' });
            return;
        }

        setIsSubmitting(true);

        try {
            const petRequest = {
                name: formData.name,
                species: formData.species,
                breed: formData.breed || "Blandras",
                dateOfBirth: formData.dob,
                weightKg: formData.weight ? parseFloat(formData.weight) : null
            };

            console.log("Skickar till backend:", petRequest);

            await petService.createPet(petRequest);

            console.log("Djuret sparat i databasen!");
            onSave();
        } catch (error) {
            console.error("Kunde inte spara djuret:", error);
            if (error.response?.status === 400) {
                const data = error.response.data;
                const errors = {};
                if (data?.errors) {
                    // Spring returnerar vanligtvis { errors: [{ field, defaultMessage }] }
                    data.errors.forEach(err => {
                        if (err.field === 'dateOfBirth') {
                            errors.dob = 'Födelsedatum kan inte vara i framtiden.';
                        } else {
                            errors[err.field] = err.defaultMessage;
                        }
                    });
                } else if (data?.dateOfBirth || data?.message?.toLowerCase().includes('date')) {
                    errors.dob = 'Födelsedatum kan inte vara i framtiden.';
                } else {
                    errors.general = 'Något gick fel vid sparning. Kontrollera att alla obligatoriska fält är ifyllda.';
                }
                setFieldErrors(errors);
            } else {
                setFieldErrors({ general: 'Något gick fel vid sparning. Försök igen.' });
        }   }
    finally {
        setIsSubmitting(false);
    }
};

    return (
        <div className="max-w-2xl mx-auto animate-in slide-in-from-bottom-4 duration-500">
            {/* TILLBAKA-KNAPP */}
            <button
                onClick={onCancel}
                className="flex items-center gap-2 text-slate-400 hover:text-slate-900 font-bold text-[10px] uppercase tracking-[0.2em] mb-8 transition group"
            >
                <span className="group-hover:-translate-x-1 transition-transform">←</span> Tillbaka till översikt
            </button>

            <div className="bg-white rounded-xl shadow-xl shadow-slate-200/50 border border-slate-200 overflow-hidden">
                {/* HEADER */}
                <div className="bg-slate-900 p-8 text-white relative">
                    <div className="relative z-10">
                        <h1 className="text-2xl font-extrabold italic tracking-tight">Registrera nytt djur</h1>
                        <p className="text-slate-400 text-[10px] mt-1 uppercase font-black tracking-[0.2em]">Skapa en hälsoprofil</p>
                    </div>
                    <div className="absolute right-8 top-8 text-5xl opacity-20">🐾</div>
                </div>

                <form onSubmit={handleSubmit} className="p-8 space-y-8">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">

                        {/* NAMN */}
                        <div className="space-y-2">
                            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">Djurets Namn *</label>
                            <input
                                type="text"
                                value={formData.name}
                                onChange={(e) => setFormData({...formData, name: e.target.value})}
                                placeholder="t.ex. Buster"
                                className="w-full px-4 py-3 rounded-lg border border-slate-200 bg-slate-50 focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition font-medium italic"
                                required
                            />
                        </div>

                        {/* ART */}
                        <div className="space-y-2">
                            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">Art *</label>
                            <div className="relative">
                                <select
                                    value={formData.species}
                                    onChange={(e) => setFormData({...formData, species: e.target.value})}
                                    className="w-full px-4 py-3 rounded-lg border border-slate-200 bg-slate-50 focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition font-medium italic appearance-none cursor-pointer"
                                >
                                    <option>Hund</option>
                                    <option>Katt</option>
                                    <option>Gnagare</option>
                                    <option>Fågel</option>
                                    <option>Häst</option>
                                </select>
                                <div className="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400 text-[10px]">▼</div>
                            </div>
                        </div>

                        {/* RAS */}
                        <div className="space-y-2">
                            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">Ras</label>
                            <input
                                type="text"
                                value={formData.breed}
                                onChange={(e) => setFormData({...formData, breed: e.target.value})}
                                placeholder="t.ex. Golden Retriever"
                                className="w-full px-4 py-3 rounded-lg border border-slate-200 bg-slate-50 focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition font-medium italic"
                            />
                        </div>

                        {/* FÖDELSEDATUM */}
                        <div className="space-y-2">
                            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">Födelsedatum *</label>
                            <input
                                type="date"
                                value={formData.dob}
                                max={today}
                                onChange={(e) => {
                                    setFormData({...formData, dob: e.target.value});
                                    if (fieldErrors.dob) setFieldErrors({...fieldErrors, dob: undefined});
                                }}
                                className={`w-full px-4 py-3 rounded-lg border bg-slate-50 focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition font-medium italic ${fieldErrors.dob ? 'border-red-400 focus:ring-red-400' : 'border-slate-200'}`}
                                required
                            />
                            {fieldErrors.dob && (
                                <p className="text-red-500 text-xs font-semibold ml-1 mt-1">{fieldErrors.dob}</p>
                            )}
                        </div>

                        {/* VIKT */}
                        <div className="space-y-2">
                            <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1">Vikt (kg)</label>
                            <input
                                type="number"
                                step="0.1"
                                min="0.1"
                                value={formData.weight}
                                onChange={(e) => setFormData({...formData, weight: e.target.value})}
                                placeholder="0.0"
                                className="w-full px-4 py-3 rounded-lg border border-slate-200 bg-slate-50 focus:ring-2 focus:ring-blue-500 focus:bg-white outline-none transition font-medium italic"
                            />
                        </div>
                    </div>

                    {fieldErrors.general && (
                        <div className="bg-red-50 border border-red-200 rounded-lg ...">
                            {fieldErrors.general}
                        </div>
                    )}
                    {/* KNAPPAR */}
                    <div className="pt-8 border-t border-slate-100 flex flex-col sm:flex-row gap-4">
                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className="flex-1 bg-slate-900 text-white font-bold py-4 rounded-lg shadow-lg hover:bg-blue-900 transition transform active:scale-95 italic text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {isSubmitting ? 'Sparar hälsoprofil...' : 'Bekräfta & Spara Profil'}
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
        </div>
    );
};

export default PetForm;