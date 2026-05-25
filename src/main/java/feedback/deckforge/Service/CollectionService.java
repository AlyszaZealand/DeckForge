package feedback.deckforge.Service;

import feedback.deckforge.Exceptions.CardNotOwnedException;
import feedback.deckforge.Exceptions.CollectionNotFoundException;
import feedback.deckforge.Model.Collection;
import feedback.deckforge.Model.CollectionItem;
import feedback.deckforge.Service.RepoInterfaces.ICollectionRepository;
import feedback.deckforge.Service.Validation.CollectionValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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


    public ValidationResult addCards(int userID, int cardID, int quantityToAdd) {
        // 1. Find collection ID ud fra User ID
        int collectionId = collectionRepository.findCollectionByUserId(userID)
                .orElseThrow(() -> new CollectionNotFoundException("Kunne ikke finde samlingen for bruger ID: " + userID))
                .getCollectionId();

        // 2. Kør validering (Har de plads? Findes kortet?)
        ValidationResult result = collectionValidation.validateAddCard(cardID, quantityToAdd);

        if (result.hasErrors()) {
            return result; // Afbryd hvis der er fejl
        }

        // 3. Tjek hvor mange de allerede har i forvejen
        int currentQty = collectionRepository.getCardQuantity(collectionId, cardID);

        // 4. Hvis de allerede har kortet, opdaterer vi antallet. Ellers tilføjer vi en ny række.
        if (currentQty > 0) {
            collectionRepository.updateCardQuantity(collectionId, cardID, currentQty + quantityToAdd);
        } else {
            collectionRepository.addCardToCollection(collectionId, cardID, quantityToAdd);
        }

        return result;
    }


    public void removeCards(int userID, int cardID, int quantityToRemove) {
        // 1. Find collection ID ud fra User ID
        int collectionId = collectionRepository.findCollectionByUserId(userID)
                .orElseThrow(() -> new CollectionNotFoundException("Kunne ikke finde samlingen for bruger ID: " + userID))
                .getCollectionId();

        // 2. Find ud af hvor mange kopier de har i forvejen
        int currentQty = collectionRepository.getCardQuantity(collectionId, cardID);

        if (currentQty <= 0) {
            throw new CardNotOwnedException("Du kan ikke fjerne et kort, du ikke har i din samling.");
        }

        // 3. Udregn det nye antal
        int newQty = currentQty - quantityToRemove;

        // 4. Hvis det nye antal er 0 eller derunder, sletter vi kortet helt fra samlingen
        if (newQty <= 0) {
            collectionRepository.removeCardFromCollection(collectionId, cardID);
        } else {
            // Ellers sænker vi bare antallet
            collectionRepository.updateCardQuantity(collectionId, cardID, newQty);
        }
    }



    public List<CollectionItem> getFilteredCollectionItems(
            int userId, String search, String rarity, String type) {

        Collection collection = collectionRepository.findCollectionByUserId(userId).orElse(null);
        if (collection == null) {
            return List.of();
        }

        return collection.getCollectionItems().stream()
                .filter(item -> search == null || search.isEmpty() ||
                        item.getCard().getCardName().toLowerCase().contains(search.toLowerCase()))
                .filter(item -> rarity == null || rarity.isEmpty() ||
                        item.getCard().getCardRarity().name().equalsIgnoreCase(rarity))
                .filter(item -> type == null || type.isEmpty() ||
                        item.getCard().getCardType().name().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }
}
