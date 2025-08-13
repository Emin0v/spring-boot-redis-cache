package com.company.controller;

import com.company.model.dto.request.ProductServiceCodeRequest;
import com.company.model.dto.response.ProductServiceCodeResponse;
import com.company.model.enums.ClassificationType;
import com.company.service.ProductServiceCodeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/codes")
public class ProductServiceCodeController {

    private final ProductServiceCodeService productServiceCodeService;

    @GetMapping
    public ResponseEntity<List<ProductServiceCodeResponse>> getAll() {
        return ResponseEntity.ok(productServiceCodeService.findAll());
    }

    @GetMapping("/by-type")
    public ResponseEntity<List<ProductServiceCodeResponse>> getByType(@RequestParam @NotNull ClassificationType type) {
        return ResponseEntity.ok(productServiceCodeService.findAllByType(type));
    }

    @PostMapping
    public ResponseEntity<ProductServiceCodeResponse> create(@RequestBody @Valid ProductServiceCodeRequest request) {
        return ResponseEntity.ok(productServiceCodeService.createCode(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductServiceCodeResponse> update(@PathVariable @NotNull Long id,
                                                             @RequestBody @Valid ProductServiceCodeRequest request) {
        return ResponseEntity.ok(productServiceCodeService.updateCode(id, request));
    }
}
