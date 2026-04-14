import React from 'react';

const Layout = ({ children, userRole, userName }) => {
    return (
        <div className="min-h-full flex">
            {/* SIDEBAR */}
            <div className="hidden md:flex md:w-64 md:flex-col md:fixed md:inset-y-0 bg-vet-navy shadow-2xl">
                <div className="flex flex-col flex-grow pt-6 pb-4 overflow-y-auto">

                    {/* LOGO */}
                    <div className="flex items-center px-6 mb-8">
                        <span className="text-2xl font-bold text-white tracking-tight italic">
                            Vet<span className="text-vet-accent">1177</span>
                        </span>
                        <span className="ml-2 px-1.5 py-0.5 rounded bg-vet-admin text-[10px] text-white font-bold uppercase tracking-tighter border border-white/20">
                            Portal
                        </span>
                    </div>

                    <nav className="flex-1 px-3 space-y-1">
                        {/* GEMENSAMMA LÄNKAR */}
                        <a href="#" className="flex items-center px-4 py-3 text-sm font-semibold rounded-lg bg-white/10 text-white border-l-4 border-vet-accent shadow-inner">
                            Hem
                        </a>

                        {/* PATIENT-SEKTION (Visas för alla djurägare/vets) */}
                        {(userRole === 'USER' || userRole === 'VET') && (
                            <div className="mt-6 pt-4 border-t border-white/5">
                                <p className="px-4 text-[10px] font-bold text-slate-500 uppercase tracking-[0.2em] mb-2">Patientportal</p>
                                <a href="#" className="flex items-center px-4 py-2 text-sm font-medium rounded-lg text-slate-300 hover:bg-white/5 hover:text-white transition group">
                                    Mina djur
                                </a>
                            </div>
                        )}

                        {/* KLINIK-SEKTION (Endast Veterinär) */}
                        {userRole === 'VET' && (
                            <div className="mt-6 pt-4 border-t border-white/5">
                                <p className="px-4 text-[10px] font-bold text-slate-500 uppercase tracking-[0.2em] mb-2">Klinikarbete</p>
                                <a href="#" className="flex items-center px-4 py-2 text-sm font-medium rounded-lg text-slate-300 hover:bg-white/5 hover:text-white transition group">
                                    Mina Patienter
                                </a>
                            </div>
                        )}

                        {/* ADMIN-SEKTION (Endast Admin) */}
                        {userRole === 'ADMIN' && (
                            <div className="mt-6 pt-4 border-t border-white/5">
                                <p className="px-4 text-[10px] font-bold text-slate-500 uppercase tracking-[0.2em] mb-2">Systemkontroll</p>
                                <a href="#" className="flex items-center px-4 py-2 text-sm font-medium rounded-lg text-slate-300 hover:bg-white/5 hover:text-white transition group">
                                    Användarlista
                                </a>
                            </div>
                        )}
                    </nav>

                    {/* USER INFO PANEL */}
                    <div className="px-6 py-6 border-t border-white/10 mt-auto bg-black/10">
                        <p className="text-[10px] text-slate-400 font-bold uppercase tracking-widest leading-none">Inloggad som</p>
                        <p className="text-sm font-bold text-white mt-2 leading-none">{userName}</p>
                        <div className="mt-2">
                            <span className="inline-flex items-center px-2 py-0.5 rounded text-[9px] font-black uppercase tracking-widest bg-blue-500/20 text-blue-300 border border-blue-500/30 italic">
                                {userRole}
                            </span>
                        </div>
                        <button className="text-left text-[10px] text-vet-accent hover:text-white uppercase font-black tracking-tighter mt-4 transition italic">
                            Logga ut säkert →
                        </button>
                    </div>
                </div>
            </div>

            {/* MAIN CONTENT AREA */}
            <div className="md:pl-64 flex flex-col flex-1">
                <main className="py-10 px-8 lg:px-12 max-w-7xl w-full">
                    {children}
                </main>
            </div>
        </div>
    );
};

export default Layout;