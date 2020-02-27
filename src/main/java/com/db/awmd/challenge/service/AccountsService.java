package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.exception.InvalidInputException;
import com.db.awmd.challenge.repository.AccountsRepository;
import java.math.BigDecimal;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Getter
  private final NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository, final NotificationService notificationService) {
    this.accountsRepository = accountsRepository;
    this.notificationService = notificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public boolean moneyTransfer(String fromAccountId, String toAccountId, BigDecimal balance)
      throws InvalidInputException, InsufficientBalanceException {

    if (balance.doubleValue() <= 0) {
      throw new InvalidInputException(
          String.format("Input value %s balance to transfer provided should be non-zero positive", balance.doubleValue())
      );

    }

    /*
     TODO: below enhancement could be done in getAccount method
      While retrieving account using getAccount method, it is assumed that, in case if account not present,
      Proper exception would be thrown from getAccount method itself. Other method would not need to worry about it.
    */
    Account fromAccount = getAccount(fromAccountId);
    Account toAccount = getAccount(toAccountId);

    boolean success = this.accountsRepository.transferMoney(fromAccount, toAccount, balance);

    if (success) {
      notificationService.notifyAboutTransfer(
          fromAccount,
          String.format("Your account is debited with %s and amount is transferred to %s", balance.doubleValue(), toAccount.getAccountId())
      );
      notificationService.notifyAboutTransfer(
          toAccount,
          String.format("Your account is credited with %s. Amount received from account %s", balance.doubleValue(), fromAccount.getAccountId())
      );
      return true;
    }
    return false;
  }
}

//annotation based validation on model