package feedback.deckforge.Service.RepoInterfaces;

import feedback.deckforge.Model.Trade;

import java.util.List;
import java.util.Optional;

public interface ITradeRepository {
    void saveTrade(Trade trade);
    void deleteTrade(int tradeID);
    Optional<Trade> findTradeById(int tradeID);
    List<Trade> findTradesByInitiatorId(int initiatorId);
    List<Trade> findTradesByReceiverId(int receiverId);
    List<Trade> findAllTradesByUserId(int userID);
}
