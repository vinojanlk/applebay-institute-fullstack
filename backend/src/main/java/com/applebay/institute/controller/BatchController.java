package com.applebay.institute.controller;

import com.applebay.institute.dto.request.BatchRequest;
import com.applebay.institute.dto.response.BatchResponse;
import com.applebay.institute.service.BatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @GetMapping
    public List<BatchResponse> getBatches(@RequestParam(required = false) Long courseId) {
        return batchService.getBatches(courseId);
    }

    @GetMapping("/{id}")
    public BatchResponse getBatch(@PathVariable Long id) {
        return batchService.getBatch(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BatchResponse createBatch(@Valid @RequestBody BatchRequest request) {
        return batchService.createBatch(request);
    }

    @PutMapping("/{id}")
    public BatchResponse updateBatch(
            @PathVariable Long id,
            @Valid @RequestBody BatchRequest request
    ) {
        return batchService.updateBatch(id, request);
    }
}
