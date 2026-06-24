package ru.yandex.finance_tracker.dto.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedTransactionResponse {
    private List<TransactionInfoDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
