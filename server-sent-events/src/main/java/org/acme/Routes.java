package org.acme;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.reactive.streams.ReactiveStreamsNoActiveSubscriptionsException;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.reactivestreams.Publisher;

@RegisterForReflection(targets = {ReactiveStreamsNoActiveSubscriptionsException.class},fields = false, methods = false)
@ApplicationScoped
public class Routes extends RouteBuilder {
    @Inject
    CamelReactiveStreamsService reactiveStreamsService;

    @Inject
    MetricsService metricsService;

    @Outgoing("runtime-metrics")
    public Publisher<RuntimeMetrics> getDataFromCamelRoute() {
        return reactiveStreamsService.fromStream("runtime-metrics", RuntimeMetrics.class);
    }

    @Override
    public void configure() throws Exception {
        // Stream subscription happens when the browser connects to the SSE
        // Therefore, ignore ReactiveStreamsNoActiveSubscriptionsException
        onException(ReactiveStreamsNoActiveSubscriptionsException.class)
                .handled(true);

        from("timer:updateMemoryInfo?period=5s")
                .process(exchange -> exchange.getMessage().setBody(metricsService.getRuntimeMetrics()))
                .to("reactive-streams:runtime-metrics");
    }
}
