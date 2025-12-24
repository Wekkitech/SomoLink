package com.owuor.somolink.network.service;

import com.owuor.somolink.network.config.RouterOSClient;
import com.owuor.somolink.network.dto.HotspotResponseDto;
import com.owuor.somolink.network.dto.ServerProfileResponseDto;
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
    private final BridgeConfigurationRepository bridgeConfigurationRepository;

    public HotspotService(RouterOSClient routerClient,
                          ServerProfileRepository profileRepository,
                          HotspotRepository hotspotRepository,
                          UserProfileRepository userProfileRepository,
                          BridgeConfigurationRepository bridgeConfigurationRepository) {
        this.routerClient = routerClient;
        this.profileRepository = profileRepository;
        this.hotspotRepository = hotspotRepository;
        this.userProfileRepository = userProfileRepository;
        this.bridgeConfigurationRepository = bridgeConfigurationRepository;
    }

    /**
     * Create a user profile on MikroTik AND save in DB
     */
    public UserProfile createUserProfile(UserProfileRequest request) throws Exception {
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

        return userProfileRepository.save(profile);
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
        BridgeConfiguration port = bridgeConfigurationRepository.findById(portId)
                .orElseThrow(() -> new RuntimeException("Port configuration not found for id: " + portId));
        System.out.println("[DEBUG] Port loaded: " + port.getId());

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
        serverProfile.setBridgeConfiguration(port);

        profileRepository.save(serverProfile);

        System.out.println("[DEBUG] Hotspot server profile saved in DB: " + profileName);
    }


    public void setupHotspotOnBridgeInterface(
            HotspotSetupRequest request,
            Long bridgeConfigurationId
    ) throws Exception {

        System.out.println("[DEBUG] Starting hotspot setup");
        System.out.println("[DEBUG] bridgeConfigurationId = " + bridgeConfigurationId);
        System.out.println("[DEBUG] requested hotspotName = " + request.getHotspotName());

        BridgeConfiguration bridge = bridgeConfigurationRepository
                .findById(bridgeConfigurationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Bridge not found")
                );

        System.out.println("[DEBUG] Bridge found: " + bridge.getBridgeName());

        ServerProfile serverProfile = bridge.getServerProfile();
        if (serverProfile == null) {
            throw new IllegalStateException("Bridge has no server profile attached");
        }

        System.out.println("[DEBUG] Using server profile: " + serverProfile.getProfileName());

        try {
            routerClient.setupHotspot(
                    bridge.getBridgeName(),
                    request.getHotspotName(),
                    serverProfile.getProfileName()
            );
        } catch (Exception ex) {
            System.err.println("[ERROR] MikroTik hotspot setup failed");
            System.err.println("[ERROR] Reason: " + ex.getMessage());
            throw new RuntimeException("Failed to setup hotspot: " + ex.getMessage(), ex);
        }

        Hotspot hotspot = new Hotspot();
        hotspot.setHotspotName(request.getHotspotName());
        hotspot.setInterfaceName(bridge.getBridgeName());
        hotspot.setProfile(serverProfile);
        hotspot.setBridgeConfiguration(bridge);
        hotspot.setConfigured(true);
        hotspot.setCreatedAt(LocalDateTime.now());
        hotspot.setConfiguredAt(LocalDateTime.now());

        hotspotRepository.save(hotspot);

        System.out.println("[DEBUG] Hotspot saved in DB successfully");
    }



    public List<ServerProfileResponseDto> getAllServerProfiles() {
        return profileRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ServerProfileResponseDto toDto(ServerProfile profile) {
        return new ServerProfileResponseDto(
                profile.getId(),
                profile.getProfileName(),
                profile.getDnsName(),
                profile.getHotspotAddress(),
                profile.isConfigured(),
                profile.getCreatedAt(),
                profile.getBridgeConfiguration().getId(),
                profile.getBridgeConfiguration().getBridgeName()
        );
    }


    public HotspotResponseDto getHotspot(Long bridgeId) {

        Hotspot hotspot = hotspotRepository.findByBridgeConfiguration_Id(bridgeId).orElseThrow(
                () -> new RuntimeException("Hotspot not found")
        );
        System.out.println("[DEBUG] Hotspot found: " + hotspot.getHotspotName());
        return toDto(hotspot);
    }

    private HotspotResponseDto toDto(Hotspot hotspot) {
        return new HotspotResponseDto(
                hotspot.getId(),
                hotspot.getHotspotName(),
                hotspot.getInterfaceName(),
                hotspot.getProfile().getId(),
                hotspot.getProfile().getProfileName(),
                hotspot.getBridgeConfiguration().getId(),
                hotspot.getBridgeConfiguration().getBridgeName(),
                hotspot.isConfigured(),
                hotspot.getCreatedAt(),
                hotspot.getConfiguredAt()
        );
    }
}
