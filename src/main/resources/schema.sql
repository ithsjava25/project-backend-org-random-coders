

CREATE TABLE IF NOT EXISTS clinic (
                                      id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                      name            VARCHAR(255) NOT NULL,
                                      address         VARCHAR(500) NOT NULL,
                                      phone_number    VARCHAR(30),
                                      email           VARCHAR(255),
                                      created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                      updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS users (
                                     id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                     name            VARCHAR(255) NOT NULL,
                                     email           VARCHAR(255) NOT NULL UNIQUE,
                                     password_hash   VARCHAR(255) NOT NULL,
                                     role            VARCHAR(20)  NOT NULL DEFAULT 'OWNER',
                                     clinic_id       UUID        REFERENCES clinic(id),
                                     is_active       BOOLEAN     NOT NULL DEFAULT TRUE,
                                     created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                     updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS vet_details (
                                           user_id         UUID        PRIMARY KEY REFERENCES users(id),
                                           license_id      VARCHAR(50)  NOT NULL UNIQUE,
                                           specialization  VARCHAR(255),
                                           booking_info    VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS pet (
                                   id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                   owner_id        UUID        NOT NULL REFERENCES users(id),
                                   clinic_id       UUID        REFERENCES clinic(id),
                                   name            VARCHAR(255) NOT NULL,
                                   species         VARCHAR(100) NOT NULL,
                                   breed           VARCHAR(255),
                                   date_of_birth   DATE,
                                   weight_kg       DECIMAL(6,2),
                                   created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                   updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS medical_record (
                                              id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                              title           VARCHAR(500) NOT NULL,
                                              description     TEXT,
                                              status          VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
                                              pet_id          UUID        NOT NULL REFERENCES pet(id),
                                              owner_id        UUID        NOT NULL REFERENCES users(id),
                                              clinic_id       UUID        NOT NULL REFERENCES clinic(id),
                                              assigned_vet_id UUID        REFERENCES users(id),
                                              created_by      UUID        NOT NULL REFERENCES users(id),
                                              created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                              updated_by      UUID        REFERENCES users(id),
                                              updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                              closed_at       TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS comment (
                                       id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                       record_id       UUID        NOT NULL REFERENCES medical_record(id),
                                       author_id       UUID        NOT NULL REFERENCES users(id),
                                       body            TEXT        NOT NULL,
                                       created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                       updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS attachment (
                                          id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                          record_id       UUID        NOT NULL REFERENCES medical_record(id) ON DELETE CASCADE,
                                          uploaded_by     UUID        REFERENCES users(id) ON DELETE SET NULL,
                                          file_name       VARCHAR(500) NOT NULL,
                                          description     VARCHAR(500),
                                          s3_key          VARCHAR(1000) NOT NULL UNIQUE,
                                          s3_bucket       VARCHAR(255) NOT NULL,
                                          file_type       VARCHAR(100),
                                          file_size_bytes BIGINT,
                                          uploaded_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS activity_log (
                                            id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                            record_id       UUID        REFERENCES medical_record(id),
                                            action          VARCHAR(50)  NOT NULL,
                                            entity_type     VARCHAR(50)  NOT NULL,
                                            entity_id       UUID,
                                            performed_by    UUID        NOT NULL REFERENCES users(id),
                                            details         TEXT,
                                            performed_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index
CREATE INDEX IF NOT EXISTS idx_users_email        ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_clinic       ON users(clinic_id);
CREATE INDEX IF NOT EXISTS idx_record_pet         ON medical_record(pet_id);
CREATE INDEX IF NOT EXISTS idx_record_status      ON medical_record(status);
CREATE INDEX IF NOT EXISTS idx_comment_record     ON comment(record_id, created_at);
CREATE INDEX IF NOT EXISTS idx_attachment_record  ON attachment(record_id);
CREATE INDEX IF NOT EXISTS idx_log_record         ON activity_log(record_id, performed_at);