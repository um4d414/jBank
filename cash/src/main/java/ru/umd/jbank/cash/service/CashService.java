package ru.umd.jbank.cash.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.umd.jbank.cash.integration.client.account.AccountClient;
import ru.umd.jbank.cash.service.dto.CashOperationRequest;

@Service
@RequiredArgsConstructor
public class CashService {
    private final AccountClient accountClient;

    public void processDeposit(CashOperationRequest cashOperationRequest) {
        accountClient.makeDeposit(cashOperationRequest.accountId(), cashOperationRequest.amount());
    }

    public void processWithdrawal(CashOperationRequest cashOperationRequest) {
        accountClient.makeWithdrawal(cashOperationRequest.accountId(), cashOperationRequest.amount());
    }
}
