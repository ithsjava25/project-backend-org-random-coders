import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    }
});

/**
 * REQUEST INTERCEPTOR
 */
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token') || sessionStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

/**
 * RESPONSE INTERCEPTOR
 */
api.interceptors.response.use(
    (response) => response,
    (error) => {
        const isAuthEndpoint = error.config?.url?.includes('/auth/login') ||
            error.config?.url?.includes('/auth/register');

        // 401: Token utgången
        if (error.response?.status === 401 && !isAuthEndpoint) {
            localStorage.removeItem('token');
            sessionStorage.removeItem('token');
            window.dispatchEvent(new CustomEvent('auth:logout'));
        }

        // 403: Behörighet saknas (Visa toast/meddelande)
        if (error.response?.status === 403) {
            window.dispatchEvent(new CustomEvent('app:error', {
                detail: "Du saknar behörighet för att utföra denna åtgärd."
            }));
        }

        return Promise.reject(error);
    }
);

// --- SERVICES ---

export const authService = {
    login: (email, password) => api.post('/auth/login', { email, password }),
    register: (userData) => api.post('/auth/register', userData),
};

export const activityService = {
    getAll: () => api.get('/activity-logs/all'),
    getLogsByRecord: (recordId) => api.get(`/activity-logs/record/${recordId}`),
    getByRecord: (recordId) => api.get(`/activity-logs/record/${recordId}`),
};


export const petService = {
    getAllPets: () => api.get('/pets'),
    getPetsByOwner: (ownerId) => api.get(`/pets/owner/${ownerId}`),
    getPetById: (id) => api.get(`/pets/${id}`),
    createPet: (data) => api.post('/pets', data),
};

export const medicalRecordService = {
    // Grundläggande hantering
    createRecord: (data) => api.post('/medical-records', data),
    getRecordById: (id) => api.get(`/medical-records/${id}`),
    update: (id, data) => api.put(`/medical-records/${id}`, data),

    // Listor för olika roller
    getMyRecords: () => api.get('/medical-records/my-records'),
    getRecordsByClinic: (clinicId) => api.get(`/medical-records/clinic/${clinicId}`),
    getRecordsByClinicAndStatus: (clinicId, status) => api.get(`/medical-records/clinic/${clinicId}/status/${status}`),
    getRecordsByPet: (petId) => api.get(`/medical-records/pet/${petId}`),

    // Arbetsflöde (Veterinär)
    assignVet: (id, vetId) => api.put(`/medical-records/${id}/assign-vet`, { vetId }),
    updateStatus: (id, status) => api.put(`/medical-records/${id}/status`, { status }),
    closeRecord: (id) => api.put(`/medical-records/${id}/close`),
    getMyAssignedRecords: () => api.get('/medical-records/my-assigned'),
};

export const attachmentService = {
    upload: (recordId, formData) => api.post(`/attachments/record/${recordId}`, formData),


    getByRecord: (recordId) => api.get(`/attachments/record/${recordId}`),
    download: (id) => api.get(`/attachments/${id}/download`, { responseType: 'blob' }),
    delete: (id) => api.delete(`/attachments/${id}`),
};

export const commentService = {
    getByRecord: (recordId) => api.get(`/comments/record/${recordId}`),
    createComment: (data) => api.post('/comments', data),
};


export const clinicService = {
    getAll: () => api.get('/clinics'),
    getById: (id) => api.get(`/clinics/${id}`),
    create: (data) => api.post('/clinics', data),
    update: (id, data) => api.put(`/clinics/${id}`, data),
    delete: (id) => api.delete(`/clinics/${id}`),
};

export const userService = {
    getAll: () => api.get('/users'),
    getById: (id) => api.get(`/users/${id}`),
    create: (data) => api.post('/users', data),
    update: (id, data) => api.put(`/users/${id}`, data),
    delete: (id) => api.delete(`/users/${id}`),
    searchByEmail: (email) => api.get(`/users/search?email=${email}`),
};

export const vetService = {
    create: (vetData) => api.post('/vets', vetData),
    getAll: () => api.get('/vets'),
    getById: (id) => api.get(`/vets/${id}`),
};

export default api;