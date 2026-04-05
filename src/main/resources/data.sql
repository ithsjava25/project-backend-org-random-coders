-- Klinik
INSERT INTO clinic (id, name, address, phone_number, email, created_at, updated_at)
VALUES
    ('a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5',
     'Djurkliniken Centrum',
     'Storgatan 1, Stockholm',
     '08-123456',
     'centrum@klinik.se',
     NOW(), NOW())
    ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

-- Owner Anna
INSERT INTO users (id, name, email, password_hash, role, is_active, created_at, updated_at)
VALUES
    ('c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7',
     'Anna Svensson',
     'anna@test.se',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'OWNER',
     true,
     NOW(), NOW())
    ON CONFLICT (email) DO UPDATE SET id = EXCLUDED.id, name = EXCLUDED.name;
--                                 ↑ viktigt — uppdaterar UUID:t om emailen finns

-- Vet Erik
INSERT INTO users (id, name, email, password_hash, role, clinic_id, is_active, created_at, updated_at)
VALUES
    ('d4e5f6a7-b8c9-4d5e-1f2a-b3c4d5e6f7a8',
     'Erik Veterinär',
     'erik@klinik.se',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'VET',
     'a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5',
     true,
     NOW(), NOW())
    ON CONFLICT (email) DO UPDATE SET id = EXCLUDED.id, name = EXCLUDED.name;

-- Admin Sara
INSERT INTO users (id, name, email, password_hash, role, is_active, created_at, updated_at)
VALUES
    ('e5f6a7b8-c9d0-4e5f-2a3b-c4d5e6f7a8b9',
     'Sara Admin',
     'sara@admin.se',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'ADMIN',
     true,
     NOW(), NOW())
    ON CONFLICT (email) DO UPDATE SET id = EXCLUDED.id, name = EXCLUDED.name;

-- Djur
INSERT INTO pet (id, owner_id, clinic_id, name, species, breed, created_at, updated_at)
VALUES
    ('f6a7b8c9-d0e1-4f5a-3b4c-d5e6f7a8b9c0',
     'c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7',
     'a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5',
     'Fido', 'Hund', 'Labrador',
     NOW(), NOW()),
    ('a7b8c9d0-e1f2-4a5b-4c5d-e6f7a8b9c0d1',
     'c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7',
     'a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5',
     'Missan', 'Katt', 'Persisk',
     NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- Ärenden
INSERT INTO medical_record (id, title, description, status, pet_id, owner_id, clinic_id, created_by, created_at, updated_at)
VALUES
    ('b8c9d0e1-f2a3-4b5c-5d6e-f7a8b9c0d1e2',
     'Fido haltar',
     'Hunden har haltat sedan igår',
     'OPEN',
     'f6a7b8c9-d0e1-4f5a-3b4c-d5e6f7a8b9c0',
     'c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7',
     'a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5',
     'c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7',
     NOW(), NOW()),
    ('c9d0e1f2-a3b4-4c5d-6e7f-a8b9c0d1e2f3',
     'Missan äter inte',
     'Katten har inte ätit på 2 dagar',
     'IN_PROGRESS',
     'a7b8c9d0-e1f2-4a5b-4c5d-e6f7a8b9c0d1',
     'c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7',
     'a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5',
     'd4e5f6a7-b8c9-4d5e-1f2a-b3c4d5e6f7a8',
     NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;