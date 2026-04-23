import React, { useState, useEffect, useCallback } from 'react';
import { userService, clinicService, activityService } from '../services/api';
import UserModal from '../components/admin/UserModal';
import ClinicModal from '../components/admin/ClinicModal';
import AuditLogView from '../components/admin/AuditLogView'; // Import av den nya vyn
import {
    Users,
    Hospital,
    ShieldAlert,
    Edit2,
    Trash2,
    PlusCircle,
    Loader2,
    History,
    Settings
} from 'lucide-react';

const AdminDashboard = ({ userName, initialTab = 'USERS' }) => {
    // Data State
    const [users, setUsers] = useState([]);
    const [clinics, setClinics] = useState([]);
    const [activities, setActivities] = useState([]); // Nytt state för loggar
    const [loading, setLoading] = useState(true);

    // UI State
    const [activeTab, setActiveTab] = useState('USERS');
    const [isUserModalOpen, setIsUserModalOpen] = useState(false);
    const [isClinicModalOpen, setIsClinicModalOpen] = useState(false);
    const [selectedItem, setSelectedItem] = useState(null);

    // Avgör om vi ska visa statistik (Översiktsvyn från Hem-knappen)
    const showStats = initialTab === 'OVERVIEW';

    // Synka fliken när vi navigerar från sidomenyn
    useEffect(() => {
        if (initialTab === 'OVERVIEW') {
            setActiveTab('USERS');
        } else {
            setActiveTab(initialTab);
        }
    }, [initialTab]);

    const fetchData = useCallback(async () => {
        setLoading(true);
        try {
            // Här hämtar vi användare, kliniker och loggar parallellt
            // OBS: Om du inte skapat en global endpoint än kan logRes.data vara tom
            const [userRes, clinicRes, logRes] = await Promise.all([
                userService.getAll(),
                clinicService.getAll(),
                activityService.getAll()
            ]);

            setUsers(userRes.data);
            setClinics(clinicRes.data);
            setActivities(logRes.data);

        } catch (err) {
            console.error("Synkroniseringsfel:", err);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    const openCreateModal = () => {
        setSelectedItem(null);
        if (activeTab === 'USERS') setIsUserModalOpen(true);
        else setIsClinicModalOpen(true);
    };

    const openEditModal = (item) => {
        setSelectedItem(item);
        if (activeTab === 'USERS') setIsUserModalOpen(true);
        else setIsClinicModalOpen(true);
    };

    const handleSaveUser = async (userData) => {
        const response = selectedItem
            ? await userService.update(selectedItem.id, userData)
            : await userService.create(userData);

        fetchData();
        return response;
    };

    const handleSaveClinic = async (clinicData) => {
        selectedItem ? await clinicService.update(selectedItem.id, clinicData) : await clinicService.create(clinicData);
        fetchData();
    };

    const handleDeleteClinic = async (clinicId, name) => {
        if (window.confirm(`Vill du verkligen radera kliniken ${name}?`)) {
            try {
                await clinicService.delete(clinicId);
                fetchData();
            } catch (err) {
                alert("Kunde inte radera kliniken. Den kan ha kopplade veterinärer.");
            }
        }
    };

    const handleDeleteUser = async (userId, name) => {
        if (window.confirm(`Radera ${name}?`)) {
            try { await userService.delete(userId); fetchData(); } catch (err) { alert("Kunde inte radera."); }
        }
    };

    const stats = {
        totalUsers: users.length,
        totalClinics: clinics.length,
        admins: users.filter(u => u.role?.includes('ADMIN')).length
    };

    return (
        <div className="animate-in fade-in slide-in-from-bottom-4 duration-500 pb-20">
            {/* HEADER */}
            <header className="mb-10 flex justify-between items-end">
                <div>
                    <h1 className="text-4xl font-extrabold text-slate-900 italic tracking-tight flex items-center gap-3">
                        {showStats ? <Settings className="text-blue-500" size={32} /> :
                            activeTab === 'USERS' ? <Users className="text-blue-500" size={32} /> :
                                activeTab === 'CLINICS' ? <Hospital className="text-emerald-500" size={32} /> :
                                    <History className="text-purple-500" size={32} />}

                        {showStats ? "Systemkontroll" :
                            activeTab === 'USERS' ? "Användarregister" :
                                activeTab === 'CLINICS' ? "Kliniköversikt" : "Aktivitetslogg"}
                    </h1>
                    <p className="text-slate-500 mt-2 font-semibold uppercase tracking-wider text-[10px] italic">
                        Admin: {userName} • {showStats ? "Övergripande status" : `Hantering av ${activeTab.toLowerCase()}`}
                    </p>
                </div>

                {/* SKAPA-KNAPP: Visas inte om vi är i Audit Log */}
                {!showStats && activeTab !== 'LOGS' && (
                    <button
                        onClick={openCreateModal}
                        className="flex items-center gap-2 px-8 py-4 bg-slate-900 text-white text-[10px] font-black uppercase tracking-widest rounded-2xl hover:bg-blue-700 transition-all shadow-xl active:scale-95"
                    >
                        <PlusCircle size={18} />
                        Ny {activeTab === 'USERS' ? 'Användare' : 'Klinik'}
                    </button>
                )}
            </header>

            {/* STATS CARDS */}
            {showStats && (
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
                    <div className="bg-white p-6 rounded-[2rem] border border-slate-200 shadow-sm">
                        <div className="flex items-center gap-4">
                            <div className="bg-blue-50 p-3 rounded-2xl text-blue-600"><Users size={24} /></div>
                            <div>
                                <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest">Användare</p>
                                <p className="text-3xl font-black text-slate-900">{stats.totalUsers}</p>
                            </div>
                        </div>
                    </div>
                    <div className="bg-white p-6 rounded-[2rem] border border-slate-200 shadow-sm">
                        <div className="flex items-center gap-4">
                            <div className="bg-emerald-50 p-3 rounded-2xl text-emerald-600"><Hospital size={24} /></div>
                            <div>
                                <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest">Kliniker</p>
                                <p className="text-3xl font-black text-slate-900">{stats.totalClinics}</p>
                            </div>
                        </div>
                    </div>
                    <div className="bg-white p-6 rounded-[2rem] border border-slate-200 shadow-sm border-b-4 border-b-red-400">
                        <div className="flex items-center gap-4">
                            <div className="bg-red-50 p-3 rounded-2xl text-red-600"><ShieldAlert size={24} /></div>
                            <div>
                                <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest">Administratörer</p>
                                <p className="text-3xl font-black text-slate-900">{stats.admins}</p>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* TABELL/LOGG-OMRÅDE */}
            <div className="bg-white border border-slate-200 rounded-[2.5rem] overflow-hidden shadow-sm">
                {/* FLIKAR + KNAPP */}
                {showStats && (
                    <div className="flex justify-between items-center bg-slate-50/50 p-4 border-b border-slate-100">
                        <div className="flex gap-2">
                            <button
                                onClick={() => setActiveTab('USERS')}
                                className={`px-6 py-2 rounded-xl text-[10px] font-black uppercase tracking-widest transition-all ${
                                    activeTab === 'USERS' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-400'
                                }`}
                            >
                                Användare
                            </button>
                            <button
                                onClick={() => setActiveTab('CLINICS')}
                                className={`px-6 py-2 rounded-xl text-[10px] font-black uppercase tracking-widest transition-all ${
                                    activeTab === 'CLINICS' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-400'
                                }`}
                            >
                                Kliniker
                            </button>
                            <button
                                onClick={() => setActiveTab('LOGS')}
                                className={`px-6 py-2 rounded-xl text-[10px] font-black uppercase tracking-widest transition-all ${
                                    activeTab === 'LOGS' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-400'
                                }`}
                            >
                                Aktivitetslogg
                            </button>
                        </div>

                        {activeTab !== 'LOGS' && (
                            <button
                                onClick={openCreateModal}
                                className="flex items-center gap-2 px-5 py-2 bg-slate-900 text-white text-[10px] font-black uppercase rounded-xl hover:bg-blue-700 transition-all"
                            >
                                <PlusCircle size={14} /> Ny {activeTab === 'USERS' ? 'Användare' : 'Klinik'}
                            </button>
                        )}
                    </div>
                )}

                {/* INNEHÅLL BASERAT PÅ FLIK */}
                {activeTab === 'LOGS' ? (
                    <AuditLogView logs={activities} loading={loading} />
                ) : (
                    <table className="w-full text-left">
                        <thead className="bg-slate-50/20 border-b border-slate-100">
                        <tr>
                            <th className="px-8 py-5 text-[10px] font-black text-slate-400 uppercase italic">Information</th>
                            <th className="px-8 py-5 text-[10px] font-black text-slate-400 uppercase italic">Detaljer</th>
                            <th className="px-8 py-5 text-[10px] font-black text-slate-400 uppercase italic text-right">Åtgärder</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-50">
                        {loading ? (
                            <tr><td colSpan="3" className="p-20 text-center"><Loader2 className="animate-spin mx-auto text-blue-500" /></td></tr>
                        ) : activeTab === 'USERS' ? (
                            users.map(user => (
                                <tr key={user.id} className="hover:bg-slate-50/80 group">
                                    <td className="px-8 py-5">
                                        <div className="font-bold text-slate-900 italic text-lg">{user.name}</div>
                                        <div className="text-[10px] text-slate-400 font-bold uppercase">{user.email}</div>
                                    </td>
                                    <td className="px-8 py-5">
                                            <span className={`text-[9px] font-black px-3 py-1 rounded-full border uppercase ${
                                                user.role?.includes('ADMIN') ? 'bg-red-50 text-red-600 border-red-100' : 'bg-blue-50 text-blue-600 border-blue-100'
                                            }`}>
                                                {user.role?.replace('ROLE_', '')}
                                            </span>
                                    </td>
                                    <td className="px-8 py-5 text-right">
                                        <div className="flex justify-end gap-2 opacity-0 group-hover:opacity-100">
                                            <button onClick={() => openEditModal(user)} className="p-2 text-slate-400 hover:text-slate-900 transition-all"><Edit2 size={16}/></button>
                                            <button onClick={() => handleDeleteUser(user.id, user.name)} className="p-2 text-slate-400 hover:text-red-600 transition-all"><Trash2 size={16}/></button>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        ) : (
                            clinics.map(clinic => (
                                <tr key={clinic.id} className="hover:bg-slate-50/80 group">
                                    <td className="px-8 py-5">
                                        <div className="font-bold text-slate-900 italic text-lg">{clinic.name}</div>
                                        <div className="text-[10px] text-slate-400 font-bold uppercase">{clinic.address}</div>
                                    </td>
                                    <td className="px-8 py-5 font-mono text-sm text-slate-600">{clinic.phoneNumber}</td>
                                    <td className="px-8 py-5 text-right">
                                        <div className="flex justify-end gap-2 opacity-0 group-hover:opacity-100">
                                            <button onClick={() => openEditModal(clinic)} className="p-2 text-slate-400 hover:text-slate-900 transition-all"><Edit2 size={16}/></button>
                                            <button onClick={() => handleDeleteClinic(clinic.id, clinic.name)} className="p-2 text-slate-400 hover:text-red-600 transition-all"><Trash2 size={16}/></button>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                )}
            </div>

            <UserModal isOpen={isUserModalOpen} onClose={() => setIsUserModalOpen(false)} onSave={handleSaveUser} initialData={selectedItem} clinics={clinics} />
            <ClinicModal isOpen={isClinicModalOpen} onClose={() => setIsClinicModalOpen(false)} onSave={handleSaveClinic} initialData={selectedItem} />
        </div>
    );
};

export default AdminDashboard;