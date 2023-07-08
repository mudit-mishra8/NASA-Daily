// nasa application

package stream.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;

import java.util.Properties;

public class nasaApplication {

    public static void main(String[] args) {

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "nasa_applicatio_01");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // Create a Serde for strings
        Serde<String> stringSerde = Serdes.String();
        // Create a Serde for JSON objects using the Jackson library
        JsonSerializer jsonSerializer = new JsonSerializer();
        JsonDeserializer jsonDeserializer = new JsonDeserializer();
        Serde jsonSerde = Serdes.serdeFrom(jsonSerializer, jsonDeserializer);

        //for create json value(json node) from separate fields

           // Create a JsonNodeFactory
          JsonNodeFactory factory = JsonNodeFactory.instance;
          // Create an empty ObjectNode
          ObjectNode objectNode = factory.objectNode();

        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, JsonNode> inputStream = builder.stream("api_producer", Consumed.with(stringSerde, jsonSerde));
       // input stream have null key, we make key:"date" and for values we include only "url" and "explanation"
        KStream<String, JsonNode> inputStream_modified = inputStream
                                         .map((key, jsonvalue) -> {
                                             String date_key=  jsonvalue.get("date").asText();
                                             String explanation_text=jsonvalue.get("explanation").asText();
                                             String url = jsonvalue.get("hdurl") != null ? jsonvalue.get("hdurl").asText() :
                                                     jsonvalue.get("url") != null ? jsonvalue.get("url").asText() : null;
                                             // Add the two fields to the object node
                                             objectNode.put("explanation", explanation_text);
                                             objectNode.put("url", url);
                                             // Convert the object node to a JsonNode
                                             JsonNode some_jsonvalues = objectNode;
                                             return new KeyValue<>(date_key, some_jsonvalues);
                                           })
                                           ;
        // keep only the latest value for each key
        KStream<String, JsonNode> modified_deduplicated = inputStream_modified
                                                         .groupByKey(Grouped.with(stringSerde,jsonSerde))
                                                         .reduce((value1, value2) -> value2, Materialized.as("store-name"))
                                                                 .toStream();



        modified_deduplicated.to("api_producer_transformed");

        modified_deduplicated.peek((key, value)-> System.out.println("key:" + key +" value: "+ value));























        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        // Update:
        // print the topology every 10 seconds for learning purposes
        while(true){
            System.out.println(streams.toString());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
        }

    }
}
