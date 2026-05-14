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
        Format format = formatRepository.findFormatByID(formatId)
                .orElseThrow(() -> new FormatNotFoundException("Format ikke fundet"));

        Deck newDeck = new Deck();
        newDeck.setDeckName(name);
        newDeck.setFormat(format);

        // Opret en bruger og sæt ID'et
        User user = new User();
        user.setUserID(userID);
        newDeck.setUser(user);

        deckRepository.saveDeck(newDeck);
    }

    // Henter decket og udregner ejerskab til builder-visningen
    public Deck getDeckForBuilder(int deckID, int userID) {
        Deck deck = deckRepository.findDeckByID(deckID)
                .orElseThrow(() -> new DeckNotFoundException("Deck ikke fundet"));

        int collectionId = collectionRepository.findCollectionByUserId(userID)
                .orElseThrow(() -> new CollectionNotFoundException("Samling ikke fundet")).getCollectionId();

        for (DeckItem item : deck.getDeckItems()) {
            int owned = collectionRepository.getCardQuantity(collectionId, item.getCard().getCardID());
            item.setOwnedQuantity(owned);
        }

        if (deck.getCommander() != null && deck.getCommander().getCardID() > 0) {
            Card fullCommander = cardRepository.findCardByID(deck.getCommander().getCardID()).orElse(null);
            deck.setCommander(fullCommander);
        }
        return deck;
    }

    public void addCardToDeck(int deckID, int cardID, int quantity) {
        // 1. Hent decket
        Deck deck = deckRepository.findDeckByID(deckID)
                .orElseThrow(() -> new DeckNotFoundException("Deck ikke fundet"));

        // 2. VIGTIGT: Hvis der er en commander, skal vi hente dens farver fra databasen!
        if (deck.getCommander() != null && deck.getCommander().getCardID() > 0) {
            Card fullCommander = cardRepository.findCardByID(deck.getCommander().getCardID())
                    .orElse(null);
            deck.setCommander(fullCommander);
        }

        // 3. Hent det kort der skal tilføjes
        Card card = cardRepository.findCardByID(cardID)
                .orElseThrow(() -> new RuntimeException("Kort ikke fundet"));

        // 4. Nu virker valideringen, fordi deck.getCommander() har sine farver ("R")
        ValidationResult result = deckValidation.validateAddCard(deck, card, quantity);

        if (result.hasErrors()) {
            throw new IllegalDeckCompositionException(String.join(", ", result.getErrors()));
        }

        // ... gem kortet i databasen herunder ...
        int currentQty = deckRepository.getCardQuantity(deckID, cardID);
        if (currentQty > 0) {
            deckRepository.updateCardQuantity(deckID, cardID, currentQty + quantity);
        } else {
            deckRepository.addCardToDeck(deckID, cardID, quantity);
        }
    }



    public void addOneToDeck(int deckID, int cardID) {
        // 1. Hent decket (som kun har en "skal" af en commander)
        Deck deck = deckRepository.findDeckByID(deckID)
                .orElseThrow(() -> new DeckNotFoundException("Deck ikke fundet"));

        // 2. VIGTIGT: Hent de fulde detaljer for commanderen (inkl. farver!),
        if (deck.getCommander() != null && deck.getCommander().getCardID() > 0) {
            Card fullCommander = cardRepository.findCardByID(deck.getCommander().getCardID())
                    .orElse(null);
            deck.setCommander(fullCommander);
        }

        // 3. Hent kortet der skal trykkes '+' på
        Card card = cardRepository.findCardByID(cardID)
                .orElseThrow(() -> new CardNotFoundException("Kort ikke fundet"));

        ValidationResult result = deckValidation.validateAddCard(deck, card, 1);

        if (result.hasErrors()) {
            throw new IllegalDeckCompositionException(String.join(", ", result.getErrors()));
        }

        // 5. Opdater mængden i databasen
        int currentQty = deckRepository.getCardQuantity(deckID, cardID);
        deckRepository.updateCardQuantity(deckID, cardID, currentQty + 1);
    }

    // Fjerner 1 kopi af et kort fra decket
    public void removeOneFromDeck(int deckID, int cardID) {
        int currentQty = deckRepository.getCardQuantity(deckID, cardID);

        if (currentQty > 1) {
            // Hvis der er mere end 1, trækker vi én fra
            deckRepository.updateCardQuantity(deckID, cardID, currentQty - 1);
        } else {
            // Hvis der kun er 1 tilbage (eller 0), fjerner vi kortet helt fra tabellen
            deckRepository.removeCardFromDeck(deckID, cardID);
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
                .orElseThrow(() -> new RuntimeException("Kort ikke fundet"));

        // Valider om kortet må være en commander
        ValidationResult result = deckValidation.validateCommander(card);
        if (result.hasErrors()) {
            throw new IllegalDeckCompositionException(String.join(", ", result.getErrors()));
        }

        // Gem commander på decket og opdater databasen
        deck.setCommander(card);
        deckRepository.updateDeck(deck);
    }

    public void deleteDeck(int deckID){
        deckRepository.deleteDeck(deckID);
    }
}
