package org.verapdf.crawler.app.healthchecks;

import com.codahale.metrics.health.HealthCheck;
import org.verapdf.crawler.app.engine.HeritrixClient;

public class HeritrixHealthCheck extends HealthCheck {
    private final HeritrixClient client;

    public HeritrixHealthCheck(HeritrixClient client) {
        this.client = client;
    }

    @Override
    protected Result check() throws Exception {
        if(client.testHeritrixAvailability()) {
            return Result.healthy();
        }
        return Result.unhealthy("Heritrix is not available");
    }
}
