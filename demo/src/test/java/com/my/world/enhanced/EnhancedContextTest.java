package com.my.world.enhanced;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnhancedContextTest {

    private EnhancedContext c1;
    private EnhancedContext c2;
    private EnhancedContext c3;

    @Before
    public void before() {
        c1 = EnhancedContext.obtain(null);
        c1.setReverse(true);
        c2 = EnhancedContext.obtain(c1);
        c3 = EnhancedContext.obtain(c2);
        c1.setPrefix("c1");
        c2.setPrefix("c2");
        c3.setPrefix("c3");
    }

    @Test
    public void testReverse() {
        c1.set("A", "123");
        c2.set("A", "234");
        c3.set("A", "345");
        assertEquals("123", c1.get("A", String.class));
        assertEquals("123", c2.get("A", String.class));
        assertEquals("123", c3.get("A", String.class));
    }

    @Test
    public void testPrefix() {
        c1.set("A", "000");
        c1.set("c1.A", "111");
        assertEquals("000", c1.get("A", String.class));
        c1.set("B", "AAA");
        c1.set("c2.B", "BBB");
        assertEquals("BBB", c2.get("B", String.class));
        c1.set("C", "123");
        c1.set("c3.C", "234");
        c1.set("c2.c3.C", "345");
        assertEquals("345", c3.get("C", String.class));
    }
}
