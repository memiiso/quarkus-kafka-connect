package org.memiiso.quarkus.kafka.connect;

import com.github.dockerjava.api.command.InspectContainerResponse;
import io.smallrye.common.annotation.Experimental;
import org.testcontainers.redpanda.RedpandaContainer;
import org.testcontainers.shaded.com.google.common.base.Joiner;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.Collections;

@Experimental("Testing only..")
public class RedpandaContainerV2 extends RedpandaContainer {

  public RedpandaContainerV2(String image) {
    super(DockerImageName.parse(image));
  }

  public void containerIsStarting(InspectContainerResponse containerInfo) {
    super.containerIsStarting(containerInfo);

    ArrayList<String> command = new ArrayList<>();
    Collections.addAll(command, "redpanda", "start", "--mode", "dev-container", "--smp=1", "--memory=1G");
    Collections.addAll(command, "--kafka-addr", "PLAINTEXT://0.0.0.0:29092,OUTSIDE://0.0.0.0:9092");
    Collections.addAll(command, "--advertise-kafka-addr", "PLAINTEXT://127.0.0.1:29092,OUTSIDE://" + getHost() + ":" + getMappedPort(9092));
    Collections.addAll(command, "--check=false");
    this.withCommand(Joiner.on(" ").join(command));
  }
}
