package org.fmazmz.springbootai.gateway.application;

import org.fmazmz.springbootai.gateway.domain.ProviderType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ProviderClientFactory {
    private final Map<ProviderType, ProviderClient> clientsByProvider;

    public ProviderClientFactory(List<ProviderClient> clients) {
        this.clientsByProvider = new EnumMap<>(ProviderType.class);
        for (ProviderClient client : clients) {
            this.clientsByProvider.put(client.providerType(), client);
        }
    }

    public ProviderClient getClient(ProviderType providerType) {
        ProviderClient client = clientsByProvider.get(providerType);
        if (client == null) {
            throw new IllegalArgumentException("Unsupported provider: " + providerType);
        }
        return client;
    }
}
