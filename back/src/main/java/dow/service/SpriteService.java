package dow.service;

import dow.exception.AlreadyExist;
import dow.model.dto.HitboxDto;
import dow.model.dto.ModifSpriteDto;
import dow.model.dto.SpriteInfos;
import dow.model.entities.Animation;
import dow.model.entities.Sprite;
import dow.model.enumeration.AnimationType;
import dow.repository.AnimationRepository;
import dow.repository.SpriteRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class SpriteService {

    private static final Logger log = LoggerFactory.getLogger(SpriteService.class);
    private static final String SEPARATOR = "═══════════════════════════════════════════════════════════";
    private static final int ALPHA_THRESHOLD = 10;
    private static final int MIN_ABSOLUTE_WIDTH = 5;
    private static final double RESIDUAL_THRESHOLD = 0.3;
    private static final double LARGE_BLOCK_FACTOR = 1.9;
    private static final double STDDEV_MULTIPLIER = 2.0;
    private static final List<AnimationType> ANIMATION_TYPES = List.of(
            AnimationType.IDLE,
            AnimationType.WALK,
            AnimationType.ATTACK
    );

    private final Path storageRoot;
    private final SpriteRepository spriteRepository;
    private final AnimationRepository animationRepository;

    @Value("${sprite.storage.root}")
    private String spriteStorage;

    public SpriteService(SpriteRepository spriteRepository,
                         AnimationRepository animationRepository,
                         @Value("${sprite.storage.root}") String storageRoot) {
        this.spriteRepository = spriteRepository;
        this.animationRepository = animationRepository;
        this.storageRoot = Paths.get(storageRoot).toAbsolutePath().normalize();
        initStorage();
    }

    private void initStorage() {
        try {
            if (Files.notExists(storageRoot)) {
                Files.createDirectories(storageRoot);
                log.info("Stockage créé: {}", storageRoot);
            } else {
                log.debug("Stockage existant: {}", storageRoot);
            }
        } catch (IOException e) {
            log.error("Impossible d'initialiser le stockage des sprites", e);
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public SpriteInfos processSpriteZip(MultipartFile zipFile) {
        log.info("Début traitement ZIP: {}", zipFile.getOriginalFilename());
        validateZipFile(zipFile);

        Path tempDir = null;
        try {
            tempDir = unzipToTempDirectory(zipFile);
            File spriteRoot = findSpriteRoot(tempDir);
            String spriteName = spriteRoot.getName();
            log.info("Sprite détecté: '{}'", spriteName);

            validateSpriteNotExists(spriteName);

            Sprite sprite = new Sprite(spriteName);
            sprite.setScale(1);

            processAnimationsMetaData(spriteRoot, sprite);
            storeSpriteFiles(spriteRoot, spriteName);
            spriteRepository.save(sprite);

            log.info("Sprite '{}' importé avec succès", spriteName);
            logSeparator();

            return spriteRepository.findSpriteInfosByTypeAndName(AnimationType.IDLE, spriteName);

        } catch (AlreadyExist e) {
            log.warn("Sprite déjà existant: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erreur ZIP: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur import sprite: " + e.getMessage(), e);
        } finally {
            cleanupTempDirectory(tempDir);
        }
    }

    private void validateZipFile(MultipartFile zipFile) {
        if (zipFile == null || zipFile.isEmpty()) {
            throw new IllegalArgumentException("ZIP vide");
        }
        log.debug(
                "Validation ZIP OK: {} ({} bytes)",
                zipFile.getOriginalFilename(),
                zipFile.getSize()
        );
    }

    private void validateSpriteNotExists(String spriteName) {
        if (spriteRepository.findByName(spriteName).isPresent()) {
            throw new AlreadyExist(
                    "Sprite déjà existant: '" + spriteName + "'"
            );
        }
        log.debug("Nom disponible: '{}'", spriteName);
    }

    private void processAnimationsMetaData(File spriteRoot, Sprite sprite) {
        log.info("Analyse des animations...");
        int total = 0;

        for (AnimationType type : ANIMATION_TYPES) {
            File typeDir = new File(spriteRoot, type.name());
            if (!typeDir.exists()) {
                continue;
            }

            File[] images = getImageFiles(typeDir);
            if (images.length == 0) {
                continue;
            }

            Map<File, Integer> frameCount = analyzeAnimationFrames(images);
            createAnimations(images, frameCount, type, sprite);
            total += images.length;
        }

        log.info("{} animations créées", total);
    }

    private File[] getImageFiles(File dir) {
        File[] images = dir.listFiles(f ->
                f.isFile() && f.getName().toLowerCase().endsWith(".png")
        );

        if (images == null) {
            return new File[0];
        }

        Arrays.sort(images, Comparator.comparing(File::getName));
        return images;
    }

    private Map<File, Integer> analyzeAnimationFrames(File[] images) {
        Map<File, Integer> frameCount = new HashMap<>();

        for (File f : images) {
            try {
                BufferedImage img = ImageIO.read(f);
                frameCount.put(f, img != null ? detectFrames(img) : 1);
            } catch (IOException e) {
                log.warn("Image illisible {}: 1 frame par défaut", f.getName());
                frameCount.put(f, 1);
            }
        }

        return frameCount;
    }

    private void createAnimations(
            File[] images,
            Map<File, Integer> frameCountByImage,
            AnimationType type,
            Sprite sprite
    ) {
        for (int i = 0; i < images.length; i++) {
            File imgFile = images[i];
            try {
                BufferedImage img = ImageIO.read(imgFile);
                if (img != null) {
                    sprite.addAnimation(
                            new Animation(
                                    frameCountByImage.get(imgFile),
                                    img.getWidth(),
                                    img.getHeight(),
                                    type,
                                    i + 1,
                                    8
                            )
                    );
                }
            } catch (IOException e) {
                log.error(
                        "Erreur création animation {}.{}: {}",
                        type,
                        i + 1,
                        e.getMessage()
                );
            }
        }
    }

    private int detectFrames(BufferedImage img) {
        boolean[] columns = scanColumns(img);
        List<Integer> widths = extractFrameWidths(columns);

        if (widths.isEmpty()) {
            return 1;
        }

        double avgWidth = calculateRobustAverageWidth(widths);
        return Math.max(1, countFrames(widths, avgWidth));
    }

    private boolean[] scanColumns(BufferedImage img) {
        boolean[] cols = new boolean[img.getWidth()];

        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int alpha = (img.getRGB(x, y) >>> 24) & 0xff;
                if (alpha > ALPHA_THRESHOLD) {
                    cols[x] = true;
                    break;
                }
            }
        }

        return cols;
    }

    private List<Integer> extractFrameWidths(boolean[] columns) {
        List<Integer> widths = new ArrayList<>();
        int current = 0;

        for (boolean hasPixel : columns) {
            if (hasPixel) {
                current++;
            } else if (current > 0) {
                widths.add(current);
                current = 0;
            }
        }

        if (current > 0) {
            widths.add(current);
        }

        return widths;
    }

    private double calculateRobustAverageWidth(List<Integer> widths) {
        List<Integer> filtered = widths.stream()
                .filter(w -> w >= MIN_ABSOLUTE_WIDTH)
                .toList();

        if (filtered.isEmpty()) {
            return 0;
        }

        double mean = filtered.stream()
                .mapToInt(i -> i)
                .average()
                .orElse(0);

        double stdDev = Math.sqrt(
                filtered.stream()
                        .mapToDouble(i -> (i - mean) * (i - mean))
                        .average()
                        .orElse(0)
        );

        return filtered.stream()
                .filter(w -> Math.abs(w - mean) <= STDDEV_MULTIPLIER * stdDev)
                .mapToInt(i -> i)
                .average()
                .orElse(mean);
    }

    private int countFrames(List<Integer> widths, double avgWidth) {
        int total = 0;

        for (int w : widths) {
            if (w < MIN_ABSOLUTE_WIDTH || w < avgWidth * RESIDUAL_THRESHOLD) {
                continue;
            }

            if (w > avgWidth * LARGE_BLOCK_FACTOR) {
                total += Math.max(1, (int) Math.floor(w / avgWidth));
            } else {
                total += 1;
            }
        }

        return total;
    }

    private Path unzipToTempDirectory(MultipartFile zipFile) throws IOException {
        Path dest = Files.createTempDirectory("sprite_upload_");

        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                Path newPath = dest.resolve(entry.getName()).normalize();

                if (!newPath.startsWith(dest)) {
                    throw new IOException("ZIP invalide: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Files.createDirectories(newPath.getParent());
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        return dest;
    }

    private File findSpriteRoot(Path tempDir) {
        File[] folders = tempDir.toFile().listFiles(File::isDirectory);

        List<File> valid = Arrays.stream(folders != null ? folders : new File[0])
                .filter(f -> !f.getName().startsWith("__"))
                .toList();

        if (valid.size() != 1) {
            throw new RuntimeException("ZIP doit contenir un seul dossier racine");
        }

        return valid.getFirst();
    }

    private void cleanupTempDirectory(Path tempDir) {
        if (tempDir != null) {
            try {
                FileSystemUtils.deleteRecursively(tempDir);
            } catch (IOException ignored) {
            }
        }
    }

    private void storeSpriteFiles(File sourceRoot, String spriteName) throws IOException {
        Path targetRoot = storageRoot.resolve(spriteName);

        if (Files.exists(targetRoot)) {
            FileSystemUtils.deleteRecursively(targetRoot);
        }

        Files.createDirectories(targetRoot);

        int totalFiles = 0;

        for (AnimationType type : ANIMATION_TYPES) {
            totalFiles += copyAnimationFiles(sourceRoot, targetRoot, type);
        }

        log.info("{} fichiers copiés vers le stockage", totalFiles);
    }

    private int copyAnimationFiles(File srcRoot, Path targetRoot, AnimationType type) throws IOException {
        File srcDir = new File(srcRoot, type.name());

        if (!srcDir.exists()) {
            return 0;
        }

        Path targetDir = targetRoot.resolve(type.name());
        Files.createDirectories(targetDir);

        File[] images = getImageFiles(srcDir);
        AtomicInteger counter = new AtomicInteger(1);

        for (File img : images) {
            Path targetPath = targetDir.resolve(counter.getAndIncrement() + ".png");
            Files.copy(img.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return images.length;
    }

    @Transactional
    public SpriteInfos normalizeSpriteSheet(Long animationId) throws IOException {
        SpriteInfos info = spriteRepository.findSpriteInfosByAnimationId(animationId);

        if (info == null) {
            throw new IllegalArgumentException("Animation introuvable ID: " + animationId);
        }

        Path filePath = Paths.get(spriteStorage, info.getImageUrl());
        BufferedImage original = ImageIO.read(filePath.toFile());

        if (original == null) {
            throw new IOException("Impossible de lire l'image");
        }

        BufferedImage normalized = rebuildFinalSprite(original, info.getFrames());

        File targetFile = filePath.toFile();
        ImageIO.write(normalized, "png", targetFile);

        optimizeWithPngQuant(targetFile);

        Animation anim = animationRepository.findById(animationId).orElseThrow();
        anim.setWidth(normalized.getWidth());
        anim.setHeight(normalized.getHeight());
        animationRepository.save(anim);

        log.info(
                "Reconstruction et compression terminées: {}x{}px, {} frames",
                normalized.getWidth(),
                normalized.getHeight(),
                info.getFrames()
        );

        logSeparator();
        return spriteRepository.findSpriteInfosByAnimationId(animationId);
    }

    private void optimizeWithPngQuant(File file) {
        try {
            String[] command = {
                    "pngquant",
                    "--force",
                    "--ext", ".png",
                    "--quality=60-80",
                    "--strip",
                    file.getAbsolutePath()
            };

            log.debug("Exécution pngquant sur: {}", file.getName());

            Process process = Runtime.getRuntime().exec(command);

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Compression pngquant réussie pour: {}", file.getName());
            } else {
                log.warn("Pngquant a terminé avec le code erreur: {}", exitCode);
            }
        } catch (Exception e) {
            log.error("Échec de l'optimisation pngquant (l'image reste non compressée): {}", e.getMessage());
        }
    }

    private BufferedImage rebuildFinalSprite(BufferedImage original, int frameCount) {
        List<BufferedImage> frames = splitByFrameCount(original, frameCount);
        int[] boundsArr = calculateGlobalBounds(frames);

        if (boundsArr == null) {
            return original;
        }

        Bounds bounds = new Bounds(boundsArr);
        int newWidth = bounds.width();
        int newHeight = bounds.height();
        BufferedImage output = new BufferedImage(frameCount * newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        int xCursor = 0;
        for (BufferedImage frame : frames) {
            for (int x = 0; x < newWidth; x++) {
                for (int y = 0; y < newHeight; y++) {
                    int sx = bounds.minX + x;
                    int sy = bounds.minY + y;

                    if (sx < frame.getWidth() && sy < frame.getHeight()) {
                        output.setRGB(xCursor + x, y, frame.getRGB(sx, sy));
                    }
                }
            }
            xCursor += newWidth;
        }

        return output;
    }

    private List<BufferedImage> splitByFrameCount(BufferedImage img, int frameCount) {
        int fw = img.getWidth() / frameCount;
        List<BufferedImage> frames = new ArrayList<>();

        for (int i = 0; i < frameCount; i++) {
            BufferedImage frame = img.getSubimage(i * fw, 0, fw, img.getHeight());
            frames.add(frame);
        }

        return frames;
    }

    private int[] calculateGlobalBounds(List<BufferedImage> frames) {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        boolean hasContent = false;

        for (BufferedImage frame : frames) {
            int[] b = computeContentBounds(frame);
            if (b != null) {
                hasContent = true;
                minX = Math.min(minX, b[0]);
                maxX = Math.max(maxX, b[1]);
                minY = Math.min(minY, b[2]);
                maxY = Math.max(maxY, b[3]);
            }
        }

        return hasContent ? new int[]{minX, maxX, minY, maxY} : null;
    }

    private int[] computeContentBounds(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();

        int minX = w;
        int maxX = 0;
        int minY = h;
        int maxY = 0;
        boolean hasPixels = false;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int alpha = (img.getRGB(x, y) >>> 24) & 0xff;
                if (alpha > ALPHA_THRESHOLD) {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                    hasPixels = true;
                }
            }
        }

        return hasPixels ? new int[]{minX, maxX, minY, maxY} : null;
    }

    public ResponseEntity<Resource> getSprite(HttpServletRequest request) throws IOException {
        String relative = request.getRequestURI().replace("/api/sprite/sprite-storage/", "");
        Path filePath = Paths.get(spriteStorage, relative);

        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            Resource resource = new UrlResource(filePath.toUri());
            String type = Files.probeContentType(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(type != null ? type : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName() + "\"")
                    .body(resource);
        }

        return ResponseEntity.notFound().build();
    }

    public List<SpriteInfos> findAllSpriteInfosByAnimationType() {
        return spriteRepository.findAllSpriteInfosByAnimationType(AnimationType.IDLE);
    }

    public List<SpriteInfos> findAllAnimationsBySpriteName(String name) {
        return spriteRepository.findAllAnimationsBySpriteName(name);
    }

    @Transactional
    public void deleteSpriteByName(String name) {
        log.info("Suppression du sprite '{}'", name);
        spriteRepository.deleteByName(name);

        try {
            FileSystemUtils.deleteRecursively(storageRoot.resolve(name));
        } catch (IOException ignored) {
        }

        logSeparator();
    }

    @Transactional
    public SpriteInfos renameSprite(ModifSpriteDto dto) {
        Sprite sprite = spriteRepository.findByName(dto.getOldName()).orElseThrow();

        if (!sprite.getName().equals(dto.getNewName())) {
            renameSpriteFolder(sprite, dto.getNewName());
        }

        if (!Objects.equals(sprite.getScale(), dto.getScale())) {
            sprite.setScale(dto.getScale());
        }

        spriteRepository.save(sprite);
        return spriteRepository.findSpriteInfosByTypeAndName(AnimationType.IDLE, sprite.getName());
    }

    private void renameSpriteFolder(Sprite sprite, String newName) {
        Path oldPath = storageRoot.resolve(sprite.getName());
        Path newPath = storageRoot.resolve(newName);
        sprite.setName(newName);

        try {
            if (Files.exists(oldPath)) {
                Files.move(
                        oldPath,
                        newPath,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur renommage sprite", e);
        }
    }

    @Transactional
    public void flipHorizontal(Long animationId) {
        SpriteInfos info = spriteRepository.findSpriteInfosByAnimationId(animationId);
        Path filePath = Paths.get(spriteStorage, info.getImageUrl());

        try {
            BufferedImage original = ImageIO.read(filePath.toFile());
            BufferedImage flipped = new BufferedImage(
                    original.getWidth(),
                    original.getHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );

            Graphics2D g2d = flipped.createGraphics();
            int fw = original.getWidth() / info.getFrames();

            for (int i = 0; i < info.getFrames(); i++) {
                g2d.drawImage(
                        original,
                        (i + 1) * fw,
                        0,
                        i * fw,
                        original.getHeight(),
                        i * fw,
                        0,
                        (i + 1) * fw,
                        original.getHeight(),
                        null
                );
            }

            g2d.dispose();
            ImageIO.write(flipped, "png", filePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Erreur flip sprite", e);
        }
    }

    private void logSeparator() {
        log.info(SEPARATOR);
    }

    public Animation saveFrameRate(Long animationId, int frameRate) {
        Animation anim = animationRepository.findById(animationId).orElseThrow();
        anim.setFrameRate(frameRate);
        return animationRepository.save(anim);
    }

    @Transactional
    public void saveHitbox(Long animationId, HitboxDto hitboxDto) {
        Animation animation = animationRepository.findById(animationId)
                .orElseThrow(() -> new IllegalArgumentException("Animation not found: " + animationId));

        animation.setHitboxX(hitboxDto.getX());
        animation.setHitboxY(hitboxDto.getY());
        animation.setHitboxWidth(hitboxDto.getWidth());
        animation.setHitboxHeight(hitboxDto.getHeight());

        animationRepository.save(animation);
        log.info("Hitbox saved for animation {}: {}x{} at ({}, {})",
                animationId, hitboxDto.getWidth(), hitboxDto.getHeight(),
                hitboxDto.getX(), hitboxDto.getY());
    }

    @Transactional
    public void deleteHitbox(Long animationId) {
        Animation animation = animationRepository.findById(animationId)
                .orElseThrow(() -> new IllegalArgumentException("Animation not found: " + animationId));

        animation.setHitboxX(null);
        animation.setHitboxY(null);
        animation.setHitboxWidth(null);
        animation.setHitboxHeight(null);

        animationRepository.save(animation);
        log.info("Hitbox deleted for animation {}", animationId);
    }

    private record Bounds(int minX, int maxX, int minY, int maxY) {
        Bounds(int[] arr) {
            this(arr[0], arr[1], arr[2], arr[3]);
        }

        int width() {
            return maxX - minX + 1;
        }

        int height() {
            return maxY - minY + 1;
        }
    }
}
