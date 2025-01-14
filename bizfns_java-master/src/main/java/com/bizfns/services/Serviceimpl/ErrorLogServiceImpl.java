package com.bizfns.services.Serviceimpl;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import com.bizfns.services.Service.ErrorLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Service
public class ErrorLogServiceImpl implements ErrorLogService {


    public void errorLog(Object request, String errorMessage, String apiUrl, String id, String deviceInfo) {
        // String userId = request.get("userId");
        final String UPLOAD_FOLDER_PATH_TEST = "D:\\bizFnsTestPath\\BizFnsLog";

        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Create the date and time string using the current date and time
        String dateTimeString = currentDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        // Create the folder path with the current date
        String folderPath = UPLOAD_FOLDER_PATH_TEST + File.separator + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Create the file name with the current date and time along with the jobId
        String fileName = id + ".txt";

        // Create the folder if it doesn't exist
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Create the file path
        String filePath = folderPath + File.separator + fileName;

        try {
            // Check if the file exists in the specified path
            File file = new File(filePath);

            // Append content to the file
            FileWriter writer = new FileWriter(file, true); // true flag for append mode
            if (!file.exists()) {
                // Write separator lines if it's a new file
                writer.write("=========================================\n");
            }
            writer.write("Timestamp: " + currentDateTime.toString() + "\n");
            writer.write("Job ID: " + id + "\n");
            writer.write("apiUrl: " + apiUrl + "\n");
            writer.write("deviceInfo: " + deviceInfo + "\n");

            writer.write("Request: " + request.toString() + "\n");
            writer.write("errorMessage: " + errorMessage + "\n");

            writer.write("=========================================\n");
            writer.close();

            //       return ResponseEntity.accepted().body(new GlobalResponseDTO(true, "", "userId"));
        } catch (Exception e) {
            // Handle any potential IO exception
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new GlobalResponseDTO(false, "Error occurred while creating or appending to the file", "userId"));
        }
    }


}
