package com.verity.controller;

import com.verity.model.Document;
import com.verity.model.User;
import com.verity.service.DocumentIngestionService;
import com.verity.service.DocumentService;
import com.verity.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    private static final String COOKIE_NAME =
            "VERITY_SESSION";

    private final DocumentService documentService;
    private final DocumentIngestionService ingestionService;
    private final UserService userService;

    public DocumentController(
            DocumentService documentService,
            DocumentIngestionService ingestionService,
            UserService userService) {

        this.documentService = documentService;
        this.ingestionService = ingestionService;
        this.userService = userService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPdf(

            @CookieValue(
                    value = COOKIE_NAME,
                    required = false
            )
            String token,

            @RequestParam("file")
            MultipartFile file,

            @RequestParam(
                    value = "uploadedBy",
                    required = false
            )
            String ignoredUploadedBy,

            @RequestParam(
                    value = "requiredRole",
                    defaultValue = "ALL"
            )
            String requiredRole) {

        try {

            /*
             * Authenticate the actual logged-in user.
             */
            User user =
                    userService.findByToken(token);

            if (user == null) {

                return ResponseEntity
                        .status(401)
                        .body("Please login first.");
            }

            /*
             * Validate file.
             */
            if (file == null ||
                    file.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("File is empty.");
            }

            String fileName =
                    file.getOriginalFilename();

            if (fileName == null ||
                    !fileName
                            .toLowerCase()
                            .endsWith(".pdf")) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "Only PDF files are supported."
                        );
            }

            /*
             * IMPORTANT:
             * We deliberately use the authenticated
             * user's email rather than trusting the
             * uploadedBy value sent by the browser.
             */
            Document document =
                    ingestionService.ingest(
                            file,
                            user.getEmail(),
                            requiredRole
                    );

            return ResponseEntity.ok(document);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Failed to process document: "
                                    + e.getMessage()
                    );
        }
    }

    @GetMapping
    public ResponseEntity<?> getDocuments(

            @CookieValue(
                    value = COOKIE_NAME,
                    required = false
            )
            String token,

            @RequestParam(
                    value = "uploadedBy",
                    required = false
            )
            String ignoredUploadedBy) {

        User user =
                userService.findByToken(token);

        if (user == null) {

            return ResponseEntity
                    .status(401)
                    .body("Please login first.");
        }

        return ResponseEntity.ok(
                documentService.getDocumentsForUser(
                        user.getEmail()
                )
        );
    }
}