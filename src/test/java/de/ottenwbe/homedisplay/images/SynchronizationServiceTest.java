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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.awt.image.BufferedImage;

import static org.junit.Assert.*;

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
        imageRepository.deleteAll();
        // path is set to /resources/images in application-test.yml
        //When:
        BufferedImage testImage = synchronizationService.getRandomImage();
        //Then:
        assertNull(testImage);
    }
}