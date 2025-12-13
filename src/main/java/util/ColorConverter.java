package util;

import javafx.scene.paint.Color;
import de.tudresden.sumo.objects.SumoColor;

public class ColorConverter { 

    public static SumoColor toSumoColor(Color fxColor) { 
    	if (fxColor == null) return new SumoColor(255, 255, 0, 255); // Mặc định Vàng
        return new SumoColor(
            (int) (fxColor.getRed() * 255),   // 0.5 -> 127
            (int) (fxColor.getGreen() * 255),
            (int) (fxColor.getBlue() * 255),
            255 // Alpha (độ đục), mặc định là 255 (đặc)
        );
    }
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
            a / 255.0 // Chia cho 255.0 để ra 0.0 - 1.0
        );
    }
}