package de.ottenwbe.homedisplay.images;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.awt.image.BufferedImage;

import static org.junit.Assert.*;

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

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(
        properties = {
                "images.sync.init.enabled=false"
        }
)
public class SynchronizationServiceTest {

    @Autowired
    SynchronizationService synchronizationService;

    @Autowired
    ImageRepository imageRepository;

    @Test
    public void synchronizationTest() throws InterruptedException {
        //Given:
        imageRepository.deleteAll(); //An empty repository
        // path is set to /resources/images in application-test.yml
        //When:
        synchronizationService.synchronizeImages();
        synchronizationService.waitForSynchronization();
        //Then:
        assertEquals(1, imageRepository.count());
    }

    @Test
    public void duplicatedSynchronizationTest() throws InterruptedException {
        //Given:
        imageRepository.deleteAll(); //An empty repository
        // path is set to /resources/images in application-test.yml
        //When:
        synchronizationService.synchronizeImages();
        synchronizationService.waitForSynchronization();
        synchronizationService.synchronizeImages();
        synchronizationService.waitForSynchronization();
        //Then:
        assertEquals(1, imageRepository.count());
    }

    @Test
    public void getRandomImageTest() throws InterruptedException {
        //Given:
        synchronizationService.synchronizeImages(); // test image loaded
        synchronizationService.waitForSynchronization();
        // path is set to /resources/images in application-test.yml
        //When:
        BufferedImage testImage = synchronizationService.getRandomImage();
        //Then:
        assertNotNull(testImage);
    }

    @Test
    public void getRandomImageInEmptyDBTest() {
        //Given:
        imageRepository.deleteAll();;
        // path is set to /resources/images in application-test.yml
        //When:
        BufferedImage testImage = synchronizationService.getRandomImage();
        //Then:
        assertNull(testImage);
    }
}