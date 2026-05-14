package ru.yandex.finance_tracker.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "email", unique = true, nullable = false, length = 60)
    String email;

    @Column(name = "password_hash", nullable = false, length = 60)
    String passwordHash;

    @Column(name = "name", nullable = false, length = 30)
    String firstName;

    @Enumerated(EnumType.STRING)
    UserRole role = UserRole.ROLE_USER;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Account> accounts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Transaction> transactions;


    public User(String email, String passwordHash, String firstName, UserRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.role = role;
    }

    public UserDetails toUserDetails() {
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(this.email)
                .password(this.passwordHash)
                .authorities(this.role.toAuthority())
                .build();
    }
}
