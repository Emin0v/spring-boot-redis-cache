package com.company.model.dto.response;

import com.company.model.enums.ClassificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductServiceCodeResponse {

    private Long id;
    private String code;
    private String name;
    private ClassificationType type;
}
