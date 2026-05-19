package feedback.deckforge.Service.Validation;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Enum.TradeStatus;
import feedback.deckforge.Model.Trade;
import feedback.deckforge.Service.CollectionService;
import feedback.deckforge.Service.RepoInterfaces.ITradeRepository;
import feedback.deckforge.Service.TradeCollectionService;
import feedback.deckforge.Service.TradeService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TradeValidation {

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

        // ==========================================
        // 1. Tjek INITIATOR's kort (Trækkes fra deres Private Collection)
        // ==========================================
        List<Card> offeredCards = trade.getOfferedCards();

        // Lav en liste med kun de unikke kort, så vi ikke tjekker det samme kort flere gange
        List<Card> distinctOffered = offeredCards.stream()
                .distinct()
                .collect(Collectors.toList());

        for (Card card : distinctOffered) {
            // Tæl hvor mange kopier af DITTE specifikke kort initiatoren prøver at bytte
            long neededAmount = offeredCards.stream()
                    .filter(c -> c.getCardID() == card.getCardID())
                    .count();

            // Slå op i databasen: Hvor mange har de, og hvor mange er reserveret?
            long totalOwned = collectionService.getTotalOwnedQuantity(trade.getInitiator().getUserID(), card.getCardID());
            long lockedAmount = getLockedQuantity(trade.getInitiator().getUserID(), card.getCardID());

            long available = totalOwned - lockedAmount;

            if (available < neededAmount) {
                result.addError("Du har ikke nok ledige kopier af kortet: " + card.getCardName() + " (inkl. reserverede i andre handler).");
                break; // Vi stopper loopet, da fejlen allerede er fundet
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

            long totalOwned = tradeCollectionService.getTotalTradelistQuantity(trade.getReceiver().getUserID(), card.getCardID());
            long lockedAmount = getLockedQuantity(trade.getReceiver().getUserID(), card.getCardID());

            long available = totalOwned - lockedAmount;

            if (available < neededAmount) {
                result.addError("Partneren har ikke længere nok ledige kopier af kortet: " + card.getCardName() + ".");
                break;
            }
        }

        return result;
    }

    public long getLockedQuantity(int userId, int cardId) {
        List<Trade> activeTrades = tradeRepository.findAllTradesByUserId(userId).stream()
                .filter(t -> t.getTradeStatus() == TradeStatus.ACCEPTED || t.getTradeStatus() == TradeStatus.WAITING_FOR_PARTNER)
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


