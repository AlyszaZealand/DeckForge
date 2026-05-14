package feedback.deckforge.Service.RepoInterfaces;

import feedback.deckforge.Model.Deck;

import java.util.List;
import java.util.Optional;

public interface IDeckRepository {
    void saveDeck(Deck deck);
    void deleteDeck(int deckID);
    void updateDeck(Deck deck);
    void addCardToDeck(int deckID, int cardID, int quantity);
    void removeCardFromDeck(int deckID, int cardID);
    Optional<Deck> findDeckByID(int deckID);
    List<Deck> findAllDecksByUserID(int userID);
    int getCardQuantity(int deckID, int cardID);
    void updateCardQuantity(int deckID, int cardID, int quantity);



}
