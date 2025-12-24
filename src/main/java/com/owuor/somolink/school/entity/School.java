package com.owuor.somolink.school.entity;

import com.owuor.somolink.auth.entity.User;
import com.owuor.somolink.network.entity.BridgeConfiguration;
import com.owuor.somolink.network.entity.WlanConfiguration;
import lombok.Data;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Data
public class School {

    @Id @GeneratedValue
    private Long id;

    private String name;
    private String code;
    private String location;

    private boolean active = true;

    @OneToMany(mappedBy = "school")
    private List<User> users;


    @OneToMany(mappedBy = "school")
    private List<Device> devices;

    @OneToOne(mappedBy = "school")
    private BridgeConfiguration bridgeConfiguration;

    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WlanConfiguration> wlans;


}
