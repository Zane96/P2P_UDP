package com.zane.p2pclient;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by Zane on 2017/6/20.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class UtilsTest {

    @Test
    public void testGetHost() {
        assertEquals("192.168.0.1", new Utils().getHost("192.168.0.1:1000"));
        assertEquals("1000", new Utils().getPort("192.168.0.1:1000"));
    }

}
