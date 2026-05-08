package feedback.deckforge.Service;

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
}
