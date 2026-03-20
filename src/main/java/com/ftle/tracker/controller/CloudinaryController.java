package com.ftle.tracker.controller;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/")
public class CloudinaryController {

    @Autowired
    private Cloudinary cloudinary;

    @GetMapping("/private/signature")
    public Map<String, Object> generateSignature(@RequestParam(value = "folder", required = false) String folder) {

        long timestamp = System.currentTimeMillis() / 1000;

        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", timestamp);

        // CRITICAL: If you send 'folder' from React, it MUST be here
        if (folder != null && !folder.isEmpty()) {
            params.put("folder", folder);
        }

        // apiSignRequest automatically alphabetizes and signs the params
        String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", timestamp);
        response.put("signature", signature);
        response.put("apiKey", cloudinary.config.apiKey);
        response.put("cloudName", cloudinary.config.cloudName);
        response.put("folder", folder); // Return it so frontend knows what was signed

        return response;
    }
}
