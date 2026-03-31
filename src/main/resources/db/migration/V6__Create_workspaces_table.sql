CREATE TABLE IF NOT EXISTS workspaces (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    slug VARCHAR(255) NOT NULL UNIQUE,
    is_personal BOOLEAN DEFAULT FALSE,
    created_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE NULL,
    CONSTRAINT fk_workspaces_created_by FOREIGN KEY (created_by)
        REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_fk_workspaces_created_by ON workspaces(created_by);
CREATE INDEX idx_fk_workspaces_slug ON workspaces(slug);
CREATE INDEX idx_fk_workspaces_is_personal ON workspaces(is_personal);
CREATE INDEX idx_fk_workspaces_deleted_at ON workspaces(deleted_at);
