package com.endava.endabank.dto.Transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCreateDto {

    @NotNull
    private Double amount;

    @Size(min = 16, max = 20)
    private String bankAccountNumberIssuer;

    @Size(min = 16, max = 20)
    private String bankAccountNumberReceiver;

    private String description;

    @Size(min = 6, max = 15)
    private String address;

}
