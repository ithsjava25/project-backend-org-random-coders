// src/utils/statusHelper.js
//
// Gemensam mappning från backend-enum till svenska visningstexter.
// Används överallt där status eller aktivitetsloggar visas i UI:t så att
// texterna förblir konsekventa. Motsvarighet finns i backend
// (RecordStatus.displayLabel()) för loggrader som skrivs till databasen.

export const STATUS_MAP = {
    'OPEN':          { label: 'Öppen',            color: 'bg-blue-50 text-blue-700 border-blue-100' },
    'IN_PROGRESS':   { label: 'Under behandling', color: 'bg-yellow-50 text-yellow-700 border-yellow-100' },
    'AWAITING_INFO': { label: 'Väntar på svar',   color: 'bg-purple-50 text-purple-700 border-purple-100' },
    'CLOSED':        { label: 'Avslutad',         color: 'bg-slate-50 text-slate-500 border-slate-100' }
};

// Statusar som VET kan välja i dropdown. CLOSED utesluts — stängning sker
// via "Slutför & Stäng"-modalen för att tvinga fram en slutnotering.
export const ACTIVE_STATUS_KEYS = ['OPEN', 'IN_PROGRESS', 'AWAITING_INFO'];

// Svensk label för en status, med fallback till raw-värdet om enum-värdet
// inte känns igen (t.ex. nytt värde tillagt i backend men inte här).
export const getStatusLabel = (status) => STATUS_MAP[status]?.label ?? status;

// Mappning för ActivityType-enum → svensk rubrik.
// Används av AuditLogView i admin-vyn där log.action visas direkt.
export const ACTION_LABELS = {
    'CASE_CREATED':   'Ärende skapat',
    'STATUS_CHANGED': 'Status ändrad',
    'COMMENT_ADDED':  'Kommentar tillagd',
    'ASSIGNED':       'Veterinär tilldelad',
    'UPDATED':        'Ärende uppdaterat'
};

export const getActionLabel = (action) => ACTION_LABELS[action] ?? action;
