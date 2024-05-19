package com.kmaebashi.blog.util;

import com.kmaebashi.blog.service.Util;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RandomIdGeneratorTest {
    @Test
    void getRandomIdTest001() throws Exception {
        String rnd = RandomIdGenerator.getRandomId();
        assertEquals(32, rnd.length());
    }
}