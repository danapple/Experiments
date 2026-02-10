package com.danapple.experiments.atomic.loggedaccounts;

import static com.danapple.experiments.atomic.loggedaccounts.BalanceLogStatus.ABORTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TransferServiceTest
{
    private final static String ACCOUNT_NUMBER_SOURCE = "Source Account";
    private final static String ACCOUNT_NUMBER_DESTINATION = "Destination Account";
    private final static BigDecimal THREE_POINT_SEVEN = new BigDecimal("3.7");

    private final TransferService transferService = new TransferService();
    private final Account sourceAccount = new Account(ACCOUNT_NUMBER_SOURCE);
    private final Account destinationAccount = new Account(ACCOUNT_NUMBER_DESTINATION);

    @Test
    void rejectsZeroValueTransfer()
    {
        assertThatThrownBy(() -> transferService.transferBalance(sourceAccount,
                                                                 destinationAccount,
                                                                 BigDecimal.ZERO));
    }

    @Test
    void rejectsNegativeValueTransfer()
    {
        assertThatThrownBy(() -> transferService.transferBalance(sourceAccount,
                                                                 destinationAccount,
                                                                 BigDecimal.ONE.negate()));
    }

    @Test
    void rejectsTransferToSourceAccount()
    {
        assertThatThrownBy(() -> transferService.transferBalance(sourceAccount,
                                                                 sourceAccount,
                                                                 BigDecimal.TEN));
    }

    @Test
    void transfersPositiveIntegralValue()
    {
        seedSourceAccount();
        transferService.transferBalance(sourceAccount,
                                        destinationAccount,
                                        BigDecimal.TEN);

        assertThat(sourceAccount.getBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(destinationAccount.getBalance()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    void transfersPositiveDecimalValue()
    {
        seedSourceAccount();
        transferService.transferBalance(sourceAccount,
                                        destinationAccount,
                                        THREE_POINT_SEVEN);

        assertThat(sourceAccount.getBalance()).isEqualTo(BigDecimal.TEN.add(THREE_POINT_SEVEN.negate()));
        assertThat(destinationAccount.getBalance()).isEqualTo(THREE_POINT_SEVEN);
    }

    @Test
    void refusesTransferWithInsufficientSourceAccount()
    {
        assertThatThrownBy(() ->
                                   transferService.transferBalance(sourceAccount,
                                                                   destinationAccount,
                                                                   THREE_POINT_SEVEN))
                .hasMessageContaining("insufficient");

        assertThat(sourceAccount.getBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(destinationAccount.getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void abortedTransferDoesNotAffectSourceAccount()
    {
        Account badDestinationAccount = mock(Account.class);
        Mockito.doThrow(RuntimeException.class).when(badDestinationAccount).adjustBalance(any(), any());

        assertThatThrownBy(() -> transferService.transferBalance(sourceAccount,
                                                                 badDestinationAccount,
                                                                 BigDecimal.TEN));

        assertThat(sourceAccount.getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void transferRejectedWithAbortedState()
    {
        seedSourceAccount();

        BalanceLogState state = new BalanceLogState();
        state.abort();

        assertThatThrownBy(() -> transferService.transferBalance(sourceAccount,
                                            destinationAccount,
                                            BigDecimal.TEN,
                                            state))
                .hasMessageContaining("before transfer was completed");

        assertThat(sourceAccount.getBalance()).isEqualTo(BigDecimal.TEN);
        assertThat(destinationAccount.getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void transferRejectedWithSourceAccountRefusingAdjustment()
    {
        Account badSourceAccount = mock(Account.class);
        when(badSourceAccount.getAccountNumber()).thenReturn("bad source account");
        when(badSourceAccount.adjustBalance(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> transferService.transferBalance(badSourceAccount,
                                                                 destinationAccount,
                                                                 BigDecimal.TEN))
                .hasMessageContaining("Could not transfer balance with");

        assertThat(destinationAccount.getBalance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void transferRejectedWithDestinationAccountRefusingAdjustment()
    {
        seedSourceAccount();
        Account badDestinationAccount = mock(Account.class);
        when(badDestinationAccount.adjustBalance(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> transferService.transferBalance(sourceAccount,
                                                                 badDestinationAccount,
                                                                 BigDecimal.TEN))
                .hasMessageContaining("Could not transfer balance with");

        assertThat(sourceAccount.getBalance()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    void singleTransferAttemptRejectedWithSourceAccountRefusingAdjustment()
    {
        BalanceLogState state = new BalanceLogState();
        Account badSourceAccount = mock(Account.class);
        Account testDestinationAccount = mock(Account.class);

        when(badSourceAccount.getAccountNumber()).thenReturn("bad source account");
        when(badSourceAccount.adjustBalance(any(), any())).thenReturn(false);
        when(testDestinationAccount.adjustBalance(any(), any())).thenReturn(true);

        assertThat(transferService.transferBalance(badSourceAccount,
                                                   testDestinationAccount,
                                                   BigDecimal.TEN,
                                                   state)).isFalse();

        assertThat(state.getStatus()).isEqualTo(ABORTED);
        assertThat(destinationAccount.getBalance()).isEqualTo(BigDecimal.ZERO);
        verify(testDestinationAccount, times(0)).adjustBalance(any(), any());
    }

    @Test
    void singleTransferAttemptRejectedWithDestinationAccountRefusingAdjustment()
    {
        seedSourceAccount();

        BalanceLogState state = new BalanceLogState();
        Account badDestinationAccount = mock(Account.class);
        when(badDestinationAccount.adjustBalance(any(), any())).thenReturn(false);

        assertThat(transferService.transferBalance(sourceAccount,
                                                   badDestinationAccount,
                                                   BigDecimal.TEN,
                                                   state)).isFalse();

        assertThat(state.getStatus()).isEqualTo(ABORTED);
        assertThat(sourceAccount.getBalance()).isEqualTo(BigDecimal.TEN);

    }

    private void seedSourceAccount()
    {
        BalanceLogState depositLogState = new BalanceLogState();
        sourceAccount.adjustBalance(BigDecimal.TEN, depositLogState);
        depositLogState.complete();
    }
}
