import React, { useState, useEffect, useId } from 'react'; // Importera useId för unika ID:n
import { X, Hospital, MapPin, Phone, Loader2 } from 'lucide-react';

const ClinicModal = ({ isOpen, onClose, onSave, initialData = null }) => {
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState({
        name: '',
        address: '',
        phoneNumber: ''
    });

    // Skapa unika ID:n för tillgänglighet
    const modalTitleId = useId();
    const nameInputId = useId();
    const addressInputId = useId();
    const phoneInputId = useId();

    useEffect(() => {
        if (isOpen) {
            if (initialData) {
                setFormData({
                    name: initialData.name || '',
                    address: initialData.address || '',
                    phoneNumber: initialData.phoneNumber || ''
                });
            } else {
                setFormData({ name: '', address: '', phoneNumber: '' });
            }
        }
    }, [isOpen, initialData]);

    if (!isOpen) return null;

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            await onSave(formData);
            onClose();
        } catch (err) {
            alert(err.response?.data?.message || "Ett fel uppstod vid sparandet av kliniken.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200">
            {/* 1. Tillagt role="dialog", aria-modal="true" och aria-labelledby */}
            <div
                className="bg-white w-full max-w-md rounded-[2.5rem] shadow-2xl overflow-hidden border border-slate-200"
                role="dialog"
                aria-modal="true"
                aria-labelledby={modalTitleId}
            >
                {/* HEADER */}
                <div className="px-8 pt-8 pb-4 flex justify-between items-center">
                    <div>
                        {/* 2. Tillagt ID för rubriken */}
                        <h2 id={modalTitleId} className="text-2xl font-black text-slate-900 italic tracking-tight">
                            {initialData ? 'Redigera Klinik' : 'Ny Klinik'}
                        </h2>
                        <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest mt-1">
                            {initialData ? 'Uppdatera klinikuppgifter' : 'Registrera ny vårdenhet'}
                        </p>
                    </div>
                    {/* 3. Tillagt aria-label på stäng-knappen */}
                    <button
                        onClick={onClose}
                        className="p-2 hover:bg-slate-100 rounded-full transition-colors text-slate-400"
                        aria-label="Stäng modal"
                    >
                        <X size={20} />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="p-8 space-y-5">
                    {/* KLINIKNAMN */}
                    <div className="space-y-1.5">
                        {/* 4. Använt htmlFor och kopplat till input-ID */}
                        <label htmlFor={nameInputId} className="text-[10px] font-black text-slate-500 uppercase tracking-widest ml-1 italic">
                            Klinikens Namn
                        </label>
                        <div className="relative">
                            <Hospital className="absolute left-4 top-3 text-slate-400" size={18} aria-hidden="true" />
                            <input
                                id={nameInputId}
                                required
                                type="text"
                                className="w-full pl-11 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-slate-900 focus:border-transparent outline-none transition-all font-medium text-slate-700"
                                placeholder="t.ex. Vet1177 Central"
                                value={formData.name}
                                onChange={(e) => setFormData({...formData, name: e.target.value})}
                            />
                        </div>
                    </div>

                    {/* ADRESS */}
                    <div className="space-y-1.5">
                        <label htmlFor={addressInputId} className="text-[10px] font-black text-slate-500 uppercase tracking-widest ml-1 italic">
                            Gatuadress & Ort
                        </label>
                        <div className="relative">
                            <MapPin className="absolute left-4 top-3 text-slate-400" size={18} aria-hidden="true" />
                            <input
                                id={addressInputId}
                                required
                                type="text"
                                className="w-full pl-11 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-slate-900 focus:border-transparent outline-none transition-all font-medium text-slate-700"
                                placeholder="Storgatan 1, 123 45 Stad"
                                value={formData.address}
                                onChange={(e) => setFormData({...formData, address: e.target.value})}
                            />
                        </div>
                    </div>

                    {/* TELEFON */}
                    <div className="space-y-1.5">
                        <label htmlFor={phoneInputId} className="text-[10px] font-black text-slate-500 uppercase tracking-widest ml-1 italic">
                            Telefonnummer
                        </label>
                        <div className="relative">
                            <Phone className="absolute left-4 top-3 text-slate-400" size={18} aria-hidden="true" />
                            <input
                                id={phoneInputId}
                                required
                                type="tel"
                                className="w-full pl-11 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl focus:ring-2 focus:ring-slate-900 focus:border-transparent outline-none transition-all font-medium text-slate-700"
                                placeholder="010-123 45 67"
                                value={formData.phoneNumber}
                                onChange={(e) => setFormData({...formData, phoneNumber: e.target.value})}
                            />
                        </div>
                    </div>

                    {/* ACTION BUTTONS */}
                    <div className="pt-4 flex gap-3">
                        <button
                            type="button"
                            onClick={onClose}
                            className="flex-1 px-6 py-4 border border-slate-200 text-slate-400 text-[10px] font-black uppercase tracking-[0.2em] rounded-2xl hover:bg-slate-50 transition-all italic"
                        >
                            Avbryt
                        </button>
                        <button
                            disabled={loading}
                            type="submit"
                            className={`flex-2 px-10 py-4 text-white text-[10px] font-black uppercase tracking-[0.2em] rounded-2xl transition-all italic shadow-lg flex items-center justify-center gap-2 ${
                                initialData
                                    ? 'bg-blue-600 hover:bg-blue-700 shadow-blue-900/20'
                                    : 'bg-emerald-600 hover:bg-emerald-700 shadow-emerald-900/20'
                            }`}
                        >
                            {loading ? (
                                <Loader2 className="animate-spin" size={16} />
                            ) : (
                                initialData ? 'Uppdatera Klinik' : 'Registrera Klinik'
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ClinicModal;