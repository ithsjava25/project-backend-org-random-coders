import React from 'react';
import {
    PawPrint,
    Home,
    ClipboardList,
    Users,
    Stethoscope,
    LogOut,
    PlusCircle
} from 'lucide-react';

const Layout = ({ children, userRole, userName, onLogout, onNavigate, currentView }) => {


    const getLinkStyle = (viewNames) => {

        const isActive = Array.isArray(viewNames)
            ? viewNames.includes(currentView)
            : currentView === viewNames;

        const baseStyle = "flex items-center gap-3 px-4 py-2.5 text-sm font-medium rounded-lg transition group w-full ";

        return isActive
            ? baseStyle + "bg-white/10 text-white border-l-4 border-vet-accent shadow-inner"
            : baseStyle + "text-slate-300 hover:bg-white/5 hover:text-white";
    };

    return (
        <div className="min-h-screen flex bg-slate-50">
            {/* SIDEBAR */}
            <div className="hidden md:flex md:w-64 md:flex-col md:fixed md:inset-y-0 bg-vet-navy shadow-2xl">
                <div className="flex flex-col flex-grow pt-6 pb-4 overflow-y-auto">

                    {/* LOGO */}
                    <div className="flex items-center gap-3 px-6 mb-10">
                        <div className="bg-vet-accent p-2 rounded-lg shadow-lg shadow-vet-accent/20 rotate-12">
                            <PawPrint size={20} className="text-white" />
                        </div>
                        <div>
                            <h1 className="text-xl font-black text-white tracking-tighter italic leading-none">
                                VET<span className="text-vet-accent">1177</span>
                            </h1>
                            <p className="text-[8px] text-slate-400 uppercase font-bold tracking-[0.2em] mt-1">
                                Digital djurvård
                            </p>
                        </div>
                    </div>

                    <nav className="flex-1 px-3 space-y-1">
                        {/* HEM */}
                        <button
                            onClick={() => onNavigate('dashboard')}
                            className={getLinkStyle('dashboard')}
                        >
                            <Home size={18} className={currentView === 'dashboard' ? "text-vet-accent" : "group-hover:text-vet-accent"} />
                            Hem
                        </button>

                        {/* PATIENT-SEKTION (Djurägare & Vets) */}
                        {(userRole?.includes('OWNER') || userRole?.includes('VET')) && (
                            <div className="mt-8 pt-4">
                                <p className="px-4 text-[10px] font-bold text-slate-500 uppercase tracking-[0.2em] mb-3">
                                    Patientportal
                                </p>
                                <div className="space-y-1">
                                    <button
                                        onClick={() => onNavigate('my-pets')}
                                        className={getLinkStyle(['my-pets', 'pet-detail'])}
                                    >
                                        <PawPrint size={18} className={(currentView === 'my-pets' || currentView === 'pet-detail') ? "text-vet-accent" : "group-hover:text-vet-accent"} />
                                        Mina djur
                                    </button>
                                    <button
                                        onClick={() => onNavigate('my-cases')}
                                        className={getLinkStyle(['my-cases', 'case-detail'])}
                                    >
                                        <ClipboardList size={18} className={(currentView === 'my-cases' || currentView === 'case-detail') ? "text-vet-accent" : "group-hover:text-vet-accent"} />
                                        Mina ärenden
                                    </button>
                                </div>
                            </div>
                        )}

                        {/* KLINIK-SEKTION (Endast Veterinär) */}
                        {userRole === 'ROLE_VET' && (
                            <div className="mt-8 pt-4 border-t border-white/5">
                                <p className="px-4 text-[10px] font-bold text-slate-500 uppercase tracking-[0.2em] mb-3">Klinikarbete</p>
                                <button onClick={() => onNavigate('vet-dashboard')} className={getLinkStyle('vet-dashboard')}>
                                    <Stethoscope size={18} />
                                    Mina Patienter
                                </button>
                            </div>
                        )}

                        {/* ADMIN-SEKTION */}
                        {userRole === 'ROLE_ADMIN' && (
                            <div className="mt-8 pt-4 border-t border-white/5">
                                <p className="px-4 text-[10px] font-bold text-slate-500 uppercase tracking-[0.2em] mb-3">Systemkontroll</p>
                                <button onClick={() => onNavigate('admin-dashboard')} className={getLinkStyle('admin-dashboard')}>
                                    <Users size={18} />
                                    Användarlista
                                </button>
                            </div>
                        )}
                    </nav>

                    {/* USER INFO PANEL */}
                    <div className="mx-3 mb-4 p-4 rounded-2xl bg-black/20 border border-white/5">
                        <p className="text-[10px] text-slate-500 font-bold uppercase tracking-widest leading-none">Inloggad som</p>
                        <p className="text-sm font-bold text-white mt-2 leading-none truncate">{userName}</p>

                        <div className="mt-3 flex items-center justify-between">
                            <span className="inline-flex items-center px-2 py-0.5 rounded text-[9px] font-black uppercase tracking-widest bg-vet-accent/10 text-vet-accent border border-vet-accent/20 italic">
                                {userRole === 'ROLE_ADMIN' ? 'Administratör' :
                                    userRole === 'ROLE_VET' ? 'Veterinär' :
                                        userRole === 'ROLE_OWNER' ? 'Djurägare' : 'Användare'}
                            </span>
                            <button
                                onClick={onLogout}
                                className="p-1.5 text-slate-400 hover:text-red-400 hover:bg-red-400/10 rounded-lg transition-all"
                            >
                                <LogOut size={16} />
                            </button>
                        </div>
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