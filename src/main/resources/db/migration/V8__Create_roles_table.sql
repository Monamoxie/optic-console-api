CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description TEXT,
    scope VARCHAR(50) NOT NULL,
    is_system BOOLEAN DEFAULT FALSE,
    workspace_id BIGINT
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_roles_workspaces FOREIGN KEY (workspace_id)
        REFERENCES workspace(id) ON DELETE CASCADE,
    CONSTRAINT roles_workspace_slug_unique UNIQUE (workspace, slug)
);

CREATE INDEX idx_roles_workspace_id ON roles(workspace_id);
CREATE INDEX idx_roles_is_system ON roles(is_system);
CREATE INDEX idx_roles_scope ON roles(scope);
