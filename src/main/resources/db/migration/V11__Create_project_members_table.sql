CREATE TABLE IF NOT EXISTS project_members(
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_project_members_project FOREIGN KEY(project_id)
        REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_members_user FOREIGN KEY(user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_members_role FOREIGN KEY(role_id)
        REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT project_members_unique UNIQUE(project_id, user_id)
);

CREATE INDEX idx_project_members_project_id ON project_members(project_id);
CREATE INDEX idx_project_members_user_id ON project_members(user_id);
CREATE INDEX idx_project_members_role_id ON project_members(role_id);
