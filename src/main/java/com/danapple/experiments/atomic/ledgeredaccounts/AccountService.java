package com.danapple.experiments.atomic.ledgeredaccounts;

public class AccountService
{
    private final Ledger ledger;

    public AccountService(final Ledger ledger)
    {
        this.ledger = ledger;
    }

    public boolean createAccount(final String accountNumber)
    {
        int retries = 0;
        while (retries++ < ledger.getRetryCount())
        {
            if (createAccountAtomic(accountNumber)) {
                return true;
            }
        }
        return false;
    }

    private boolean createAccountAtomic(final String accountNumber)
    {
        Accounts startingAccounts = ledger.getSnapshot();
        if (startingAccounts.getAccount(accountNumber) != null)
        {
            throw new RuntimeException("Account " + accountNumber + " already exists");
        }

        Account newAccount = new Account(accountNumber);
        Accounts newAccounts = startingAccounts.newVersion(newAccount);

        return ledger.replaceSnapshot(startingAccounts, newAccounts);
    }

    public Account getAccount(final String accountNumber)
    {
        Accounts startingAccounts = ledger.getSnapshot();
        return startingAccounts.getAccount(accountNumber);
    }
}
