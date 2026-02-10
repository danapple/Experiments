package com.danapple.experiments.atomic.loggedaccounts;

import static com.danapple.experiments.atomic.loggedaccounts.BalanceLogStatus.ABORTED;
import static com.danapple.experiments.atomic.loggedaccounts.BalanceLogStatus.PENDING;
import static com.danapple.experiments.atomic.loggedaccounts.BalanceLogStatus.COMPLETE;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BalanceLogStateTest
{
    @Test
    void startsInPending()
    {
        BalanceLogState state = new BalanceLogState();
        assertThat(state.getStatus()).isEqualTo(PENDING);
    }

    @Test
    void acceptsChangeToCompleted()
    {
        BalanceLogState state = new BalanceLogState();
        assertThat(state.complete()).isTrue();
        assertThat(state.getStatus()).isEqualTo(COMPLETE);
    }

    @Test
    void acceptsChangeToAborted()
    {
        BalanceLogState state = new BalanceLogState();
        assertThat(state.abort()).isTrue();
        assertThat(state.getStatus()).isEqualTo(ABORTED);
    }

    @Test
    void refusedChangeFromAbortedToComplete()
    {
        BalanceLogState state = new BalanceLogState();
        assertThat(state.abort()).isTrue();
        assertThat(state.complete()).isFalse();
        assertThat(state.getStatus()).isEqualTo(ABORTED);
    }

    @Test
    void refusedChangeFromCompleteToAborted()
    {
        BalanceLogState state = new BalanceLogState();
        assertThat(state.complete()).isTrue();
        assertThat(state.abort()).isFalse();
        assertThat(state.getStatus()).isEqualTo(COMPLETE);
    }
}
