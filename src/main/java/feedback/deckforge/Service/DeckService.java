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

        // Vi behøver ikke længere kalde cardRepository for commanderen her,
        // da repository.findDeckByID() allerede har fyldt den ud med detaljer.

        return deck;
    }

    public void addCardToDeck(int deckID, int cardID, int quantity) {
        // findDeckByID henter nu automatisk Commanderen med farver, så valideringen virker!
        Deck deck = deckRepository.findDeckByID(deckID)
                .orElseThrow(() -> new DeckNotFoundException("Deck ikke fundet"));

        Card card = cardRepository.findCardByID(cardID)
                .orElseThrow(() -> new CardNotFoundException("Kort ikke fundet"));

        ValidationResult result = deckValidation.validateAddCard(deck, card, quantity);

        if (result.hasErrors()) {
            throw new IllegalDeckCompositionException(String.join(", ", result.getErrors()));
        }

        int currentQty = deckRepository.getCardQuantity(deckID, cardID);
        if (currentQty > 0) {
            deckRepository.updateCardQuantity(deckID, cardID, currentQty + quantity);
        } else {
            deckRepository.addCardToDeck(deckID, cardID, quantity);
        }
    }

    public void addOneToDeck(int deckID, int cardID) {
        // Igen, decket kommer nu med fuld Commander-info direkte fra Repo
        Deck deck = deckRepository.findDeckByID(deckID)
                .orElseThrow(() -> new DeckNotFoundException("Deck ikke fundet"));

        Card card = cardRepository.findCardByID(cardID)
                .orElseThrow(() -> new CardNotFoundException("Kort ikke fundet"));

        ValidationResult result = deckValidation.validateAddCard(deck, card, 1);

        if (result.hasErrors()) {
            throw new IllegalDeckCompositionException(String.join(", ", result.getErrors()));
        }

        int currentQty = deckRepository.getCardQuantity(deckID, cardID);
        deckRepository.updateCardQuantity(deckID, cardID, currentQty + 1);
    }

    public void removeOneFromDeck(int deckID, int cardID) {
        int currentQty = deckRepository.getCardQuantity(deckID, cardID);

        if (currentQty > 1) {
            deckRepository.updateCardQuantity(deckID, cardID, currentQty - 1);
        } else {
            deckRepository.removeCardFromDeck(deckID, cardID);
        }
    }

    // VIGTIG FOR TÆLLEREN: findAllDecksByUserID i Repo henter nu items, så vi kan tælle 45/60
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
}