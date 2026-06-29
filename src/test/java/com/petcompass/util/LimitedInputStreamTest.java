package com.petcompass.util;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class LimitedInputStreamTest {

    @Test
    public void testUnderLimit() throws IOException {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        InputStream bis = new ByteArrayInputStream(data);
        RegionScanner.LimitedInputStream lis = new RegionScanner.LimitedInputStream(bis, 10);

        byte[] result = new byte[5];
        int read = lis.read(result);

        assertEquals(5, read);
        assertArrayEquals(data, result);
    }

    @Test
    public void testOverLimit() {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        InputStream bis = new ByteArrayInputStream(data);
        RegionScanner.LimitedInputStream lis = new RegionScanner.LimitedInputStream(bis, 3);

        assertThrows(IOException.class, () -> {
            byte[] result = new byte[5];
            lis.read(result);
        });
    }

    @Test
    public void testOverLimitSingleRead() throws IOException {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        InputStream bis = new ByteArrayInputStream(data);
        RegionScanner.LimitedInputStream lis = new RegionScanner.LimitedInputStream(bis, 2);

        assertEquals(1, lis.read());
        assertEquals(2, lis.read());
        assertThrows(IOException.class, () -> {
            lis.read();
        });
    }
}
