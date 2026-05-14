package ru.yandex.finance_tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", columnDefinition = "currency_type", nullable = false)
    private Currency currency;
    @Column(name = "balance", nullable = false)
    private Float balance;
}
