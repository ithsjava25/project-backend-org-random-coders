import React from 'react';
import {
    FilePlus,
    RefreshCw,
    MessageSquare,
    UserPlus,
    Clock,
    User as UserIcon,
    AlertCircle
} from 'lucide-react';
import { getActionLabel } from '../../utils/statusHelper';

const AuditLogView = ({ logs = [], loading }) => {

    // Mappa actions till färger och ikoner
    const getActionStyle = (action) => {
        switch (action) {
            case 'CASE_CREATED': return { icon: <FilePlus size={16} />, color: 'text-emerald-500', bg: 'bg-emerald-50' };
            case 'STATUS_CHANGED': return { icon: <RefreshCw size={16} />, color: 'text-blue-500', bg: 'bg-blue-50' };
            case 'COMMENT_ADDED': return { icon: <MessageSquare size={16} />, color: 'text-purple-500', bg: 'bg-purple-50' };
            case 'ASSIGNED': return { icon: <UserPlus size={16} />, color: 'text-orange-500', bg: 'bg-orange-50' };
            default: return { icon: <AlertCircle size={16} />, color: 'text-slate-500', bg: 'bg-slate-50' };
        }
    };

    if (loading) return <div className="p-20 text-center italic text-slate-400">Laddar historik...</div>;
    if (logs.length === 0) return (
        <div className="p-20 text-center">
            <Clock className="mx-auto text-slate-200 mb-4" size={48} />
            <p className="text-slate-400 font-bold italic uppercase tracking-widest text-xs">Ingen aktivitet hittades</p>
        </div>
    );

    return (
        <div className="p-8 space-y-6">
            {logs.map((log) => {
                const style = getActionStyle(log.action);
                const date = new Date(log.createdAt).toLocaleString('sv-SE', {
                    month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
                });

                return (
                    <div key={log.id} className="relative flex gap-6 group">
                        {/* Timeline line */}
                        <div className="absolute left-6 top-10 bottom-[-24px] w-px bg-slate-100 group-last:hidden" />

                        {/* Icon */}
                        <div className={`relative z-10 w-12 h-12 rounded-2xl ${style.bg} ${style.color} flex items-center justify-center shadow-sm border border-white`}>
                            {style.icon}
                        </div>

                        {/* Content */}
                        <div className="flex-1 pb-8">
                            <div className="flex justify-between items-start mb-1">
                                <h4 className="font-black text-slate-900 italic tracking-tight uppercase text-sm">
                                    {getActionLabel(log.action)}
                                </h4>
                                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                                    {date}
                                </span>
                            </div>
                            <p className="text-slate-600 text-sm font-medium mb-2">{log.description}</p>

                            <div className="flex items-center gap-2">
                                <div className="flex items-center gap-1.5 px-3 py-1 bg-slate-50 rounded-full border border-slate-100">
                                    <UserIcon size={10} className="text-slate-400" />
                                    <span className="text-[10px] font-black text-slate-500 uppercase italic">
                                        {log.performedByName}
                                    </span>
                                </div>
                                <span className="text-[10px] text-slate-300 font-bold italic">ID: {log.recordId.substring(0, 8)}...</span>
                            </div>
                        </div>
                    </div>
                );
            })}
        </div>
    );
};

export default AuditLogView;