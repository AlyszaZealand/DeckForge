package feedback.deckforge.Service;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Service.RepoInterfaces.ICardRepository;
import feedback.deckforge.Service.Validation.CardValidation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CardService {

    private ICardRepository cardRepository;
    private CardValidation cardValidation;

    public CardService(ICardRepository cardRepository, CardValidation cardValidation) {
        this.cardRepository = cardRepository;
        this.cardValidation = cardValidation;
    }

    public void saveCard(Card card){
        cardValidation.validateCard(card);
        cardRepository.saveCard(card);
    }

    public Optional<Card> getCardById(int cardID){
        return cardRepository.findCardById(cardID);
    }

    public List<Card> getAllCards(){
        return cardRepository.findAllCards();
    }

    public List<Card> searchCards(String name, String rarity, String color, String collectionType, Integer userId) {

        // Hvis vi søger i hele systemet, behøver vi intet userId
        if ("CATALOG".equals(collectionType)) {
            return cardRepository.searchAllCards(name, rarity, color);
        }

        // Til alle de personlige lister kræver vi et userId
        if (userId == null || userId <= 0) return List.of();

        // Trafiklederen vælger den rigtige tabel i databasen!
        return switch (collectionType) {
            case "COLLECTION" -> cardRepository.searchCollectionCards(userId, name, rarity, color);
            case "TRADE"      -> cardRepository.searchTradeCards(userId, name, rarity, color);
            case "WISH"       -> cardRepository.searchWishlistCards(userId, name, rarity, color);
            default           -> List.of(); // Sikkerhedsnet hvis noget går galt
        };
    }
}


