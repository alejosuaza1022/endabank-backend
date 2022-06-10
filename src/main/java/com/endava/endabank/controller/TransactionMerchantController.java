package com.endava.endabank.controller;

import com.endava.endabank.constants.Routes;
import com.endava.endabank.dto.transaction.TransactionCreateDto;
import com.endava.endabank.dto.transaction.TransactionCreatedDto;
import com.endava.endabank.dto.transaction.TransactionFromMerchantDto;
import com.endava.endabank.dto.user.UserPrincipalSecurity;
import com.endava.endabank.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping(Routes.API_MERCHANT_ROUTE)
@AllArgsConstructor
public class TransactionMerchantController {
    private TransactionService transactionService;

    @PostMapping(Routes.PAY_TO_MERCHANT)
    public ResponseEntity<Map<String, Object>> createPayTransaction(Principal principal, @Valid @RequestBody TransactionFromMerchantDto transferFromMerchantDto) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) principal;
        UserPrincipalSecurity user = (UserPrincipalSecurity) usernamePasswordAuthenticationToken.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransactionFromMerchant(user.getId(),transferFromMerchantDto));
    }
}