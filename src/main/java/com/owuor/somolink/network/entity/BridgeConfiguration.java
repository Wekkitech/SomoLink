package com.owuor.somolink.network.entity;

import com.owuor.somolink.school.entity.School;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "bridge_configurations")
public class BridgeConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bridgeName;

    private String cidr;
    private int subnetMask;
    private String networkCidr;

    private String dhcpPoolName;
    private String dhcpPoolRange;

    private String description;

    private boolean configured;

    @ElementCollection
    @CollectionTable(name = "bridge_interfaces", joinColumns = @JoinColumn(name = "bridge_id"))
    @Column(name = "interface_name")
    private List<String> interfaces;

    @OneToOne
    @JoinColumn(name = "school_id")
    private School school;
}
