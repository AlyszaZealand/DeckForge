package feedback.deckforge.Service;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Deck;
import feedback.deckforge.Service.RepoInterfaces.IDeckRepository;
import feedback.deckforge.Service.Validation.DeckValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeckService {

    private IDeckRepository deckRepository;
    private DeckValidation deckValidation;

    public DeckService(IDeckRepository deckRepository, DeckValidation deckValidation) {
        this.deckRepository = deckRepository;
        this.deckValidation = deckValidation;
    }

    public void saveDeck(Deck newDeck){
        deckRepository.saveDeck(newDeck);
    }

    public void deleteDeck(int deckID) {
        deckRepository.deleteDeck(deckID);
    }
    public void updateDeck(Deck deck) {
        deckRepository.updateDeck(deck);
    }

    public ValidationResult addCardToDeck(Deck currentDeck, Card newCard, int quantity) {

        ValidationResult result = deckValidation.validateAddCard(currentDeck, newCard, quantity);

        if (!result.hasErrors()) {
            deckRepository.addCardToDeck(currentDeck.getDeckId(), newCard.getCardID(), quantity);

            currentDeck.addCard(newCard, quantity);
        }

        return result;
    }
    public Optional<Deck> findDeckById(int deckID) {
        return deckRepository.findDeckById(deckID);
    }

    public List<Deck> findAllDecksByUserId(int userID) {
        return deckRepository.findAllDecksByUserId(userID);
    }


}
