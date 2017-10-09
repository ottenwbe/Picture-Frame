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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpServerErrorException;

import java.awt.image.BufferedImage;
import java.io.IOException;

@Controller
@RequestMapping(value = "/images")
@Slf4j
public class ImageController {

    private final SynchronizationService synchronizationService;

    @Autowired
    public ImageController(SynchronizationService synchronizationService) {
        this.synchronizationService = synchronizationService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String getRoot(
            @RequestParam(value = "imageId", required = false, defaultValue = "RND") String imageId,
            Model model) throws IOException {
        model.addAttribute("imageId", imageId);
        return "images";
    }

    @RequestMapping(value = "/rnd-image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getRandomImage() throws IOException {
        final BufferedImage image = synchronizationService.getRandomImage();
        byte[] imageInByte = SynchronizationService.getImageBytes(image);
        if (imageInByte != null) {
            return createImageResponse(imageInByte);
        } else {
            throw new NoImagesAvailableException();
        }
    }

    private ResponseEntity<byte[]> createImageResponse(byte[] imageInByte) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(imageInByte, headers, HttpStatus.OK);
    }

    static class NoImagesAvailableException extends HttpServerErrorException {
        NoImagesAvailableException() {
            super(HttpStatus.SERVICE_UNAVAILABLE, "There are currently no images available! We are sorry for the inconvenience!");
        }
    }
}