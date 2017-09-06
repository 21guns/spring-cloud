package com.guns21.cloud.limiting;

import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;

import java.util.Map;

/**
 * Created by charles on 2017/5/22.
 */
public class LabelAndWeightMetadataRuleEureka extends LabelAndWeightMetadataRule {

    @Override
    protected Map<String, String> getMetadata(Server server) {
        return ((DiscoveryEnabledServer) server).getInstanceInfo().getMetadata();
    }
}
