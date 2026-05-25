package feedback.deckforge.Service.RepoInterfaces;

import feedback.deckforge.Model.Collection;

import java.util.Optional;

public interface ICollectionRepository {

    Optional<Collection> findCollectionByUserId(int userID);

    void addCardToCollection(int collectionID, int cardID, int quantity);

    void removeCardFromCollection(int collectionID, int cardID);

    void updateCardQuantity(int collectionID, int cardID, int newQuantity);

    int getCardQuantity(int collectionID, int cardID);

    void initCollection(int userID);
}

