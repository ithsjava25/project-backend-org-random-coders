import React, { useState, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode';
import Layout from './components/Layout';
import OwnerDashboard from './pages/OwnerDashboard';
import PetForm from './components/PetForm';
import PetDetail from './pages/PetDetail';
import CreateCase from './pages/CreateCase';
import CaseDetail from './pages/CaseDetail';
import AdminDashboard from './pages/AdminDashboard'; // Ny import
import Login from './pages/Login';
import Register from './pages/Register'; // Ny import
import { petService, medicalRecordService } from './services/api';

function App() {
    const [currentView, setCurrentView] = useState('dashboard');
    const [isRegistering, setIsRegistering] = useState(false); // Ny state för att växla vy
    const [selectedPet, setSelectedPet] = useState(null);
    const [selectedRecord, setSelectedRecord] = useState(null);
    const [myPets, setMyPets] = useState([]);
    const [myRecords, setMyRecords] = useState([]);
    const [loading, setLoading] = useState(true);
    const [currentUser, setCurrentUser] = useState(null);

    useEffect(() => {
        const token = localStorage.getItem('token');

        if (token) {
            try {
                const decoded = jwtDecode(token);
                setCurrentUser({
                    id: decoded.userId,
                    role: decoded.role, // Kommer vara ROLE_OWNER, ROLE_VET, etc.
                    email: decoded.sub
                });
                fetchData();
            } catch (error) {
                console.error("Ogiltig token", error);
                handleLogout();
            }
        } else {
            setLoading(false);
        }
    }, []);

    const fetchData = async () => {
        try {
            setLoading(true);
            const [petRes, recordRes] = await Promise.all([
                petService.getAllPets(),
                medicalRecordService.getMyRecords()
            ]);
            setMyPets(petRes.data);
            setMyRecords(recordRes.data);
        } catch (error) {
            console.error("Kunde inte hämta data:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('token');
        setCurrentUser(null);
        setCurrentView('dashboard');
        setIsRegistering(false);
    };

    const goBackToDashboard = () => {
        setSelectedPet(null);
        setSelectedRecord(null);
        setCurrentView('dashboard');
    };

    const renderAuthenticatedContent = () => {
        if (loading) return (
            <div className="flex flex-col items-center justify-center p-20 text-slate-400">
                <div className="w-8 h-8 border-4 border-slate-200 border-t-blue-500 rounded-full animate-spin mb-4"></div>
                <p className="italic">Hämtar din journal...</p>
            </div>
        );

        // Om rollen är ADMIN, visa Admin-vyn direkt
        if (currentUser?.role === 'ROLE_ADMIN') {
            return <AdminDashboard />;
        }

        switch (currentView) {
            case 'dashboard':
            default:
                return (
                    <OwnerDashboard
                        pets={myPets}
                        records={myRecords}
                        onAddPet={() => setCurrentView('add-pet')}
                        onPetClick={(pet) => { setSelectedPet(pet); setCurrentView('pet-detail'); }}
                        onRegisterCase={() => { setSelectedRecord(null); setCurrentView('create-case'); }}
                        onCaseClick={async (record) => {
                            const res = await medicalRecordService.getRecordById(record.id);
                            setSelectedRecord(res.data);
                            setCurrentView('case-detail');
                        }}
                    />
                );
            case 'add-pet':
                return <PetForm onCancel={goBackToDashboard} onSave={fetchData} />;
            case 'pet-detail':
                return <PetDetail pet={selectedPet} onBack={goBackToDashboard} />;
            case 'create-case':
                return <CreateCase pets={myPets} existingCase={selectedRecord} onCancel={goBackToDashboard} onSave={fetchData} />;
            case 'case-detail':
                return (
                    <CaseDetail
                        caseData={selectedRecord}
                        currentUser={currentUser}
                        onBack={goBackToDashboard}
                    />
                );
        }
    };

    // LOGIK FÖR HELSKÄRM (Login / Register)
    if (!currentUser) {
        return (
            <div className="min-h-screen bg-slate-50 flex items-center justify-center p-6">
                {isRegistering ? (
                    <Register onSwitchToLogin={() => setIsRegistering(false)} />
                ) : (
                    <Login
                        onLoginSuccess={() => window.location.reload()}
                        onSwitchToRegister={() => setIsRegistering(true)}
                    />
                )}
            </div>
        );
    }

    // LOGIK FÖR INLOGGAT LÄGE
    return (
        <Layout
            userName={currentUser?.email || "Användare"}
            userRole={
                currentUser?.role === 'ROLE_ADMIN' ? 'Administratör' :
                    currentUser?.role === 'ROLE_VET' ? 'Veterinär' : 'Djurägare'
            }
            onLogout={handleLogout}
        >
            <div className="container mx-auto px-4 py-8">
                {renderAuthenticatedContent()}
            </div>
        </Layout>
    );
}

export default App;