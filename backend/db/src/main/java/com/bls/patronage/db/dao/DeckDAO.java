package com.bls.patronage.db.dao;

import com.bls.patronage.db.exception.DataAccessException;
import com.bls.patronage.db.mapper.DeckMapper;
import com.bls.patronage.db.model.Deck;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;

import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RegisterMapper(DeckMapper.class)
@UseStringTemplate3StatementLocator
public abstract class DeckDAO {

    @SqlQuery("select decks.id, decks.name, decks.isPublic from decks join usersDecks on usersDecks.deckId = decks.id " +
            "where usersDecks.userId = :userId group by decks.id")
    public abstract Collection<Deck> getAllUserDecks(@Bind("userId") UUID userId);

    @SqlQuery("select count(flashcards.id) from flashcards where flashcards.deckId = :deckId")
    public abstract Integer getFlashcardsCount(@Bind("deckId") UUID deckId);

    @SqlUpdate("insert into decks (id, name, isPublic) values (:id, :name, :isPublic)")
    abstract void insertDeck(@BindBean Deck deck);

    @SqlUpdate("insert into usersDecks (deckId, userId, creationDate) values (:id, :userId, :date)")
    abstract void insertUsersDeck(@BindBean Deck deck, @Bind("userId") UUID userId, @Bind("date") String date);

    @SqlUpdate("update decks set name = :name, isPublic = :isPublic where id = :id")
    public abstract void update(@BindBean Deck deck);

    @SqlUpdate("delete from decks where id = :id")
    public abstract void deleteDeck(@Bind("id") UUID id);

    @SqlQuery("select id, name, isPublic from decks where id = :id")
    abstract Deck getDeck(@Bind("id") UUID id);

    @SqlQuery("select userId from usersDecks where deckId = :id")
    abstract String getDeckUserId(@Bind("id") UUID id);

    @SqlQuery("select email from users where id = :id")
    abstract String getCreatorEmailFromUserId(@Bind("id") UUID id);

    @SqlQuery("select id, name, isPublic from decks where name like :name and isPublic='true'")
    abstract List<Deck> getDecksUsingName(@Bind("name") String name);

    @SqlQuery("select id, name, isPublic from decks " +
            "inner join usersDecks on decks.id = usersDecks.deckId " +
            "where decks.name like :name and usersDecks.userId = :id and decks.isPublic='false'")
    abstract List<Deck> getUserDecksUsingName(@Bind("name") String name, @Bind("id") UUID userId);

    @SqlQuery("select id, name, isPublic from decks where isPublic='true'")
    abstract Collection<Deck> getDecks();

    @SqlQuery("select decks.id, decks.name, decks.isPublic from decks " +
            "join usersDecks on usersDecks.deckId = decks.id " +
            "where usersDecks.userId = :userId or decks.isPublic = 'true' " +
            "limit 1 offset floor(:random*:number)")
    public abstract Collection<Deck> getRandomDeck(@Bind("userId") UUID userId,
                                                   @Bind("random") Double random,
                                                   @Bind("number") Integer number);

    @SqlQuery("select count(*) from decks join usersDecks on usersDecks.deckId = decks.id " +
            "where usersDecks.userId = :userId or decks.isPublic = 'true' ")
    abstract Integer getCountUserDecks(@Bind("userId") UUID userId);

    @SqlQuery("select creationDate from usersDecks where deckId = :id")
    public abstract String getDeckCreationDate(@Bind("id") UUID id);

    public void createDeck(Deck deck, UUID userId, Instant date) {
        insertDeck(deck);
        insertUsersDeck(deck, userId, date.toString());
    }

    public Deck getDeckById(UUID deckId, UUID userId) {
        Deck deck = getDeck(deckId);
        if (deck == null) {
            throw new DataAccessException("There is no deck with specified ID");
        }
        if (!(getDeckUserId(deckId).equals(userId.toString()) || deck.getIsPublic())) {
            throw new DataAccessException("You have no permission.", Response.Status.FORBIDDEN.getStatusCode());
        }
        return deck;
    }

    public Collection<Deck> getDecksByName(String name) {
        List<Deck> decks = getDecksUsingName(name);
        return decks;
    }

    public Collection<Deck> getUserDecksByName(String name, UUID userId) {
        List<Deck> decks = getUserDecksUsingName(name, userId);
        return decks;
    }

    public Collection<Deck> getAllDecks() {
        Collection<Deck> decks = getDecks();
        return decks;
    }

    public String getCreatorEmailFromDeckId(UUID deckId){
        return getCreatorEmailFromUserId(UUID.fromString(getDeckUserId(deckId)));
    }

    public Collection<Deck> getRandomDecks(UUID userId){
        Integer number = getCountUserDecks(userId);
        Collection<Deck> decks = getRandomDeck(userId, Math.random(), number);
        return decks;
    }
}
