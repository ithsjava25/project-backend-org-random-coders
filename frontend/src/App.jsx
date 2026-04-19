import React, { useState, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode';
import Layout from './components/Layout';
import OwnerDashboard from './pages/OwnerDashboard';
import PetForm from './components/PetForm';
import PetDetail from './pages/PetDetail';
import CreateCase from './pages/CreateCase';
import CaseDetail from './pages/CaseDetail';
import AdminDashboard from './pages/AdminDashboard';
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

    // 1. Hämta användare från token vid start
    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            try {
                const decoded = jwtDecode(token);
                const user = {
                    id: decoded.userId || decoded.id,
                    role: decoded.role,
                    email: decoded.sub,
                    name: decoded.name
                };
                setCurrentUser(user);
                fetchInitialData();
            } catch (error) {
                console.error("Ogiltig token", error);
                handleLogout();
            }
        } else {
            setLoading(false);
        }
    }, []);

    // 2. NYTT: Lyssna på globala utloggnings-events från API-interceptor (CodeRabbit fix)
    useEffect(() => {
        const handleGlobalLogout = () => {
            handleLogout();
            // Vi ger användaren en förklaring till varför de blev utloggade
            alert("Din session har gått ut. Vänligen logga in igen.");
        };

        window.addEventListener('auth:logout', handleGlobalLogout);

        return () => {
            window.removeEventListener('auth:logout', handleGlobalLogout);
        };
    }, []);

    const fetchInitialData = async () => {
        await fetchData();
    };

    const fetchData = async () => {
        setLoading(true);
        try {
            const petRes = await petService.getAllPets();
            setMyPets(petRes.data);
        } catch (error) {
            console.error("Kunde inte hämta djur:", error);
            setMyPets([]);
        }

        try {
            const recordRes = await medicalRecordService.getMyRecords();
            setMyRecords(recordRes.data);
        } catch (error) {
            console.error("Kunde inte hämta journaler:", error);
            setMyRecords([]);
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        setCurrentUser(null);
        setCurrentView('dashboard'); // Reset till standardvy för inloggad profil
        setIsRegistering(false);
    };

    const goBackToDashboard = () => {
        setSelectedPet(null);
        setSelectedRecord(null);
        setCurrentView('dashboard');
    };

    const renderAuthenticatedContent = () => {
        if (loading && myPets.length === 0) return (
            <div className="flex flex-col items-center justify-center p-20 text-slate-400">
                <div className="w-8 h-8 border-4 border-slate-200 border-t-blue-500 rounded-full animate-spin mb-4"></div>
                <p className="italic">Laddar din information...</p>
            </div>
        );

        if (currentUser?.role === 'ROLE_ADMIN') {
            return <AdminDashboard />;
        }

        switch (currentView) {
            case 'add-pet':
                return <PetForm onCancel={goBackToDashboard} onSave={async () => { await fetchData(); goBackToDashboard(); }} />

            case 'pet-detail': {
                const filteredRecords = myRecords.filter(r => {
                    const recordPetId = r.petId || (r.pet && r.pet.id);
                    if (recordPetId && selectedPet?.id && String(recordPetId) === String(selectedPet.id)) {
                        return true;
                    }

                    if (r.petName && selectedPet?.name &&
                        r.petName.toLowerCase() === selectedPet.name.toLowerCase()) {
                        return true;
                    }

                    return false;
                });

                return (
                    <PetDetail
                        pet={selectedPet}
                        petRecords={filteredRecords}
                        onBack={goBackToDashboard}
                        onRegisterCase={(pet) => { setSelectedPet(pet); setCurrentView('create-case'); }}
                        onCaseClick={async (record) => {
                            try {
                                const res = await medicalRecordService.getRecordById(record.id);
                                setSelectedRecord(res.data);
                                setCurrentView('case-detail');
                            } catch (err) {
                                console.error("Kunde inte hämta journal", err);
                            }
                        }}
                    />
                );
            }

            case 'create-case':
                return <CreateCase pets={myPets} preSelectedPet={selectedPet} existingCase={selectedRecord} onCancel={goBackToDashboard} onSave={async () => { await fetchData(); goBackToDashboard(); }} />

            case 'case-detail':
                return (
                    <CaseDetail
                        caseData={selectedRecord}
                        currentUser={currentUser}
                        currentUserId={currentUser?.id}
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

            case 'vet-dashboard':
                return (
                    <div className="flex flex-col items-center justify-center p-12 bg-white rounded-xl border border-slate-200 shadow-sm text-center">
                        <div className="w-16 h-16 bg-blue-50 text-blue-600 rounded-full flex items-center justify-center mb-4">
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
                            </svg>
                        </div>
                        <h2 className="text-2xl font-bold text-slate-800">Veterinärportal</h2>
                        <p className="text-slate-500 mt-2 max-w-md">
                            Denna vy är under utveckling och kommer snart att innehålla verktyg för att hantera inkommande ärenden.
                        </p>
                        <button
                            onClick={() => setCurrentView('dashboard')}
                            className="mt-6 px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg transition-colors"
                        >
                            Tillbaka till min översikt
                        </button>
                    </div>
                );

            case 'dashboard':
            case 'my-pets':
            case 'my-cases':
            default:
                return (
                    <OwnerDashboard
                        userName={currentUser?.name}
                        pets={myPets}
                        records={myRecords}
                        viewMode={currentView}
                        onAddPet={() => setCurrentView('add-pet')}
                        onPetClick={(pet) => { setSelectedPet(pet); setCurrentView('pet-detail'); }}
                        onRegisterCase={() => { setSelectedRecord(null); setCurrentView('create-case'); }}
                        onCaseClick={async (record) => {
                            try {
                                const res = await medicalRecordService.getRecordById(record.id);
                                setSelectedRecord(res.data);
                                setCurrentView('case-detail');
                            } catch (err) {
                                alert("Kunde inte öppna journalen just nu.");
                            }
                        }}
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
            <div className="container mx-auto px-4 py-8">
                {renderAuthenticatedContent()}
            </div>
        </Layout>
    );
}

export default App;