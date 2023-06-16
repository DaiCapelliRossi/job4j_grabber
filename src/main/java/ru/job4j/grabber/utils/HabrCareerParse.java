package ru.job4j.grabber.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);
    private static final String PAGE_NUM = "?page=";

    public static void main(String[] args) throws IOException {
        HabrCareerParse hbp = new HabrCareerParse();
        for (int i = 1; i < 6; i++) {
            Connection connection = Jsoup.connect(PAGE_LINK + PAGE_NUM + i);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                Element dateElement = row.select(".vacancy-card__date").first().child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                DateTimeParser date = new HabrCareerDateTimeParser();
                LocalDateTime vacancyDate = date.parse(String.format("%s", dateElement.attr("datetime")));

                String descriptionElement;
                try {
                    descriptionElement = hbp.retrieveDescription(link);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                System.out.printf("%s %s %s%n" +
                                "%n" +
                                "%s%n" +
                                "----------------------------------------------------------%n",
                        vacancyName, link, vacancyDate, descriptionElement);
            });
        }
    }

    private String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Element descriptionElement = document.getElementsByClass("faded-content__container").first();
        return descriptionElement.text();
    }

}