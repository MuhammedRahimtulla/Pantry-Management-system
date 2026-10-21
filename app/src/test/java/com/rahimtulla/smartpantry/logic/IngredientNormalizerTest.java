package com.rahimtulla.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class IngredientNormalizerTest {

    private static String n(String s) {
        return IngredientNormalizer.normalize(s);
    }

    @Test
    public void pluralsMatchSingulars() {
        assertEquals(n("tomato"), n("tomatoes"));
        assertEquals(n("potato"), n("Potatoes"));
        assertEquals(n("onion"), n("onions"));
        assertEquals(n("egg"), n("eggs"));
        assertEquals(n("berry"), n("berries"));
        assertEquals(n("peach"), n("peaches"));
        assertEquals(n("cheese"), n("cheeses"));
        assertEquals(n("lentil"), n("lentils"));
    }

    @Test
    public void caseWhitespaceAndPunctuationIgnored() {
        assertEquals("tomato", n("  TOMATOES  "));
        assertEquals("spring onion", n("Spring-Onions"));
        assertEquals("egg", n("eggs!"));
    }

    @Test
    public void descriptorsAreDropped() {
        assertEquals("tomato", n("Fresh ripe tomatoes"));
        assertEquals("egg", n("large free-range eggs"));
    }

    @Test
    public void synonymsMapToOneName() {
        assertEquals(n("beef mince"), n("ground beef"));
        assertEquals(n("spring onion"), n("scallions"));
        assertEquals(n("flour"), n("cake flour"));
        assertEquals(n("maize meal"), n("mealie meal"));
        assertEquals(n("cooking oil"), n("sunflower oil"));
    }

    @Test
    public void wordsEndingInUsOrSsAreKept() {
        assertEquals("couscous", n("couscous"));
        assertEquals("hummus", n("hummus"));
    }

    @Test
    public void differentIngredientsStayDifferent() {
        assertNotEquals(n("olive oil"), n("cooking oil"));
        assertNotEquals(n("chicken"), n("chicken stock"));
        assertNotEquals(n("milk"), n("milk powder"));
    }
}
