package ru.job4j.grabber.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.Parse;
import ru.job4j.grabber.Post;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String PAGE_LINK = "/vacancies/java_developer";
    private static final String PAGE_NUM = "?page=";

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Element descriptionElement = document.getElementsByClass("faded-content__container").first();
        return descriptionElement.text();
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> listOfPosts = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            Connection connection = Jsoup.connect(link + PAGE_LINK + PAGE_NUM + i);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                Element dateElement = row.select(".vacancy-card__date").first().child(0);
                String vacancyName = titleElement.text();
                String pageLink = String.format("%s%s", link, linkElement.attr("href"));
                LocalDateTime vacancyDate = this.dateTimeParser.parse(String.format("%s", dateElement.attr("datetime")));

                String descriptionElement;
                try {
                    descriptionElement = this.retrieveDescription(pageLink);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Post post = new Post(0, vacancyName, pageLink, vacancyDate, descriptionElement);
                listOfPosts.add(post);
                post.setId(listOfPosts.indexOf(post));
            });
        }
        return listOfPosts;
    }
}