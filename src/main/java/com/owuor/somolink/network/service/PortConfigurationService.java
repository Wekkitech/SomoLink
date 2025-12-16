package com.owuor.somolink.network.service;

import com.owuor.somolink.network.config.RouterOSClient;
import com.owuor.somolink.network.dto.ConfigureBridgeRequest;
import com.owuor.somolink.network.dto.ConfigurePortRequest;
import com.owuor.somolink.network.entity.BridgeConfiguration;
import com.owuor.somolink.network.entity.PortConfiguration;
import com.owuor.somolink.network.repository.BridgeConfigurationRepository;
import com.owuor.somolink.network.repository.PortConfigurationRepository;
import com.owuor.somolink.network.utils.NetworkUtils;
import com.owuor.somolink.school.entity.School;
import com.owuor.somolink.school.repository.SchoolRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PortConfigurationService {

    private final RouterOSClient routerClient;
    private final PortConfigurationRepository repository;
    private final SchoolRepository schoolRepository;
    private final BridgeConfigurationRepository bridgeConfigurationRepository;

    public PortConfigurationService(RouterOSClient routerClient, PortConfigurationRepository repository, SchoolRepository schoolRepository, BridgeConfigurationRepository bridgeConfigurationRepository) {
        this.routerClient = routerClient;
        this.repository = repository;
        this.schoolRepository = schoolRepository;
        this.bridgeConfigurationRepository = bridgeConfigurationRepository;
    }

    public List<String> listInterfaces() throws Exception {
        return routerClient.getInterfaces();
    }

    public void configurePort(ConfigurePortRequest request, Long schoolId) throws Exception {

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new Exception("School not found"));

        System.out.println("=== Starting PORT configuration for: " + request.getPortName() + " ===");

        // -----------------------------------------------------
        // STEP 1: Validate physical interface exists on router
        // -----------------------------------------------------
        List<String> interfaces = routerClient.getInterfaces();
        System.out.println("Available interfaces: " + interfaces);

        if (!interfaces.contains(request.getPortName())) {
            throw new IllegalArgumentException(
                    "Physical interface does not exist: " + request.getPortName()
            );
        }

        // -----------------------------------------------------
        // STEP 2: Validate DB (port not already configured)
        // -----------------------------------------------------
        if (repository.existsByPortName(request.getPortName())) {
            throw new IllegalArgumentException(
                    "This port is already configured in the system"
            );
        }

        // -----------------------------------------------------
        // STEP 3: Calculate network values
        // -----------------------------------------------------
        String cidr = request.getIpAddress() + "/" + request.getSubnetMask();
        String networkCidr = NetworkUtils.toNetworkCidr(
                request.getIpAddress(),
                request.getSubnetMask()
        );
        String poolRange = NetworkUtils.calculatePool(
                request.getIpAddress(),
                request.getSubnetMask()
        );
        String poolName = "pool_" + request.getPortName() + "_" +
                UUID.randomUUID().toString().substring(0, 6);

        System.out.println("CIDR: " + cidr);
        System.out.println("Network CIDR: " + networkCidr);
        System.out.println("DHCP Pool Range: " + poolRange);
        System.out.println("Pool Name: " + poolName);

        // -----------------------------------------------------
        // STEP 4: Apply PORT configuration on MikroTik
        // (NO BRIDGE involved)
        // -----------------------------------------------------
        try {
            // 4a: Assign IP directly to physical interface
            System.out.println("Assigning IP to port: " + request.getPortName());
            routerClient.assignIp(request.getPortName(), cidr);

            // 4b: Create DHCP server on the same port
            System.out.println("Creating DHCP server on port: " + request.getPortName());
            routerClient.createDhcpAuto(
                    request.getPortName(),
                    request.getIpAddress(),
                    request.getSubnetMask(),
                    poolName,
                    poolRange,
                    networkCidr
            );

            System.out.println("Port configuration applied successfully on MikroTik.");

        } catch (Exception ex) {
            System.out.println("Error applying port config to MikroTik: " + ex.getMessage());
            throw new RuntimeException(
                    "Failed to apply port configuration to MikroTik",
                    ex
            );
        }

        // -----------------------------------------------------
        // STEP 5: Persist PORT configuration in DB
        // -----------------------------------------------------
        PortConfiguration config = new PortConfiguration();
        config.setPortName(request.getPortName());
        config.setCidr(cidr);
        config.setSubnetMask(request.getSubnetMask());
        config.setNetworkCidr(networkCidr);
        config.setDhcpPoolRange(poolRange);
        config.setDhcpPoolName(poolName);
        config.setDescription(request.getDescription());
        config.setConfigured(true);
        config.setSchool(school);

        repository.save(config);

        school.setPortConfiguration(config);
        schoolRepository.save(school);

        System.out.println("Port configuration saved successfully in DB.");
        System.out.println("=== Port configuration completed ===");
    }

    public void configureBridge(ConfigureBridgeRequest request, Long schoolId) throws Exception {

        // =========================================================
        // STEP 0: Load school
        // =========================================================
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new Exception("School not found"));

        // =========================================================
        // STEP 1: Ensure school does NOT already have a bridge
        // Business rule: ONE bridge per school
        // =========================================================
        if (bridgeConfigurationRepository.existsBySchoolId(schoolId)) {
            throw new IllegalStateException("This school already has a bridge configured.");
        }

        // =========================================================
        // STEP 2: Generate SAFE bridge name (RouterOS compatible)
        // =========================================================
        String safeSchoolName = school.getName()
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "-");

        String bridgeName = "bridge-" + safeSchoolName + "-lan-" +
                UUID.randomUUID().toString().substring(0, 6);

        System.out.println("=== Starting BRIDGE configuration for: " + bridgeName + " ===");

        // =========================================================
        // STEP 3: Validate interfaces exist on MikroTik
        // =========================================================
        List<String> availableInterfaces = routerClient.getInterfaces();
        System.out.println("Available interfaces: " + availableInterfaces);

        for (String iface : request.getInterfaces()) {
            if (!availableInterfaces.contains(iface)) {
                throw new IllegalArgumentException("Interface does not exist on router: " + iface);
            }
        }

        // =========================================================
        // STEP 4: Network calculations
        // =========================================================
        String cidr = request.getIpAddress() + "/" + request.getSubnetMask();
        String networkCidr = NetworkUtils.toNetworkCidr(
                request.getIpAddress(),
                request.getSubnetMask()
        );

        String poolRange = NetworkUtils.calculatePool(
                request.getIpAddress(),
                request.getSubnetMask()
        );

        String poolName = "pool_" + bridgeName + "_" +
                UUID.randomUUID().toString().substring(0, 6);

        System.out.println("CIDR: " + cidr);
        System.out.println("Network CIDR: " + networkCidr);
        System.out.println("DHCP Pool Range: " + poolRange);
        System.out.println("Pool Name: " + poolName);

        // =========================================================
        // STEP 5: Apply BRIDGE configuration on MikroTik
        // Includes rollback safety
        // =========================================================
        try {
            // 5a: Create bridge and add interfaces
            routerClient.createBridge(bridgeName, request.getInterfaces());

            // 5b: Assign IP address to bridge
            routerClient.assignIp(bridgeName, cidr);

            // 5c: Create DHCP server & network on bridge
            routerClient.createDhcpAuto(
                    bridgeName,
                    request.getIpAddress(),
                    request.getSubnetMask(),
                    poolName,
                    poolRange,
                    networkCidr
            );

            System.out.println("Bridge configuration applied successfully on MikroTik.");

        } catch (Exception ex) {
            // =====================================================
            // ROLLBACK: Remove partially created bridge
            // =====================================================
            System.err.println("Error during bridge configuration. Rolling back...");
            try {
                routerClient.deleteBridgeIfExists(bridgeName);
            } catch (Exception rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }

            throw new RuntimeException(
                    "Failed to apply bridge configuration to MikroTik: " + ex.getMessage(),
                    ex
            );
        }

        // =========================================================
        // STEP 6: Persist BRIDGE configuration to database
        // =========================================================
        BridgeConfiguration bridgeConfig = new BridgeConfiguration();
        bridgeConfig.setBridgeName(bridgeName);
        bridgeConfig.setCidr(cidr);
        bridgeConfig.setSubnetMask(String.valueOf(request.getSubnetMask()));
        bridgeConfig.setNetworkCidr(networkCidr);
        bridgeConfig.setDhcpPoolName(poolName);
        bridgeConfig.setDhcpPoolRange(poolRange);
        bridgeConfig.setDescription(request.getDescription());
        bridgeConfig.setConfigured(true);
        bridgeConfig.setInterfaces(request.getInterfaces());
        bridgeConfig.setSchool(school);

        bridgeConfigurationRepository.save(bridgeConfig);

        // =========================================================
        // STEP 7: Link bridge to school
        // =========================================================
        school.setBridgeConfiguration(bridgeConfig);
        schoolRepository.save(school);

        System.out.println("Bridge configuration saved successfully in DB.");
        System.out.println("=== Bridge configuration completed ===");
    }

}
