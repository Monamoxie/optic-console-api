ALTER TABLE users
    ADD COLUMN last_accessed_workspace_id BIGINT NULL,
    ADD CONSTRAINT fk_users_last_workspace FOREIGN KEY (last_accessed_workspace_id)
        REFERENCES workspaces(id) ON DELETE SET NULL;
