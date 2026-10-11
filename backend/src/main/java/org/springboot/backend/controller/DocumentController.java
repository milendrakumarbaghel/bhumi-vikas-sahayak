package org.springboot.backend.controller;

import java.net.MalformedURLException;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final Path sourcePdf;

    public DocumentController(@Value("${bvs.pdf-file}") String sourcePdf) {
        this.sourcePdf = Path.of(sourcePdf).toAbsolutePath().normalize();
    }

    @GetMapping("/rules.pdf")
    public ResponseEntity<Resource> rulesPdf() throws MalformedURLException {
        Resource resource = new UrlResource(sourcePdf.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename("MP_Bhumi_Vikas_Rules_2012_EN.pdf").build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
    }
}
