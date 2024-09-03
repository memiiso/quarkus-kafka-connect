package org.memiiso.quarkus.kafka.connect;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.apache.kafka.connect.cli.ConnectDistributed;
import org.apache.kafka.connect.runtime.Connect;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;

@ApplicationScoped
public class KafkaConnectRunner {
  private static final String KAFKA_CONNECT_DISTRIBUTED_CONFIG_PREFIX = "connect.";
  private static final Logger LOGGER = Logger.getLogger(KafkaConnectRunner.class);
  private static int RETRY_COUNT = 0;

  public KafkaConnectRunner() {
  }

  public static Map<String, String> getConfigSubset(String prefix) {
    Config config = ConfigProvider.getConfig();
    final Map<String, String> ret = new HashMap<>();

    for (String propName : config.getPropertyNames()) {
      if (propName.startsWith(prefix)) {
        final String newPropName = propName.substring(prefix.length());
        ret.put(newPropName, config.getValue(propName, String.class));
      }
    }

    return ret;
  }

  void onStart(@Observes StartupEvent ev) {
    LOGGER.info("The application is starting...");
    start();
  }

  void onStop(@Observes ShutdownEvent ev) {
    LOGGER.info("The application is stopping...");
  }

  private void start() {
    LOGGER.info("On start...");
    Map<String, String> propertiesMap = new HashMap<>();
    propertiesMap = getConfigSubset(KAFKA_CONNECT_DISTRIBUTED_CONFIG_PREFIX);
    LOGGER.info("Starting Kafka Connect, startConnect()...");
    Connect connect = new ConnectDistributed().startConnect(propertiesMap);
    while (!connect.isRunning()) {
      if (RETRY_COUNT >= 12) {
        throw new RuntimeException("Kafka Connect failed to start...");
      }
      LOGGER.info("Kafka not yet started. Waiting...");
      try {
        RETRY_COUNT++;
        SECONDS.sleep(10);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    LOGGER.info("Kafka Connect Successfully Started");
  }
}