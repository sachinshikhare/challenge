package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.exception.InvalidInputException;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;


  @Before
  public void setup() {

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
    this.testDataSetup_addMultipleAccounts();
  }

  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }

  }

  @Test
  public void transferMoney_successInCaseOfCorrectInput() throws InvalidInputException, InsufficientBalanceException {

    boolean result = accountsService.moneyTransfer("Id-100", "Id-101", new BigDecimal(50));
    assertTrue(result);
  }

  @Test(expected = InsufficientBalanceException.class)
  public void transferMoney_failureInCaseOfInsufficientFundsInFromAccount() throws InvalidInputException, InsufficientBalanceException {

    boolean result = accountsService.moneyTransfer("Id-100", "Id-101", new BigDecimal(5000));
    assertFalse(result);
  }

  @Test(expected = InvalidInputException.class)
  public void transferMoney_failureInCaseOfNegativeInputBalance() throws InvalidInputException, InsufficientBalanceException {

    boolean result = accountsService.moneyTransfer("Id-100", "Id-101", new BigDecimal(-50));
    assertFalse(result);
  }

  private void testDataSetup_addMultipleAccounts() {

    Account account = new Account("Id-100");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    account = new Account("Id-101");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    account = new Account("Id-102");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);
  }
}
