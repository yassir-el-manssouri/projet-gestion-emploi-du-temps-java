package main.utils;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class Constants {
    public static final List<String> SITES = Arrays.asList(
        "EMSI Agdal 1", "EMSI Agdal 2", "EMSI Hassan",
        "EMSI centre 1", "EMSI centre 2", "EMSI bouregrag", "EMSI souissi"
    );
    
    public static final List<DayOfWeek> SCHOOL_DAYS = Arrays.asList(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
    );
    
    public static final LocalTime MORNING_START = LocalTime.of(8, 0);
    public static final LocalTime MORNING_END = LocalTime.of(12, 0);
    public static final LocalTime AFTERNOON_START = LocalTime.of(14, 0);
    public static final LocalTime AFTERNOON_END = LocalTime.of(18, 0);
    
    public static final int COURSE_DURATION_MINUTES = 120;
}