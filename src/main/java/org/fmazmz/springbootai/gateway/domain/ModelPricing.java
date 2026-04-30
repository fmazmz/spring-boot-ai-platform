package org.fmazmz.springbootai.gateway.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ModelPricing {

    private String prompt;
    private String completion;
}
