package com.bizfns.services.Controller;


import com.bizfns.services.Serviceimpl.GoogleDocsService;
import com.google.api.services.docs.v1.model.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/api/docs")
public class GoogleDocsController {

    private final GoogleDocsService googleDocsService;

    @Autowired
    public GoogleDocsController(GoogleDocsService googleDocsService) {
        this.googleDocsService = googleDocsService;
    }

    @PostMapping("/createDocument")
    public ResponseEntity<Document> createDocument(@RequestBody String title) {
        try {
            Document createdDocument = googleDocsService.getDocument(title);
            return new ResponseEntity<>(createdDocument, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/{documentId}/write")
    public ResponseEntity<String> writeToDocument(
            @PathVariable String documentId,
            @RequestBody String text) {
        try {
            googleDocsService.writeToDocument(documentId, text);
            return new ResponseEntity<>("Text successfully written to document.", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{documentId}/read")
    public ResponseEntity<String> readFromDocument(@PathVariable String documentId) {
        try {
            String content = googleDocsService.readFromDocument(documentId);
            return new ResponseEntity<>(content, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }


}
