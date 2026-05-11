package feedback.deckforge.Service;

import feedback.deckforge.Exceptions.CollectionNotFoundException;
import feedback.deckforge.Model.Collection;
import feedback.deckforge.Service.RepoInterfaces.ICollectionRepository;
import feedback.deckforge.Service.Validation.CollectionValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CollectionService {

    private final ICollectionRepository collectionRepository;
    private final CollectionValidation collectionValidation;

    public CollectionService(ICollectionRepository collectionRepository, CollectionValidation collectionValidation) {
        this.collectionRepository = collectionRepository;
        this.collectionValidation = collectionValidation;
    }


    public Optional<Collection> findCollectionByUserId(int userID) {
        return collectionRepository.findCollectionByUserId(userID);
    }

    public ValidationResult addCardToCollection(int collectionID, int cardID, int quantity) {
        ValidationResult result = collectionValidation.validateAddCard(cardID, quantity);
        if (!result.hasErrors()) {
            collectionRepository.addCardToCollection(collectionID, cardID, quantity);
        }
        return result;
    }

    public void removeCardFromCollection(int collectionID, int cardID) {
        collectionRepository.removeCardFromCollection(collectionID, cardID);
    }

    //hvis man har 4 kopier, men fjerner en fra sin collection
    public void updateCardQuantity(int collectionID, int cardID, int newQuantity) {
        if (newQuantity <= 0) {
            collectionRepository.removeCardFromCollection(collectionID, cardID);
        } else {
            collectionRepository.updateCardQuantity(collectionID, cardID, newQuantity);
        }
    }

    public void addOne(int userID, int cardID) {
        int collectionId = collectionRepository.findCollectionByUserId(userID)
                .orElseThrow(() -> new CollectionNotFoundException("Kunne ikke finde samlingen for bruger ID: " + userID))
                .getCollectionId();
        int qty = collectionRepository.getCardQuantity(collectionId, cardID);

        if (qty > 0) {
            collectionRepository.updateCardQuantity(collectionId, cardID, qty + 1);
        } else {
            collectionRepository.addCardToCollection(collectionId, cardID, 1);
        }
    }

    public void removeOne(int userID, int cardID) {
        int collectionId = collectionRepository.findCollectionByUserId(userID)
                .orElseThrow(() -> new CollectionNotFoundException("Kunne ikke finde samlingen for bruger ID: " + userID))
                .getCollectionId();
        int qty = collectionRepository.getCardQuantity(collectionId, cardID);

        if (qty > 1) {
            collectionRepository.updateCardQuantity(collectionId, cardID, qty - 1);
        } else {
            collectionRepository.removeCardFromCollection(collectionId, cardID);
        }
    }
}
