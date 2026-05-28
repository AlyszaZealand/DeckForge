package feedback.deckforge.Service;

import feedback.deckforge.Exceptions.*;
import feedback.deckforge.Model.*;
import feedback.deckforge.Service.RepoInterfaces.ICardRepository;
import feedback.deckforge.Service.RepoInterfaces.ICollectionRepository;
import feedback.deckforge.Service.RepoInterfaces.IDeckRepository;
import feedback.deckforge.Service.RepoInterfaces.IFormatRepository;
import feedback.deckforge.Service.Validation.DeckValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeckService {

    private final IDeckRepository deckRepository;
    private final IFormatRepository formatRepository;
    private final ICollectionRepository collectionRepository;
    private final ICardRepository cardRepository;
    private final DeckValidation deckValidation;

    public DeckService(IDeckRepository deckRepository, IFormatRepository formatRepository,
                       ICollectionRepository collectionRepository, ICardRepository cardRepository,
                       DeckValidation deckValidation) {
        this.deckRepository = deckRepository;
        this.formatRepository = formatRepository;
        this.collectionRepository = collectionRepository;
        this.cardRepository = cardRepository;
        this.deckValidation = deckValidation;
    }

    // Opretter et helt nyt tomt deck
    public void createNewDeck(String name, int formatId, int userID) {

        if (name == null || name.trim().length() < 3 || name.trim().length() > 50){
            throw new DeckNameNotValid("Decknavnet skal være mellem 3 og 50 tegn langt.");
        }

        Format format = formatRepository.findFormatByID(formatId).orElseThrow(() -> new FormatNotFoundException("Format ikke fundet"));

        Deck newDeck = new Deck();
        newDeck.setDeckName(name);
        newDeck.setFormat(format);

        User user = new User();
        user.setUserID(userID);
        newDeck.setUser(user);

        deckRepository.saveDeck(newDeck);
    }

    // OPTIMERET: findDeckByID henter nu selv commander og items med beskrivelser
    public Deck getDeckForBuilder(int deckID, int userID) {
        Deck deck = deckRepository.findDeckByID(deckID)
                .orElseThrow(() -> new DeckNotFoundException("Deck ikke fundet"));

        // Vi mangler stadig at vide, om brugeren ejer kortene i sit deck
        int collectionId = collectionRepository.findCollectionByUserId(userID)
                .orElseThrow(() -> new CollectionNotFoundException("Samling ikke fundet")).getCollectionId();

        for (DeckItem item : deck.getDeckItems()) {
            int owned = collectionRepository.getCardQuantity(collectionId, item.getCard().getCardID());
            item.setOwnedQuantity(owned);
        }

        return deck;
    }

    public void addCardsToDeck(int deckID, int cardID, int quantityToAdd) {
        Deck deck = deckRepository.findDeckByID(deckID)
                .orElseThrow(() -> new DeckNotFoundException("Deck ikke fundet"));

        Card card = cardRepository.findCardByID(cardID)
                .orElseThrow(() -> new CardNotFoundException("Kort ikke fundet"));

        // Validering tjekker automatisk om der er plads i formatet, max kopier tilladt, osv.
        ValidationResult result = deckValidation.validateAddCard(deck, card, quantityToAdd);

        if (result.hasErrors()) {
            throw new IllegalDeckCompositionException(String.join(", ", result.getErrors()));
        }

        int currentQty = deckRepository.getCardQuantity(deckID, cardID);
        if (currentQty > 0) {
            // Hvis kortet allerede er i decket, skruer vi bare antallet op
            deckRepository.updateCardQuantity(deckID, cardID, currentQty + quantityToAdd);
        } else {
            // Ellers tilføjer vi det som en ny række
            deckRepository.addCardToDeck(deckID, cardID, quantityToAdd);
        }
    }


    public void removeCardsFromDeck(int deckID, int cardID, int quantityToRemove) {
        int currentQty = deckRepository.getCardQuantity(deckID, cardID);

        int newQty = currentQty - quantityToRemove;

        if (newQty <= 0) {
            deckRepository.removeCardFromDeck(deckID, cardID);
        } else {
            // Ellers opdaterer vi bare mængden til det nye, lavere tal
            deckRepository.updateCardQuantity(deckID, cardID, newQty);
        }
    }


    public List<Deck> findAllDecksByUserId(int userID) {
        return deckRepository.findAllDecksByUserID(userID);
    }

    public List<Format> getAllFormats() {
        return formatRepository.findAllFormats();
    }

    public void setCommander(int deckID, int cardID) {
        Deck deck = deckRepository.findDeckByID(deckID)
                .orElseThrow(() -> new DeckNotFoundException("Deck ikke fundet"));

        Card card = cardRepository.findCardByID(cardID)
                .orElseThrow(() -> new CardNotFoundException("Kort ikke fundet"));

        ValidationResult result = deckValidation.validateCommander(card);
        if (result.hasErrors()) {
            throw new IllegalDeckCompositionException(String.join(", ", result.getErrors()));
        }

        deck.setCommander(card);
        deckRepository.updateDeck(deck);
    }

    public void deleteDeck(int deckID){
        deckRepository.deleteDeck(deckID);
    }

    public Deck getDeck(int deckID) {
        return deckRepository.findDeckByID(deckID)
                .orElseThrow(() -> new DeckNotFoundException("Deck ikke fundet"));
    }

    public void removeCommander(int deckID) {
        Deck deck = deckRepository.findDeckByID(deckID)
                .orElseThrow(() -> new DeckNotFoundException("Deck ikke fundet"));

        deck.setCommander(null); // Sætter feltet til null
        deckRepository.updateDeck(deck); // Gemmer ændringen i databasen
    }
}