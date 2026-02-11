package com.danapple.experiments.atomic.loggedaccounts;

import static com.danapple.experiments.atomic.loggedaccounts.BalanceLogStatus.ABORTED;
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
    void startsWithSubmittedNegativeBalance()
    {
        Balance balance = new Balance(BigDecimal.TEN.negate());
        assertThat(balance.getBalanceValue()).isEqualTo(BigDecimal.TEN.negate());
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

        assertThat(balance.getLogLength()).isEqualTo(1);
        assertThat(balance.getBalanceValue()).isEqualTo(BigDecimal.TEN.add(BigDecimal.ONE));
    }

    @Test
    void returnsPendingInitialLog()
    {
        BalanceLogState state = new BalanceLogState();
        BalanceLogEntry logEntry = new BalanceLogEntry(BigDecimal.ONE, state, System.currentTimeMillis());
        List<BalanceLogEntry> log = List.of(logEntry);
        Balance balance = new Balance(BigDecimal.TEN, log);

        assertThat(balance.getPendingLogEntries()).containsExactly(logEntry);
    }

    @Test
    void doesNotReturnCompletedInitialLog()
    {
        BalanceLogState state = new BalanceLogState();
        BalanceLogEntry logEntry = new BalanceLogEntry(BigDecimal.ONE, state, System.currentTimeMillis());
        List<BalanceLogEntry> log = List.of(logEntry);
        Balance balance = new Balance(BigDecimal.TEN, log);
        state.complete();

        assertThat(balance.getPendingLogEntries()).isEmpty();
    }

    @Test
    void pendingLogEntriesIncludesPendingLog()
    {
        BalanceLogState state = new BalanceLogState();
        BalanceLogEntry logEntry = new BalanceLogEntry(BigDecimal.ONE, state, System.currentTimeMillis());
        Balance balance = new Balance(BigDecimal.TEN);
        Balance newBalance = balance.addLogEntry(logEntry);

        assertThat(newBalance.getPendingLogEntries()).containsExactly(logEntry);
    }

    @Test
    void pendingLogEntriesDoesNotIncludeCompletedLog()
    {
        BalanceLogState state = new BalanceLogState();
        BalanceLogEntry logEntry = new BalanceLogEntry(BigDecimal.ONE, state, System.currentTimeMillis());
        Balance balance = new Balance(BigDecimal.TEN);

        Balance newBalance = balance.addLogEntry(logEntry);
        state.complete();

        assertThat(newBalance.getPendingLogEntries()).isEmpty();
    }

    @Test
    void pendingLogEntriesDoesNotIncludeAbortedLog()
    {
        BalanceLogState state = new BalanceLogState();
        BalanceLogEntry logEntry = new BalanceLogEntry(BigDecimal.ONE, state, System.currentTimeMillis());
        Balance balance = new Balance(BigDecimal.TEN);

        Balance newBalance = balance.addLogEntry(logEntry);
        state.abort();

        assertThat(newBalance.getPendingLogEntries()).isEmpty();
    }

    @Test
    void flattenedBalanceHasNewValue()
    {
        BalanceLogState state = new BalanceLogState();
        BalanceLogEntry logEntry = new BalanceLogEntry(BigDecimal.ONE, state, System.currentTimeMillis());
        Balance balance = new Balance(BigDecimal.TEN);

        Balance newBalance = balance.addLogEntry(logEntry);
        state.complete();

        Balance flattenedBalance = newBalance.flattenLog();
        assertThat(flattenedBalance.getBalanceValue()).isEqualTo(BigDecimal.TEN.add(BigDecimal.ONE));
        assertThat(flattenedBalance.getLogLength()).isZero();
    }

    @Test
    void doesNotRemoveRecentPendingLogEntry()
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
    void abortsAndRemovesStalePendingLogEntry()
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
        assertThat(state.getStatus()).isEqualTo(ABORTED);
    }
}
