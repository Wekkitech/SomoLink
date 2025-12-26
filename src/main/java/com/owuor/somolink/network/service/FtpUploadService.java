package com.owuor.somolink.network.service;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class FtpUploadService {

    public void uploadFiles(MultipartFile[] files) throws Exception {
        FTPClient ftpClient = new FTPClient();

        try {
            // MikroTik IP
            String FTP_HOST = "192.168.88.1";
            int FTP_PORT = 21;
            ftpClient.connect(FTP_HOST, FTP_PORT);
            String FTP_USER = "admin";
            String FTP_PASS = "admin";
            ftpClient.login(FTP_USER, FTP_PASS);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            for (MultipartFile file : files) {
                String remoteFile = "/hotspot/" + file.getOriginalFilename();
                try (InputStream inputStream = file.getInputStream()) {
                    boolean done = ftpClient.storeFile(remoteFile, inputStream);
                    if (!done) {
                        throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename());
                    }
                }
            }
        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
    }
}
