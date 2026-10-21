package com.rahimtulla.smartpantry.logic;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Units of measure the app understands. Each unit belongs to a {@link Dimension} and knows how many
 * base units (grams, millilitres or pieces) it equals, so quantities in different units of the same
 * dimension can be compared - e.g. 1 kg rice in the pantry satisfies a recipe needing 500 g.
 */
public enum Unit {
    GRAM("g", Dimension.MASS, 1),
    KILOGRAM("kg", Dimension.MASS, 1000),
    MILLILITRE("ml", Dimension.VOLUME, 1),
    LITRE("l", Dimension.VOLUME, 1000),
    TEASPOON("tsp", Dimension.VOLUME, 5),
    TABLESPOON("tbsp", Dimension.VOLUME, 15),
    CUP("cup", Dimension.VOLUME, 250),
    PIECE("pcs", Dimension.COUNT, 1),
    DOZEN("dozen", Dimension.COUNT, 12);

    public enum Dimension { MASS, VOLUME, COUNT }

    /** Short code stored in the database and shown in the UI. */
    public final String code;
    public final Dimension dimension;
    /** How many base units (g / ml / pieces) one of this unit equals. */
    public final double toBase;

    Unit(String code, Dimension dimension, double toBase) {
        this.code = code;
        this.dimension = dimension;
        this.toBase = toBase;
    }

    /** Units offered in the add/edit dropdown (dozen is only accepted when parsing). */
    public static final Unit[] SELECTABLE = {GRAM, KILOGRAM, MILLILITRE, LITRE, TEASPOON, TABLESPOON, CUP, PIECE};

    private static final Map<String, Unit> ALIASES = new HashMap<>();

    static {
        alias(GRAM, "g", "gr", "gram", "grams", "gramme", "grammes");
        alias(KILOGRAM, "kg", "kgs", "kilo", "kilos", "kilogram", "kilograms");
        alias(MILLILITRE, "ml", "millilitre", "millilitres", "milliliter", "milliliters");
        alias(LITRE, "l", "lt", "litre", "litres", "liter", "liters");
        alias(TEASPOON, "tsp", "teaspoon", "teaspoons");
        alias(TABLESPOON, "tbsp", "tbs", "tablespoon", "tablespoons");
        alias(CUP, "cup", "cups", "c");
        alias(PIECE, "pcs", "pc", "piece", "pieces", "each", "whole", "unit", "units", "x", "");
        alias(DOZEN, "dozen", "doz");
    }

    private static void alias(Unit unit, String... names) {
        for (String n : names) {
            ALIASES.put(n, unit);
        }
    }

    /** Parses a unit code or common spelling ("Grams", "litre", "tbsp."). Returns null if unknown. */
    public static Unit parse(String text) {
        if (text == null) {
            return PIECE;
        }
        String key = text.trim().toLowerCase(Locale.ROOT).replace(".", "");
        return ALIASES.get(key);
    }

    /** Converts a quantity in this unit to the dimension's base unit. */
    public double toBaseAmount(double quantity) {
        return quantity * toBase;
    }

    /** Converts a base-unit amount back into this unit. */
    public double fromBaseAmount(double baseAmount) {
        return baseAmount / toBase;
    }

    public boolean isCompatibleWith(Unit other) {
        return other != null && dimension == other.dimension;
    }
}
