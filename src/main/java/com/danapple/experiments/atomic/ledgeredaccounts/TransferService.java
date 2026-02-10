package com.danapple.experiments.atomic.ledgeredaccounts;

import java.math.BigDecimal;

public class TransferService
{
    private final Ledger ledger;

    public TransferService(final Ledger ledger)
    {
        this.ledger = ledger;
    }

    public boolean transferBalance(final String sourceAccountNumber,
                                   final String destinationAccountNumber,
                                   final BigDecimal transferAmount)
    {
        if (transferAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new RuntimeException("Transfer amount " + transferAmount + " must be positive");
        }
        if (sourceAccountNumber.equals(destinationAccountNumber))
        {
            throw new RuntimeException("May not transfer back to the same account " + sourceAccountNumber);
        }

        int retries = 0;
        while (retries++ < ledger.getRetryCount())
        {
            if (transferBalanceAtomic(sourceAccountNumber,
                                      destinationAccountNumber,
                                      transferAmount)) {
                return true;
            }
        }
        return false;
    }

    private boolean transferBalanceAtomic(final String sourceAccountNumber,
                                          final String destinationAccountNumber,
                                          final BigDecimal transferAmount)
    {
        Accounts startingAccounts = ledger.getSnapshot();

        Account sourceAccount = startingAccounts.getAccount(sourceAccountNumber);
        if (sourceAccount == null)
        {
            throw new RuntimeException("Source account " + sourceAccountNumber + " does not exist");
        }
        Account destinationAccount = startingAccounts.getAccount(destinationAccountNumber);
        if (destinationAccount == null)
        {
            throw new RuntimeException("Destination account " + destinationAccountNumber + " does not exist");
        }

        Account newSourceAccount = sourceAccount.adjustBalance(transferAmount.negate());
        Account newDestinationAccount = destinationAccount.adjustBalance(transferAmount);

        Accounts newAccounts = startingAccounts.newVersion(newSourceAccount, newDestinationAccount);

        return ledger.replaceSnapshot(startingAccounts, newAccounts);
    }

}
