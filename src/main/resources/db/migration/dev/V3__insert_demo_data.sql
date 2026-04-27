-- V3__insert_demo_data.sql

-- Klinik
INSERT INTO clinic (id, name, address, phone_number, email, created_at, updated_at)
VALUES ('a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5', 'Djurkliniken Centrum', 'Storgatan 1, Stockholm', '08-123456', 'centrum@klinik.se', NOW(), NOW());

-- Users
INSERT INTO users (id, name, email, password_hash, role, clinic_id, is_active, created_at, updated_at)
VALUES
    ('c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7', 'Anna Svensson', 'anna@test.se', '$2a$10$mxL7BIFi9S2jdA867vmuDeRe4SZ/krIiHsCzvOdh/3j5nviJ0G2xy', 'OWNER', NULL, true, NOW(), NOW()),
    ('b1c2d3e4-f5a6-4b7c-8d9e-f0a1b2c3d4e5', 'Lars Johansson', 'lars@test.se', '$2a$10$mxL7BIFi9S2jdA867vmuDeRe4SZ/krIiHsCzvOdh/3j5nviJ0G2xy', 'OWNER', NULL, true, NOW(), NOW()),
    ('d4e5f6a7-b8c9-4d5e-1f2a-b3c4d5e6f7a8', 'Erik Veterinär', 'erik@klinik.se', '$2a$10$mxL7BIFi9S2jdA867vmuDeRe4SZ/krIiHsCzvOdh/3j5nviJ0G2xy', 'VET', 'a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5', true, NOW(), NOW()),
    ('e5f6a7b8-c9d0-4e5f-2a3b-c4d5e6f7a8b9', 'Sara Admin', 'sara@admin.se', '$2a$10$mxL7BIFi9S2jdA867vmuDeRe4SZ/krIiHsCzvOdh/3j5nviJ0G2xy', 'ADMIN', NULL, true, NOW(), NOW());

-- Pets
INSERT INTO pet (id, owner_id, clinic_id, name, species, breed, weight_kg, created_at, updated_at)
VALUES
    ('f6a7b8c9-d0e1-4f5a-3b4c-d5e6f7a8b9c0', 'c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7', 'a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5', 'Fido', 'Hund', 'Labrador', 28.5, NOW(), NOW()),
    ('a7b8c9d0-e1f2-4a5b-4c5d-e6f7a8b9c0d1', 'c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7', 'a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5', 'Missan', 'Katt', 'Persisk', 4.2, NOW(), NOW()),
    ('c8d9e0f1-a2b3-4c4d-5e6f-a7b8c9d0e1f2', 'b1c2d3e4-f5a6-4b7c-8d9e-f0a1b2c3d4e5', 'a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5', 'Birk', 'Hund', 'Golden Retriever', 32.0, NOW(), NOW()),
    ('d9e0f1a2-b3c4-4d5e-6f7a-b8c9d0e1f2a3', 'b1c2d3e4-f5a6-4b7c-8d9e-f0a1b2c3d4e5', 'a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5', 'Misse', 'Katt', 'Maine Coon', 6.8, NOW(), NOW());

-- Medical Records
INSERT INTO medical_record (id, title, description, status, pet_id, owner_id, clinic_id, created_by, created_at, updated_at)
VALUES
    ('b8c9d0e1-f2a3-4b5c-5d6e-f7a8b9c0d1e2', 'Fido haltar', 'Hunden har haltat sedan igår.', 'OPEN', 'f6a7b8c9-d0e1-4f5a-3b4c-d5e6f7a8b9c0', 'c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7', 'a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5', 'c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7', NOW(), NOW()),
    ('c9d0e1f2-a3b4-4c5d-6e7f-a8b9c0d1e2f3', 'Missan äter inte', 'Katten har inte ätit på 2 dagar.', 'IN_PROGRESS', 'a7b8c9d0-e1f2-4a5b-4c5d-e6f7a8b9c0d1', 'c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7', 'a1b2c3d4-e5f6-4a5b-8c9d-e0f1a2b3c4d5', 'd4e5f6a7-b8c9-4d5e-1f2a-b3c4d5e6f7a8', NOW(), NOW());

-- Comments
INSERT INTO comment (id, record_id, author_id, body, comment_type, created_at, updated_at)
VALUES
    ('a2b3c4d5-e6f7-4a5b-9c0d-e1f2a3b4c5d6', 'c9d0e1f2-a3b4-4c5d-6e7f-a8b9c0d1e2f3', 'c3d4e5f6-a7b8-4c5d-0e1f-a2b3c4d5e6f7', 'Hon dricker vatten men vägrar all mat.', 'OWNER_MESSAGE', NOW(), NOW());