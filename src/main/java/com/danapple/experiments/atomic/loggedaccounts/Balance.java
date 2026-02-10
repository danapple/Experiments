package com.danapple.experiments.atomic.loggedaccounts;

import static com.danapple.experiments.atomic.loggedaccounts.BalanceLogStatus.PENDING;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Balance
{
    private final BigDecimal balance;
    private final List<BalanceLogEntry> log;

    Balance(final BigDecimal balance)
    {
        this.balance = balance;
        this.log = Collections.emptyList();
    }

    Balance(final BigDecimal balance,
            final List<BalanceLogEntry> newLog)
    {
        this.balance = balance;
        this.log = newLog;
    }

    Balance addLogEntry(BalanceLogEntry logEntry)
    {
        List<BalanceLogEntry> newList = new ArrayList<>(log);
        newList.add(logEntry);
        return new Balance(balance, newList);
    }

    BigDecimal getBalanceValue()
    {
        BigDecimal computedBalance = balance;
        for (BalanceLogEntry entry : log)
        {
            if (entry.state().getStatus() != BalanceLogStatus.COMPLETE)
            {
                continue;
            }
            computedBalance = computedBalance.add(entry.delta());
        }
        return computedBalance;
    }

    int getLogLength()
    {
        return log.size();
    }

    List<BalanceLogEntry> getPendingLogEntries()
    {
        return log
                .stream()
                .filter(log -> log.state().getStatus().equals(PENDING))
                .toList();
    }
}
