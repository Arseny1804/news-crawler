package org.example.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import org.example.model.NewsArticle;

import java.io.IOException;

public class ElasticsearchService {

    private static final String INDEX_NAME = "news";

    private final ElasticsearchClient client;

    public ElasticsearchService() {

        RestClient restClient =
                RestClient.builder(
                        new HttpHost("localhost", 9200)
                ).build();

        RestClientTransport transport =
                new RestClientTransport(
                        restClient,
                        new JacksonJsonpMapper()
                );

        client =
                new ElasticsearchClient(transport);

        createIndexIfNotExists();
    }

    private void createIndexIfNotExists() {

        try {

            boolean exists =
                    client.indices()
                            .exists(e -> e.index(INDEX_NAME))
                            .value();

            if (!exists) {

                CreateIndexResponse response =
                        client.indices()
                                .create(c -> c.index(INDEX_NAME));

                System.out.println(
                        "Индекс создан: "
                                + response.index()
                );

            } else {

                System.out.println(
                        "Индекс уже существует"
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Ошибка создания индекса"
            );

            e.printStackTrace();
        }
    }

    public void saveArticle(NewsArticle article) {

        try {

            IndexResponse response =
                    client.index(i -> i
                            .index(INDEX_NAME)
                            .id(article.getId())
                            .document(article)
                    );

            System.out.println(
                    "Документ сохранен в Elasticsearch: "
                            + response.id()
            );

        } catch (IOException e) {

            System.out.println(
                    "Ошибка сохранения в Elasticsearch"
            );

            e.printStackTrace();
        }
    }
}