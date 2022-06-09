package com.endava.endabank.service.impl;

import com.endava.endabank.constants.Strings;
import com.endava.endabank.dao.BankAccountDao;
import com.endava.endabank.dao.MerchantDao;
import com.endava.endabank.dao.TransactionDao;
import com.endava.endabank.dao.UserDao;
import com.endava.endabank.dto.transaction.TransactionCreateDto;
import com.endava.endabank.dto.transaction.TransactionCreatedDto;
import com.endava.endabank.dto.transaction.TransactionFromMerchantDto;
import com.endava.endabank.model.*;
import com.endava.endabank.service.BankAccountService;
import com.endava.endabank.service.MerchantService;
import com.endava.endabank.service.TransactionService;
import com.endava.endabank.service.UserService;
import com.endava.endabank.utils.transaction.TransactionValidations;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Service
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private BankAccountService bankAccountService;
    private TransactionDao transactionDao;
    private ModelMapper modelMapper;
    private EntityManager entityManager;
    private TransactionValidations transactionValidations;
    private MerchantService merchantService;
    private UserService userService;
    private PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public TransactionCreatedDto createTransaction(Integer userId, TransactionCreateDto transactionCreateDto) {
        BankAccount bankAccountIssuer = bankAccountService.
                findByAccountNumber(transactionCreateDto.getBankAccountNumberIssuer());
        BankAccount bankAccountReceiver = bankAccountService.
                findByAccountNumber(transactionCreateDto.getBankAccountNumberReceiver());
        transactionValidations.validateTransaction(userId, bankAccountIssuer, bankAccountReceiver,transactionCreateDto);
        TransactionType transactionType = TransactionType.builder().id(1).build();
        Transaction transaction = Transaction.
                builder().amount(transactionCreateDto.getAmount()).
                description(transactionCreateDto.getDescription()).
                address(transactionCreateDto.getAddress()).
                transactionType(transactionType).bankAccountIssuer(bankAccountIssuer).
                bankAccountReceiver(bankAccountReceiver).build();
        setStateAndBalanceOfTransaction(transaction, bankAccountIssuer, bankAccountReceiver,
                transactionCreateDto.getAmount());
        transaction = transactionDao.save(transaction);
        return modelMapper.map(transaction, TransactionCreatedDto.class);
    }

    @Transactional
    @Override
    public void setStateAndBalanceOfTransaction(Transaction transaction,
                                                BankAccount bankAccountIssuer,
                                                BankAccount bankAccountReceiver, Double amount) {
        StateType stateType = entityManager.getReference(StateType.class, Strings.TRANSACTION_APPROVED_STATE);
        if (amount > bankAccountIssuer.getBalance()) {
            stateType = entityManager.getReference(StateType.class, Strings.TRANSACTION_FAILED_STATE);
            transaction.setStateType(stateType);
            transaction.setStateDescription(Strings.NOT_FOUNDS_ENOUGH);
            return;
        }
        bankAccountService.reduceBalance(bankAccountIssuer, amount);
        bankAccountService.increaseBalance(bankAccountReceiver, amount);
        transaction.setStateType(stateType);
        transaction.setStateDescription(Strings.TRANSACTION_COMPLETED);
    }

    @Transactional
    @Override
    public TransactionCreatedDto createTransactionFromMerchant(TransactionFromMerchantDto transactionFromMerchantDto){
        User user = userService.findByIdentifier(transactionFromMerchantDto.getIdentifier());
        BankAccount bankAccountIssuer = bankAccountService.findByUser(user);
        Merchant merchant = merchantService.findByMerchantKey(transactionFromMerchantDto.getMerchantKey());
        User userMerchant = userService.findById(merchant.getUser().getId());
        BankAccount bankAccountReceiver = bankAccountService.findByUser(userMerchant);
        String apiId = merchant.getApiId();
        String bankAccountPassword = bankAccountIssuer.getPassword();
        transactionValidations.
                validateExternalTransaction(apiId,bankAccountPassword,transactionFromMerchantDto,passwordEncoder);
        TransactionCreateDto transactionCreateDto = new TransactionCreateDto();
        transactionCreateDto.setBankAccountNumberIssuer(bankAccountIssuer.getAccountNumber());
        transactionCreateDto.setBankAccountNumberReceiver(bankAccountReceiver.getAccountNumber());
        transactionCreateDto.setAddress(transactionFromMerchantDto.getAddress());
        transactionCreateDto.setAmount(transactionFromMerchantDto.getAmount());
        transactionCreateDto.setDescription(transactionFromMerchantDto.getDescription());
        return createTransaction(user.getId(),transactionCreateDto);
    }
}
