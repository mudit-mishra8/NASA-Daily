//target is s3 bucket
// nasa consumer application

package io.conduktor.demos.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
public class nasaConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProducerDemo.class.getSimpleName());

    public static void main(String[] args) {

        String topic = "api_producer_transformed";
        String groupId = "my-nasaConsumer-application";

        String bucketName = "nasatarget";
        String accessKey = "youraccesskey";
        String secretKey = "yoursecretkey";
        String region = "bucketregion";
        AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);


        S3Client s3 = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(region))
                .build();


        // 1 create  Properties
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("key.deserializer", StringDeserializer.class.getName());
        properties.setProperty("value.deserializer", StringDeserializer.class.getName());
        properties.setProperty("group.id", groupId);
        properties.setProperty("auto.offset.reset", "earliest");

        // 2- create the consumer

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // get a reference to the main thread
        final Thread mainThread = Thread.currentThread();

        //adding a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.info("Detected a shutdown,let's exit by calling consumer.wakeup()...");
                consumer.wakeup();

                // join the main thread to allow the execution of the code in to the main thread

                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


        try{

            // 3- subscribe to a topic

            consumer.subscribe(Arrays.asList(topic));


            // 4- poll for data

            while (true) {
                //log.info("Polling");

                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> record : records) {
                    log.info("key: " + record.key() + ", Value: " + record.value());
                    log.info("Partition: " + record.partition() + ", Value: " + record.offset());

                    //s3 logic

                    // Create a unique S3 object key for the record
                    String objectKey = String.format("%s/%s", record.topic(), record.key());

                    // Convert record value to input stream
                    InputStream inputStream = new ByteArrayInputStream(record.value().getBytes(StandardCharsets.UTF_8));

                    // Upload the object to S3 bucket
                    PutObjectRequest request = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build();
                    s3.putObject(request, RequestBody.fromInputStream(inputStream, record.value().length()));

                }
            }
        }
        catch (WakeupException e){
            log.info("Consumer is starting to shut down");
        }
        catch (Exception e){
            log.error("Unexcepted exception in the consumer",e);
        }
        finally {
            consumer.close();// close the consumer, this will also commit the offset.
        }
        log.info("The consumer is now gracefully shut down");
    }
}








//kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group my-nasaConsumer-application --reset-offsets --to-earliest --execute --topic api_producer_transformed