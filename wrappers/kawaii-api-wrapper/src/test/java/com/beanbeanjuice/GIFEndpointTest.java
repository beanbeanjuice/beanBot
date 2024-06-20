package com.beanbeanjuice;

import com.beanbeanjuice.kawaiiapi.wrapper.KawaiiAPI;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;

public class GIFEndpointTest {

    @Test
    @DisplayName("Test Gif Endpoint")
    public void testGifEndpoint() {
        KawaiiAPI kawaiiAPI = new KawaiiAPI("anonymous");

        Assertions.assertTrue(kawaiiAPI.GIF.getGIF("hug").isPresent());

        Assertions.assertTrue(kawaiiAPI.GIF.getGIF("hug").get().startsWith("https://api.kawaii.red/gif/hug/"));
        Assertions.assertTrue(kawaiiAPI.GIF.getGIF("bruh").isEmpty());
    }

}