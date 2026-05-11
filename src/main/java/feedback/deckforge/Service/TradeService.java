package feedback.deckforge.Service;

import feedback.deckforge.Exceptions.TradeNotFoundException;
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
    public void respondToTrade(int tradeId, boolean isAccepted) {
        Trade trade = tradeRepository.findTradeById(tradeId).orElseThrow(() -> new TradeNotFoundException("Byttehandlen kunne ikke findes"));

        if (isAccepted) {
            trade.setTradeStatus(TradeStatus.ACCEPTED);

            // NYT: Reserver kortene fra begge parter (Mængden er altid 1 pr. kort i listen)
            for (Card card : trade.getOfferedCards()) {
                tradeCollectionService.reserveCardsFromTradeCollection(trade.getInitiator().getUserID(), card.getCardID(), 1);
            }
            for (Card card : trade.getRequestedCards()) {
                tradeCollectionService.reserveCardsFromTradeCollection(trade.getReceiver().getUserID(), card.getCardID(), 1);
            }

        } else {
            // Hvis handlen var ACCEPTED før, og nu annulleres, lægges kortene tilbage (Return)
            if (trade.getTradeStatus() == TradeStatus.ACCEPTED) {
                for (Card card : trade.getOfferedCards()) {
                    tradeCollectionService.returnCardsToTradeCollection(trade.getInitiator().getUserID(), card.getCardID(), 1);
                }
                for (Card card : trade.getRequestedCards()) {
                    tradeCollectionService.returnCardsToTradeCollection(trade.getReceiver().getUserID(), card.getCardID(), 1);
                }
            }
            trade.setTradeStatus(TradeStatus.CANCELLED);
        }

        tradeRepository.updateTradeStatus(tradeId, trade.getTradeStatus());
    }

    // ==========================================
    // 3. ANNULLER BYTTE
    // ==========================================
    public void cancelTrade(int tradeID) {
        Trade trade = tradeRepository.findTradeById(tradeID).orElseThrow(() -> new TradeNotFoundException("Byttehandlen kunne ikke findes"));

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
        tradeRepository.updateTradeStatus(tradeID, trade.getTradeStatus());
    }

    // ==========================================
    // 4. FÆRDIGGØR IRL BYTTE
    // ==========================================
    public void finalizeTrade(int tradeID, int userID) {
        Trade trade = tradeRepository.findTradeById(tradeID).orElseThrow(() -> new TradeNotFoundException("Byttehandlen kunne ikke findes"));

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
                tradeCollectionService.syncTradeCollectionWithPrivateCollection(trade.getInitiator().getUserID(), card.getCardID());
            }
            for (Card card : trade.getRequestedCards()) {
                collectionService.removeOne(trade.getReceiver().getUserID(), card.getCardID());
                collectionService.addOne(trade.getInitiator().getUserID(), card.getCardID());
                tradeCollectionService.syncTradeCollectionWithPrivateCollection(trade.getInitiator().getUserID(), card.getCardID());
            }
        } else {
            trade.setTradeStatus(TradeStatus.WAITING_FOR_PARTNER);
        }
        tradeRepository.updateTradeStatus(tradeID, trade.getTradeStatus());
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
