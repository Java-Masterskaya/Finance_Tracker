package ru.yandex.finance_tracker.mapper;

import org.mapstruct.*;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.dto.input.TransactionUpdateRequest;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;
import ru.yandex.finance_tracker.model.Transaction;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "category", source = "category.name")
    TransactionInfoDto toResponse(Transaction transaction);

    Transaction toEntity(TransactionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTransactionFromDto(TransactionUpdateRequest request, @MappingTarget Transaction transaction);
}
