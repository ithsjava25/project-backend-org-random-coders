import React, { useState, useMemo } from 'react';
import LogDetailsModal from './LogDetailsModal'; // Importeras nu som den enda källan till sanning
import {
    FilePlus,
    RefreshCw,
    MessageSquare,
    UserPlus,
    Clock,
    User as UserIcon,
    AlertCircle,
    Search,
    MapPin,
    X,
    Activity,
    Hash
} from 'lucide-react';
import { getActionLabel } from '../../utils/statusHelper';

// --- HUVUDKOMPONENT ---
const AuditLogView = ({ logs = [], loading }) => {
    const [searchTerm, setSearchTerm] = useState('');
    const [roleFilter, setRoleFilter] = useState('ALL');
    const [actionFilter, setActionFilter] = useState('ALL');
    const [clinicFilter, setClinicFilter] = useState('ALL');
    const [dateFilter, setDateFilter] = useState('');

    // States för modal
    const [selectedLog, setSelectedLog] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);

    const openLogDetails = (log) => {
        setSelectedLog(log);
        setIsModalOpen(true);
    };

    const clinics = useMemo(() => {
        const uniqueClinics = [...new Set(logs.map(log => log.clinicName))].filter(Boolean);
        return uniqueClinics.sort();
    }, [logs]);

    const getActionStyle = (action) => {
        switch (action) {
            case 'CASE_CREATED': return { icon: <FilePlus size={16} />, color: 'text-emerald-500', bg: 'bg-emerald-50' };
            case 'STATUS_CHANGED': return { icon: <RefreshCw size={16} />, color: 'text-blue-500', bg: 'bg-blue-50' };
            case 'COMMENT_ADDED': return { icon: <MessageSquare size={16} />, color: 'text-purple-500', bg: 'bg-purple-50' };
            case 'USER_REGISTERED': return { icon: <UserPlus size={16} />, color: 'text-orange-500', bg: 'bg-orange-50' };
            case 'ASSIGNED': return { icon: <UserPlus size={16} />, color: 'text-orange-500', bg: 'bg-orange-50' };
            default: return { icon: <AlertCircle size={16} />, color: 'text-slate-500', bg: 'bg-slate-50' };
        }
    };

    const filteredLogs = useMemo(() => {
        return logs.filter(log => {
            const searchLower = searchTerm.toLowerCase();
            const matchesSearch =
                log.performedByName?.toLowerCase().includes(searchLower) ||
                log.description?.toLowerCase().includes(searchLower) ||
                log.clinicName?.toLowerCase().includes(searchLower) ||
                log.petName?.toLowerCase().includes(searchLower) ||
                log.recordId?.includes(searchTerm);

            const matchesAction = actionFilter === 'ALL' || log.action === actionFilter;
            const matchesRole = roleFilter === 'ALL' ||
                log.performedByRole?.replace('ROLE_', '') === roleFilter.replace('ROLE_', '');

            const matchesClinic = clinicFilter === 'ALL' || log.clinicName === clinicFilter;
            const matchesDate = !dateFilter || (log.createdAt?.startsWith(dateFilter) ?? false);

            return matchesSearch && matchesAction && matchesRole && matchesDate && matchesClinic;
        });
    }, [logs, searchTerm, actionFilter, roleFilter, clinicFilter, dateFilter]);

    if (loading) return <div className="p-20 text-center italic text-slate-400">Laddar administrativ historik...</div>;

    return (
        <div className="space-y-0">
            {/* FILTER PANEL */}
            <div className="p-6 bg-slate-50 border-b border-slate-200 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4 items-end text-left">
                <div>
                    <label className="block text-[10px] font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Sök</label>
                    <div className="relative">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={14} />
                        <input
                            type="text"
                            className="w-full pl-10 pr-4 py-2 bg-white border border-slate-200 rounded-xl text-xs focus:ring-2 focus:ring-[#003f5a] outline-none transition-all"
                            placeholder="Sök..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>
                </div>
                <div>
                    <label className="block text-[10px] font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Händelse</label>
                    <select value={actionFilter} onChange={(e) => setActionFilter(e.target.value)} className="w-full bg-white border border-slate-200 rounded-xl px-3 py-2 text-xs font-bold outline-none">
                        <option value="ALL">ALLA</option>
                        <option value="CASE_CREATED">NYA ÄRENDEN</option>
                        <option value="STATUS_CHANGED">STATUS</option>
                        <option value="COMMENT_ADDED">KOMMENTARER</option>
                        <option value="ASSIGNED">TILLDELNING</option>
                    </select>
                </div>
                <div>
                    <label className="block text-[10px] font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Roll</label>
                    <select value={roleFilter} onChange={(e) => setRoleFilter(e.target.value)} className="w-full bg-white border border-slate-200 rounded-xl px-3 py-2 text-xs font-bold outline-none">
                        <option value="ALL">ALLA</option>
                        <option value="VET">VETERINÄR</option>
                        <option value="OWNER">ÄGARE</option>
                        <option value="ADMIN">ADMIN</option>
                    </select>
                </div>
                <div>
                    <label className="block text-[10px] font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Klinik</label>
                    <select value={clinicFilter} onChange={(e) => setClinicFilter(e.target.value)} className="w-full bg-white border border-slate-200 rounded-xl px-3 py-2 text-xs font-bold outline-none">
                        <option value="ALL">ALLA</option>
                        {clinics.map(name => <option key={name} value={name}>{name.toUpperCase()}</option>)}
                    </select>
                </div>
                <div>
                    <label className="block text-[10px] font-black text-slate-400 uppercase tracking-widest mb-2 ml-1">Datum</label>
                    <input type="date" value={dateFilter} onChange={(e) => setDateFilter(e.target.value)} className="w-full bg-white border border-slate-200 rounded-xl px-3 py-2 text-xs font-bold outline-none text-slate-600" />
                </div>
            </div>

            {/* LOG LIST */}
            <div className="p-8 pt-6">
                {filteredLogs.length === 0 ? (
                    <div className="p-20 text-center">
                        <Clock className="mx-auto text-slate-200 mb-4" size={48} />
                        <p className="text-slate-400 font-bold italic uppercase tracking-widest text-xs">Inga loggar matchar filter</p>
                    </div>
                ) : (
                    <div className="space-y-2 text-left">
                        {filteredLogs.map((log) => {
                            const style = getActionStyle(log.action);
                            const dateStr = new Date(log.createdAt).toLocaleString('sv-SE', {
                                month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
                            });

                            return (
                                <div
                                    key={log.id}
                                    onClick={() => openLogDetails(log)}
                                    className="relative flex gap-6 group border-b border-slate-50 last:border-0 cursor-pointer hover:bg-slate-50/80 p-2 rounded-2xl transition-all"
                                >
                                    <div className="absolute left-8 top-12 bottom-[-8px] w-px bg-slate-100 group-last:hidden" />
                                    <div className={`relative z-10 w-12 h-12 rounded-2xl ${style.bg} ${style.color} flex items-center justify-center shadow-sm border border-white shrink-0`}>
                                        {style.icon}
                                    </div>
                                    <div className="flex-1 pb-6 pt-2">
                                        <div className="flex justify-between items-start mb-1">
                                            <div className="flex items-center gap-3">
                                                <h4 className="font-black text-slate-900 italic tracking-tight uppercase text-sm">
                                                    {getActionLabel(log.action)}
                                                </h4>
                                                <span className={`text-[9px] font-black px-2 py-0.5 rounded-md border ${
                                                    log.performedByRole?.includes('VET') ? 'bg-blue-50 text-blue-600 border-blue-100' :
                                                        log.performedByRole?.includes('ADMIN') ? 'bg-red-50 text-red-600 border-red-100' :
                                                            'bg-emerald-50 text-emerald-600 border-emerald-100'
                                                }`}>
                                                    {log.performedByRole?.replace('ROLE_', '')}
                                                </span>
                                            </div>
                                            <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">{dateStr}</span>
                                        </div>
                                        <p className="text-slate-600 text-sm font-medium mb-3 truncate max-w-2xl">{log.description}</p>
                                        <div className="flex flex-wrap items-center gap-3">
                                            <div className="flex items-center gap-1.5 px-3 py-1 bg-white rounded-lg border border-slate-200 shadow-sm">
                                                <UserIcon size={10} className="text-slate-400" />
                                                <span className="text-[10px] font-black text-slate-700 uppercase italic">{log.performedByName}</span>
                                            </div>
                                            {log.petName && (
                                                <div className="flex items-center gap-1.5 px-3 py-1 bg-amber-50 rounded-lg border border-amber-100 shadow-sm">
                                                    <span className="text-[10px] font-black text-amber-700 uppercase italic">Patient: {log.petName}</span>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                )}
            </div>

            {/* MODAL RENDER - Använder nu den importerade komponenten */}
            <LogDetailsModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                log={selectedLog}
            />
        </div>
    );
};

export default AuditLogView;