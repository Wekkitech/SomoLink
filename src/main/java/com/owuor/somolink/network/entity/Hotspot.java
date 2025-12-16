package com.owuor.somolink.network.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class Hotspot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String hotspotName;

    @Column(nullable = false)
    private String interfaceName;//links it to the port configured

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private ServerProfile profile;

    // Link to the port configuration
    @OneToOne
    @JoinColumn(name = "port_configuration_id")
    private PortConfiguration portConfiguration;

    // Link to the bridge configuration
    @OneToOne
    @JoinColumn(name = "bridge_configuration_id")
    private BridgeConfiguration bridgeConfiguration;

    private LocalDateTime createdAt;
    private LocalDateTime configuredAt;

    private boolean configured;   // true if successfully applied to MikroTik

    @OneToMany(mappedBy = "hotspot", cascade = CascadeType.ALL)
    private List<AccessPoint> accessPoints; // All APs linked to this hotspot

}
