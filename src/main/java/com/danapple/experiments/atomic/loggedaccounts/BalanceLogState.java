package com.danapple.experiments.atomic.loggedaccounts;

import static com.danapple.experiments.atomic.loggedaccounts.BalanceLogStatus.ABORTED;
import static com.danapple.experiments.atomic.loggedaccounts.BalanceLogStatus.COMPLETE;
import static com.danapple.experiments.atomic.loggedaccounts.BalanceLogStatus.PENDING;

import java.util.concurrent.atomic.AtomicReference;

class BalanceLogState
{
    private final AtomicReference<BalanceLogStatus> status;

    BalanceLogState()
    {
        status = new AtomicReference<>(PENDING);
    }

    BalanceLogStatus getStatus()
    {
        return status.get();
    }

    boolean abort()
    {
        return status.compareAndSet(PENDING, ABORTED);
    }

    boolean complete()
    {
        return status.compareAndSet(PENDING, COMPLETE);
    }
}
