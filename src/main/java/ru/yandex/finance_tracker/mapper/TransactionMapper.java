package ru.yandex.finance_tracker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;
import ru.yandex.finance_tracker.model.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "accountBalance", source = "account.balance")
    TransactionInfoDto toResponse(Transaction transaction);

    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "user", ignore = true)
    Transaction toEntity (TransactionRequest request);

}
