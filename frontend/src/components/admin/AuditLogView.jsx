import React, { useState, useMemo } from 'react';
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
    ShieldCheck,
    X,
    Activity,
    Hash,
    Info
} from 'lucide-react';

// --- MODAL KOMPONENT ---
const LogDetailsModal = ({ isOpen, onClose, log }) => {
    if (!isOpen || !log) return null;

    const fullDate = new Date(log.createdAt).toLocaleString('sv-SE', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });

    return (
        <div className="fixed inset-0 z-[70] flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="bg-white w-full max-w-lg rounded-[2.5rem] shadow-2xl overflow-hidden border border-slate-200">
                {/* HEADER */}
                <div className="bg-purple-50/50 px-8 pt-10 pb-8 relative text-left">
                    <button
                        onClick={onClose}
                        className="absolute top-6 right-6 p-2 hover:bg-white rounded-full transition-all text-slate-400 shadow-sm"
                    >
                        <X size={20} />
                    </button>
                    <div className="flex items-center gap-5">
                        <div className="w-20 h-20 bg-white rounded-3xl border border-slate-200 shadow-sm flex items-center justify-center text-purple-500">
                            <ShieldCheck size={40} />
                        </div>
                        <div>
                            <p className="text-[10px] font-black text-purple-400 uppercase tracking-widest italic mb-1">Systemlogg / Detaljer</p>
                            <h2 className="text-2xl font-black text-slate-900 italic tracking-tight uppercase leading-none">
                                {log.action.replace('_', ' ')}
                            </h2>
                        </div>
                    </div>
                </div>

                {/* INNEHÅLL */}
                <div className="p-8 space-y-8">
                    <div className="bg-slate-50 p-6 rounded-[2rem] border border-slate-100 relative text-left">
                        <Info size={16} className="absolute top-4 right-4 text-slate-300" />
                        <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic mb-2">Beskrivning</p>
                        <p className="text-slate-700 font-bold leading-relaxed">{log.description}</p>
                    </div>

                    <div className="grid grid-cols-2 gap-6">
                        <div className="space-y-1 text-left">
                            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Utförd av</p>
                            <div className="flex items-center gap-2 text-slate-700 font-bold text-sm">
                                <UserIcon size={16} className="text-purple-400" />
                                {log.performedByName}
                            </div>
                        </div>
                        <div className="space-y-1 text-left">
                            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Klinik</p>
                            <div className="flex items-center gap-2 text-slate-700 font-bold text-sm">
                                <MapPin size={16} className="text-purple-400" />
                                {log.clinicName || 'System'}
                            </div>
                        </div>
                        <div className="space-y-1 text-left">
                            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Patient</p>
                            <div className="flex items-center gap-2 text-amber-700 font-bold text-sm">
                                <Activity size={16} className="text-amber-500" />
                                {log.petName || 'N/A'}
                            </div>
                        </div>
                        <div className="space-y-1 text-left">
                            <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Tidpunkt</p>
                            <div className="flex items-center gap-2 text-slate-700 font-bold text-sm">
                                <Clock size={16} className="text-purple-400" />
                                {fullDate}
                            </div>
                        </div>
                    </div>

                    <div className="pt-6 border-t border-slate-100 flex justify-between items-center">
                        <div className="flex items-center gap-3 text-left">
                            <div className="p-2 bg-slate-50 rounded-xl text-slate-400"><Hash size={18} /></div>
                            <div>
                                <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest italic">Referens-ID</p>
                                <p className="font-mono text-xs font-bold text-slate-600">{log.recordId}</p>
                            </div>
                        </div>
                        <button
                            onClick={onClose}
                            className="px-8 py-3 bg-slate-900 text-white text-[10px] font-black uppercase tracking-widest rounded-xl hover:bg-purple-600 transition-all italic"
                        >
                            Stäng
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

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
                                                    {log.action.replace('_', ' ')}
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

            {/* MODAL RENDER */}
            <LogDetailsModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                log={selectedLog}
            />
        </div>
    );
};

export default AuditLogView;