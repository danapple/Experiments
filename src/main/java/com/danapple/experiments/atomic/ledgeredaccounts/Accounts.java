package com.danapple.experiments.atomic.ledgeredaccounts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class Accounts
{
    private final Map<String, Account> accounts;

    Accounts() {
        accounts = new HashMap<>();
    }

    private Accounts(final Map<String, Account> newMap)
    {
        accounts = newMap;
    }

    Account getAccount(final String accountNumber)
    {
        return accounts.get(accountNumber);
    }

    Accounts newVersion(final Account... replacementAccounts)
    {
        Map<String, Account> newMap = new HashMap<>(accounts);
        Arrays.asList(replacementAccounts)
                .forEach(account -> newMap.put(account.getAccountNumber(), account));

        return new Accounts(newMap);
    }
}
