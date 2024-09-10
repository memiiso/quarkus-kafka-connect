package org.memiiso.quarkus.kafka.connect;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.redpanda.RedpandaContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

class KafkaConnectRunnerDockerTest {

  // -----------------------------------------------------------
  static Network network = Network.newNetwork();
  static String kafkaHostname = "kafka";
  static RedpandaContainer kafka = new RedpandaContainer("docker.redpanda.com/vectorized/redpanda:v23.1.2")
//        .withExposedPorts(9644, 9092, 8081, 8082, 29092)
      .withCommand("redpanda", "start", "--overprovisioned", "--smp", "1", "--memory", "1G", "--reserve-memory", "0M", "--node-id", "0", "--kafka-addr", "PLAINTEXT://0.0.0.0:29092,OUTSIDE://0.0.0.0:9092", "--advertise-kafka-addr", "PLAINTEXT://" + kafkaHostname + ":29092,OUTSIDE://localhost:9092", "--check=false")
      .withNetwork(network)
      .withNetworkAliases(kafkaHostname);
  // -----------------------------------------------------------
  static ImageFromDockerfile dockerFile =
      new ImageFromDockerfile("quarkus-kafka-connect:latest", true)
          .withDockerfilePath("./src/main/docker/Dockerfile.jvm")
          .withFileFromPath("/", Paths.get("."));
  // -----------------------------------------------------------
  static GenericContainer<?> kafkaConnect = new GenericContainer<>(dockerFile)
      .withNetwork(network)
      .dependsOn(kafka)
      .withNetworkAliases("connect")
      .withEnv("CONNECT_BOOTSTRAP_SERVERS", kafkaHostname + ":29092")
//        .withEnv("CONNECT_LISTENERS", "http://0.0.0.0:8083")
//        .withEnv("CONNECT_REST_HOST_NAME", "0.0.0.0")
//        .withEnv("CONNECT_REST_PORT", "8083")
      .withEnv("CONNECT_REST_ADVERTISED_HOST_NAME", "connect")
      .withEnv("CONNECT_GROUP_ID", "connect-docker-test-group")
      .withEnv("CONNECT_CONNECTOT_CLASS", "io.tabular.iceberg.connect.IcebergSinkConnector")
      .withEnv("CONNECT_CONFIG_STORAGE_TOPIC", "_connectors_offsets")
      .withEnv("CONNECT_OFFSET_STORAGE_TOPIC", "_connectors_configs")
      .withEnv("CONNECT_STATUS_STORAGE_TOPIC", "_connectors_status")
      .withEnv("CONNECT_KEY_CONVERTER", "org.apache.kafka.connect.storage.StringConverter")
      .withEnv("CONNECT_VALUE_CONVERTER", "org.apache.kafka.connect.storage.StringConverter")
      .withEnv("CONNECT_PLUGIN_PATH", "/deployments/lib/")
      .withEnv("DEFAULT_METADATA_RECOVERY_STRATEGY", "none")
//        .withCopyToContainer(Transferable.of("src/main/resources/application.properties"), "/deployments/application.properties")
      .withCopyFileToContainer(
          MountableFile.forHostPath(Paths.get("src/main/resources/application.properties")),
          "/deployments/application.properties"
      )
      .withExposedPorts(8083);
  // -----------------------------------------------------------

  @BeforeAll
  public static void setup() {
    System.out.println("Start");
  }

  @AfterAll
  public static void teardown() {
    if (kafka != null) {
      kafka.stop();
    }
    if (kafkaConnect != null) {
      kafkaConnect.stop();
    }
  }


  @Test
  void testDockerWithKafkaSetup() throws InterruptedException {
    kafka.start();
    kafkaConnect.start();
    Awaitility.await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
      Assertions.assertTrue(false);
      // @TODO FIX wait and read message from logs that kafkaConnect successfully setup and running!!
    });
  }
}
