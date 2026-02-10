package com.danapple.experiments.atomic.loggedaccounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class AccountTest
{
    private final static String ACCOUNT_NUMBER_1 = "Account 1";
    private final static BigDecimal TWO_POINT_THREE = new BigDecimal("2.3");

    @Test
    void accountHoldsAccountNumbers()
    {
        Account account = new Account(ACCOUNT_NUMBER_1);
        assertThat(account.getAccountNumber()).isEqualTo(ACCOUNT_NUMBER_1);
    }

    @Test
    void accountStartsWithZeroBalance()
    {
        Account account = new Account(ACCOUNT_NUMBER_1);
        assertThat(account.getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void accountRejectsOverWithdrawalFromZeroBalance()
    {
        Account account = new Account(ACCOUNT_NUMBER_1);
        assertThatThrownBy(() -> account.adjustBalance(BigDecimal.TEN.negate(), new BalanceLogState()));
    }

    @Test
    void accountRejectsOverWithdrawalFromInsufficientPositiveBalance()
    {
        Account account = new Account(ACCOUNT_NUMBER_1);
        BalanceLogState logState = new BalanceLogState();
        account.adjustBalance(BigDecimal.ONE, logState);
        logState.complete();

        assertThatThrownBy(() -> account.adjustBalance(BigDecimal.TEN.negate(), new BalanceLogState()));
    }

    @Test
    void accountAcceptsOverWithdrawalFromSufficientPositiveBalance()
    {
        Account account = new Account(ACCOUNT_NUMBER_1);
        BalanceLogState depositLogState = new BalanceLogState();
        account.adjustBalance(BigDecimal.TEN, depositLogState);
        depositLogState.complete();

        BalanceLogState withdrawalLogState = new BalanceLogState();
        assertThat(account.adjustBalance(BigDecimal.ONE.negate(), withdrawalLogState)).isTrue();
        withdrawalLogState.complete();

        assertThat(account.getBalance()).isEqualTo(BigDecimal.TEN.add(BigDecimal.ONE.negate()));
    }

    @Test
    void accountIgnoresPendingAdjustment()
    {
        Account account = new Account(ACCOUNT_NUMBER_1);
        BalanceLogState sharedState = new BalanceLogState();

        account.adjustBalance(BigDecimal.TEN, sharedState);
        assertThat(account.getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void accountIgnoresAbortedAdjustment()
    {
        Account account = new Account(ACCOUNT_NUMBER_1);
        BalanceLogState sharedState = new BalanceLogState();

        account.adjustBalance(BigDecimal.TEN, sharedState);
        sharedState.abort();

        assertThat(account.getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void accountBalanceIncludesIntegralCompletedAdjustment()
    {
        Account account = new Account(ACCOUNT_NUMBER_1);
        BalanceLogState sharedState = new BalanceLogState();

        account.adjustBalance(BigDecimal.TEN, sharedState);
        sharedState.complete();

        assertThat(account.getBalance()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    void accountBalanceIncludesFloatCompletedAdjustment()
    {
        Account account = new Account(ACCOUNT_NUMBER_1);
        BalanceLogState sharedState = new BalanceLogState();

        account.adjustBalance(TWO_POINT_THREE, sharedState);
        sharedState.complete();

        assertThat(account.getBalance()).isEqualTo(TWO_POINT_THREE);
    }

    @Test
    void logContainsOneEntryAfterAdjustment()
    {
        Account account = new Account(ACCOUNT_NUMBER_1);
        BalanceLogState sharedState = new BalanceLogState();

        account.adjustBalance(TWO_POINT_THREE, sharedState);
        sharedState.complete();

        Balance rawBalance = account.getRawBalance();
        assertThat(rawBalance.getLogLength()).isEqualTo(1);
    }

    @Test
    void logContainsOneEntryAfterTwoAdjustments()
    {
        Account account = new Account(ACCOUNT_NUMBER_1);

        BalanceLogState sharedState1 = new BalanceLogState();
        account.adjustBalance(TWO_POINT_THREE, sharedState1);
        sharedState1.complete();

        BalanceLogState sharedState2 = new BalanceLogState();
        account.adjustBalance(TWO_POINT_THREE, sharedState2);
        sharedState2.complete();

        Balance rawBalance = account.getRawBalance();
        assertThat(rawBalance.getLogLength()).isEqualTo(2);
    }

    @Test
    void logIsResetToZeroLengthAfterBalanceQuery()
    {
        Account account = new Account(ACCOUNT_NUMBER_1);
        BalanceLogState sharedState = new BalanceLogState();

        account.adjustBalance(TWO_POINT_THREE, sharedState);
        sharedState.complete();
        assertThat(account.getBalance()).isEqualTo(TWO_POINT_THREE);

        Balance rawBalance = account.getRawBalance();
        assertThat(rawBalance.getLogLength()).isZero();
    }
}
