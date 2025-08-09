package ru.umd.jbank.blocker.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.umd.jbank.blocker.service.BlockerService;

@RestController
@RequiredArgsConstructor
public class BlockerController {
    private final BlockerService blockerService;

    @PostMapping("/validate")
    public BlockerResponseDto validate() {
        var result = blockerService.validate();
        return new BlockerResponseDto(result, result ? "OK" : "Validation failed");
    }

    private record BlockerResponseDto(
        boolean valid, String message
    ) {
    }
}
