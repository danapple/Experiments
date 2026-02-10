Please feel to peruse and comment on the experiments contained in this repository.

The first set of experiments was to build lock-free atomic funds transfers between accounts.  These were purely in-memory exercises, without any database.

My first attempt was ledgeredaccounts. In this version, all Accounts and their balances are held in a snapshot, and a transfer between Accounts builds a new snapshot of all Accounts. Accounts and their balances are immutable, so changing the balance on an Account creates a new version of the Account. When funds are transferred between two Accounts, new versions of both of the Accounts are created. Then a new snapshot of all of the Accounts are created, with the new versions of the two subject Accounts with their new balances replacing the old versions.

This approach is interesting because it provides a consistent view of all Accounts in the system.  In a single snapshot, the balances on all Accounts will always sum to zero.  

However, it is very heavyweight, with a high memory and processor demand to create new snapshots, so it is not suitable for a system with a large number of accounts and a high frequency of updates.

The second version was loggedaccounts, which takes a completely different approach.  Instead of a snapshot of all accounts, each Account has a replaceable but immutable Balance object.  The Balance object contains a starting balance value and a log of changes to the value.  As transfers occur, an entry is added to the log of both the Balances of both Accounts involved in the transfer.

However, what makes the transfer atomic is that both log entries, one for each Account, share a common BalanceLogState which has an atomically referenced BalanceLogStatus. The BalanceLogState starts with a status of PENDING, during which the Account's balance computation will ignore the log entry. Only once the TransferService determines that the log entries were successfully added to both Accounts' Balances will it atomically change the log entry status to COMPLETE. In this manner, both Accounts will simultaneously change the results of subsequent calls to return their balance values.

There is no consistent snapshot created which will guarantee a view of all Accounts, or even two Accounts which were involved in a transfer, having a consistent set of balances.  This version, however, is highly scalable and does not have heavy memory or processor demands.

The loggedaccounts version was inspired by Lie Ryan's 25 May 2015 comment in this stackoverflow:
https://stackoverflow.com/a/29281872
However, instead of using a lock, I used an AtomicReference to hold a status.

References to other discussions about this problem are:
https://stackoverflow.com/questions/29280857/java-synchronisation-atomically-moving-money-across-account-pairs/29281872#29281872

https://halilural5.medium.com/how-to-write-a-simple-money-transfer-service-that-is-thread-safe-and-lock-free-bc3c282948d6