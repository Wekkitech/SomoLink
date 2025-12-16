package com.owuor.somolink.network.service;

import com.owuor.somolink.network.config.RouterOSClient;
import com.owuor.somolink.network.dto.UserProfileRequest;
import com.owuor.somolink.network.dto.HotspotSetupRequest;
import com.owuor.somolink.network.entity.*;
import com.owuor.somolink.network.repository.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class HotspotService {

    private final RouterOSClient routerClient;
    private final ServerProfileRepository profileRepository;
    private final UserProfileRepository userProfileRepository;
    private final HotspotRepository hotspotRepository;
    private final PortConfigurationRepository portConfigurationRepository;
    private final BridgeConfigurationRepository bridgeConfigurationRepository;

    public HotspotService(RouterOSClient routerClient,
                          ServerProfileRepository profileRepository,
                          HotspotRepository hotspotRepository,
                          PortConfigurationRepository portConfigurationRepository,
                          UserProfileRepository userProfileRepository,
                          BridgeConfigurationRepository bridgeConfigurationRepository) {
        this.routerClient = routerClient;
        this.profileRepository = profileRepository;
        this.hotspotRepository = hotspotRepository;
        this.portConfigurationRepository = portConfigurationRepository;
        this.userProfileRepository = userProfileRepository;
        this.bridgeConfigurationRepository = bridgeConfigurationRepository;
    }

    /**
     * Create a user profile on MikroTik AND save in DB
     */
    public void createUserProfile(UserProfileRequest request) throws Exception {
        // 1. Apply to MikroTik
        routerClient.createHotspotUserProfile(
                request.getProfileName(),
                request.getRateLimitUpload(),
                request.getRateLimitDownload(),
                request.getSessionTimeout(),
                request.getIdleTimeout()
        );

        // 2. Save to DB
        UserProfile profile = new UserProfile();
        profile.setProfileName(request.getProfileName());
        profile.setRateLimitUpload(request.getRateLimitUpload());
        profile.setRateLimitDownload(request.getRateLimitDownload());
        profile.setSessionTimeout(request.getSessionTimeout());
        profile.setIdleTimeout(request.getIdleTimeout());
        profile.setAmount(request.getAmount());

        userProfileRepository.save(profile);
    }

    /**
     * Get all user hotspot profiles
     */
    public List<UserProfile> getAllUserProfiles() {
        System.out.println("[DEBUG] Fetching all hotspot user profiles");
        return userProfileRepository.findAll();
    }

    /**
     * Get user hotspot profile by ID
     */
    public UserProfile getUserProfileById(Long id) {
        System.out.println("[DEBUG] Fetching hotspot user profile with ID: " + id);
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotspot user profile not found with id: " + id));
    }

    /**
     * Create a MikroTik hotspot server profile using a port configuration
     *
     * @param portId      ID of the port configuration
     * @param profileName Name of the hotspot server profile
     * @throws Exception if RouterOS fails
     */
    public void createHotspotServerProfile(Long portId, String profileName, String dnsName) throws Exception {
        System.out.println("[DEBUG] Starting hotspot server profile creation...");

        // 1. Load port configuration
        PortConfiguration port = portConfigurationRepository.findById(portId)
                .orElseThrow(() -> new RuntimeException("Port configuration not found for id: " + portId));
        System.out.println("[DEBUG] Port loaded: " + port);

        // 2. Strip CIDR to get the gateway IP
        String hotspotAddress = port.getCidr().split("/")[0];
        System.out.println("[DEBUG] Hotspot gateway IP extracted from CIDR: " + hotspotAddress);

        // 3. Create DNS name for hotspot
        System.out.println("[DEBUG] Hotspot DNS name will be: " + dnsName);

        // 4. Call RouterOS client to create hotspot server profile
        routerClient.createHotspotServerProfile(profileName, hotspotAddress, dnsName);

        // 5. Persist in DB
        ServerProfile serverProfile = new ServerProfile();
        serverProfile.setProfileName(profileName);
        serverProfile.setHotspotAddress(hotspotAddress);
        serverProfile.setDnsName(dnsName);
        serverProfile.setConfigured(true);
        serverProfile.setCreatedAt(LocalDateTime.now());


        profileRepository.save(serverProfile);

        System.out.println("[DEBUG] Hotspot server profile saved in DB: " + profileName);
    }

    /**
     * Setup hotspot on interface AND save in DB
     */
    public void setupHotspotOnInterface(HotspotSetupRequest request, Long portConfigurationId) throws Exception {
        PortConfiguration portConfiguration = portConfigurationRepository.findById(portConfigurationId).orElseThrow(
                () -> new IllegalArgumentException("PortConfiguration not found: " + portConfigurationId)
        );

        // 🔥 Resolve or auto-create profile
        String gatewayIp = portConfiguration.getCidr().split("/")[0];

        // 1. Try reuse existing profile
        ServerProfile serverProfile =
                profileRepository.findByHotspotAddress(gatewayIp).orElseThrow(
                        () ->
                                new IllegalArgumentException("Server profile not found")
                );

        // 2. Apply to MikroTik
        routerClient.setupHotspot(
                portConfiguration.getPortName(),
                request.getHotspotName(),
                serverProfile.getProfileName()
        );

        // 3. Save to DB
        Hotspot hotspot = new Hotspot();
        hotspot.setHotspotName(request.getHotspotName());
        hotspot.setInterfaceName(portConfiguration.getPortName());
        hotspot.setProfile(serverProfile);
        hotspot.setConfigured(true);
        hotspot.setCreatedAt(LocalDateTime.now());
        hotspot.setConfiguredAt(LocalDateTime.now());
        hotspot.setPortConfiguration(portConfiguration);

        hotspotRepository.save(hotspot);
    }

    public void setupHotspotOnBridgeInterface(
            HotspotSetupRequest request,
            Long bridgeConfigurationId
    ) throws Exception {

        BridgeConfiguration bridge = bridgeConfigurationRepository
                .findById(bridgeConfigurationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Bridge not found")
                );

        // 🔥 Resolve or auto-create profile
        String gatewayIp = bridge.getCidr().split("/")[0];

        // 1. Try reuse existing profile
        ServerProfile serverProfile =
                profileRepository.findByHotspotAddress(gatewayIp).orElseThrow(
                        () ->
                                new IllegalArgumentException("Bridge not found")
                );


        // Apply hotspot on MikroTik
        routerClient.setupHotspot(
                bridge.getBridgeName(),
                request.getHotspotName(),
                serverProfile.getProfileName()
        );

        Hotspot hotspot = new Hotspot();
        hotspot.setHotspotName(
                request.getHotspotName()
        );
        hotspot.setInterfaceName(bridge.getBridgeName());
        hotspot.setProfile(serverProfile);
        hotspot.setBridgeConfiguration(bridge);
        hotspot.setConfigured(true);
        hotspot.setCreatedAt(LocalDateTime.now());
        hotspot.setConfiguredAt(LocalDateTime.now());

        hotspotRepository.save(hotspot);
    }
}
