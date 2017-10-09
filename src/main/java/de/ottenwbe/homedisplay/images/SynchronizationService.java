/*
 *    Copyright 2017 Beate Ottenwälder
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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

    private static final FileFilter DIR_FILTER = File::isDirectory;

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
