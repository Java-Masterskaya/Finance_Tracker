package ru.yandex.finance_tracker.model;

//При изменении списка валют требуется зафиксировать изменение в Liquibase, в enum типе currency_type
public enum Currency {
    RUB,
    EUR,
    USD
}
