package com.danapple.experiments.atomic.loggedaccounts;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

public class Account
{
    private final String accountNumber;
    private final AtomicReference<Balance> balance;

    public Account(final String accountNumber)
    {
        this.accountNumber = accountNumber;
        this.balance = new AtomicReference<>(new Balance(BigDecimal.ZERO));
    }

    public String getAccountNumber()
    {
        return accountNumber;
    }

    // Coverage ignores this method if it is lower in the class.  Weird, but I want 100% coverage :-)
    Balance getRawBalance()
    {
        return balance.get();
    }

    public BigDecimal getBalance()
    {
        Balance oldBalance = balance.get();
        Balance newBalance = oldBalance.flattenLog();
        balance.compareAndSet(oldBalance, newBalance);
        return oldBalance.getBalanceValue();
    }

    boolean adjustBalance(final BigDecimal adjustment,
                          final BalanceLogState sharedState)
    {
        Balance oldBalance = balance.get();
        if (adjustment.compareTo(BigDecimal.ZERO) < 0)
        {
            BigDecimal oldBalanceValue = oldBalance.getBalanceValue();
            if (adjustment.abs().compareTo(oldBalanceValue) > 0)
            {
                throw new RuntimeException("Account " + accountNumber +
                                                   " has insufficient balance (" + oldBalanceValue +
                                                   " ) for withdrawal of " + adjustment);
            }
        }

        BalanceLogEntry logEntry = new BalanceLogEntry(adjustment, sharedState, System.currentTimeMillis());
        Balance newBalance = oldBalance.addLogEntry(logEntry);
        return balance.compareAndSet(oldBalance, newBalance);
    }
}
