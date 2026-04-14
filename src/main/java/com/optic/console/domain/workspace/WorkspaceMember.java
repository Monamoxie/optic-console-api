package com.optic.console.domain.workspace;

import com.optic.console.domain.auth.Role;
import com.optic.console.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.*;

@Entity
@Table(
        name = "workspace_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"workspace_id", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public boolean isOwner() {
        return role != null && "owner".equalsIgnoreCase(role.getSlug());
    }

    public boolean isAdmin() {
        return "admin".equals(role);
    }

    public boolean isMember() {
        return "member".equals(role);
    }

    public boolean canManageMembers() {
        return isAdmin() || isOwner();
    }

    public boolean canManageSettings() {
        return isAdmin() || isOwner();
    }

}
