package org.fmazmz.springbootai.gateway.application;

import org.fmazmz.springbootai.gateway.domain.Model;
import org.fmazmz.springbootai.gateway.domain.LlmProvider;
import org.fmazmz.springbootai.gateway.dto.ModelOptionResponse;
import org.fmazmz.springbootai.gateway.infra.providers.openrouter.ModelMapper;
import org.fmazmz.springbootai.gateway.repository.ModelRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ModelCatalogService {
    private final ModelMapper openRouterModelMapper;
    private final ModelRepository modelRepository;

    public ModelCatalogService(ModelMapper openRouterModelMapper, ModelRepository modelRepository) {
        this.openRouterModelMapper = openRouterModelMapper;
        this.modelRepository = modelRepository;
    }

    @Cacheable(
            value = "llmModels",
            key = "T(java.lang.String).format('%s|%s|%d|%d|%s', #providerType, #search, #pageable.pageNumber, #pageable.pageSize, #pageable.sort)"
    )
    public Page<ModelOptionResponse> getModels(LlmProvider providerType, String search, Pageable pageable) {
        if (providerType != LlmProvider.OPENROUTER) {
            return Page.empty(pageable);
        }
        Page<Model> modelsPage;
        if (StringUtils.hasText(search)) {
            modelsPage = modelRepository.findBySlugContainingIgnoreCaseOrNameContainingIgnoreCase(search, search, pageable);
        } else {
            modelsPage = modelRepository.findAllByOrderBySlugAsc(pageable);
        }

        return modelsPage
                .map(this::toOption);
    }

    @CacheEvict(value = "llmModels", allEntries = true)
    public List<ModelOptionResponse> syncModels(LlmProvider providerType) {
        List<Model> synced = switch (providerType) {
            case OPENROUTER -> openRouterModelMapper.syncOpenRouterModels();
        };
        return synced.stream()
                .map(this::toOption)
                .toList();
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
