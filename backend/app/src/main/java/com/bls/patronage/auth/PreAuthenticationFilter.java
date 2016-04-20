package com.bls.patronage.auth;

import org.glassfish.jersey.internal.util.Base64;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;

@Priority(Priorities.AUTHENTICATION - 100)
public class PreAuthenticationFilter implements ContainerRequestFilter {
    public static String defultUserEmail = "anon";
    public static String defultUserPassword = "password";

    private final boolean isAuthenticationRequired;

    public PreAuthenticationFilter(final boolean isAuthenticationRequired) {
        this.isAuthenticationRequired = isAuthenticationRequired;
    }

    @Override
    public void filter(ContainerRequestContext request) throws IOException {
        boolean hasValidHeader = false;
        if (request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            final String header = request.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (header.toLowerCase().startsWith("basic")) {
                hasValidHeader = true;
            }
        }
        if (!hasValidHeader) {
            final String base64 = Base64.encodeAsString(defultUserEmail + ":" + defultUserPassword);
            request.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, "Basic " + base64);
        }
    }
}