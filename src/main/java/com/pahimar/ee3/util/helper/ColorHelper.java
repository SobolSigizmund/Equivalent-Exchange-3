package com.pahimar.ee3.util.helper;

import net.minecraft.item.EnumDyeColor;

import java.awt.*;
import java.util.regex.Pattern;

public class ColorHelper {

    private static final Pattern HEX_COLOR_CODE_PATTERN = Pattern.compile("^([A-Fa-f0-9]{6})$");

    public static final boolean isValidColor(String color) {
        return HEX_COLOR_CODE_PATTERN.matcher(color).matches();
    }

    public static float[] getRGB(EnumDyeColor dyeColor) {
        return getRGB(dyeColor.getMapColor().colorValue);
    }

    public static float[] getRGB(String hexCodeColor) {
        if (isValidColor(hexCodeColor)) {
            return getRGB(Integer.parseInt(hexCodeColor, 16));
        }

        return new float[] {1f, 1f, 1f};
    }

    public static float[] getRGB(int intColor) {
        return new Color(intColor).getRGBColorComponents(null);
    }

    public static final String blendAsString(int color1, int color2) {
        return blendAsString(color1, 1, color2, 1);
    }

    public static final String blendAsString(int color1, int weight1, int color2, int weight2) {
        return Integer.toHexString(blend(new Color(color1), weight1, new Color(color2), weight2).getRGB());
    }

    public static final int blendAsInt(int color1, int color2) {
        return blendAsInt(color1, 1, color2, 1);

    }
    public static final int blendAsInt(int color1, int weight1, int color2, int weight2) {
        return blend(new Color(color1), weight1, new Color(color2), weight2).getRGB();
    }

    private static final Color blend (Color color1, int weight1, Color color2, int weight2) {

        if (color1 == null) {
            color1 = Color.WHITE;
        }

        if (color2 == null) {
            color2 = Color.WHITE;
        }

        if (weight1 <= 0) {
            weight1 = 1;
        }

        if (weight2 <= 0) {
            weight2 = 1;
        }

        double totalWeight = weight1 + weight2;
        double adjWeight1 = weight1 / totalWeight;
        double adjWeight2 = weight2 / totalWeight;

        double r = adjWeight1 * color1.getRed() + adjWeight2 * color2.getRed();
        double g = adjWeight1 * color1.getGreen() + adjWeight2 * color2.getGreen();
        double b = adjWeight1 * color1.getBlue() + adjWeight2 * color2.getBlue();

        return new Color((int) r, (int) g, (int) b, 0);
    }

}
