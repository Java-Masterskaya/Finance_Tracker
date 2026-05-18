package ru.yandex.finance_tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, columnDefinition = "transaction_type")
    private Type type;
    @Column(name = "amount", nullable = false)
    private Float amount;
    @Column(name = "category", nullable = false, length = 50)
    private String category;
    @Column(name = "date", nullable = false)
    private LocalDate date;
    @Column(name = "description", length = 300)
    private String description;
}
