package com.example.soen345;

import org.junit.Test;

import static org.junit.Assert.*;

public class SessionManagerTest {

    static class InMemorySession implements SessionManager {
        private String id;
        @Override public void saveCurrentUserId(String id) { this.id = id; }
        @Override public String getCurrentUserId() { return id; }
        @Override public void clearCurrentUserId() { this.id = null; }
    }

    @Test
    public void saveAndClearCurrentUserId_works() {
        InMemorySession s = new InMemorySession();
        assertNull(s.getCurrentUserId());
        s.saveCurrentUserId("user_123");
        assertEquals("user_123", s.getCurrentUserId());
        s.clearCurrentUserId();
        assertNull(s.getCurrentUserId());
    }
}

