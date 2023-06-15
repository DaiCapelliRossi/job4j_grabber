package ru.job4j.grabber.utils;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class HabrCareerDateTimeParserTest {

    @Test
    public void whenParsed() {
        String time = "2023-06-15T17:39:19+03:00";
        DateTimeParser dtp = new HabrCareerDateTimeParser();
        LocalDateTime ldt = dtp.parse(time);

        assertThat(ldt.toString(), is("2023-06-15T17:39:19"));
    }

}