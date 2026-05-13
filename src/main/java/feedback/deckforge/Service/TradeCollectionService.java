package feedback.deckforge.Service;

import feedback.deckforge.Exceptions.CollectionNotFoundException;
import feedback.deckforge.Exceptions.InsufficientCardsException;
import feedback.deckforge.Model.Collection;
import feedback.deckforge.Model.TradeCollection;
import feedback.deckforge.Service.RepoInterfaces.ICollectionRepository;
import feedback.deckforge.Service.RepoInterfaces.ITradeCollectionRepository;
import feedback.deckforge.Service.Validation.TradeCollectionValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public void addCardToTradeCollection(int userID, int tradeCollectionID, int cardID, int quantityToAdd) {
        Collection privateCol = collectionRepository.findCollectionByUserId(userID).orElse(null);

        // 1. Find ud af, hvor mange der allerede ligger på byttelisten
        int currentTradeQty = tradeCollectionRepository.getCardQuantity(tradeCollectionID, cardID);

        // 2. Regn den nye total ud
        int newTotalQuantity = currentTradeQty + quantityToAdd;

        // 3. Valider med den NYE total for at sikre, at brugeren faktisk har nok
        ValidationResult result = tradeCollectionValidation.validateAddCardToTradeCollection(cardID, newTotalQuantity, privateCol);

        // 4. Hvis der er fejl, KAST exception, så din GlobalExceptionHandler griber den!
        if (result.hasErrors()) {
            throw new InsufficientCardsException(result.getErrors().get(0));
        }

        // 5. Hvis ingen fejl, opdater databasen
        tradeCollectionRepository.addCardToTradeCollection(tradeCollectionID, cardID, quantityToAdd);
    }

    public void removeCardFromTradeCollection(int tradeCollectionID, int cardID){
        tradeCollectionRepository.removeCardFromTradeCollection(tradeCollectionID, cardID);
    }

    public void setCardQuantity(int tradeCollectionID, int cardID, int newQuantity){
        tradeCollectionRepository.setCardQuantity(tradeCollectionID, cardID, newQuantity);
    }

    public void addOne(int userID, int cardID) {
        int tradeColId = tradeCollectionRepository.findTradeCollectionByUserId(userID).orElseThrow().getTradeCollectionId();
        int qty = tradeCollectionRepository.getCardQuantity(tradeColId, cardID);

        if (qty > 0) {
            tradeCollectionRepository.setCardQuantity(tradeColId, cardID, qty + 1);
        } else {
            tradeCollectionRepository.addCardToTradeCollection(tradeColId, cardID, 1);
        }
    }

    public void removeOne(int userID, int cardID) {
        int tradeColId = tradeCollectionRepository.findTradeCollectionByUserId(userID).orElseThrow().getTradeCollectionId();
        int qty = tradeCollectionRepository.getCardQuantity(tradeColId, cardID);

        if (qty > 1) {
            tradeCollectionRepository.setCardQuantity(tradeColId, cardID, qty - 1);
        } else {
            tradeCollectionRepository.removeCardFromTradeCollection(tradeColId, cardID);
        }
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

   //reservation
    public void reserveCardsFromTradeCollection(int userID, int cardID, int quantityToReserve) {
        int tradeColId = tradeCollectionRepository.findTradeCollectionByUserId(userID)
                .orElseThrow(() -> new CollectionNotFoundException("Kunne ikke reservere kort: Bytteliste ikke fundet for bruger ID: " + userID))
                .getTradeCollectionId();
        int currentTradeQty = tradeCollectionRepository.getCardQuantity(tradeColId, cardID);

        // Regner ud hvor mange der skal være tilbage (Sikrer at vi ikke går i minus)
        int newTradeQty = Math.max(0, currentTradeQty - quantityToReserve);

        if (newTradeQty == 0) {
            tradeCollectionRepository.removeCardFromTradeCollection(tradeColId, cardID);
        } else {
            tradeCollectionRepository.setCardQuantity(tradeColId, cardID, newTradeQty);
        }
    }

    // Annullering
    public void returnCardsToTradeCollection(int userID, int cardID, int quantityToReturn) {
        int tradeColId = tradeCollectionRepository.findTradeCollectionByUserId(userID)
                .orElseThrow(() -> new CollectionNotFoundException("Kunne ikke returnere kort: Bytteliste ikke fundet for bruger ID: " + userID))
                .getTradeCollectionId();
        int currentTradeQty = tradeCollectionRepository.getCardQuantity(tradeColId, cardID);

        if (currentTradeQty > 0) {
            tradeCollectionRepository.setCardQuantity(tradeColId, cardID, currentTradeQty + quantityToReturn);
        } else {
            tradeCollectionRepository.addCardToTradeCollection(tradeColId, cardID, quantityToReturn);
        }

        // Vi kører en auto-sync til sidst for at sikre, at vi ikke har lagt flere tilbage, end de ejer!
        syncTradeCollectionWithPrivateCollection(userID, cardID);
    }


}
