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

    List<Post> listOfPosts = new ArrayList<>();

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);
    private static final String PAGE_NUM = "?page=";

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws IOException {
        HabrCareerParse hbp = new HabrCareerParse(new HabrCareerDateTimeParser());

        hbp.listOfPosts = hbp.list(PAGE_LINK + PAGE_NUM);

        for (Post p : hbp.listOfPosts) {
            System.out.printf("%d. %s %s %s%n" +
                    "%n%s" +
                    "%n---------------------------------------------------------------------------------------------%n",
                    p.getId(), p.getTitle(), p.getLink(), p.getCreated(), p.getDescription());
        }
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
        for (int i = 1; i < 2; i++) {
            Connection connection = Jsoup.connect(link + i);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                Element dateElement = row.select(".vacancy-card__date").first().child(0);
                String vacancyName = titleElement.text();
                String pageLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
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