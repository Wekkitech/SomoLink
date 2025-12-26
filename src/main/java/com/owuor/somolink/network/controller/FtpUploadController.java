package com.owuor.somolink.network.controller;

import com.owuor.somolink.network.service.FtpUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/mikrotik")
public class FtpUploadController {

    private final FtpUploadService ftpUploadService;

    @Autowired
    public FtpUploadController(FtpUploadService ftpUploadService) {
        this.ftpUploadService = ftpUploadService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body("No files selected");
        }

        try {
            ftpUploadService.uploadFiles(files);
            return ResponseEntity.ok("Files uploaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }
}
