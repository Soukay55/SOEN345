package com.example.soen345;

import org.junit.Test;

import static org.junit.Assert.*;

public class AddEventActivityTest {

    @Test
    public void isValidEventInput_allFieldsFilled_returnsTrue() {
        boolean result = AddEventActivity.isValidEventInput(
                "Tech Conference",
                "2026-03-20",
                "Montreal",
                "Technology"
        );

        assertTrue(result);
    }

    @Test
    public void isValidEventInput_emptyTitle_returnsFalse() {
        boolean result = AddEventActivity.isValidEventInput(
                "",
                "2026-03-20",
                "Montreal",
                "Technology"
        );

        assertFalse(result);
    }

    @Test
    public void isValidEventInput_emptyDate_returnsFalse() {
        boolean result = AddEventActivity.isValidEventInput(
                "Tech Conference",
                "",
                "Montreal",
                "Technology"
        );

        assertFalse(result);
    }

    @Test
    public void isValidEventInput_emptyLocation_returnsFalse() {
        boolean result = AddEventActivity.isValidEventInput(
                "Tech Conference",
                "2026-03-20",
                "",
                "Technology"
        );

        assertFalse(result);
    }

    @Test
    public void isValidEventInput_emptyCategory_returnsFalse() {
        boolean result = AddEventActivity.isValidEventInput(
                "Tech Conference",
                "2026-03-20",
                "Montreal",
                ""
        );

        assertFalse(result);
    }

    @Test
    public void isValidEventInput_whitespaceOnly_returnsFalse() {
        boolean result = AddEventActivity.isValidEventInput(
                "   ",
                "2026-03-20",
                "Montreal",
                "Technology"
        );

        assertFalse(result);
    }

    @Test
    public void isValidEventInput_nullField_returnsFalse() {
        boolean result = AddEventActivity.isValidEventInput(
                null,
                "2026-03-20",
                "Montreal",
                "Technology"
        );

        assertFalse(result);
    }
}