package util;

import javafx.scene.paint.Color;
import de.tudresden.sumo.objects.SumoColor;

public class ColorConverter { 
	/**
     * Converts a JavaFX {@link Color} object into a {@code SumoColor} object compatible with the simulation core.
     * <p>
     * <b>Conversion Logic:</b>
     * JavaFX represents colors using normalized double values (0.0 to 1.0), while SUMO typically requires 
     * integer values (0 to 255). This method performs the necessary scaling: {@code (int) (val * 255)}.
     * </p>
     *
     * @param fxColor The source JavaFX {@link Color} to convert.
     * @return A new {@code SumoColor} instance representing the same color with full opacity (Alpha = 255).
     * <br><b>Fallback:</b> Returns a default <b>Yellow</b> (255, 255, 0) if the input {@code fxColor} is {@code null}.
     */
    public static SumoColor toSumoColor(Color fxColor) { 
    	if (fxColor == null) return new SumoColor(255, 255, 0, 255); // Mặc định Vàng
        return new SumoColor(
            (int) (fxColor.getRed() * 255),   // 0.5 -> 127
            (int) (fxColor.getGreen() * 255),
            (int) (fxColor.getBlue() * 255),
            255 // Alpha (độ đục), mặc định là 255 (đặc)
        );
    }
    /**
     * Converts a simulation-specific {@code SumoColor} object into a JavaFX {@link Color} for rendering.
     * <p>
     * <b>Technical Implementation Note:</b>
     * <br>In Java, the {@code byte} data type is signed (-128 to 127). However, standard RGB color values 
     * range from 0 to 255 (unsigned).
     * <ul>
     * <li>The bitwise operation {@code & 0xFF} is used to mask the bits, effectively converting the signed byte 
     * into a positive integer (0-255).</li>
     * <li>The Alpha (opacity) channel is normalized from the integer range (0-255) to the double range (0.0-1.0) 
     * required by JavaFX.</li>
     * </ul>
     * </p>
     *
     * @param sumoColor The source color object from the simulation.
     * @return The corresponding JavaFX {@link Color}. 
     * <br><b>Fallback:</b> Returns {@link Color#YELLOW} if the input {@code sumoColor} is {@code null}.
     */
    public static Color toFXColor(SumoColor sumoColor) {
        if (sumoColor == null) return Color.YELLOW;

        // Dùng (x & 0xFF) để ép kiểu byte âm thành số nguyên dương (0-255)
        int r = sumoColor.r & 0xFF;
        int g = sumoColor.g & 0xFF;
        int b = sumoColor.b & 0xFF;
        int a = sumoColor.a & 0xFF;

        return Color.rgb(
            r, 
            g, 
            b, 
            a / 255.0 
        );
    }
}