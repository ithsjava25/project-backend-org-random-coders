// src/utils/statusHelper.js
export const STATUS_MAP = {
    'OPEN': { label: 'Inskickat', color: 'bg-blue-50 text-blue-700 border-blue-100' },
    'IN_PROGRESS': { label: 'Under behandling', color: 'bg-yellow-50 text-yellow-700 border-yellow-100' },
    'AWAITING_INFO': { label: 'Väntar på svar', color: 'bg-purple-50 text-purple-700 border-purple-100' },
    'CLOSED': { label: 'Avslutat', color: 'bg-slate-50 text-slate-500 border-slate-100' }
};