package com.owuor.somolink.network.entity;

import com.owuor.somolink.school.entity.School;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class WlanConfiguration {

    @Id
    @GeneratedValue
    private Long id;

    private String wlanInterface;
    private String ssidName;

    private boolean configured = false;

    @ManyToOne
    @JoinColumn(name = "school_id")
    private School school;
}
