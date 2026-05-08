package feedback.deckforge.Service.RepoInterfaces;

import feedback.deckforge.Model.Card;
import java.util.List;
import java.util.Optional;

public interface ICardRepository {
    void saveCard(Card card);
    Optional<Card> findCardById(int cardId);
    List<Card> findAllCards();
}
