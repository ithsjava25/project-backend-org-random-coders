import React, { useState, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode'; // Importera dekodaren
import Layout from './components/Layout';
import OwnerDashboard from './pages/OwnerDashboard';
import PetForm from './components/PetForm';
import PetDetail from './pages/PetDetail';
import CreateCase from './pages/CreateCase';
import CaseDetail from './pages/CaseDetail';
import Login from './pages/Login'; // Vi skapar denna i nästa steg
import { petService, medicalRecordService } from './services/api';

function App() {
    const [currentView, setCurrentView] = useState('dashboard');
    const [selectedPet, setSelectedPet] = useState(null);
    const [selectedRecord, setSelectedRecord] = useState(null);
    const [myPets, setMyPets] = useState([]);
    const [myRecords, setMyRecords] = useState([]);
    const [loading, setLoading] = useState(true);

    // NYTT: State för den inloggade användaren
    const [currentUser, setCurrentUser] = useState(null);

    useEffect(() => {
        const token = localStorage.getItem('token');

        if (token) {
            try {
                const decoded = jwtDecode(token);
                // Mappa data från JWT (userId och role kommer från din JwtService.java)
                setCurrentUser({
                    id: decoded.userId,
                    role: decoded.role,
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
        setCurrentView('login');
    };

    const goBackToDashboard = () => {
        setSelectedPet(null);
        setSelectedRecord(null);
        setCurrentView('dashboard');
    };

    // Rendera vyer baserat på inloggningsstatus och roll
    const renderContent = () => {
        if (!currentUser) {
            return <Login onLoginSuccess={() => window.location.reload()} />;
        }

        if (loading) return (
            <div className="flex justify-center p-20 italic text-slate-400">
                Ansluter till kliniken...
            </div>
        );

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
                return <PetForm onCancel={goBackToDashboard} onSave={goBackToDashboard} />;
            case 'pet-detail':
                return <PetDetail pet={selectedPet} onBack={goBackToDashboard} />;
            case 'create-case':
                return <CreateCase pets={myPets} existingCase={selectedRecord} onCancel={goBackToDashboard} onSave={goBackToDashboard} />;
            case 'case-detail':
                return (
                    <CaseDetail
                        caseData={selectedRecord}
                        currentUser={currentUser} // Skicka med inloggad användare till detaljvyn
                        onBack={goBackToDashboard}
                    />
                );
        }
    };

    return (
        <Layout
            userName={currentUser?.email || "Gäst"}
            userRole={currentUser?.role === 'ROLE_VET' ? 'Veterinär' : 'Djurägare'}
            onLogout={handleLogout}
        >
            <div className="container mx-auto px-4 py-8">
                {renderContent()}
            </div>
        </Layout>
    );
}

export default App;