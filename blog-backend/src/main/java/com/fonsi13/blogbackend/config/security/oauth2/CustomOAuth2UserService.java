package com.fonsi13.blogbackend.config.security.oauth2;

import com.fonsi13.blogbackend.models.AuthProvider;
import com.fonsi13.blogbackend.models.User;
import com.fonsi13.blogbackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return processOAuth2User(userRequest, oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId, oAuth2User.getAttributes());

        if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationException("Email no encontrado del proveedor OAuth2");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Si el usuario existe pero con diferente proveedor, actualizar
            if (!user.getProvider().equals(AuthProvider.valueOf(registrationId.toUpperCase()))) {
                user = updateExistingUser(user, oAuth2UserInfo, registrationId);
            }
        } else {
            user = registerNewUser(oAuth2UserInfo, registrationId);
        }

        return new CustomUserPrincipal(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserInfo oAuth2UserInfo, String registrationId) {
        User user = User.builder()
                .provider(AuthProvider.valueOf(registrationId.toUpperCase()))
                .providerId(oAuth2UserInfo.getId())
                .username(generateUsername(oAuth2UserInfo.getEmail()))
                .email(oAuth2UserInfo.getEmail())
                .profilePicture(oAuth2UserInfo.getImageUrl())
                .emailVerified(true)
                .role("USER")
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo, String registrationId) {
        existingUser.setProvider(AuthProvider.valueOf(registrationId.toUpperCase()));
        existingUser.setProviderId(oAuth2UserInfo.getId());
        existingUser.setProfilePicture(oAuth2UserInfo.getImageUrl());
        existingUser.setEmailVerified(true);
        return userRepository.save(existingUser);
    }

    private String generateUsername(String email) {
        String baseUsername = email.split("@")[0];
        // Si el username ya existe, agregar timestamp
        if (userRepository.existsByUsername(baseUsername)) {
            return baseUsername + "_" + System.currentTimeMillis();
        }
        return baseUsername;
    }
}
