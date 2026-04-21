import React, { useState, useEffect, useCallback } from 'react';
import { STATUS_MAP } from '../utils/statusHelper';
import { medicalRecordService } from '../services/api';
import {
    ClipboardList,
    UserPlus,
    AlertCircle,
    CheckCircle
} from 'lucide-react';

/**
 * VetDashboard
 * @param isPersonalView - Om true, visas endast ärenden tilldelade den inloggade veterinären.
 */
const VetDashboard = ({ userName, clinicId, currentUserId, onCaseClick, isPersonalView = false }) => {
    const [records, setRecords] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState('ALL');

    const fetchRecords = useCallback(async () => {
        if (!isPersonalView && !clinicId) {
            console.warn("Väntar på clinicId för klinikvy...");
            return;
        }

        setLoading(true);
        try {
            // Välj API-anrop baserat på om vi vill se "Mina ärenden" eller hela kliniken
            const res = isPersonalView
                ? await medicalRecordService.getMyAssignedRecords()
                : await medicalRecordService.getRecordsByClinic(clinicId);

            setRecords(res.data);
        } catch (err) {
            console.error("Kunde inte hämta ärenden:", err);
        } finally {
            setLoading(false);
        }
    }, [clinicId, isPersonalView]);

    useEffect(() => {
        fetchRecords();
    }, [fetchRecords]);

    const handleAssignToMe = async (e, recordId) => {
        e.stopPropagation();

        if (!currentUserId) {
            alert("Kunde inte identifiera din användarprofil. Prova att logga ut och in igen.");
            return;
        }

        try {
            await medicalRecordService.assignVet(recordId, currentUserId);
            fetchRecords();
        } catch (err) {
            console.error("Fel vid tilldelning:", err);
            alert("Kunde inte tilldela ärendet till dig.");
        }
    };

    // Filtreringslogik (Används främst i klinikvyn för att hitta lediga ärenden)
    const filteredRecords = records.filter(r => {
        if (filter === 'UNASSIGNED') return !r.assignedVetName;
        return true;
    });

    // Dynamisk statistik baserat på vy
    const stats = {
        open: records.filter(r => r.status === 'OPEN' || r.status === 'IN_PROGRESS').length,
        actionRequired: isPersonalView
            ? records.filter(r => r.status === 'IN_PROGRESS').length
            : records.filter(r => !r.assignedVetName).length,
        total: records.length
    };

    return (
        <div className="animate-in fade-in duration-500">
            {/* HEADER */}
            <header className="mb-10">
                <h1 className="text-4xl font-extrabold text-slate-900 italic tracking-tight">
                    {isPersonalView ? "Mina Aktiva Ärenden" : "Kliniköversikt"}
                </h1>
                <p className="text-slate-500 mt-2 font-semibold uppercase tracking-wider text-sm italic">
                    {isPersonalView
                        ? `Dr. ${userName?.split(' ')[0]}, här är de patienter du ansvarar för`
                        : `Välkommen, Dr. ${userName?.split(' ')[0]} • Hantera klinikens inkommande ärenden`}
                </p>
            </header>

            {/* STATS CARDS */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
                <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                    <div className="flex items-center gap-4">
                        <div className="bg-blue-50 p-3 rounded-xl text-blue-600">
                            <AlertCircle size={24} />
                        </div>
                        <div>
                            <p className="text-xs font-bold text-slate-400 uppercase tracking-widest">Aktiva</p>
                            <p className="text-2xl font-black text-slate-900">{stats.open}</p>
                        </div>
                    </div>
                </div>
                <div className={`bg-white p-6 rounded-2xl border border-slate-200 shadow-sm border-l-4 ${isPersonalView ? 'border-l-emerald-400' : 'border-l-orange-400'}`}>
                    <div className="flex items-center gap-4">
                        <div className={isPersonalView ? "bg-emerald-50 text-emerald-600 p-3 rounded-xl" : "bg-orange-50 text-orange-600 p-3 rounded-xl"}>
                            {isPersonalView ? <CheckCircle size={24} /> : <UserPlus size={24} />}
                        </div>
                        <div>
                            <p className="text-xs font-bold text-slate-400 uppercase tracking-widest">
                                {isPersonalView ? "Under behandling" : "Ej tilldelade"}
                            </p>
                            <p className="text-2xl font-black text-slate-900">{stats.actionRequired}</p>
                        </div>
                    </div>
                </div>
                <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm">
                    <div className="flex items-center gap-4">
                        <div className="bg-slate-50 p-3 rounded-xl text-slate-600">
                            <ClipboardList size={24} />
                        </div>
                        <div>
                            <p className="text-xs font-bold text-slate-400 uppercase tracking-widest">Totalt</p>
                            <p className="text-2xl font-black text-slate-900">{stats.total}</p>
                        </div>
                    </div>
                </div>
            </div>

            {/* FILTER TABS (Visas endast i klinikvyn) */}
            {!isPersonalView && (
                <div className="flex gap-2 mb-6 bg-slate-100 p-1 rounded-xl w-fit">
                    {[
                        { key: 'ALL', label: 'Alla ärenden' },
                        { key: 'UNASSIGNED', label: 'Väntar på veterinär' }
                    ].map((tab) => (
                        <button
                            key={tab.key}
                            onClick={() => setFilter(tab.key)}
                            className={`px-4 py-2 rounded-lg text-xs font-bold uppercase tracking-widest transition-all ${
                                filter === tab.key ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-500 hover:text-slate-700'
                            }`}
                        >
                            {tab.label}
                        </button>
                    ))}
                </div>
            )}

            {/* ÄRENDELISTA */}
            <div className="bg-white border border-slate-200 rounded-[2rem] overflow-hidden shadow-sm">
                <table className="w-full text-left border-collapse">
                    <thead className="bg-slate-50 border-bottom border-slate-200">
                    <tr>
                        <th className="px-6 py-4 text-[10px] font-black text-slate-400 uppercase tracking-[0.2em]">Patient & Ägare</th>
                        <th className="px-6 py-4 text-[10px] font-black text-slate-400 uppercase tracking-[0.2em]">Ärende</th>
                        <th className="px-6 py-4 text-[10px] font-black text-slate-400 uppercase tracking-[0.2em]">Status</th>
                        <th className="px-6 py-4 text-[10px] font-black text-slate-400 uppercase tracking-[0.2em]">Ansvarig</th>
                        <th className="px-6 py-4 text-[10px] font-black text-slate-400 uppercase tracking-[0.2em]">Åtgärd</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                    {loading ? (
                        <tr><td colSpan="5" className="p-10 text-center italic text-slate-400">Laddar ärenden...</td></tr>
                    ) : filteredRecords.length > 0 ? (
                        filteredRecords.map(record => {
                            const statusConfig = STATUS_MAP[record.status] || { label: record.status, color: 'bg-slate-100 text-slate-600 border-slate-200' };
                            return (
                                <tr
                                    key={record.id}
                                    onClick={() => onCaseClick(record)}
                                    className="hover:bg-slate-50 transition-colors cursor-pointer group"
                                >
                                    <td className="px-6 py-4">
                                        <div className="font-bold text-slate-900 italic">{record.petName || 'Okänt djur'}</div>
                                        <div className="text-[10px] text-slate-400 font-bold uppercase tracking-tighter">{record.ownerName || 'Djurägare'}</div>
                                    </td>
                                    <td className="px-6 py-4">
                                        <div className="font-bold text-slate-700 truncate max-w-[200px]">
                                            {record.title || 'Ingen rubrik'}
                                        </div>
                                        <div className="text-[10px] text-slate-400 italic">
                                            {record.createdAt ? new Date(record.createdAt).toLocaleDateString('sv-SE') : 'Datum saknas'}
                                        </div>
                                    </td>
                                    <td className="px-6 py-4">
                                            <span className={`text-[9px] font-black px-2 py-1 rounded uppercase border italic ${statusConfig.color}`}>
                                                {statusConfig.label}
                                            </span>
                                    </td>
                                    <td className="px-6 py-4">
                                        <div className="flex items-center gap-2">
                                            <div className={`w-2 h-2 rounded-full ${record.assignedVetName ? 'bg-emerald-400' : 'bg-slate-300'}`}></div>
                                            <span className="text-sm font-medium text-slate-600">
                                                    {record.assignedVetName || 'Ej tilldelad'}
                                                </span>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4">
                                        {!record.assignedVetName ? (
                                            <button
                                                onClick={(e) => handleAssignToMe(e, record.id)}
                                                className="flex items-center gap-1.5 px-3 py-1.5 bg-slate-900 text-white text-[10px] font-black uppercase tracking-widest rounded-lg hover:bg-blue-700 transition-all italic shadow-md"
                                            >
                                                <UserPlus size={12} /> Ta ärende
                                            </button>
                                        ) : (
                                            <button className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic group-hover:text-blue-600 transition-colors">
                                                Öppna Journal
                                            </button>
                                        )}
                                    </td>
                                </tr>
                            );
                        })
                    ) : (
                        <tr>
                            <td colSpan="5" className="p-10 text-center italic text-slate-400">
                                {isPersonalView ? "Du har inga tilldelade ärenden just nu." : "Inga ärenden hittades för din klinik."}
                            </td>
                        </tr>
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default VetDashboard;