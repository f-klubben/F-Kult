package dk.fklub.fkult.config.security;

import dk.fklub.fkult.business.services.LoginRejectedException;
import dk.fklub.fkult.business.services.LoginService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.List;

/** Turns a successful F-Klub SSO login into a local F-Kult user. */
@Service
public class FkultOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    /** The stregsystem emits the stregusername as "username", not "preferred_username". */
    static final String USERNAME_CLAIM = "username";
    static final String NAME_CLAIM = "name";

    private final LoginService loginService;

    public FkultOidcUserService(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcIdToken idToken = userRequest.getIdToken();
        String stregUsername = idToken.getClaimAsString(USERNAME_CLAIM);
        if (stregUsername == null || stregUsername.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("missing_username"),
                    "F-Klub SSO did not return a " + USERNAME_CLAIM + " claim");
        }

        try {
            List<GrantedAuthority> authorities =
                    loginService.loginWithSso(stregUsername, idToken.getClaimAsString(NAME_CLAIM));
            return new DefaultOidcUser(authorities, idToken, USERNAME_CLAIM);
        } catch (LoginRejectedException e) {
            throw new OAuth2AuthenticationException(new OAuth2Error(e.getReason()), e.getMessage(), e);
        }
    }
}
