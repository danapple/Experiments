package com.danapple.experiments.atomic.ledgeredaccounts;

import java.math.BigDecimal;

public class Account
{
    private final String accountNumber;
    private final BigDecimal balance;

    public Account(final String accountNumber)
    {
        this.accountNumber = accountNumber;
        this.balance = BigDecimal.ZERO;
    }

    private Account(final Account baseAccount, final BigDecimal newBalance)
    {
        this.accountNumber = baseAccount.accountNumber;
        this.balance = newBalance;
    }

    public String getAccountNumber()
    {
        return accountNumber;
    }

    public BigDecimal getBalance()
    {
        return balance;
    }

    Account adjustBalance(final BigDecimal adjustment)
    {
        return new Account(this, balance.add(adjustment));
    }
}
