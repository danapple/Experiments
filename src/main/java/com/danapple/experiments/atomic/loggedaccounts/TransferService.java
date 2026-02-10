package com.danapple.experiments.atomic.loggedaccounts;

import java.math.BigDecimal;

public class TransferService
{
    private final int atomicRetries;

    public TransferService()
    {
        this(10);
    }

    public TransferService(final int atomicRetries)
    {
        this.atomicRetries = atomicRetries;
    }

    public void transferBalance(final Account sourceAccount,
                                final Account destinationAccount,
                                final BigDecimal transferAmount)
    {
        if (transferAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new RuntimeException("Transfer amount " + transferAmount + " must be positive");
        }
        if (sourceAccount.getAccountNumber().equals(destinationAccount.getAccountNumber()))
        {
            throw new RuntimeException("May not transfer back to the same account "
                                               + sourceAccount.getAccountNumber());
        }

        for (int i = 0; i < atomicRetries; i++)
        {
            if (transferBalance(sourceAccount, destinationAccount, transferAmount, new BalanceLogState()))
            {
                return;
            }
        }
        throw new RuntimeException("Could not transfer balance with " + atomicRetries + " tries");
    }

    boolean transferBalance(final Account sourceAccount,
                            final Account destinationAccount,
                            final BigDecimal transferAmount,
                            final BalanceLogState sharedState)
    {
        boolean adjusted = sourceAccount.adjustBalance(transferAmount.negate(), sharedState);
        if (adjusted)
        {
            adjusted = destinationAccount.adjustBalance(transferAmount, sharedState);
        }
        if (!adjusted)
        {
            sharedState.abort();
            return false;
        }

        if (!sharedState.complete())
        {
            throw new RuntimeException("Transfer status was " + sharedState.getStatus() +
                                               " before transfer was completed");
        }
        return true;
    }
}
