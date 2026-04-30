package org.fmazmz.springbootai.gateway.openrouter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fmazmz.springbootai.gateway.domain.Model;
import org.fmazmz.springbootai.gateway.domain.ModelPricing;
import org.fmazmz.springbootai.gateway.domain.OpenRouterModel;
import org.fmazmz.springbootai.gateway.repository.ModelRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModelMapper {
    private final OpenRouterGw openRouterGw;
    private final ModelRepository modelRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ModelMapper(
            OpenRouterGw openRouterGw,
            ModelRepository modelRepository
    ) {
        this.openRouterGw = openRouterGw;
        this.modelRepository = modelRepository;
    }

    public List<Model> syncOpenRouterModels() {
        String payload = openRouterGw.getModels().block();
        if (payload == null || payload.isBlank()) {
            return List.of();
        }

        List<OpenRouterModel> parsedModels = parseOpenRouterModels(payload);
        if (parsedModels.isEmpty()) {
            return List.of();
        }

        Map<String, Model> existingBySlug = new HashMap<>();
        for (Model existing : modelRepository.findAllBySlugIn(
                parsedModels.stream().map(Model::getSlug).toList())) {
            existingBySlug.put(existing.getSlug(), existing);
        }

        List<Model> toSave = new ArrayList<>();
        for (OpenRouterModel parsed : parsedModels) {
            Model target = existingBySlug.getOrDefault(parsed.getSlug(), new OpenRouterModel());
            target.setSlug(parsed.getSlug());
            target.setPricing(parsed.getPricing());
            target.setSupportedParameters(parsed.getSupportedParameters());
            toSave.add(target);
        }

        return modelRepository.saveAll(toSave);
    }

    private List<OpenRouterModel> parseOpenRouterModels(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode data = root.path("data");
            if (!data.isArray()) {
                return List.of();
            }

            List<OpenRouterModel> models = new ArrayList<>();
            for (JsonNode node : data) {
                String slug = node.path("id").asText(null);
                if (slug == null || slug.isBlank()) {
                    continue;
                }

                OpenRouterModel model = new OpenRouterModel();
                model.setSlug(slug);
                model.setSupportedParameters(readSupportedParameters(node.path("supported_parameters")));

                ModelPricing pricing = new ModelPricing();
                JsonNode pricingNode = node.path("pricing");
                pricing.setPrompt(asNullableText(pricingNode.path("prompt")));
                pricing.setCompletion(asNullableText(pricingNode.path("completion")));
                model.setPricing(pricing);

                models.add(model);
            }
            return models;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse OpenRouter models response", e);
        }
    }

    private List<String> readSupportedParameters(JsonNode supportedParametersNode) {
        if (!supportedParametersNode.isArray()) {
            return List.of();
        }

        List<String> supportedParameters = new ArrayList<>();
        for (JsonNode parameterNode : supportedParametersNode) {
            if (parameterNode.isTextual()) {
                supportedParameters.add(parameterNode.asText());
            }
        }
        return supportedParameters;
    }

    private String asNullableText(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value == null || value.isBlank() ? null : value;
    }
}
