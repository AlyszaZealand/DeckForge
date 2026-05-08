package feedback.deckforge.Service;

import feedback.deckforge.Model.Collection;
import feedback.deckforge.Model.TradeCollection;
import feedback.deckforge.Service.RepoInterfaces.ICollectionRepository;
import feedback.deckforge.Service.RepoInterfaces.ITradeCollectionRepository;
import feedback.deckforge.Service.Validation.TradeCollectionValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TradeCollectionService {

    private ITradeCollectionRepository tradeCollectionRepository;
    private final ICollectionRepository collectionRepository;
    private final TradeCollectionValidation tradeCollectionValidation;

    public TradeCollectionService(ITradeCollectionRepository tradeCollectionRepository, ICollectionRepository collectionRepository, TradeCollectionValidation tradeCollectionValidation) {
        this.tradeCollectionRepository = tradeCollectionRepository;
        this.collectionRepository = collectionRepository;
        this.tradeCollectionValidation = tradeCollectionValidation;
    }

    public Optional<TradeCollection> getTradeCollectionByUserID(int userID){
        return tradeCollectionRepository.findTradeCollectionByUserId(userID);
    }

    public ValidationResult addCardToTradeCollection(int userID, int tradeCollectionID, int cardID, int quantity) {
        Collection privateCol = collectionRepository.findCollectionByUserId(userID).orElse(null);

        ValidationResult result = tradeCollectionValidation.validateAddCardToTradeCollection(cardID,quantity, privateCol);

        if (!result.hasErrors()) {
            tradeCollectionRepository.addCardToTradeCollection(tradeCollectionID, cardID, quantity);
        }

        return result;
    }

    public void removeCardFromTradeCollection(int tradeCollectionID, int cardID){
        tradeCollectionRepository.removeCardFromTradeCollection(tradeCollectionID, cardID);
    }

    public void setCardQuantity(int tradeCollectionID, int cardID, int newQuantity){
        tradeCollectionRepository.setCardQuantity(tradeCollectionID, cardID, newQuantity);
    }


}
