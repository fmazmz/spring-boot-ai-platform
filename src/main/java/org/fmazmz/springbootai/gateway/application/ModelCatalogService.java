package org.fmazmz.springbootai.gateway.application;

import org.fmazmz.springbootai.gateway.domain.Model;
import org.fmazmz.springbootai.gateway.domain.LlmProvider;
import org.fmazmz.springbootai.gateway.dto.ModelOptionResponse;
import org.fmazmz.springbootai.gateway.infra.providers.openrouter.ModelMapper;
import org.fmazmz.springbootai.gateway.repository.ModelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelCatalogService {
    private final ModelMapper openRouterModelMapper;
    private final ModelRepository modelRepository;

    public ModelCatalogService(ModelMapper openRouterModelMapper, ModelRepository modelRepository) {
        this.openRouterModelMapper = openRouterModelMapper;
        this.modelRepository = modelRepository;
    }

    public List<ModelOptionResponse> getModels(LlmProvider providerType) {
        if (providerType != LlmProvider.OPENROUTER) {
            return List.of();
        }
        return modelRepository.findAllByOrderBySlugAsc()
                .stream()
                .map(this::toOption)
                .toList();
    }

    public List<ModelOptionResponse> syncModels(LlmProvider providerType) {
        List<Model> synced = switch (providerType) {
            case OPENROUTER -> openRouterModelMapper.syncOpenRouterModels();
        };
        return synced.stream().map(this::toOption).toList();
    }

    private ModelOptionResponse toOption(Model model) {
        return new ModelOptionResponse(
                model.getId(),
                model.getName(),
                model.getSlug(),
                model.getPricing() != null ? model.getPricing().getPrompt() : null,
                model.getPricing() != null ? model.getPricing().getCompletion() : null,
                model.getSupportedParameters() != null ? model.getSupportedParameters() : List.of()
        );
    }
}
