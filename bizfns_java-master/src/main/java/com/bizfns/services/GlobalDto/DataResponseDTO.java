package com.bizfns.services.GlobalDto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataResponseDTO {
    private boolean success;
    private Map<String, Object> data;
}
