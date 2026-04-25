-- Skapar tabellen för misslyckade raderingar
CREATE TABLE orphaned_s3_objects (
                                     id UUID PRIMARY KEY,
                                     s3_key VARCHAR(512) NOT NULL,
                                     s3_bucket VARCHAR(255) NOT NULL,
                                     created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     retry_count INT NOT NULL DEFAULT 0,
                                     last_attempt_at TIMESTAMP WITH TIME ZONE,
                                     last_error TEXT,
                                     CONSTRAINT uk_orphaned_s3_key UNIQUE (s3_key)
);


CREATE INDEX idx_orphaned_s3_retry ON orphaned_s3_objects (retry_count, last_attempt_at);