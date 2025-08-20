package ru.umd.jbank.front_ui.integration.client.transfer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.umd.jbank.front_ui.integration.client.transfer.dto.ExternalTransferRequestDto;
import ru.umd.jbank.front_ui.integration.client.transfer.dto.InnerTransferRequestDto;
import ru.umd.jbank.front_ui.integration.client.transfer.dto.TransferResponseDto;

@FeignClient(
    name = "gateway-service",
    contextId = "transfer-client",
    path = "/transfer"
)
public interface TransferClient {
    @PostMapping("/inner")
    ResponseEntity<TransferResponseDto> innerTransfer(@RequestBody InnerTransferRequestDto request);
    
    @PostMapping("/external")
    ResponseEntity<TransferResponseDto> externalTransfer(@RequestBody ExternalTransferRequestDto request);
}
