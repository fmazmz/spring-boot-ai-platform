package org.fmazmz.springbootai.gateway.repository;

import org.fmazmz.springbootai.gateway.domain.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ModelRepository extends JpaRepository<Model, UUID> {
    List<Model> findAllBySlugIn(Collection<String> slugs);

    List<Model> findAllByOrderBySlugAsc();
}
