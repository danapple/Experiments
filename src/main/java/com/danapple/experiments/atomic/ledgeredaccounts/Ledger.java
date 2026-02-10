package com.danapple.experiments.atomic.ledgeredaccounts;

import java.util.concurrent.atomic.AtomicReference;

public class Ledger
{
    private final AtomicReference<Accounts> accounts = new AtomicReference<>();
    private final int retryCount;

    public int getRetryCount()
    {
        return retryCount;
    }

    public Ledger()
    {
        this(10);
    }

    public Ledger(final int retryCount)
    {
        accounts.set(new Accounts());
        this.retryCount = retryCount;
    }

    Accounts getSnapshot()
    {
        return accounts.get();
    }

    boolean replaceSnapshot(final Accounts oldAccounts, final Accounts newAccounts)
    {
        return accounts.compareAndSet(oldAccounts, newAccounts);
    }
}
