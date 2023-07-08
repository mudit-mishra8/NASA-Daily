//nasa- api - producer

package io.conduktor.demos.kafka.wikimedia;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.util.Properties;

public class apidataextractProducer {

    public static <JSONObject> void main(String[] args) throws IOException {
        // Create Kafka producer properties
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer", StringSerializer.class.getName());

        // Create Kafka producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        // Make HTTP request to API endpoint
            OkHttpClient client = new OkHttpClient();
            String baseUrl = "https://api.nasa.gov/planetary/apod?date=%s&api_key=%s";
            String apiKey = "enterthekey";

        for (int month = 1; month <= 12; month++) {
            for (int day = 1; day <= 31; day++) {
                String date = String.format("2018-%02d-%02d", month, day);
                String url = String.format(baseUrl, date, apiKey);

                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = client.newCall(request).execute();


                // Extract JSON data from response and send to Kafka topic
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    String topic = "api_producer";

                    ProducerRecord<String, String> record = new ProducerRecord<>(topic, jsonData);
                    producer.send(record);
                } else {
                    System.err.println("HTTP request failed: " + response.code() + " " + response.message());
                }

                response.close();

            }
        }

        // Close resources
        producer.close();

    }
}
