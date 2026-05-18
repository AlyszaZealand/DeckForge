package feedback.deckforge.Service.RepoInterfaces;

import feedback.deckforge.Model.Card;
import java.util.List;
import java.util.Optional;

public interface ICardRepository {
    void saveCard(Card card);
    Optional<Card> findCardByID(int cardId);
    List<Card> findAllCards();
    List<Card> searchAllCards(String name, String rarity, String color, String cardType);
    List<Card> searchCollectionCards(int userId, String name, String rarity, String color, String cardType);
    List<Card> searchTradeCards(int userId, String name, String rarity, String color, String cardType);
    List<Card> searchWishlistCards(int userId, String name, String rarity, String color, String cardType);
    void deleteCard(int cardId);
}
