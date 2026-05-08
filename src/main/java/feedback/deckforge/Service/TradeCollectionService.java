package feedback.deckforge.Service;

import feedback.deckforge.Model.Collection;
import feedback.deckforge.Model.TradeCollection;
import feedback.deckforge.Service.RepoInterfaces.ICollectionRepository;
import feedback.deckforge.Service.RepoInterfaces.ITradeCollectionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TradeCollectionService {

    private ITradeCollectionRepository tradeCollectionRepository;
    private final ICollectionRepository collectionRepository;

    public TradeCollectionService(ITradeCollectionRepository tradeCollectionRepository, ICollectionRepository collectionRepository) {
        this.tradeCollectionRepository = tradeCollectionRepository;
        this.collectionRepository = collectionRepository;
    }

    public Optional<TradeCollection> getTradeCollectionByUserID(int userID){
        return tradeCollectionRepository.findTradeCollectionByUserId(userID);
    }

    public boolean addCardToTradeCollection(int userID, int tradeCollectionID, int cardID, int quantity) {
        Optional<Collection> userCol = collectionRepository.findCollectionByUserId(userID);

        if (userCol.isPresent()) {
            boolean ownsEnough = userCol.get().getCollectionItems().stream()
                    .anyMatch(item -> item.getCard().getCardId() == cardID && item.getQuantity() >= quantity);

            if (ownsEnough) {
                tradeCollectionRepository.addCardToTradeCollection(tradeCollectionID, cardID, quantity);
                return true;
            }
        }

        return false;
    }

    public void removeCardFromTradeCollection(int tradeCollectionID, int cardID){
        tradeCollectionRepository.removeCardFromTradeCollection(tradeCollectionID, cardID);
    }


}
