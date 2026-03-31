CREATE TABLE IF NOT EXISTS projects(
    id BIGSERIAL PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    api_key VARCHAR(64) NOT NULL UNIQUE,
    allowed_origins TEXT[],
    enforce_origin_check BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE  DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_projects_workspace_id FOREIGN KEY(workspace_id)
        REFERENCES workspaces(id) ON DELETE CASCADE,
    CONSTRAINT projects_workspace_slug_unique UNIQUE(workspace_id, slug)
);

CREATE INDEX idx_workspace_id ON projects(workspace_id);
CREATE INDEX idx_slug ON projects(slug);
CREATE INDEX idx_api_key ON projects(api_key);
