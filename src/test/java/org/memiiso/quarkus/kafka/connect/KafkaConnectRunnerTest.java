package org.memiiso.quarkus.kafka.connect;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.redpanda.RedpandaContainer;

import javax.inject.Inject;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@QuarkusTest
@TestProfile(KafkaConnectRunnerTest.TestProfile.class)
class KafkaConnectRunnerTest {

  //  // -----------------------------------------------------------
  static RedpandaContainer kafka = new RedpandaContainer("docker.redpanda.com/vectorized/redpanda:v23.1.2");
  @Inject
  KafkaConnectRunner runner;

  @AfterAll
  public static void teardown() {
    if (kafka != null) {
      kafka.stop();
    }
  }

  @Test
  void testIcebergConsumer() throws InterruptedException {
    System.out.println(runner.toString());
    Awaitility.await().atMost(120, TimeUnit.SECONDS).untilAsserted(() -> {
      Assertions.assertTrue(false);
    });
  }

  public static class TestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      kafka.start();
      //////////////////////////////////////////////////////////////////////
      Map<String, String> config = new ConcurrentHashMap<>();
      config.put("connect.bootstrap.servers", kafka.getHost() + ":" + kafka.getMappedPort(9092));
      config.put("connect.plugin.path", Paths.get("build/quarkus-app/lib/main").toAbsolutePath().toString());
      return config;
    }
  }
}
