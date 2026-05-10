package feedback.deckforge.Service.Validation;

import feedback.deckforge.Model.Trade;
import org.springframework.stereotype.Component;

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

        return result;
    }
}
