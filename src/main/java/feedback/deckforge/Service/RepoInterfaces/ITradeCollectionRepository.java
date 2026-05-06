package feedback.deckforge.Service.RepoInterfaces;

import feedback.deckforge.Model.TradeCollection;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

public interface ITradeCollectionRepository {


    Optional<TradeCollection> findTradeCollectionByUserId(int userID);

    void addCardToTradeCollection(int tradeCollectionID, int cardID, int quantity);

    void removeCardFromTradeCollection(int tradeCollectionID, int cardID);

    void setCardQuantity(int tradeCollectionID, int cardID, int newQuantity);
}

