import axios from 'axios';

// Vi skapar en instans av axios med din bas-URL
const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    }
});

/**
 * INTERCEPTOR FÖR REQUESTS
 * Körs innan varje anrop lämnar din webbläsare.
 */
api.interceptors.request.use(
    (config) => {
        // Hämta din JWT från localStorage
        const token = localStorage.getItem('token');

        // Om token finns, lägg till den i Authorization-headern
        // Din backend (JwtAuthenticationFilter) förväntar sig formatet "Bearer <token>"
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

/**
 * INTERCEPTOR FÖR RESPONSES
 * Körs när ett svar kommer tillbaka från backenden, innan det når din .then() eller await.
 */
api.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        // Om backenden svarar med 401 Unauthorized betyder det oftast
        // att din token har gått ut eller är ogiltig.
        if (error.response && error.response.status === 401) {
            console.warn("Token ogiltig eller utgången. Loggar ut...");

            // Rensa sparad data
            localStorage.removeItem('token');

            // Här kan du välja att skicka användaren till login-sidan
            // window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

// --- DINA EXISTERANDE SERVICES ---

export const petService = {
    getAllPets: () => api.get('/pets'),
    getPetById: (id) => api.get(`/pets/${id}`),
    createPet: (data) => api.post('/pets', data),
};

export const medicalRecordService = {
    getMyRecords: () => api.get('/medical-records/my-records'),
    getRecordById: (id) => api.get(`/medical-records/${id}`),
};

export const commentService = {
    getByRecord: (recordId) => api.get(`/comments/record/${recordId}`),
    createComment: (data) => api.post('/comments', data),
};

export const attachmentService = {
    getByRecord: (recordId) => api.get(`/attachments/record/${recordId}`),
    upload: (recordId, formData) => api.post(`/attachments/record/${recordId}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    }),
    delete: (id) => api.delete(`/attachments/${id}`),
};

export const activityService = {
    getLogsByRecord: (recordId) => api.get(`/activity-logs/record/${recordId}`),
};

// Ny service för inloggning
export const authService = {
    login: (email, password) => api.post('/auth/login', { email, password }),
    register: (userData) => api.post('/auth/register', userData),
};

export default api;