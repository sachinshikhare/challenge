package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

  private final Map<String, Account> accounts = new ConcurrentHashMap<>();

  @Override
  public void createAccount(Account account) throws DuplicateAccountIdException {
    Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
    if (previousAccount != null) {
      throw new DuplicateAccountIdException(
        "Account id " + account.getAccountId() + " already exists!");
    }
  }

  @Override
  public Account getAccount(String accountId) {
    return accounts.get(accountId);
  }

  @Override
  public void clearAccounts() {
    accounts.clear();
  }

  @Override
  public boolean transferMoney(final Account fromAccount, final Account toAccount, final BigDecimal balance)
      throws InsufficientBalanceException {

    synchronized (fromAccount) {
      if (fromAccount.getBalance().doubleValue() < balance.doubleValue()) {
        throw new InsufficientBalanceException(
            String.format(
                "Account with id %s doesn't have sufficient balance to initiate transfer of %s",
                fromAccount.getAccountId(),
                balance.doubleValue()
            )
        );
      }
      fromAccount.setBalance(fromAccount.getBalance().subtract(balance));
    }
    synchronized (toAccount) {
      toAccount.setBalance(toAccount.getBalance().add(balance));
    }
    return true;
  }

}
