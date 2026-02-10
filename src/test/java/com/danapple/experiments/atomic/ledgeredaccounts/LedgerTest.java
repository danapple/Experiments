package com.danapple.experiments.atomic.ledgeredaccounts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LedgerTest
{
    private Ledger ledger;

    @BeforeEach
    void beforeEach()
    {
        ledger = new Ledger();
    }

    @Test
    void replacesSnapshotWhenOldSnapshotUnchanged()
    {
        Accounts oldSnapshot = ledger.getSnapshot();
        Accounts newSnapshot = oldSnapshot.newVersion();

        assertThat(ledger.replaceSnapshot(oldSnapshot, newSnapshot)).isTrue();

        assertThat(ledger.getSnapshot()).isEqualTo(newSnapshot);
    }


    @Test
    void doesNotReplaceSnapshotWhenOldSnapshotChanged()
    {
        Accounts firstSnapshot = ledger.getSnapshot();

        Accounts newSnapshot = firstSnapshot.newVersion();

        assertThat(ledger.replaceSnapshot(firstSnapshot, newSnapshot)).isTrue();

        assertThat(ledger.replaceSnapshot(firstSnapshot, newSnapshot)).isFalse();
    }

    @Test
    void returnsDefaultRetryCount()
    {
        assertThat(ledger.getRetryCount()).isEqualTo(10);
    }

    @Test
    void returnsCustomRetryCount()
    {
        ledger = new Ledger(11);
        assertThat(ledger.getRetryCount()).isEqualTo(11);
    }

}
