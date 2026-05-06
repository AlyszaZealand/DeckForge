package feedback.deckforge.Service.RepoInterfaces;

import feedback.deckforge.Model.Collection;

import java.util.Optional;

public interface ICollectionRepository {

    Optional<Collection> findCollectionByUserId(int userId);

    void addCardToCollection(int collectionId, int cardId, int quantity);

    void removeCardFromCollection(int collectionId, int cardId);

    //hvis man har 4 kopier, men fjerner en fra sin collection
    void updateCardQuantity(int collectionId, int cardId, int newQuantity);
}

