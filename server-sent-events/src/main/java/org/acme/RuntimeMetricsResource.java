package org.acme;

import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.reactive.RestStreamElementType;

@Path("/metrics")
public class RuntimeMetricsResource {
    @Inject
    @Channel("runtime-metrics")
    Multi<RuntimeMetrics> runtimeMetrics;

    @GET
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<RuntimeMetrics> runtimeMetrics() {
        return runtimeMetrics;
    }
}
