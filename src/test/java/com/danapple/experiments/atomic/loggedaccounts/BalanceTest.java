package com.danapple.experiments.atomic.loggedaccounts;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class BalanceTest
{
    @Test
    void startsWithSubmittedZeroBalance()
    {
        Balance balance = new Balance(BigDecimal.ZERO);
        assertThat(balance.getBalanceValue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void startsWithSubmittedPositiveBalance()
    {
        Balance balance = new Balance(BigDecimal.TEN);
        assertThat(balance.getBalanceValue()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    void startsWithEmptyLog()
    {
        Balance balance = new Balance(BigDecimal.TEN);
        assertThat(balance.getLogLength()).isZero();
    }

    @Test
    void startsWithPopulatedLog()
    {
        BalanceLogState state = new BalanceLogState();
        List<BalanceLogEntry> log = List.of(new BalanceLogEntry(BigDecimal.ONE, state, System.currentTimeMillis()));
        Balance balance = new Balance(BigDecimal.TEN, log);

        assertThat(balance.getLogLength()).isEqualTo(1);
    }

    @Test
    void ignoresPendingState()
    {
        BalanceLogState state = new BalanceLogState();
        List<BalanceLogEntry> log = List.of(new BalanceLogEntry(BigDecimal.ONE, state, System.currentTimeMillis()));
        Balance balance = new Balance(BigDecimal.TEN, log);

        assertThat(balance.getLogLength()).isEqualTo(1);

        assertThat(balance.getBalanceValue()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    void includesCompletedLog()
    {
        BalanceLogState state = new BalanceLogState();
        List<BalanceLogEntry> log = List.of(new BalanceLogEntry(BigDecimal.ONE, state, System.currentTimeMillis()));
        Balance balance = new Balance(BigDecimal.TEN, log);
        state.complete();

        assertThat(balance.getBalanceValue()).isEqualTo(BigDecimal.TEN.add(BigDecimal.ONE));
    }

    @Test
    void returnsPendingLog()
    {
        BalanceLogState state = new BalanceLogState();
        BalanceLogEntry logEntry = new BalanceLogEntry(BigDecimal.ONE, state, System.currentTimeMillis());
        List<BalanceLogEntry> log = List.of(logEntry);
        Balance balance = new Balance(BigDecimal.TEN, log);

        assertThat(balance.getPendingLogEntries()).containsExactly(logEntry);
    }

    @Test
    void doesNotReturnCompletedLog()
    {
        BalanceLogState state = new BalanceLogState();
        BalanceLogEntry logEntry = new BalanceLogEntry(BigDecimal.ONE, state, System.currentTimeMillis());
        List<BalanceLogEntry> log = List.of(logEntry);
        Balance balance = new Balance(BigDecimal.TEN, log);
        state.complete();

        assertThat(balance.getPendingLogEntries()).isEmpty();
    }

    @Test
    void createsNewBalanceWithPendingLog()
    {
        BalanceLogState state = new BalanceLogState();
        BalanceLogEntry logEntry = new BalanceLogEntry(BigDecimal.ONE, state, System.currentTimeMillis());
        Balance balance = new Balance(BigDecimal.TEN);
        Balance newBalance = balance.addLogEntry(logEntry);

        assertThat(newBalance.getPendingLogEntries()).containsExactly(logEntry);
    }

    @Test
    void createsNewBalanceWithoutCompletedLog()
    {
        BalanceLogState state = new BalanceLogState();
        BalanceLogEntry logEntry = new BalanceLogEntry(BigDecimal.ONE, state, System.currentTimeMillis());
        Balance balance = new Balance(BigDecimal.TEN);
        state.complete();

        Balance newBalance = balance.addLogEntry(logEntry);

        assertThat(newBalance.getPendingLogEntries()).isEmpty();
    }

    @Test
    void doesNotRemoveRecentLogEntry()
    {
        long nowTime = System.currentTimeMillis();

        BalanceLogState state = new BalanceLogState();
        BalanceLogEntry logEntry = new BalanceLogEntry(BigDecimal.ONE, state, nowTime);
        Balance balance = new Balance(BigDecimal.TEN);
        Balance adjustedBalance = balance.addLogEntry(logEntry);
        assertThat(adjustedBalance.getLogLength()).isEqualTo(1);

        Balance flattendBalance = adjustedBalance.flattenLog();

        assertThat(flattendBalance.getLogLength()).isEqualTo(1);
    }

    @Test
    void removesStaleLogEntry()
    {
        long nowTime = System.currentTimeMillis();
        long testTime = nowTime - 1000;

        BalanceLogState state = new BalanceLogState();
        BalanceLogEntry logEntry = new BalanceLogEntry(BigDecimal.ONE, state, testTime);
        Balance balance = new Balance(BigDecimal.TEN);
        Balance adjustedBalance = balance.addLogEntry(logEntry);
        assertThat(adjustedBalance.getLogLength()).isEqualTo(1);

        Balance flattendBalance = adjustedBalance.flattenLog();

        assertThat(flattendBalance.getLogLength()).isEqualTo(0);
    }
}
