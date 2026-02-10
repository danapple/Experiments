package com.danapple.experiments.atomic.ledgeredaccounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AccountServiceTest
{
    private final static String ACCOUNT_NUMBER_1 = "Account 1";
    private final static String ACCOUNT_NUMBER_2 = "Account 2";
    private final static String ACCOUNT_NUMBER_3 = "Account 3";

    private Ledger ledger;
    private AccountService accountService;

    @BeforeEach
    void beforeEach()
    {
        ledger = new Ledger();

        accountService = new AccountService(ledger);
        boolean effect = accountService.createAccount(ACCOUNT_NUMBER_1);
        assertThat(effect).isTrue();
        effect = accountService.createAccount(ACCOUNT_NUMBER_2);
        assertThat(effect).isTrue();
    }

    @Test
    void cannotCreateAccountWithSameNumberAsExistingAccount()
    {
        assertThatThrownBy(() -> accountService.createAccount(ACCOUNT_NUMBER_1));
    }

    @Test
    void failsToCreateAccountAfterTooManyRetries()
    {
        ledger = new Ledger(0);
        accountService = new AccountService(ledger);
        boolean effect = accountService.createAccount(ACCOUNT_NUMBER_1);
        assertThat(effect).isFalse();
    }

    @Test
    void returnsCreatedAccount()
    {
        Account account = accountService.getAccount(ACCOUNT_NUMBER_1);
        assertThat(account).isNotNull();
        assertThat(account.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER_1);
    }
}
