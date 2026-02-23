package com.github.eirikma.appstate.calendar.tagging;

import java.time.LocalDate;

/**
 * Computes the date of Easter Sunday using the Anonymous Gregorian algorithm (Meeus/Jones/Butcher).
 */
public final class Computus {

    private Computus() {
    }

    public static LocalDate easterSunday(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = Math.floorMod(19 * a + b - d - g + 15, 30);
        int i = c / 4;
        int k = c % 4;
        int l = Math.floorMod(32 + 2 * e + 2 * i - h - k, 7);
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = (h + l - 7 * m + 114) % 31 + 1;
        return LocalDate.of(year, month, day);
    }
}
