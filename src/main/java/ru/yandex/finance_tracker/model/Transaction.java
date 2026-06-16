package ru.yandex.finance_tracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
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
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "type", nullable = false, columnDefinition = "transaction_type")
    private Type type;
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "currency", columnDefinition = "currency_type", nullable = false)
    private Currency currency;
    @Column(name = "category", nullable = false, length = 50)
    private String category;
    @Column(name = "date", nullable = false)
    private LocalDate date;
    @Column(name = "description", length = 300)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
