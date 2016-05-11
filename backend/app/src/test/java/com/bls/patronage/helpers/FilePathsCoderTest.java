package com.bls.patronage.helpers;

import com.bls.patronage.resources.StorageResource;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class FilePathsCoderTest {

    private String baseLocation = "./storage";
    private UUID userId = UUID.randomUUID();
    private UUID fileId = UUID.randomUUID();

    private String encoded = new StringBuilder()
            .append(UriBuilder.fromResource(StorageResource.class).build(userId).toString())
            .append("/")
            .append(fileId)
            .toString();
    private Path decoded = Paths.get(baseLocation, userId.toString(), fileId.toString());

    @Test
    public void testEncode() throws MalformedURLException {
        URI uri = FilePathsCoder.encodeFilePath(decoded);

        assertThat(uri.toString()).isEqualTo(encoded);
    }

    @Test
    public void testDecode() {
        Path path = FilePathsCoder.decodeFilePath(userId, fileId);

        assertThat(path).isEqualTo(decoded);
    }
}
