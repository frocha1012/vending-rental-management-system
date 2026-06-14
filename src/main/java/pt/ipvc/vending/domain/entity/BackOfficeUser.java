package pt.ipvc.vending.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import pt.ipvc.vending.domain.enums.BackOfficeRole;

import java.time.LocalDateTime;

@Entity
@Table(name = "backoffice_users")
public class BackOfficeUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BackOfficeRole role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }

    public String getUsername()            { return username; }
    public void setUsername(String u)      { this.username = u; }

    public String getPasswordHash()        { return passwordHash; }
    public void setPasswordHash(String h)  { this.passwordHash = h; }

    public BackOfficeRole getRole()        { return role; }
    public void setRole(BackOfficeRole r)  { this.role = r; }

    public boolean isActive()              { return active; }
    public void setActive(boolean a)       { this.active = a; }

    public LocalDateTime getCreatedAt()            { return createdAt; }
    public void setCreatedAt(LocalDateTime dt)     { this.createdAt = dt; }
}
