package com.chickennw.utils.models.config.head;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class HeadEntity {

    @Id
    @Column
    private UUID uuid;

    @Column(length = 1024)
    private String texture;

    public HeadEntity(UUID uuid, String texture) {
        this.uuid = uuid;
        this.texture = texture;
    }
}
