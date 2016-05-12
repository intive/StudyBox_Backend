package com.bls.patronage.cv;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public class CVRequest {

    public final URI location;
    public final String action;

    public CVRequest(URI location, String action) {
        this.location = location;
        this.action = action;
    }

    @JsonProperty
    public URI getLocation() {
        return location;
    }

    @JsonProperty
    public String getAction() {
        return action;
    }
}