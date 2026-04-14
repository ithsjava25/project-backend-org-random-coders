import axios from 'axios';

// VIKTIGT: Byt ut detta mot ett UUID som faktiskt finns i din User-tabell
const TEMP_USER_ID = 'c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7';

const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
        'currentUserId': TEMP_USER_ID
    }
});

export const petService = {
    getAllPets: (ownerId = TEMP_USER_ID) => api.get(`/pets/owner/${ownerId}`),
    getPetById: (id) => api.get(`/pets/${id}`),
    createPet: (petData) => api.post('/pets', petData, { params: { ownerId: TEMP_USER_ID } })
};

export const medicalRecordService = {
    // Hämtar listan (MedicalRecordSummaryResponse)
    getMyRecords: () => api.get('/medical-records/my-records'),

    // Hämtar ett specifikt ärende (MedicalRecordResponse)
    getRecordById: (id) => api.get(`/medical-records/${id}`),

    // Skapar nytt (CreateMedicalRecordRequest)
    createRecord: (data) => api.post('/medical-records', data),

    // Uppdaterar (UpdateMedicalRecordRequest)
    updateRecord: (id, data) => api.put(`/medical-records/${id}`, data)
};

// Lägg till detta i din befintliga api.js under medicalRecordService
export const commentService = {
    getByRecord: (recordId) => api.get(`/comments/record/${recordId}`),
    createComment: (data) => api.post('/comments', data)
};

export const activityService = {
    // Notera: Just nu skickar vi userId i headern enligt din controller
    getLogsByRecord: (recordId, userId) => api.get(`/activity-logs/record/${recordId}`, {
        headers: { 'userId': userId }
    })
};

export const attachmentService = {
    // POST /api/attachments/record/{recordId}
    upload: (recordId, formData) => api.post(`/attachments/record/${recordId}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    }),
    // GET /api/attachments/record/{recordId}
    getByRecord: (recordId) => api.get(`/attachments/record/${recordId}`),
    // DELETE /api/attachments/{id}
    delete: (id) => api.delete(`/attachments/${id}`)
};

export default api;