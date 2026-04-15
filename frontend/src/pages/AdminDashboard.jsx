import React from 'react';

const AdminDashboard = () => {
    // I framtiden kommer dessa siffror från useEffect + API-anrop
    const stats = [
        { label: 'Registrerade Kliniker', value: '12', border: false },
        { label: 'Totala Användare', value: '1,402', border: true },
        { label: 'Aktiva Ärenden', value: '89', border: false },
    ];

    return (
        <div className="space-y-8 animate-in fade-in duration-500">
            {/* Header */}
            <div>
                <h1 className="text-3xl font-bold text-slate-900">Systemöversikt</h1>
                <p className="text-slate-500">Global kontroll för VET1177.</p>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {stats.map((stat, index) => (
                    <div
                        key={index}
                        className={`bg-white p-6 rounded-3xl border border-slate-200 shadow-sm ${
                            stat.border ? 'border-l-4 border-l-emerald-500' : ''
                        }`}
                    >
                        <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">
                            {stat.label}
                        </p>
                        <p className="text-4xl font-bold text-slate-900 mt-3 font-mono tracking-tighter">
                            {stat.value}
                        </p>
                    </div>
                ))}
            </div>

            {/* Klinik-sektion */}
            <section className="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
                <div className="p-6 border-b border-slate-100 flex justify-between items-center bg-slate-50/50">
                    <h2 className="text-lg font-bold text-slate-800 italic uppercase tracking-tighter">
                        Hantering av Kliniker
                    </h2>
                    <button className="bg-slate-900 text-white px-4 py-2 rounded-xl text-xs font-bold hover:bg-slate-800 transition shadow-lg active:scale-95">
                        + Ny Klinik
                    </button>
                </div>

                <div className="overflow-x-auto text-sm font-medium">
                    <table className="w-full text-left">
                        <thead className="bg-slate-50">
                        <tr>
                            <th className="px-6 py-4 text-[10px] font-bold uppercase text-slate-400">Kliniknamn</th>
                            <th className="px-6 py-4 text-[10px] font-bold uppercase text-slate-400">Adress</th>
                            <th className="px-6 py-4 text-[10px] font-bold uppercase text-slate-400">Telefon</th>
                            <th className="px-6 py-4"></th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 italic">
                        {/* Här mappar vi sedan ut kliniker från backend */}
                        <tr className="hover:bg-slate-50/30 transition">
                            <td className="px-6 py-4 font-bold text-slate-900 not-italic">City-Veterinären</td>
                            <td className="px-6 py-4 text-slate-500 tracking-tight">Storgatan 12, Stockholm</td>
                            <td className="px-6 py-4 text-slate-500">08-123 45 67</td>
                            <td className="px-6 py-4 text-right space-x-3">
                                <button className="text-xs font-bold text-emerald-600 hover:underline">Redigera</button>
                                <button className="text-xs font-bold text-red-400 hover:text-red-600">Radera</button>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    );
};

export default AdminDashboard;