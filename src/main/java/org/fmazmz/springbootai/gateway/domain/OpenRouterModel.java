package org.fmazmz.springbootai.gateway.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("OPENROUTER")
@NoArgsConstructor
public class OpenRouterModel extends Model {
}
