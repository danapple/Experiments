package com.danapple.experiments.atomic.ledgeredaccounts;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class AccountTest
{
    private final Account account = new Account("TEST_ACCOUNT");

    @Test
    void accountNumberIsStored()
    {
        String accountNumber = "asdf";
        Account localAccount = new Account(accountNumber);
        assertThat(localAccount.getAccountNumber()).isEqualTo("asdf");
    }

    @Test
    void accountStartsWithZeroBalance()
    {
        assertThat(account.getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void adjustingBalanceLeavesOriginalAccountUnchanged()
    {
        account.adjustBalance(BigDecimal.ONE);
        assertThat(account.getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void accountCanBeAdjustedPositively()
    {
        Account newAccount = account.adjustBalance(BigDecimal.ONE);
        assertThat(newAccount.getBalance()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void accountCanBeAdjustedNegatively()
    {
        Account newAccount = account.adjustBalance(BigDecimal.ONE.negate());
        assertThat(newAccount.getBalance()).isEqualTo(BigDecimal.ONE.negate());
    }
}
