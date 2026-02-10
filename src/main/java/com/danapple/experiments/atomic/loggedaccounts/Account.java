package com.danapple.experiments.atomic.loggedaccounts;

import java.math.BigDecimal;
import java.util.List;
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

    public BigDecimal getBalance()
    {
        Balance oldBalance = balance.get();
        BigDecimal balanceValue = oldBalance.getBalanceValue();
        List<BalanceLogEntry> pendingLogEntries = oldBalance.getPendingLogEntries();
        Balance newBalance = new Balance(balanceValue, pendingLogEntries);
        balance.compareAndSet(oldBalance, newBalance);
        return balanceValue;
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

        BalanceLogEntry logEntry = new BalanceLogEntry(adjustment, sharedState);
        Balance newBalance = oldBalance.addLogEntry(logEntry);
        return balance.compareAndSet(oldBalance, newBalance);
    }

    Balance getRawBalance()
    {
        return balance.get();
    }
}
