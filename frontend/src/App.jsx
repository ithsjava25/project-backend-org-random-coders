import React, { useState, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode';
import Layout from './components/Layout';
import OwnerDashboard from './pages/OwnerDashboard';
import PetForm from './components/PetForm';
import PetDetail from './pages/PetDetail';
import CreateCase from './pages/CreateCase';
import CaseDetail from './pages/CaseDetail';
import AdminDashboard from './pages/AdminDashboard';
import VetDashboard from './pages/VetDashboard';
import Login from './pages/Login';
import Register from './pages/Register';
import { petService, medicalRecordService } from './services/api';

function App() {
    const [currentView, setCurrentView] = useState('dashboard');
    const [isRegistering, setIsRegistering] = useState(false);
    const [selectedPet, setSelectedPet] = useState(null);
    const [selectedRecord, setSelectedRecord] = useState(null);
    const [myPets, setMyPets] = useState([]);
    const [myRecords, setMyRecords] = useState([]);
    const [loading, setLoading] = useState(true);
    const [currentUser, setCurrentUser] = useState(null);

    // 1. Initialisering: Hämta användare från token vid start
    useEffect(() => {
        const initializeAuth = async () => {
            const token = localStorage.getItem('token') || sessionStorage.getItem('token');
            if (!token) {
                setLoading(false);
                return;
            }

            try {
                const decoded = jwtDecode(token);
                const user = {
                    id: decoded.userId || decoded.id,
                    role: decoded.role,
                    email: decoded.sub,
                    name: decoded.name,
                    clinicId: decoded.clinicId
                };

                setCurrentUser(user);

                // Styr vy baserat på roll
                if (user.role === 'ROLE_VET') {
                    setCurrentView('vet-dashboard');
                    setLoading(false);
                } else if (user.role === 'ROLE_ADMIN') {
                    setCurrentView('admin-dashboard');
                    setLoading(false);
                } else {
                    setCurrentView('dashboard');
                    await fetchData(user); // Endast för OWNER
                    setLoading(false);

                }
            } catch (error) {
                console.error("Ogiltig token vid start:", error);
                handleLogout();
                setLoading(false);
            }
        };

        initializeAuth();
    }, []);

    // 2. Global utloggnings-lyssnare
    useEffect(() => {
        const handleGlobalLogout = () => {
            handleLogout();
            alert("Din session har gått ut. Vänligen logga in igen.");
        };

        window.addEventListener('auth:logout', handleGlobalLogout);
        return () => window.removeEventListener('auth:logout', handleGlobalLogout);
    }, []);

    // 3. Datahämtning: Endast för OWNER-rollen
    const fetchData = async (user = currentUser) => {
        if (!user || user.role !== 'ROLE_OWNER') return;

        setLoading(true);
        try {
            const [petRes, recordRes] = await Promise.all([
                petService.getAllPets(),
                medicalRecordService.getMyRecords()
            ]);
            setMyPets(petRes.data);
            setMyRecords(recordRes.data);
        } catch (error) {
            console.error("Kunde inte hämta personlig data:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        sessionStorage.removeItem('token')
        setCurrentUser(null);
        setCurrentView('dashboard');
        setIsRegistering(false);
        setMyPets([]);
        setMyRecords([]);
        setSelectedPet(null);
        setSelectedRecord(null);
    };

    const goBackToDashboard = () => {
        setSelectedPet(null);
        setSelectedRecord(null);
        if (currentUser?.role === 'ROLE_VET') {
            setCurrentView('vet-dashboard');
        } else if (currentUser?.role === 'ROLE_ADMIN') {
            setCurrentView('admin-dashboard');
        } else {
            setCurrentView('dashboard');
        }
    };

    const renderAuthenticatedContent = () => {
        if (loading) return (
            <div className="flex flex-col items-center justify-center p-20 text-slate-400">
                <div className="w-8 h-8 border-4 border-slate-200 border-t-blue-500 rounded-full animate-spin mb-4"></div>
                <p className="italic text-sm font-bold uppercase tracking-widest">Synkroniserar data...</p>
            </div>
        );

        // Hjälpfunktion för att hämta och öppna en journal
        const handleCaseClick = async (record) => {
            try {
                // Vi hämtar den fullständiga journalen (inkl. beskrivning etc) från servern
                const res = await medicalRecordService.getRecordById(record.id);
                setSelectedRecord(res.data);
                setCurrentView('case-detail');
            } catch (err) {
                console.error("Kunde inte öppna journalen:", err);
                alert("Kunde inte öppna journalen. Kontrollera din anslutning.");
            }
        };

        // I din renderAuthenticatedContent i App.jsx
        if (currentUser?.role === 'ROLE_ADMIN') {
            // Här styr vi exakt vad dashboarden ska visa baserat på currentView
            let tabToOpen = 'OVERVIEW'; // Default

            if (currentView === 'admin-dashboard') {
                tabToOpen = 'OVERVIEW'; // Hem-knappen visar allt
            } else if (currentView === 'admin-users') {
                tabToOpen = 'USERS';    // Användarknappen döljer stats
            } else if (currentView === 'admin-clinics') {
                tabToOpen = 'CLINICS';  // Klinikknappen döljer stats
            } else if (currentView === 'admin-logs') {
                tabToOpen = 'LOGS';     // Direktlänk: döljer stats, visar loggar
            }

            return (
                <AdminDashboard
                    userName={currentUser.name}
                    initialTab={tabToOpen}
                />
            );
        }

        switch (currentView) {
            case 'vet-dashboard':
                return (
                    <VetDashboard
                        userName={currentUser?.name}
                        clinicId={currentUser?.clinicId}
                        currentUserId={currentUser?.id}
                        onCaseClick={handleCaseClick}
                    />
                );

            case 'my-assigned-cases':
                return (
                    <VetDashboard
                        userName={currentUser?.name}
                        clinicId={currentUser?.clinicId}
                        currentUserId={currentUser?.id}
                        isPersonalView={true}
                        onCaseClick={handleCaseClick}
                    />
                );

            case 'add-pet':
                return <PetForm onCancel={goBackToDashboard} onSave={async () => { await fetchData(); goBackToDashboard(); }} />

            case 'pet-detail':
                return (
                    <PetDetail
                        pet={selectedPet}
                        petRecords={myRecords.filter(r => (r.petId || r.pet?.id) === selectedPet?.id)}
                        onBack={goBackToDashboard}
                        onRegisterCase={(pet) => { setSelectedPet(pet); setCurrentView('create-case'); }}
                        onCaseClick={handleCaseClick}
                    />
                );

            case 'create-case':
                return <CreateCase pets={myPets} preSelectedPet={selectedPet} existingCase={selectedRecord} onCancel={goBackToDashboard} onSave={async () => { await fetchData(); goBackToDashboard(); }} />

            case 'case-detail':
                return (
                    <CaseDetail
                        caseData={selectedRecord}
                        currentUserId={currentUser?.id}
                        userRole={currentUser?.role} // VIKTIGT: Skickar med rollen för veterinär-panelen
                        onBack={goBackToDashboard}
                        onGoToPet={(petId) => {
                            const pet = myPets.find(p => p.id === petId);
                            if (pet) {
                                setSelectedPet(pet);
                                setCurrentView('pet-detail');
                            }
                        }}
                    />
                );

            case 'dashboard':
            case 'my-pets':
            case 'my-cases':
            default:
                // Om en veterinär hamnar på en odefinierad vy, skicka till klinikens dashboard
                if (currentUser?.role === 'ROLE_VET') {
                    return (
                        <VetDashboard
                            userName={currentUser?.name}
                            clinicId={currentUser?.clinicId}
                            currentUserId={currentUser?.id}
                            onCaseClick={handleCaseClick}
                        />
                    );
                }

                return (
                    <OwnerDashboard
                        userName={currentUser?.name}
                        pets={myPets}
                        records={myRecords}
                        viewMode={currentView}
                        onAddPet={() => setCurrentView('add-pet')}
                        onPetClick={(pet) => { setSelectedPet(pet); setCurrentView('pet-detail'); }}
                        onRegisterCase={() => { setSelectedRecord(null); setCurrentView('create-case'); }}
                        onCaseClick={handleCaseClick}
                    />
                );
        }
    };

    if (!currentUser) {
        return (
            <div className="min-h-screen bg-slate-50 flex items-center justify-center p-6">
                {isRegistering ? (
                    <Register onSwitchToLogin={() => setIsRegistering(false)} />
                ) : (
                    <Login onLoginSuccess={() => window.location.reload()} onSwitchToRegister={() => setIsRegistering(true)} />
                )}
            </div>
        );
    }

    return (
        <Layout
            userName={currentUser?.name || currentUser?.email || "Användare"}
            userRole={currentUser?.role}
            onLogout={handleLogout}
            onNavigate={(view) => setCurrentView(view)}
            currentView={currentView}
        >
            <div className="max-w-7xl mx-auto">
                {renderAuthenticatedContent()}
            </div>
        </Layout>
    );
}

export default App;