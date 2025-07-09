package org.kafka;

import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.dto.PriceResponse;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.util.List;

@ApplicationScoped
@Slf4j
public class PriceEventProducer {

    @Inject
    @Channel("prices")
    Emitter<Record<String, PriceResponse>> priceEmitter;

    public void publishPriceUpdate(PriceResponse priceResponse) {
        
        log.info("Publishing price update for coin: {}", priceResponse.getSymbol());

        try {
            priceEmitter.send(Record.of(priceResponse.getSymbol(), priceResponse))
                    .whenComplete((success, failure) -> {
                        if (failure != null) {
                            log.error("Failed to publish price update for {}", priceResponse.getSymbol(), failure);
                        } else {
                            log.debug("Successfully published price update for {}", priceResponse.getSymbol());
                        }
                    });
        } catch (Exception e) {
            log.error("Error publishing price update for {}", priceResponse.getSymbol(), e);
        }
    }

    public void publishPriceUpdates(List<PriceResponse> priceResponses) {
        priceResponses.forEach(this::publishPriceUpdate);
    }
}