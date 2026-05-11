package feedback.deckforge.Service;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Enum.TradeStatus;
import feedback.deckforge.Model.Trade;
import feedback.deckforge.Service.RepoInterfaces.ITradeRepository;
import feedback.deckforge.Service.Validation.TradeValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TradeService {

    private final ITradeRepository tradeRepository;
    private final CollectionService collectionService;
    private final TradeCollectionService tradeCollectionService;
    private final TradeValidation tradeValidation;

    public TradeService(ITradeRepository tradeRepository,
                        CollectionService collectionService,
                        TradeCollectionService tradeCollectionService,
                        TradeValidation tradeValidation) {
        this.tradeRepository = tradeRepository;
        this.collectionService = collectionService;
        this.tradeCollectionService = tradeCollectionService;
        this.tradeValidation = tradeValidation;
    }

    // ==========================================
    // 1. OPRET FORSLAG (Med TradeValidation)
    // ==========================================
    public ValidationResult proposeTrade(Trade trade) {
        // Her kalder vi din nye valideringsklasse!
        ValidationResult result = tradeValidation.validateProposal(trade);

        if (!result.hasErrors()) {
            trade.setTradeStatus(TradeStatus.PENDING);
            tradeRepository.saveTrade(trade);
        }
        return result;
    }

    // ==========================================
    // 2. ACCEPTER ELLER AFVIS
    // ==========================================
    public void respondToTrade(int tradeID, boolean isAccepted) {
        Trade trade = tradeRepository.findTradeById(tradeID).orElseThrow();

        // Validering: Man kan kun svare på et bytte, der er PENDING
        if (trade.getTradeStatus() != TradeStatus.PENDING) {
            return; // Eller smid en fejl
        }

        if (isAccepted) {
            trade.setTradeStatus(TradeStatus.ACCEPTED);

            // RESERVATION: Flyt logikken til de respektive services
            for (Card card : trade.getOfferedCards()) {
                tradeCollectionService.removeOne(trade.getInitiator().getUserID(), card.getCardID());
            }
            for (Card card : trade.getRequestedCards()) {
                tradeCollectionService.removeOne(trade.getReceiver().getUserID(), card.getCardID());
            }
        } else {
            trade.setTradeStatus(TradeStatus.DECLINED);
            trade.setCompletedDate(LocalDateTime.now());
        }
        tradeRepository.updateTrade(trade);
    }

    // ==========================================
    // 3. ANNULLER BYTTE
    // ==========================================
    public void cancelTrade(int tradeID) {
        Trade trade = tradeRepository.findTradeById(tradeID).orElseThrow();

        // Validering: Man kan ikke annullere et bytte, der er afvist eller allerede færdigt
        if (trade.getTradeStatus() == TradeStatus.DECLINED || trade.getTradeStatus() == TradeStatus.COMPLETED) {
            return;
        }

        // Hvis handlen var Accepteret, skal kortene lægges tilbage (reservation ophæves)
        if (trade.getTradeStatus() == TradeStatus.ACCEPTED || trade.getTradeStatus() == TradeStatus.WAITING_FOR_PARTNER) {
            for (Card card : trade.getOfferedCards()) {
                tradeCollectionService.addOne(trade.getInitiator().getUserID(), card.getCardID());
            }
            for (Card card : trade.getRequestedCards()) {
                tradeCollectionService.addOne(trade.getReceiver().getUserID(), card.getCardID());
            }
        }

        trade.setTradeStatus(TradeStatus.CANCELLED);
        trade.setCompletedDate(LocalDateTime.now());
        tradeRepository.updateTrade(trade);
    }

    // ==========================================
    // 4. FÆRDIGGØR IRL BYTTE
    // ==========================================
    public void finalizeTrade(int tradeID, int userID) {
        Trade trade = tradeRepository.findTradeById(tradeID).orElseThrow();

        // Validering: Kun muligt hvis handlen er ACCEPTED eller WAITING_FOR_PARTNER
        if (trade.getTradeStatus() != TradeStatus.ACCEPTED && trade.getTradeStatus() != TradeStatus.WAITING_FOR_PARTNER) {
            return;
        }

        if (trade.getInitiator().getUserID() == userID) {
            trade.setInitiatorConfirmed(true);
        } else if (trade.getReceiver().getUserID() == userID) {
            trade.setReceiverConfirmed(true);
        }

        if (trade.isInitiatorConfirmed() && trade.isReceiverConfirmed()) {
            trade.setTradeStatus(TradeStatus.COMPLETED);
            trade.setCompletedDate(LocalDateTime.now());

            // DEN STORE UDVEKSLING mellem personlige samlinger
            for (Card card : trade.getOfferedCards()) {
                collectionService.removeOne(trade.getInitiator().getUserID(), card.getCardID());
                collectionService.addOne(trade.getReceiver().getUserID(), card.getCardID());
            }
            for (Card card : trade.getRequestedCards()) {
                collectionService.removeOne(trade.getReceiver().getUserID(), card.getCardID());
                collectionService.addOne(trade.getInitiator().getUserID(), card.getCardID());
            }
        } else {
            trade.setTradeStatus(TradeStatus.WAITING_FOR_PARTNER);
        }
        tradeRepository.updateTrade(trade);
    }

    // ==========================================
    // 5. STANDARD HENTE-METODER
    // ==========================================
    public void deleteTrade(int tradeID){ tradeRepository.deleteTrade(tradeID); }
    public Optional<Trade> findTradeById(int tradeID){ return tradeRepository.findTradeById(tradeID); }
    public List<Trade> findTradesByInitiatorId(int initiatorId){ return tradeRepository.findTradesByInitiatorId(initiatorId); }
    public List<Trade> findTradesByReceiverId(int receiverId){ return tradeRepository.findTradesByReceiverId(receiverId); }
    public List<Trade> findAllTradesByUserId(int userID){ return tradeRepository.findAllTradesByUserId(userID); }
}
