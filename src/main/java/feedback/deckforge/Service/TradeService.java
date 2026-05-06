package feedback.deckforge.Service;

import feedback.deckforge.Model.Trade;
import feedback.deckforge.Service.RepoInterfaces.ITradeRepository;
import feedback.deckforge.Service.Validation.TradeValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TradeService {

    private final ITradeRepository tradeRepository;
    private final TradeValidation tradeValidation;

    public TradeService(ITradeRepository tradeRepository, TradeValidation tradeValidation) {
        this.tradeRepository = tradeRepository;
        this.tradeValidation = tradeValidation;
    }

    public void saveTrade(Trade trade) {
        tradeRepository.saveTrade(trade);
    }

    public void deleteTrade(int tradeID){
        tradeRepository.deleteTrade(tradeID);
    }

    public Optional<Trade> findTradeById(int tradeID){
        return tradeRepository.findTradeById(tradeID);
    }

    public List<Trade> findTradesByInitiatorId(int initiatorId){
        return tradeRepository.findTradesByInitiatorId(initiatorId);
    }

    public List<Trade> findTradesByReceiverId(int receiverId){
        return tradeRepository.findTradesByReceiverId(receiverId);
    }

    public List<Trade> findAllTradesByUserId(int userID){
        return tradeRepository.findAllTradesByUserId(userID);
    }

    public ValidationResult validateTrade(Trade trade){
        ValidationResult result = tradeValidation.validateTrade(trade);

        result.hasErrors();
        return result;
    }

}
