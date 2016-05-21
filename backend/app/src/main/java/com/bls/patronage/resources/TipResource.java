package com.bls.patronage.resources;

import com.bls.patronage.StorageContexts;
import com.bls.patronage.StorageException;
import com.bls.patronage.StorageService;
import com.bls.patronage.api.TipRepresentation;
import com.bls.patronage.db.dao.TipDAO;
import com.bls.patronage.db.model.Tip;
import com.bls.patronage.db.model.User;
import io.dropwizard.auth.Auth;
import io.dropwizard.jersey.params.UUIDParam;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

@Path("/decks/{deckId}/flashcards/{flashcardId}/tips/{tipId}")
@Produces(MediaType.APPLICATION_JSON)
public class TipResource {
    private final TipDAO tipDAO;
    private final StorageService storageService;


    public TipResource(TipDAO tipDAO, StorageService storageService) {
        this.tipDAO = tipDAO;
        this.storageService = storageService;
    }

    @GET
    public TipRepresentation getTip(@Valid @PathParam("tipId") UUIDParam tipId) {
        return new TipRepresentation(tipDAO.getTipById(tipId.get()));
    }

    @DELETE
    public void deleteTip(@Valid @PathParam("tipId") UUIDParam tipId){
        tipDAO.getTipById(tipId.get());
        tipDAO.deleteTip(tipId.get());
    }

    @PUT
    public TipRepresentation updateTip(@Valid @PathParam("tipId") UUIDParam tipId,
                                       @Valid TipRepresentation tip,
                                       @Valid @PathParam("flashcardId") UUIDParam flashcardId,
                                       @Valid @PathParam("deckId") UUIDParam deckId){
        tipDAO.getTipById(tipId.get());
        tipDAO.updateTip(
                tip.setId(tipId.get())
                        .setFlashcardId(flashcardId.get())
                        .setDeckId(deckId.get())
                        .map()
        );

        return tip;
    }

    @Path("/essenceImage")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public TipRepresentation postEssenceImage(
            @Auth User user,
            @Valid @PathParam("tipId") UUIDParam tipId,
            @Valid @PathParam("flashcardId") UUIDParam flashcardId,
            @Valid @PathParam("deckId") UUIDParam deckId,
            @FormDataParam("file") InputStream inputStream,
            @Context HttpServletRequest request) throws StorageException, MalformedURLException {

        UUID essenceImageId = storageService.create(inputStream, StorageContexts.TIPS, user.getId());
        URL essenceImageURL = storageService.createPublicURL(request, StorageResource.class, essenceImageId, StorageContexts.TIPS, user.getId());

        Tip result = tipDAO.getTipById(tipId.get()).setEssenceImageURL(essenceImageURL.toString());
        tipDAO.updateTip(result);

        return new TipRepresentation(result);
    }
}
