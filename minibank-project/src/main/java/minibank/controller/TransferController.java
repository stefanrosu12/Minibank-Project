package minibank.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import minibank.dto.TransferRequest;
import minibank.model.Transfer;
import minibank.service.TransferService;
import java.time.Instant;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<Transfer> createTransfer(@Valid @RequestBody TransferRequest request){
        Transfer createdTransfer = transferService.createTransfer(request);
        return new ResponseEntity<>(createdTransfer, HttpStatus.CREATED);
    }

    @GetMapping("/{transferId}")
    public ResponseEntity<Transfer> getTransfer(@PathVariable Long transferId){
        Transfer transfer = transferService.getTransfer(transferId);
        return ResponseEntity.ok(transfer);
    }

    @GetMapping
    public ResponseEntity<Page<Transfer>> getTransfers(
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) Instant fromDate,
            @RequestParam(required = false) Instant toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){

        Pageable pageable = PageRequest.of(page, size);
        Page<Transfer> transfers = transferService.getTransfers(iban,fromDate,toDate, pageable);
        return ResponseEntity.ok(transfers);
    }
}
