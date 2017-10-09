package de.ottenwbe.homedisplay.images;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Copyright (c) 2017 Beate Ottenwälder
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@Service
@Slf4j
public class SynchronizationService {

    private final ImageRepository imageRepository;

    // array of supported extensions (use a List if you prefer)
    private static final String IMAGE_EXTENSION = "jpg";

    @Value("${images.sync.path:}")
    private String imagePath;

    @Value("${images.sync.init.enabled:true}")
    private Boolean initEnabled;

    private volatile boolean synchronizing = false;

    @Autowired
    public SynchronizationService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @PostConstruct
    public void initialSynchronization() {
        if (initEnabled) {
            synchronizeImages();
        }
    }

    void synchronizeImages() {
        log.info("[Started] Loading Images, starting from {}", imagePath);
        synchronizing = true;
        new Thread(this::run).start();
    }

    void waitForSynchronization() throws InterruptedException {
        synchronized(this) {
            while (synchronizing) {
                wait();
            }
        }
    }

    BufferedImage getRandomImage() {
        ImageData imageData = imageRepository.findRandomImage();
        BufferedImage resultImage = null;
        if (imageData != null) {
            try {
                resultImage = ImageIO.read(new File(imageData.getImagePath()));
            } catch (IIOException e) {
                log.error("Buffered Image could not be read from disk: {}, {}", imageData.getImagePath(), e.getMessage());
            } catch (Exception unexpected) {
                log.error("Unexpected exception while reading buffered image from disk: ", unexpected);
            }
        } else {
            log.warn("No Image found in DB!");
        }
        return resultImage;
    }

    static byte[] getImageBytes(BufferedImage image) throws IOException {
        byte[] imageInByte = null;
        try (ByteArrayOutputStream imageOutputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", imageOutputStream);
            imageOutputStream.flush();
            imageInByte = imageOutputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error while getting byte stream from image: {}", e.getMessage());
        }
        return imageInByte;
    }

    private void synchronizeDirectoriesToDB(File directory) {
        log.trace("Going to potential directory: {}", directory.getName());
        if (directory.isDirectory()) {
            log.info("Going to potential directory: {}", directory.getName());
            for (final File imageFile : getImageFiles(directory)) {

                if (imageRepository.findFirstByPath(directory.getAbsolutePath()) == null) {
                    ImageData loadedImage = new ImageData(directory.getAbsolutePath(), imageFile.getAbsolutePath());
                    loadedImage = imageRepository.save(loadedImage);
                    log.info("Loaded and stored Image - " + loadedImage);
                }
            }

            // Descend to sub directories
            for (File subDirectory : getSubDirectories(directory)) {
                synchronizeDirectoriesToDB(subDirectory);
            }
        }
    }

    private static File[] getImageFiles(File directory) {
        return directory.listFiles(IMAGE_FILTER);
    }

    private static File[] getSubDirectories(File directory) {
        return directory.listFiles(DIR_FILTER);
    }

    // filter to identify images based on their extensions
    private static final FilenameFilter IMAGE_FILTER = (dir, name) -> name.endsWith(String.format(".%s", IMAGE_EXTENSION));

    private static final FileFilter DIR_FILTER = f -> f.isDirectory();

    private void run() {
        synchronized (this) {
            try {
                File dir = new File(imagePath);
                synchronizeDirectoriesToDB(dir);
            } finally {
                log.info("[Finished] Loading Images...");
                synchronizing = false;
                notifyAll();
            }
        }
    }
}
