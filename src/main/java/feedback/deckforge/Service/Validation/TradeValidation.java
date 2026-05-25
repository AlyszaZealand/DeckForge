package feedback.deckforge.Service.Validation;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Collection;
import feedback.deckforge.Model.Enum.TradeStatus;
import feedback.deckforge.Model.Trade;
import feedback.deckforge.Model.TradeCollection;
import feedback.deckforge.Service.CollectionService;
import feedback.deckforge.Service.RepoInterfaces.ICollectionRepository;
import feedback.deckforge.Service.RepoInterfaces.ITradeCollectionRepository;
import feedback.deckforge.Service.RepoInterfaces.ITradeRepository;
import feedback.deckforge.Service.TradeCollectionService;
import feedback.deckforge.Service.TradeService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TradeValidation {

    private final ITradeCollectionRepository tradeCollectionRepository;
    private final ICollectionRepository collectionRepository;
    private final ITradeRepository tradeRepository;


    public TradeValidation(ITradeCollectionRepository tradeCollectionRepository, ICollectionRepository collectionRepository, ITradeRepository tradeRepository) {
        this.tradeCollectionRepository = tradeCollectionRepository;
        this.collectionRepository = collectionRepository;
        this.tradeRepository = tradeRepository;
    }

    public ValidationResult validateProposal(Trade trade) {
        ValidationResult result = new ValidationResult();

        // ==========================================
        // 1. Tjek at begge parter eksisterer
        // ==========================================
        if (trade.getInitiator() == null || trade.getInitiator().getUserID() <= 0) {
            result.addError("Afsender af bytteforslaget mangler.");
        }
        if (trade.getReceiver() == null || trade.getReceiver().getUserID() <= 0) {
            result.addError("Modtager af bytteforslaget mangler.");
        }

        // ==========================================
        // 2. Tjek at man ikke bytter med sig selv
        // ==========================================
        if (trade.getInitiator() != null && trade.getReceiver() != null) {
            if (trade.getInitiator().getUserID() == trade.getReceiver().getUserID()) {
                result.addError("Du kan ikke bytte kort med dig selv.");
            }
        }

        // ==========================================
        // 3. Tjek at listerne med kort ikke er tomme
        // ==========================================
        if (trade.getOfferedCards() == null || trade.getOfferedCards().isEmpty()) {
            result.addError("Du skal tilbyde mindst ét kort fra din egen bytteliste.");
        }
        if (trade.getRequestedCards() == null || trade.getRequestedCards().isEmpty()) {
            result.addError("Du skal anmode om mindst ét kort fra modtagerens bytteliste.");
        }

        ValidationResult inventoryResult = validateInventory(trade);
        if (inventoryResult.hasErrors()) {
            result.getErrors().addAll(inventoryResult.getErrors());
        }

        return result;
    }

    public ValidationResult validateInventory(Trade trade) {
        ValidationResult result = new ValidationResult();

        // Sikkerhed hvis tradeId ikke findes endnu (f.eks. ved nyt propose)
        int currentTradeId = trade.getTradeId() > 0 ? trade.getTradeId() : -1;

        // ==========================================
        // 1. Tjek INITIATOR's kort (Trækkes fra deres Private Collection)
        // ==========================================
        List<Card> offeredCards = trade.getOfferedCards();
        List<Card> distinctOffered = offeredCards.stream()
                .distinct()
                .collect(Collectors.toList());

        for (Card card : distinctOffered) {
            long neededAmount = offeredCards.stream()
                    .filter(c -> c.getCardID() == card.getCardID())
                    .count();

            long totalOwned = 0;
            Optional<Collection> cOpt = collectionRepository.findCollectionByUserId(trade.getInitiator().getUserID());

            // 2. Hvis modtageren HAR en tradelist, slår vi mængden af kortet op
            if (cOpt.isPresent()) {
                totalOwned = tradeCollectionRepository.getCardQuantity(cOpt.get().getCollectionId(), card.getCardID());
            }


            // FIX: Vi beder metoden om at ignorere den handel vi p.t. validerer!
            long lockedAmount = getLockedQuantity(trade.getInitiator().getUserID(), card.getCardID(), currentTradeId);

            long available = totalOwned - lockedAmount;

            if (available < neededAmount) {
                result.addError("Du har ikke nok ledige kopier af kortet: " + card.getCardName() + " (inkl. reserverede i andre handler).");
                break;
            }
        }

        // ==========================================
        // 2. Tjek RECEIVER's kort (Trækkes fra deres Tradelist)
        // ==========================================
        List<Card> requestedCards = trade.getRequestedCards();
        List<Card> distinctRequested = requestedCards.stream()
                .distinct()
                .collect(Collectors.toList());

        for (Card card : distinctRequested) {
            long neededAmount = requestedCards.stream()
                    .filter(c -> c.getCardID() == card.getCardID())
                    .count();

            long totalOwned = 0;
            Optional<TradeCollection> tcOpt = tradeCollectionRepository.findTradeCollectionByUserId(trade.getReceiver().getUserID());

            // 2. Hvis modtageren HAR en tradelist, slår vi mængden af kortet op
            if (tcOpt.isPresent()) {
                totalOwned = tradeCollectionRepository.getCardQuantity(tcOpt.get().getTradeCollectionId(), card.getCardID());
            }


            // FIX: Vi beder metoden om at ignorere den handel vi p.t. validerer!
            long lockedAmount = getLockedQuantity(trade.getReceiver().getUserID(), card.getCardID(), currentTradeId);

            long available = totalOwned - lockedAmount;

            if (available < neededAmount) {
                result.addError("Partneren har ikke længere nok ledige kopier af kortet: " + card.getCardName() + ".");
                break;
            }
        }

        return result;
    }


    // NY METODE der tillader os at ekskludere et TradeID
    public long getLockedQuantity(int userId, int cardId, int excludeTradeId) {
        List<Trade> activeTrades = tradeRepository.findAllTradesByUserId(userId).stream()
                .filter(t -> t.getTradeStatus() == TradeStatus.ACCEPTED || t.getTradeStatus() == TradeStatus.WAITING_FOR_PARTNER)
                // FIX: Vi filtrerer den specifikke handel ud, så den ikke låser sine egne kort!
                .filter(t -> t.getTradeId() != excludeTradeId)
                .collect(Collectors.toList());

        long locked = 0;
        for (Trade t : activeTrades) {
            if (t.getInitiator().getUserID() == userId) {
                locked += t.getOfferedCards().stream().filter(c -> c.getCardID() == cardId).count();
            }
            if (t.getReceiver().getUserID() == userId) {
                locked += t.getRequestedCards().stream().filter(c -> c.getCardID() == cardId).count();
            }
        }
        return locked;
    }

}


