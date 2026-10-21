package com.rahimtulla.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UnitTest {

    @Test
    public void parsesCommonSpellings() {
        assertEquals(Unit.GRAM, Unit.parse("Grams"));
        assertEquals(Unit.KILOGRAM, Unit.parse("kg"));
        assertEquals(Unit.LITRE, Unit.parse("litre"));
        assertEquals(Unit.TABLESPOON, Unit.parse("tbsp."));
        assertEquals(Unit.PIECE, Unit.parse("pieces"));
        assertNull(Unit.parse("handful"));
    }

    @Test
    public void convertsToBaseUnits() {
        assertEquals(1500, Unit.KILOGRAM.toBaseAmount(1.5), 1e-9);
        assertEquals(750, Unit.CUP.toBaseAmount(3), 1e-9);
        assertEquals(24, Unit.DOZEN.toBaseAmount(2), 1e-9);
        assertEquals(0.5, Unit.LITRE.fromBaseAmount(500), 1e-9);
    }

    @Test
    public void compatibilityFollowsDimension() {
        assertTrue(Unit.GRAM.isCompatibleWith(Unit.KILOGRAM));
        assertTrue(Unit.TEASPOON.isCompatibleWith(Unit.LITRE));
        assertFalse(Unit.GRAM.isCompatibleWith(Unit.MILLILITRE));
        assertFalse(Unit.PIECE.isCompatibleWith(Unit.GRAM));
        assertFalse(Unit.PIECE.isCompatibleWith(null));
    }
}
