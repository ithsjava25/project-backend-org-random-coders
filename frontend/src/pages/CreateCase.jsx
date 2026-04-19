import React, { useState, useEffect, useRef } from 'react';
import { medicalRecordService, attachmentService, clinicService } from '../services/api';

const CreateCase = ({ pets, onCancel, onSave, existingCase, preSelectedPet }) => {
    // State
    const [title, setTitle] = useState(existingCase ? existingCase.title : '');
    const [selectedPet, setSelectedPet] = useState(
        existingCase ? existingCase.petId : (preSelectedPet?.id || pets[0]?.id || null)
    );
    const [selectedClinic, setSelectedClinic] = useState(existingCase ? existingCase.clinicId : '');
    const [clinics, setClinics] = useState([]);
    const [description, setDescription] = useState(existingCase ? existingCase.description : '');
    const [files, setFiles] = useState([]);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const fileInputRef = useRef(null);

    // Hämta kliniker vid start
    useEffect(() => {
        const fetchClinics = async () => {
            try {
                const res = await clinicService.getAll();
                setClinics(res.data);
                // Om vi inte redigerar och det finns kliniker, välj den första som default
                if (!existingCase && res.data.length > 0) {
                    setSelectedClinic(res.data[0].id);
                }
            } catch (err) {
                console.error("Kunde inte hämta kliniker", err);
            }
        };
        fetchClinics();
    }, [existingCase]);

    const handleFileChange = (e) => {
        if (e.target.files) setFiles(Array.from(e.target.files));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // VALIDERINGS-GUARDS (CodeRabbit fix)
        if (!selectedClinic) {
            alert("Vänligen välj en klinik.");
            return;
        }

        if (!selectedPet) {
            alert("Vänligen välj ett djur för detta ärende.");
            return;
        }

        // Starta inskickning först efter att valideringen har passerat
        setIsSubmitting(true);

        try {
            const caseData = {
                title: title,
                description: description,
                petId: selectedPet,
                clinicId: selectedClinic,
                id: existingCase?.id
            };

            const recordRes = await medicalRecordService.createRecord(caseData);
            const recordId = recordRes.data.id;

            if (files.length > 0) {
                for (const file of files) {
                    const formData = new FormData();
                    formData.append('file', file);
                    formData.append('description', `Bilaga`);
                    await attachmentService.upload(recordId, formData);
                }
            }
            onSave();
        } catch (err) {
            console.error("Fel:", err);
            alert("Något gick fel vid sparning.");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="max-w-3xl mx-auto animate-in slide-in-from-bottom-4 duration-500">
            <button onClick={onCancel} className="flex items-center gap-2 text-slate-400 hover:text-slate-900 font-bold text-[10px] uppercase tracking-[0.2em] mb-8 transition group">
                <span className="group-hover:-translate-x-1 transition-transform">←</span> Tillbaka
            </button>

            <div className="bg-white rounded-xl shadow-xl border border-slate-200 overflow-hidden">
                <div className="bg-slate-900 p-8 text-white relative">
                    <h1 className="text-2xl font-extrabold italic tracking-tight">
                        {existingCase ? 'Uppdatera ärende' : 'Nytt vårdärende'}
                    </h1>
                    <p className="text-slate-400 text-[10px] uppercase font-black tracking-[0.2em]">Sök vård för ditt djur</p>
                </div>

                <form onSubmit={handleSubmit} className="p-8 space-y-8">
                    {/* KLINIKVÄLJARE */}
                    <div className="space-y-2">
                        <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1 italic">
                            Välj klinik *
                        </label>
                        <select
                            value={selectedClinic}
                            onChange={(e) => setSelectedClinic(e.target.value)}
                            className="w-full px-4 py-3 rounded-lg border border-slate-200 bg-slate-50 focus:ring-2 focus:ring-blue-500 outline-none transition font-medium italic"
                            required
                        >
                            <option value="" disabled>Välj en klinik...</option>
                            {clinics.map(clinic => (
                                <option key={clinic.id} value={clinic.id}>
                                    {clinic.name} ({clinic.address})
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* RUBRIK */}
                    <div className="space-y-2">
                        <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1 italic">Rubrik *</label>
                        <input type="text" value={title} onChange={(e) => setTitle(e.target.value)} className="w-full px-4 py-3 rounded-lg border border-slate-200 bg-slate-50 outline-none" required />
                    </div>

                    {/* DJURVÄLJARE */}
                    <div className="space-y-2">
                        <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1 italic">Vilket djur? *</label>
                        <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                            {pets.map((pet) => (
                                <label key={pet.id} className="cursor-pointer">
                                    <input
                                        type="radio"
                                        name="petId"
                                        value={pet.id}
                                        checked={String(selectedPet) === String(pet.id)}
                                        onChange={() => setSelectedPet(pet.id)}
                                        className="peer sr-only"
                                    />
                                    <div className="p-4 border border-slate-200 rounded-xl peer-checked:border-blue-600 peer-checked:bg-blue-50 text-center transition">
                                        <span className="block font-bold text-sm">{pet.name}</span>
                                    </div>
                                </label>
                            ))}
                        </div>
                    </div>

                    {/* BESKRIVNING */}
                    <div className="space-y-2">
                        <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1 italic">Beskrivning *</label>
                        <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows="4" className="w-full px-4 py-3 rounded-lg border border-slate-200 bg-slate-50 outline-none resize-none" required />
                    </div>

                    {/* FILER */}
                    <div className="space-y-2">
                        <label className="text-[10px] font-bold text-slate-400 uppercase tracking-widest ml-1 italic">Bilagor</label>
                        <div onClick={() => fileInputRef.current.click()} className="border-2 border-dashed border-slate-200 rounded-xl p-6 text-center hover:bg-slate-50 cursor-pointer relative">
                            <input type="file" ref={fileInputRef} multiple onChange={handleFileChange} className="hidden" />
                            <p className="text-sm text-slate-500">{files.length > 0 ? `${files.length} filer valda` : 'Klicka för att ladda upp'}</p>
                        </div>
                    </div>

                    <button type="submit" disabled={isSubmitting} className="w-full bg-slate-900 text-white font-bold py-4 rounded-lg hover:bg-blue-900 transition disabled:opacity-50 uppercase tracking-widest text-xs">
                        {isSubmitting ? 'Sparar...' : 'Skicka ärende'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default CreateCase;