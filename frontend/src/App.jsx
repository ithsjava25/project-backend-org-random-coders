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

    // Hämta användare från token vid start
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
        setCurrentView('login');
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
            case 'dashboard':
            case 'my-pets':
            case 'my-cases':
            default:
                return (
                    <OwnerDashboard
                        userName={currentUser?.name}
                        pets={myPets}
                        records={myRecords}
                        viewMode={currentView} // Skickar med vilken vy som ska visas
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
            case 'add-pet':
                return <PetForm onCancel={goBackToDashboard} onSave={async () => { await fetchData(); goBackToDashboard(); }} />
            // I App.jsx, leta upp case 'pet-detail'
            case 'pet-detail':
                const filteredRecords = myRecords.filter(r => {
                    // Metod 1: Kolla ID (mest säkert om det finns)
                    const recordPetId = r.petId || (r.pet && r.pet.id);
                    if (recordPetId && selectedPet?.id && String(recordPetId) === String(selectedPet.id)) {
                        return true;
                    }

                    // Metod 2: Fallback - Kolla på namnet (eftersom vi vet att petName finns i myRecords)
                    // Vi jämför namnen och gör dem små bokstäver för att vara säkra
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
                            // Hitta djuret i listan och byt vy
                            const pet = myPets.find(p => p.id === petId);
                            if (pet) {
                                setSelectedPet(pet);
                                setCurrentView('pet-detail');
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