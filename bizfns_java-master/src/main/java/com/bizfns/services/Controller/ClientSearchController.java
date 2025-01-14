package com.bizfns.services.Controller;

import com.bizfns.services.Service.ClientSearchService;
import com.bizfns.services.Serviceimpl.ClientSearchServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//@CrossOrigin(origins = {"http://localhost", " http://13.235.93.16"})
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class ClientSearchController {
    @Autowired
    private ClientSearchService clientSearchService;




    @GetMapping("/clientList")
    public Map<String, Object> fetchAllClients() {
        return clientSearchService.fetchAllClients();
    }
    }

