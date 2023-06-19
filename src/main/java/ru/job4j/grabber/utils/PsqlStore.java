package ru.job4j.grabber.utils;

import ru.job4j.grabber.Post;
import ru.job4j.grabber.Store;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    public static void main(String[] args) throws SQLException, IOException {
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            Properties config = new Properties();
            config.load(in);
            PsqlStore psqlStore = new PsqlStore(config);
            DateTimeParser dtp = new HabrCareerDateTimeParser();
            Post post = new Post(0, "Java developer", "https://career.habr.com/vacancies/1000113345", dtp.parse("2023-06-15T17:39:19+03:00"),"Description: full-time remote Java developer");
            psqlStore.save(post);
            List<Post> posts = psqlStore.getAll();
            for (Post p : posts) {
                System.out.println(p);
            }
            Post post1 = psqlStore.findById(6);
            System.out.println(post1);
        }
    }

    private Connection cnn;

    public PsqlStore(Properties cfg) throws SQLException {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        cnn = DriverManager.getConnection(
                cfg.getProperty("jdbc.url"),
                cfg.getProperty("jdbc.username"),
                cfg.getProperty("jdbc.password")
        );
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement = cnn.prepareStatement("INSERT INTO post(name, link, created, text) VALUES (?, ?, ?, ?) ON CONFLICT (link) DO NOTHING", Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getLink());
            statement.setTimestamp(3, Timestamp.valueOf(post.getCreated()));
            statement.setString(4, post.getDescription());
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = cnn.prepareStatement("SELECT * FROM post")){
            try (ResultSet resultSet = statement.executeQuery()){
                DateTimeParser dtp = new HabrCareerDateTimeParser();
                while (resultSet.next()) {
                    posts.add(new Post(resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("link"),
                            resultSet.getTimestamp("created").toLocalDateTime(),
                            resultSet.getString("text")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement = cnn.prepareStatement("SELECT * FROM post WHERE id = ?")){
            statement.setInt(1,  id);
            try (ResultSet resultSet = statement.executeQuery()){
                while (resultSet.next()) {
                    post = new Post(resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("link"),
                            resultSet.getTimestamp("created").toLocalDateTime(),
                            resultSet.getString("text"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }
}
