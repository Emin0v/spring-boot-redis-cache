package com.company.model.dto.request;

import com.company.model.enums.ClassificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductServiceCodeRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotNull
    private ClassificationType type;
}
