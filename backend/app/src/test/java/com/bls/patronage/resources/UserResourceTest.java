package com.bls.patronage.resources;

import com.bls.patronage.db.exception.DataAccessException;
import com.bls.patronage.db.model.UserWithoutPassword;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserResourceTest extends BasicAuthenticationTest {

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new UserResource(dao))
            .build();

    private String userURI;
    private String loggingURI;

    @Before
    public void setUp() {
        super.setUp();

        userURI = UriBuilder.fromResource(UserResource.class).build().toString()
                + UriBuilder.fromMethod(UserResource.class, "getUser").build(user.getId()).toString();
        loggingURI = UriBuilder.fromResource(UserResource.class).build().toString()
                + UriBuilder.fromMethod(UserResource.class, "logInUser").build().toString();
    }

    @Test
    public void getUser() {
        when(dao.getUserById(user.getId())).thenReturn(user);
        final UserWithoutPassword receivedUser = resources.client().target(userURI)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(UserWithoutPassword.class);

        verify(dao).getUserById(user.getId());
        assertThat(receivedUser.getId()).isEqualTo(user.getId());
        assertThat(receivedUser.getName()).isEqualTo(user.getName());
        assertThat(receivedUser.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void logInUser() {
        when(dao.getUserByEmail(user.getEmail())).thenReturn(user);
        final Response response = getResponseWithCredentials(loggingURI, encodedCredentials);
        final UserWithoutPassword receivedUser = response.readEntity(UserWithoutPassword.class);

        verify(dao, times(2)).getUserByEmail(user.getEmail());
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(receivedUser.getId()).isEqualTo(user.getId());
        assertThat(receivedUser.getName()).isEqualTo(user.getName());
        assertThat(receivedUser.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void logInUserWithBadPassword() {
        when(dao.getUserByEmail(user.getEmail())).thenReturn(user);
        final Response response = getResponseWithCredentials(loggingURI, badPasswordCredentials);

        assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        verify(dao).getUserByEmail(user.getEmail());
    }

    @Test
    public void logInUserWithBadEmail() {
        when(dao.getUserByEmail(fakeEmail))
                .thenThrow(new DataAccessException(""));
        final Response response = getResponseWithCredentials(loggingURI, badEmailCredentials);

        verify(dao).getUserByEmail(fakeEmail);
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }
}
