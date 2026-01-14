package perso.dow.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import perso.dow.model.dto.HitboxDto;
import perso.dow.model.dto.ModifSpriteDto;
import perso.dow.model.dto.SpriteInfos;
import perso.dow.model.dto.SpritePlay;
import perso.dow.model.entities.Animation;
import perso.dow.service.SpriteService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/sprite")
public class SpriteController {

    private final SpriteService spriteService;


    public SpriteController(SpriteService spriteService) {
        this.spriteService = spriteService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SpriteInfos uploadSprite(@RequestParam("file") MultipartFile zipFile) {
        return spriteService.processSpriteZip(zipFile);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SpriteInfos>> getAllSpritesInfos() {
        List<SpriteInfos> spriteInfos = spriteService.getAllSpritesInfos();
        return ResponseEntity.ok(spriteInfos);
    }

    @DeleteMapping("/delete/{spriteName}")
    public ResponseEntity<Void> deleteSpriteByName(@PathVariable String spriteName) {
        spriteService.deleteSpriteByName(spriteName);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/rename")
    public SpriteInfos renameSprite(@RequestBody ModifSpriteDto modifSpriteDto) {
        return spriteService.renameSprite(modifSpriteDto);
    }

    @GetMapping("/sprite-storage/**")
    public ResponseEntity<Resource> getSprite(HttpServletRequest request) throws IOException {
        return spriteService.getSprite(request);
    }

    @GetMapping("/animations/{spriteName}")
    public List<SpriteInfos> getAllAnimationsBySpriteName(@PathVariable String spriteName) {
        return spriteService.getAllAnimationsBySpriteName(spriteName);
    }

    @GetMapping("/normalize-sprite-sheet/{animationId}")
    public SpriteInfos normalizeSpriteSheet(@PathVariable Long animationId) throws IOException {
        return spriteService.normalizeSpriteSheet(animationId);
    }

    @GetMapping("/flip-horizontal/{animationId}")
    public ResponseEntity<Void> flipHorizontal(@PathVariable Long animationId) {
        spriteService.flipHorizontal(animationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/play/{spriteName}")
    public SpritePlay getSpritePlay(@PathVariable String spriteName) {
        return spriteService.getSpritePlay(spriteName);
    }

    @PutMapping("/save-frame-rate/{animationId}/{frameRate}")
    public Animation saveFrameRate(@PathVariable Long animationId, @PathVariable int frameRate) {
        return spriteService.saveFrameRate(animationId, frameRate);
    }

    @PutMapping("/hitbox/{animationId}")
    public ResponseEntity<Void> saveHitbox(
            @PathVariable Long animationId,
            @RequestBody HitboxDto hitboxDto) {
        spriteService.saveHitbox(animationId, hitboxDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/hitbox/{animationId}")
    public ResponseEntity<Void> deleteHitbox(@PathVariable Long animationId) {
        spriteService.deleteHitbox(animationId);
        return ResponseEntity.noContent().build();
    }
}