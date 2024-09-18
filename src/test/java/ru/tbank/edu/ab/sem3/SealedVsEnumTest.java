package ru.tbank.edu.ab.sem3;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tbank.edu.ab.sem3.SealedVsEnum.EnumExample;
import ru.tbank.edu.ab.sem3.SealedVsEnum.SealedExample;


class SealedVsEnumTest {

    @Test
    @DisplayName("Работа с sealed-классами")
    void sealedClassTest() {
        var color = SealedExample.Color.fromRgb("#FF0000")
                .orElseThrow(IllegalArgumentException::new);
        var description = switch (color) {
            case SealedExample.Red ignored -> "Красный";
            case SealedExample.Blue ignored -> "Синий";
            case SealedExample.Green ignored -> "Зеленый";
        };
        System.out.printf("%s - это %s цвет%n", color.getRgb(), description);

        var color1 = SealedExample.Color.fromRgb("#FF0000")
                .orElseThrow(IllegalArgumentException::new);
        System.out.println(color1);
    }

    @Test
    @DisplayName("Работа с перечислениями")
    void enumTest() {
        var color = EnumExample.Color.fromRgb("#FF0000")
                .orElseThrow(IllegalArgumentException::new);
        var description = switch (color) {
            case EnumExample.Color.RED -> "Красный";
            case EnumExample.Color.BLUE -> "Синий";
            case EnumExample.Color.GREEN -> "Зеленый";
        };
        System.out.printf("%s - это %s цвет%n", color.getRgb(), description);

        var color1 = EnumExample.Color.fromRgb("#FF0000")
                .orElseThrow(IllegalArgumentException::new);
        System.out.println(color1);
    }

}
