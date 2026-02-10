package com.danapple.experiments.atomic.ledgeredaccounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TransferServiceTest
{
    private final static String ACCOUNT_NUMBER_1 = "Account 1";
    private final static String ACCOUNT_NUMBER_2 = "Account 2";

    private Ledger ledger;
    private TransferService transferService;

    @BeforeEach
    void beforeEach()
    {
        ledger = new Ledger();
        AccountService accountService = new AccountService(ledger);
        transferService = new TransferService(ledger);

        boolean effect = accountService.createAccount(ACCOUNT_NUMBER_1);
        assertThat(effect).isTrue();
        effect = accountService.createAccount(ACCOUNT_NUMBER_2);
        assertThat(effect).isTrue();
    }

    @Test
    void transfersBalance()
    {
        boolean effect = transferService.transferBalance(ACCOUNT_NUMBER_1,
                                                         ACCOUNT_NUMBER_2,
                                                         BigDecimal.ONE);
        assertThat(effect).isTrue();

        Accounts snapshot = ledger.getSnapshot();
        assertThat(snapshot
                           .getAccount(ACCOUNT_NUMBER_1)
                           .getBalance())
                .isEqualTo(BigDecimal.ONE.negate());
        assertThat(snapshot
                           .getAccount(ACCOUNT_NUMBER_2)
                           .getBalance())
                .isEqualTo(BigDecimal.ONE);
    }

    @Test
    void originalBalancesAreUnaffectedByTransfer()
    {
        Accounts originalSnapshot = ledger.getSnapshot();

        boolean effect = transferService.transferBalance(ACCOUNT_NUMBER_1,
                                                         ACCOUNT_NUMBER_2,
                                                         BigDecimal.ONE);
        assertThat(effect).isTrue();

        assertThat(originalSnapshot
                           .getAccount(ACCOUNT_NUMBER_1)
                           .getBalance())
                .isEqualTo(BigDecimal.ZERO);
        assertThat(originalSnapshot
                           .getAccount(ACCOUNT_NUMBER_2)
                           .getBalance())
                .isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void zeroValueTransferIsRejected()
    {
        assertThatThrownBy(() -> transferService.transferBalance(ACCOUNT_NUMBER_1,
                                                                 ACCOUNT_NUMBER_2,
                                                                 BigDecimal.ZERO));
    }

    @Test
    void negativeValueTransferIsRejected()
    {
        assertThatThrownBy(() -> transferService.transferBalance(ACCOUNT_NUMBER_1,
                                                                 ACCOUNT_NUMBER_2,
                                                                 BigDecimal.ONE.negate()));
    }

    @Test
    void nonExistentSourceAccountIsRejected()
    {
        assertThatThrownBy(() -> transferService.transferBalance("bad account number",
                                                                 ACCOUNT_NUMBER_2,
                                                                 BigDecimal.TEN));
    }

    @Test
    void nonExistentDestinationAccountIsRejected()
    {
        assertThatThrownBy(() -> transferService.transferBalance(ACCOUNT_NUMBER_1,
                                                                 "bad account #",
                                                                 BigDecimal.ONE));
    }

    @Test
    void failsToTransferBalanceAfterTooManyRetries()
    {
        ledger = new Ledger(0);
        transferService = new TransferService(ledger);

        boolean effect = transferService.transferBalance(ACCOUNT_NUMBER_1,
                                                         ACCOUNT_NUMBER_2,
                                                         BigDecimal.ONE);
        assertThat(effect).isFalse();
    }

    @Test
    void transferBackToSameAccountIsRejected()
    {
        assertThatThrownBy(() -> transferService.transferBalance(ACCOUNT_NUMBER_1,
                                                                 ACCOUNT_NUMBER_1,
                                                                 BigDecimal.ONE));
    }
}
