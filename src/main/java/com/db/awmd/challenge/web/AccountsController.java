package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.exception.InvalidInputException;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
@Validated
public class AccountsController {

  private final AccountsService accountsService;

  @Autowired
  public AccountsController(AccountsService accountsService) {
    this.accountsService = accountsService;
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
    this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }

  @PostMapping(path = "/account-transfer")
  public ResponseEntity<Object> moneyTransfer(@RequestParam @NotNull String fromAccountId, @RequestParam @NotNull String toAccountId,
      @RequestParam @NotNull BigDecimal balance) {
    log.info("Transferring amount {} from {} to {}", balance, fromAccountId, toAccountId);
    boolean result;
    try {
      result = accountsService.moneyTransfer(fromAccountId, toAccountId, balance);
    } catch (InvalidInputException | InsufficientBalanceException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    if (result) {
      return new ResponseEntity<>("Account transfer is successful", HttpStatus.OK);
    }
    return new ResponseEntity<>("internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
  }

}

// advice exception handler.