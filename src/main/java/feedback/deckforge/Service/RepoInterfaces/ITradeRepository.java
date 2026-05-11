package feedback.deckforge.Service.RepoInterfaces;

import feedback.deckforge.Model.Enum.TradeStatus;
import feedback.deckforge.Model.Trade;
import java.util.List;
import java.util.Optional;

public interface ITradeRepository {
    void saveTrade(Trade trade);
    void deleteTrade(int tradeID);
    void updateTradeStatus(int tradeId, TradeStatus status); // NY!
    Optional<Trade> findTradeById(int tradeID);
    List<Trade> findTradesByInitiatorId(int initiatorID);
    List<Trade> findTradesByReceiverId(int receiverID);
    List<Trade> findAllTradesByUserId(int userID);
}
