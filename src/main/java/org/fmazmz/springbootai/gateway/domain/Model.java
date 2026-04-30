package org.fmazmz.springbootai.gateway.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "models")
@Getter
@Setter
@NoArgsConstructor
public abstract class Model {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    @Size(max = 255)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    @Embedded
    private ModelPricing pricing;

    @ElementCollection
    @CollectionTable(name = "model_supported_parameters", joinColumns = @JoinColumn(name = "model_id"))
    @Column(name = "parameter")
    private List<String> supportedParameters;
}
