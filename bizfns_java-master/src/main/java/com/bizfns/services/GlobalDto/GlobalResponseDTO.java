package com.bizfns.services.GlobalDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GlobalResponseDTO {


    private Boolean success;
    private String message;
    private Object data;

    public GlobalResponseDTO(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public GlobalResponseDTO(String noFileProvidedForUpload) {

    }

    public GlobalResponseDTO(String success, String timesheetSavedSuccessfully) {
    }
}
