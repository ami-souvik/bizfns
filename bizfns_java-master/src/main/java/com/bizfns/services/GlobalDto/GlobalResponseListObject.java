package com.bizfns.services.GlobalDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GlobalResponseListObject {
    private Boolean success;
    private String message;
    private List data;

}
