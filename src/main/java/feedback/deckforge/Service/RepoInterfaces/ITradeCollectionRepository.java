package feedback.deckforge.Service.RepoInterfaces;

import feedback.deckforge.Model.TradeCollection;

import java.util.Optional;

public interface ITradeCollectionRepository {


    Optional<TradeCollection> findTradeCollectionByUserId(int userID);

    void addCardToTradeCollection(int tradeCollectionID, int cardID, int quantity);

    void removeCardFromTradeCollection(int tradeCollectionID, int cardID);

    void setCardQuantity(int tradeCollectionID, int cardID, int newQuantity);

    int getCardQuantity(int collectionID, int cardID);

    void initTradeCollection(int userID);

    void decreaseCardQuantity(int tradeCollectionID, int cardID);
}

