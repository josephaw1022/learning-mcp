package org.acme.model;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "UserSession", schema = "users")
public class UserSession extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_session_seq")
    @SequenceGenerator(name = "users_session_seq", sequenceName = "UserSession_SEQ", schema = "users", allocationSize = 50)
    public Long id;

    public String badgeNumber;
    public LocalDateTime loginTime;
    public LocalDateTime logoutTime;
    public String ipAddress;
}
