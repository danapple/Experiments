package com.danapple.experiments.atomic.ledgeredaccounts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class AccountsTest
{
    private final Accounts accounts = new Accounts();

    @Test
    void returnsNullForUnknownAccount() {
        assertThat(accounts.getAccount("foo")).isNull();
    }

    @Test
    void newVersionContainsNewAccount()
    {
        Account newAccount = new Account("new1");
        Accounts newAccounts = accounts.newVersion(newAccount, newAccount);
        assertThat(newAccounts.getAccount("new1")).isEqualTo(newAccount);
    }

    @Test
    void addingAccountDoesNotAddToOriginalAccounts()
    {
        Account newAccount = new Account("new1");
        accounts.newVersion(newAccount, newAccount);
        assertThat(accounts.getAccount("new1")).isNull();
    }
}
