package ru.yandex.finance_tracker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.finance_tracker.dto.input.AccountCreateRequest;
import ru.yandex.finance_tracker.dto.output.AccountInfoDto;
import ru.yandex.finance_tracker.model.Account;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountInfoDto toDto(Account account);

    @Mapping(target = "balance", source = "initialBalance")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id", ignore = true)
    Account toEntity(AccountCreateRequest request);

    List<AccountInfoDto> toDtoList(List<Account> accounts);
}
