package org.example.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.elasticsearch.ElasticsearchService;
import org.example.model.NewsArticle;
import org.example.rabbit.RabbitMQService;
import org.example.util.HashUtil;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NetallParser {

    private static final String BASE_URL =
            "https://www.netall.ru";

    private final ConcurrentLinkedQueue<String> linkQueue =
            new ConcurrentLinkedQueue<>();

    private final Set<String> processedLinks =
            ConcurrentHashMap.newKeySet();

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final RabbitMQService rabbitMQService =
            new RabbitMQService();

    private final ElasticsearchService elasticsearchService =
            new ElasticsearchService();

    private final Random random =
            new Random();

    public void parseNews() {

        try {

            Connection.Response response =
                    Jsoup.connect(BASE_URL)
                            .userAgent("Mozilla/5.0")
                            .timeout(15000)
                            .ignoreHttpErrors(true)
                            .execute();

            if (response.statusCode() != 200) {

                System.out.println(
                        "HTTP ERROR: "
                                + response.statusCode()
                );

                return;
            }

            Document mainPage =
                    response.parse();

            Elements links =
                    mainPage.select("a[href]");

            for (Element link : links) {

                String href =
                        link.attr("abs:href");

                if (!href.contains("/news/")) {
                    continue;
                }

                if (!href.endsWith(".html")) {
                    continue;
                }

                if (processedLinks.contains(href)) {
                    continue;
                }

                processedLinks.add(href);

                rabbitMQService.sendLink(href);

                System.out.println(
                        "Ссылка отправлена: "
                                + href
                );

                linkQueue.add(href);
            }

            System.out.println(
                    "Найдено ссылок: "
                            + linkQueue.size()
            );

            executor.submit(() -> {

                while (!linkQueue.isEmpty()) {

                    String url =
                            linkQueue.poll();

                    if (url == null) {
                        continue;
                    }

                    try {

                        parseArticle(url);

                    } catch (Exception e) {

                        System.out.println(
                                "Ошибка статьи: "
                                        + url
                        );

                        System.out.println(
                                e.getMessage()
                        );
                    }
                }
            });

            executor.shutdown();

            executor.awaitTermination(
                    1,
                    TimeUnit.HOURS
            );

            System.out.println(
                    "Парсинг завершен"
            );

        } catch (Exception e) {

            System.out.println(
                    "Ошибка главной страницы"
            );

            e.printStackTrace();
        }
    }

    private void parseArticle(String url)
            throws Exception {

        int pause =
                5000 + random.nextInt(5000);

        System.out.println(
                "Пауза "
                        + pause
                        + " ms"
        );

        Thread.sleep(pause);

        Connection.Response response =
                Jsoup.connect(url)
                        .userAgent(
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                        )
                        .timeout(15000)
                        .ignoreHttpErrors(true)
                        .execute();

        if (response.statusCode() == 429) {

            System.out.println(
                    "Сайт временно заблокировал запрос: "
                            + url
            );

            return;
        }

        if (response.statusCode() != 200) {

            throw new RuntimeException(
                    "HTTP ERROR: "
                            + response.statusCode()
            );
        }

        Document doc =
                response.parse();

        String title =
                doc.title();

        String text =
                doc.body().text();

        String author =
                "Unknown";

        String date =
                "Unknown";

        Element dateElement =
                doc.selectFirst("time");

        if (dateElement != null) {

            date =
                    dateElement.text();
        }

        String id =
                HashUtil.generateId(url, date);

        NewsArticle article =
                new NewsArticle(
                        id,
                        title,
                        text,
                        author,
                        date,
                        url
                );

        saveAsJson(article);

        ObjectMapper mapper =
                new ObjectMapper();

        String json =
                mapper.writeValueAsString(article);

        rabbitMQService.sendResult(json);

        elasticsearchService.saveArticle(article);

        System.out.println(
                "Сохранено: "
                        + title
        );
    }

    private void saveAsJson(NewsArticle article)
            throws IOException {

        File folder =
                new File("results");

        if (!folder.exists()) {

            boolean created =
                    folder.mkdir();

            if (!created) {

                System.out.println(
                        "Не удалось создать папку results"
                );
            }
        }

        ObjectMapper mapper =
                new ObjectMapper();

        File file =
                new File(
                        "results/"
                                + article.getId()
                                + ".json"
                );

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(file, article);
    }
}