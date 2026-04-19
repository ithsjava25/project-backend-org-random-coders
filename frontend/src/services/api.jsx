import axios from 'axios';

// Skapa axios-instans med bas-konfiguration
const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    }
});

/**
 * REQUEST INTERCEPTOR
 * Bifogar JWT-token till alla anrop automatiskt
 */
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

/**
 * RESPONSE INTERCEPTOR
 * Hanterar globala fel som 401 (ogiltig token)
 */
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token');
            window.location.reload();
        }
        return Promise.reject(error);
    }
);

// --- SERVICES ---

export const authService = {
    login: (email, password) => api.post('/auth/login', { email, password }),
    register: (userData) => api.post('/auth/register', userData),
};

export const petService = {
    getAllPets: () => api.get('/pets'),
    getPetsByOwner: (ownerId) => api.get(`/pets/owner/${ownerId}`),
    getPetById: (id) => api.get(`/pets/${id}`),
    createPet: (data) => api.post('/pets', data),
};

export const medicalRecordService = {
    // För att skapa nya ärenden i CreateCase
    createRecord: (data) => api.post('/medical-records', data),

    // För att uppdatera befintliga ärenden
    updateRecord: (id, data) => api.put(`/medical-records/${id}`, data),

    // För att städa upp om bilagor misslyckas
    deleteRecord: (id) => api.delete(`/medical-records/${id}`),

    // För att hämta listan till OwnerDashboard
    getMyRecords: () => api.get('/medical-records/my-records'),

    // För detaljvyn
    getRecordById: (id) => api.get(`/medical-records/${id}`),

    // För att uppdatera status (t.ex. stänga ärende)
    updateStatus: (id, status) => api.put(`/medical-records/${id}/status`, { status }),
    closeRecord: (id) => api.put(`/medical-records/${id}/close`),
};

export const attachmentService = {
    // Viktig: multipart/form-data för filuppladdning till S3
    upload: (recordId, formData) => api.post(`/attachments/record/${recordId}`, formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
        },
    }),
    getByRecord: (recordId) => api.get(`/attachments/record/${recordId}`),
    download: (id) => api.get(`/attachments/${id}/download`),
    delete: (id) => api.delete(`/attachments/${id}`),
};

export const commentService = {
    getByRecord: (recordId) => api.get(`/comments/record/${recordId}`),
    createComment: (data) => api.post('/comments', data),
};

export const activityService = {
    // Denna krävs av CaseDetail för att visa historik
    getLogsByRecord: (recordId) => api.get(`/activity-logs/record/${recordId}`),
};

export const clinicService = {
    getAll: () => api.get('/clinics'),
    getById: (id) => api.get(`/clinics/${id}`),
};

export default api;