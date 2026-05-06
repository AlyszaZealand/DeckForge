package feedback.deckforge.Service.RepoInterfaces;

import feedback.deckforge.Model.Deck;

import java.util.List;
import java.util.Optional;

public interface IDeckRepository {
    void saveDeck(Deck deck);
    void deleteDeck(int deckID);
    void updateDeck(Deck deck);
    Optional<Deck> findDeckById(int deckID);
    List<Deck> findAllDecksByUserId(int userID);



}
