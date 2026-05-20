package feedback.deckforge.Service;

import feedback.deckforge.Exceptions.CollectionNotFoundException;
import feedback.deckforge.Exceptions.InsufficientCardsException;
import feedback.deckforge.Model.Collection;
import feedback.deckforge.Model.TradeCollection;
import feedback.deckforge.Model.TradeCollectionItem;
import feedback.deckforge.Service.RepoInterfaces.ICollectionRepository;
import feedback.deckforge.Service.RepoInterfaces.ITradeCollectionRepository;
import feedback.deckforge.Service.Validation.TradeCollectionValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TradeCollectionService {

    private ITradeCollectionRepository tradeCollectionRepository;
    private final ICollectionRepository collectionRepository;
    private final TradeCollectionValidation tradeCollectionValidation;

    public TradeCollectionService(ITradeCollectionRepository tradeCollectionRepository, ICollectionRepository collectionRepository, TradeCollectionValidation tradeCollectionValidation) {
        this.tradeCollectionRepository = tradeCollectionRepository;
        this.collectionRepository = collectionRepository;
        this.tradeCollectionValidation = tradeCollectionValidation;
    }

    public Optional<TradeCollection> getTradeCollectionByUserID(int userID){
        return tradeCollectionRepository.findTradeCollectionByUserId(userID);
    }

    public void addCardsToTradeCollection(int userID, int cardID, int quantityToAdd) {
        // 1. Find samlingen ud fra UserID (Controlleren slipper for dette arbejde nu!)
        int tradeColId = tradeCollectionRepository.findTradeCollectionByUserId(userID)
                .orElseThrow(() -> new CollectionNotFoundException("Bytteliste ikke fundet")).getTradeCollectionId();

        Collection privateCol = collectionRepository.findCollectionByUserId(userID).orElse(null);

        // 2. Find nuværende antal
        int currentTradeQty = tradeCollectionRepository.getCardQuantity(tradeColId, cardID);
        int newTotalQuantity = currentTradeQty + quantityToAdd;

        // 3. Validering (Har de faktisk nok kort i deres private samling til at sætte dem til bytte?)
        ValidationResult result = tradeCollectionValidation.validateAddCardToTradeCollection(cardID, newTotalQuantity, privateCol);
        if (result.hasErrors()) {
            throw new InsufficientCardsException(result.getErrors().get(0));
        }

        // 4. Gem i databasen
        if (currentTradeQty > 0) {
            tradeCollectionRepository.setCardQuantity(tradeColId, cardID, newTotalQuantity);
        } else {
            tradeCollectionRepository.addCardToTradeCollection(tradeColId, cardID, quantityToAdd);
        }
    }


    public void removeCardsFromTradeCollection(int userID, int cardID, int quantityToRemove) {
        int tradeColId = tradeCollectionRepository.findTradeCollectionByUserId(userID)
                .orElseThrow(() -> new CollectionNotFoundException("Bytteliste ikke fundet")).getTradeCollectionId();

        int currentQty = tradeCollectionRepository.getCardQuantity(tradeColId, cardID);
        int newQty = currentQty - quantityToRemove;

        if (newQty <= 0) {
            tradeCollectionRepository.removeCardFromTradeCollection(tradeColId, cardID);
        } else {
            tradeCollectionRepository.setCardQuantity(tradeColId, cardID, newQty);
        }
    }


    public int getTotalTradelistQuantity(int userId, int cardId) {

        // Find brugerens bytteliste-ID
        int tradeColId = tradeCollectionRepository.findTradeCollectionByUserId(userId)
                .orElseThrow(() -> new CollectionNotFoundException("Kunne ikke finde byttelisten for bruger ID: " + userId))
                .getTradeCollectionId();

        return tradeCollectionRepository.getCardQuantity(tradeColId, cardId);
    }



    public void syncTradeCollectionWithPrivateCollection(int userID, int cardID) {
        // Hent ID'er for brugerens lister
        int tradeColId = tradeCollectionRepository.findTradeCollectionByUserId(userID)
                .orElseThrow(() -> new CollectionNotFoundException("Kunne ikke finde byttelisten for bruger ID: " + userID))
                .getTradeCollectionId();
        int collectionId = collectionRepository.findCollectionByUserId(userID)
                .orElseThrow(() -> new CollectionNotFoundException("Kunne ikke finde den private samling for bruger ID: " + userID))
                .getCollectionId();

        // Hent antallet på byttelisten og i den private samling
        int tradeQty = tradeCollectionRepository.getCardQuantity(tradeColId, cardID);
        int ownedQty = collectionRepository.getCardQuantity(collectionId, cardID);

        // Auto-Sync : Er der sat flere til bytte, end brugeren ejer?
        if (tradeQty > ownedQty) {
            if (ownedQty <= 0) {
                // Hvis brugeren slet ikke ejer kortet mere (f.eks. byttet væk), fjernes det helt
                tradeCollectionRepository.removeCardFromTradeCollection(tradeColId, cardID);
            } else {
                // Ellers sættes mængden på byttelisten ned til det faktiske antal, de ejer (f.eks. fra 4 til 1)
                tradeCollectionRepository.setCardQuantity(tradeColId, cardID, ownedQty);
            }
        }
    }



    public List<TradeCollectionItem> getFilteredTradeCollectionItems(int userId, String search, String rarity, String type) {

        TradeCollection collection = getTradeCollectionByUserID(userId).orElse(null);
        if (collection == null) {
            return List.of();
        }

        return collection.getTradeCollectionItems().stream()
                .filter(item -> search == null || search.isEmpty() ||
                        item.getCard().getCardName().toLowerCase().contains(search.toLowerCase()))
                .filter(item -> rarity == null || rarity.isEmpty() ||
                        item.getCard().getCardRarity().name().equalsIgnoreCase(rarity))
                .filter(item -> type == null || type.isEmpty() ||
                        item.getCard().getCardType().name().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }


}
