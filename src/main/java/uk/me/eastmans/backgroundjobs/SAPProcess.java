package uk.me.eastmans.backgroundjobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Service
public class SAPProcess {

    private static final Logger log = LoggerFactory.getLogger(SAPProcess.class);

    // Async jobs cannot return a job object which can be cancelled
    // check out https://vaadin.com/docs/latest/building-apps/deep-dives/application-layer/background-jobs/interaction/callbacks
    @Async
    public void executeProcess(Consumer<String> onComplete,
                               Consumer<Double> onProgress,
                               Consumer<Exception> onError) {
        // This will start a SAP process which will run in the background
        try {
            for (int i=0; i<10; i++) {
                Thread.sleep(1000); // Wait second
                log.info("Executing SAP Process step " + i);
                onProgress.accept((double)i / 10.0);
            }
            onComplete.accept("Completed SAP Process");
        } catch (Exception ex) {
            onError.accept(ex);
        }
    }

    public Flux<String> startBackgroundJob() {
        return Flux.create(
                sink -> {
                    try {
                        for (int i = 0; i < 10; i++) {
                            Thread.sleep(1000); // Wait second
                            log.info("Executing SAP background job Process step " + i);
                            sink.next("Processed " + (double) i / 10.0);
                        }
                        sink.complete();
                    } catch (Exception ex) {
                        sink.error(ex);
                    }
                });
    }
}