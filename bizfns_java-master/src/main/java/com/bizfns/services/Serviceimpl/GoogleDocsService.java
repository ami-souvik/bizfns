package com.bizfns.services.Serviceimpl;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.docs.v1.model.*;

import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDocsService {

    private static final String APPLICATION_NAME = "Huzefa";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/documents");
    private static final String CREDENTIALS_FILE_PATH = "classpath:credentials.json";

    @Autowired
    private ResourceLoader resourceLoader;

    private Docs service;

    GoogleCredential credential;

    public Document createDocument(String title) throws IOException, GeneralSecurityException {
        Resource resource = resourceLoader.getResource(CREDENTIALS_FILE_PATH);
        InputStream inputStream = resource.getInputStream();

        credential = GoogleCredential
                .fromStream(inputStream)
                .createScoped(SCOPES);

        Docs service = new Docs.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        Document document = new Document().setTitle(title);
        Document createdDocument = service.documents().create(document).execute();

        System.out.println("Created document: " + createdDocument.getDocumentId());

        return createdDocument;
    }


    public void writeToDocument(String documentId, String text) throws IOException, GeneralSecurityException {
        // Build the request for inserting text
        List<Request> requests = new ArrayList<>();
        requests.add(new Request()
                .setInsertText(new InsertTextRequest()
                        .setText(text)
                        .setLocation(new Location()
                                .setIndex(1))));

        // Execute the batch update request
        BatchUpdateDocumentRequest body = new BatchUpdateDocumentRequest().setRequests(requests);
        Docs service = new Docs.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        service.documents().batchUpdate(documentId, body).execute();
    }


    public String readFromDocument(String documentId) throws IOException, GeneralSecurityException {

        Docs service = new Docs.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        // Execute the request to retrieve the document
        Document doc = service.documents().get(documentId).execute();

        // Extract the content from the document
        StringBuilder content = new StringBuilder();
        List<StructuralElement> bodyContent = doc.getBody().getContent();
        for (StructuralElement element : bodyContent) {
            if (element.getParagraph() != null) {
                for (ParagraphElement paragraphElement : element.getParagraph().getElements()) {
                    if (paragraphElement.getTextRun() != null) {
                        content.append(paragraphElement.getTextRun().getContent());
                    }
                }
            }
        }

        return content.toString();
    }


    public Docs initializeDocsService() throws IOException, GeneralSecurityException {
        // Load credentials from the credentials file
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                        GoogleDocsService.class.getResourceAsStream("credentials.json"))
                .createScoped(Collections.singleton(DocsScopes.DOCUMENTS));

        // Initialize HTTP transport and JSON factory
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        // Create Docs service using credentials
        return new Docs.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


    //private final Docs docsService;

    @Autowired
    public GoogleDocsService() throws GeneralSecurityException, IOException {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        service = new Docs.Builder(httpTransport, JacksonFactory.getDefaultInstance(), null)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public Document getDocument(String documentId) throws IOException {
        try{
            return service.documents().get(documentId).execute();

        }catch (Exception e ){
            e.printStackTrace();
        }
        return null;
        }


}
