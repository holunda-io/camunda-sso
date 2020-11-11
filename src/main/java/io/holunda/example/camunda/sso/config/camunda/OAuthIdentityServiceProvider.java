package io.holunda.example.camunda.sso.config.camunda;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.NativeUserQuery;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.TenantQuery;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.identity.UserQuery;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.UserQueryImpl;
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OAuthIdentityServiceProvider extends AbstractManager implements ReadOnlyIdentityProvider {

    @Override
    public User findUserById(String userId) {
        return createUserQuery().userId(userId).singleResult();
    }

    @Override
    public UserQuery createUserQuery() {
        return new OAuthUserQueryImpl(this);
    }

    @Override
    public UserQuery createUserQuery(CommandContext commandContext) {
        return new OAuthUserQueryImpl(this);
    }

    private List<User> list(OAuthUserQueryImpl oAuthUserQuery) {
        return Collections.emptyList();
    }

    private long count(OAuthUserQueryImpl oAuthUserQuery) {
        return 0;
    }

    private User single(OAuthUserQueryImpl oAuthUserQuery) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2AuthenticationToken) {
            if (authentication.getPrincipal() instanceof OidcUser) {
                Map<String, Object> claims = ((OidcUser)authentication.getPrincipal()).getUserInfo().getClaims();
                String userId = (String)claims.get("sub");
                return new OAuthUser(
                    userId,
                    (String)claims.getOrDefault("given_name", userId),
                    (String)claims.getOrDefault("family_name", userId),
                    (String)claims.getOrDefault("email", userId)
                );
            }
        }
        return null;
    }


    @Override
    public NativeUserQuery createNativeUserQuery() {
        return null;
    }

    @Override
    public boolean checkPassword(String userId, String password) {
        return false;
    }

    @Override
    public Group findGroupById(String groupId) {
        return null;
    }

    @Override
    public GroupQuery createGroupQuery() {
        return null;
    }

    @Override
    public GroupQuery createGroupQuery(CommandContext commandContext) {
        return null;
    }

    @Override
    public Tenant findTenantById(String tenantId) {
        return null;
    }

    @Override
    public TenantQuery createTenantQuery() {
        return null;
    }

    @Override
    public TenantQuery createTenantQuery(CommandContext commandContext) {
        return null;
    }



    static class OAuthUserQueryImpl extends UserQueryImpl {

        private final OAuthIdentityServiceProvider oAuthIdentityServiceProvider;
        public OAuthUserQueryImpl(OAuthIdentityServiceProvider oAuthIdentityServiceProvider) {
            this.oAuthIdentityServiceProvider = oAuthIdentityServiceProvider;
        }

        @Override
        public long executeCount(CommandContext commandContext) {
            return oAuthIdentityServiceProvider.count(this);
        }

        @Override
        public List<User> executeList(CommandContext commandContext, Page page) {
            return oAuthIdentityServiceProvider.list(this);
        }

        @Override
        public User singleResult() {
            return oAuthIdentityServiceProvider.single(this);
        }
    }

    static class OAuthUser implements User {

        private final String id;
        private final String firstName;
        private final String lastName;
        private final String emailAddress;

        OAuthUser(String id, String firstName, String lastName, String emailAddress) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.emailAddress = emailAddress;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setId(String s) {
            throw new UnsupportedOperationException("Can't change user attributes");
        }

        @Override
        public String getFirstName() {
            return firstName;
        }

        @Override
        public void setFirstName(String s) {
            throw new UnsupportedOperationException("Can't change user attributes");
        }

        @Override
        public void setLastName(String s) {
            throw new UnsupportedOperationException("Can't change user attributes");
        }

        @Override
        public String getLastName() {
            return lastName;
        }

        @Override
        public void setEmail(String s) {
            throw new UnsupportedOperationException("Can't change user attributes");
        }

        @Override
        public String getEmail() {
            return emailAddress;
        }

        @Override
        public String getPassword() {
            throw new UnsupportedOperationException("Can't read user's password");
        }

        @Override
        public void setPassword(String s) {
            throw new UnsupportedOperationException("Can't change user attributes");
        }
    }
}
