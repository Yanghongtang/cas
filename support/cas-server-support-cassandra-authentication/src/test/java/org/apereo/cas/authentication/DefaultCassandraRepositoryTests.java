package org.apereo.cas.authentication;

import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CassandraAuthenticationConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCassandraRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CassandraAuthenticationConfiguration.class
}, properties = {
    "cas.authn.cassandra.tableName=users_table",
    "cas.authn.cassandra.usernameAttribute=user_attr",
    "cas.authn.cassandra.passwordAttribute=pwd_attr",
    "cas.authn.cassandra.keyspace=cas"
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Cassandra")
@EnabledIfContinuousIntegration
public class DefaultCassandraRepositoryTests {
    @Autowired
    @Qualifier("cassandraAuthenticationHandler")
    private AuthenticationHandler cassandraAuthenticationHandler;

    @Test
    public void verifyUserNotFound() {
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("baduser", "Mellon");
        assertThrows(AccountNotFoundException.class, () -> cassandraAuthenticationHandler.authenticate(c));
    }

    @Test
    public void verifyUserBadPassword() {
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "bad");
        assertThrows(FailedLoginException.class, () -> cassandraAuthenticationHandler.authenticate(c));
    }

    @Test
    @SneakyThrows
    public void verifyUser() {
        val c = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        val result = cassandraAuthenticationHandler.authenticate(c);
        assertNotNull(result);
        assertEquals("casuser", result.getPrincipal().getId());
    }
}
