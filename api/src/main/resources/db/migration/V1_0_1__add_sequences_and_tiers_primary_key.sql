-- Sequence generators required by the JPA entities (allocationSize = 50).
-- The pooled optimizer expects the database sequence to advance by the allocation size,
-- mirroring the DDL Hibernate generates for these entities.
CREATE SEQUENCE IF NOT EXISTS users_seq START WITH 50 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS tierlists_seq START WITH 50 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS sync_jobs_seq START WITH 50 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS media_source_connections_seq START WITH 50 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS media_type_settings_seq START WITH 50 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS user_media_entry_states_seq START WITH 50 INCREMENT BY 50;

-- The tiers table was created without a primary key; tiers are unique per tierlist and name.
ALTER TABLE tiers ADD CONSTRAINT pk_tiers PRIMARY KEY (tierlist_id, name);
