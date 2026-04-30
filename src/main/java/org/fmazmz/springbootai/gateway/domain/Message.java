package org.fmazmz.springbootai.gateway.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    @Size(max = 55)
    private String role;

    @Column(columnDefinition = "TEXT")
    @Size(max = 5000)
    private String content;
}
