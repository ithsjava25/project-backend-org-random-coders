import React, { useState } from 'react';

const CreateCase = ({ pets, onCancel, onSave, existingCase }) => {
    // Om vi redigerar ett ärende, använd dess data, annars starta tomt
    const [selectedPet, setSelectedPet] = useState(existingCase ? existingCase.petId : (pets[0]?.id || null));
    const [description, setDescription] = useState(existingCase ? (existingCase.observation || existingCase.description) : '');

    const handleSubmit = (e) => {
        e.preventDefault();

        const caseData = {
            petId: selectedPet,
            description: description,
            id: existingCase?.id // Skicka med ID om det är en uppdatering
        };

        console.log(existingCase ? "Uppdaterar ärende:" : "Skapar nytt ärende:", caseData);

        // Här anropar vi onSave som skickar oss tillbaka till önskad vy
        onSave();
    };

    return (
        <div className="max-w-3xl mx-auto animate-in slide-in-from-bottom-4 duration-500">
            <button
                onClick={onCancel}
                className="flex items-center gap-2 text-slate-400 hover:text-vet-navy font-bold text-[10px] uppercase tracking-[0.2em] mb-8 transition group"
            >
                <span className="group-hover:-translate-x-1 transition-transform">←</span> Tillbaka
            </button>

            <div className="bg-white rounded-xl shadow-xl shadow-slate-200/50 border border-slate-200 overflow-hidden">
                <div className="bg-vet-navy p-8 text-white relative">
                    <div className="relative z-10">
                        <h1 className="text-2xl font-extrabold italic tracking-tight">
                            {existingCase ? 'Uppdatera ärende' : 'Nytt vårdärende'}
                        </h1>
                        <p className="text-slate-400 text-[10px] mt-1 uppercase font-black tracking-[0.2em]">
                            {existingCase ? `Ändrar ärende för ${existingCase.petName}` : 'Beskriv ditt djurs besvär'}
                        </p>
                    </div>
                    <div className="absolute right-8 top-8 text-5xl opacity-20">📋</div>
                </div>

                <form onSubmit={handleSubmit} className="p-8 space-y-8">
                    {/* VÄLJ DJUR (Inaktiverat vid redigering för att undvika fel) */}
                    <div className="space-y-2">
                        <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1 italic">
                            Vilket djur gäller det? *
                        </label>
                        <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                            {pets.map((pet) => (
                                <label key={pet.id} className={`relative ${existingCase ? 'cursor-not-allowed opacity-60' : 'cursor-pointer'}`}>
                                    <input
                                        type="radio"
                                        name="petId"
                                        value={pet.id}
                                        checked={selectedPet === pet.id}
                                        onChange={() => !existingCase && setSelectedPet(pet.id)}
                                        disabled={existingCase} // Man byter sällan djur på ett befintligt ärende
                                        className="peer sr-only"
                                    />
                                    <div className="p-4 border border-slate-200 rounded-xl peer-checked:border-vet-accent peer-checked:bg-blue-50/50 hover:bg-slate-50 transition text-center">
                                        <span className="block text-2xl mb-1">{pet.species}</span>
                                        <span className="block font-bold text-slate-900 text-sm">{pet.name}</span>
                                    </div>
                                </label>
                            ))}
                        </div>
                    </div>

                    {/* BESKRIVNING */}
                    <div className="space-y-2">
                        <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1 italic">
                            Beskriv besvären *
                        </label>
                        <textarea
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            rows="5"
                            placeholder="När började det? Hur beter sig djuret? Har det ätit/druckit normalt?"
                            className="w-full px-4 py-3 rounded-lg border border-slate-200 bg-slate-50 focus:ring-2 focus:ring-vet-accent focus:bg-white outline-none transition font-medium italic resize-none"
                            required
                        ></textarea>
                    </div>

                    {/* FILUPPLADDNING */}
                    <div className="space-y-2">
                        <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1 italic">
                            Bifoga bilder eller dokument
                        </label>
                        <div className="border-2 border-dashed border-slate-200 rounded-xl p-8 text-center hover:border-vet-accent hover:bg-blue-50/30 transition group cursor-pointer relative">
                            <input type="file" multiple className="absolute inset-0 w-full h-full opacity-0 cursor-pointer" />
                            <div className="space-y-2">
                                <div className="text-3xl group-hover:scale-110 transition-transform">📸</div>
                                <p className="text-sm font-bold text-slate-600">
                                    {existingCase ? 'Lägg till fler bilder' : 'Klicka för att ladda upp eller dra filer hit'}
                                </p>
                                <p className="text-[10px] text-slate-400 font-medium uppercase tracking-tighter">JPG, PNG eller PDF (Max 10MB per fil)</p>
                            </div>
                        </div>
                    </div>

                    {/* KNAPPAR */}
                    <div className="pt-8 border-t border-slate-100 flex flex-col sm:flex-row gap-4">
                        <button type="submit" className="flex-1 bg-vet-navy text-white font-bold py-4 rounded-lg shadow-lg shadow-vet-navy/20 hover:bg-vet-accent transition transform active:scale-95 italic text-sm">
                            {existingCase ? 'Spara ändringar' : 'Skicka in ärende'}
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

export default CreateCase;