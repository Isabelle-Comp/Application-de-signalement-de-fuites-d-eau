package com.example.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/signalements")
public class SignalementController {

    private final SignalementRepository repo;
    private final Path dossier;

    public SignalementController(SignalementRepository repo,
                                 @Value("${app.photos-dir}") String dir) throws IOException {
        this.repo = repo;
        this.dossier = Path.of(dir).toAbsolutePath().normalize();
        Files.createDirectories(dossier);
    }

    @GetMapping
    public List<Signalement> lister() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "dateCreation"));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Signalement creer(@RequestParam String quartier,
                             @RequestParam String description,
                             @RequestParam MultipartFile photo) throws IOException {
        if (quartier.isBlank() || description.isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Champs obligatoires");
        if (photo.getSize() > 2 * 1024 * 1024)
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Photo > 2 Mo");

        String type = photo.getContentType();
        String ext = "image/jpeg".equals(type) ? ".jpg" : "image/png".equals(type) ? ".png" : null;
        if (ext == null)
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "JPEG ou PNG seulement");

        String nom = UUID.randomUUID() + ext;
        Files.copy(photo.getInputStream(), dossier.resolve(nom));

        Signalement s = new Signalement();
        s.setQuartier(quartier);
        s.setDescription(description);
        s.setPhoto(nom);
        return repo.save(s);
    }

    @PatchMapping("/{id}/statut")
    public Signalement changerStatut(@PathVariable Long id, @RequestParam Statut statut) {
        Signalement s = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        s.setStatut(statut);
        return repo.save(s);
    }

    @GetMapping("/stats")
    public List<SignalementRepository.QuartierCount> stats() {
        return repo.compterParQuartier();
    }

    @GetMapping("/photos/{nom}")
    public ResponseEntity<Resource> photo(@PathVariable String nom) throws IOException {
        Path f = dossier.resolve(nom).normalize();
        if (!f.startsWith(dossier) || !Files.exists(f))
            return ResponseEntity.notFound().build();
        MediaType mt = nom.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(mt).body(new UrlResource(f.toUri()));
    }
}