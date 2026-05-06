package feedback.deckforge.Service.Validation;

import feedback.deckforge.Model.Trade;
import org.springframework.stereotype.Component;

@Component
public class TradeValidation {

    public ValidationResult validateTrade(Trade trade){
        ValidationResult result = new ValidationResult();

        if(trade.getInitiator() == trade.getReceiver()){
            result.addError("Du kan ikke bytte med dig selv.");
        }

        if(trade.getOfferedCards() == null || trade.getOfferedCards().isEmpty()){
            result.addError("Byttehandlen skal indeholde mindst ét kort.");
        }

        if(trade.getRequestedCards() == null || trade.getRequestedCards().isEmpty()){
            result.addError("Byttehandlen skal indeholde mindst ét kort.");
        }

        return result;
    }


    //Gemini's forsalg til trade validation.
    //Den vill gerne have en tradeItem som model klasse.
   /* public ValidationResult validateNewTrade(Trade trade, List<TradeItem> tradeItems) {
        ValidationResult result = new ValidationResult();

        // 1. You cannot trade with yourself
        if (trade.getInitiatorID() == trade.getReceiverID()) {
            result.addError("Du kan ikke bytte med dig selv."); // You cannot trade with yourself
        }

        // 2. The trade cannot be completely empty
        if (tradeItems == null || tradeItems.isEmpty()) {
            result.addError("Byttehandlen skal indeholde mindst ét kort.");
            return result; // Stop here if empty to prevent crashes below
        }

        // 3. Your rule: Must offer at least one card AND request at least one card
        boolean hasOfferedCards = false;
        boolean hasRequestedCards = false;

        for (TradeItem item : tradeItems) {
            if (item.getQuantity() <= 0) {
                result.addError("Antallet af kort skal være mindst 1.");
            }

            if (item.isOfferedByInitiator()) {
                hasOfferedCards = true;
            } else {
                hasRequestedCards = true;
            }
        }

        if (!hasOfferedCards) {
            result.addError("Du skal tilbyde mindst ét kort for at lave en byttehandel.");
        }
        if (!hasRequestedCards) {
            result.addError("Du skal anmode om mindst ét kort fra den anden spiller.");
        }

        // 4. Advanced: Inventory Checks (Pseudo-code)
        // You should eventually add a check to your database here to ensure:
        // - Does the initiator actually own the cards they are offering?
        // - Does the receiver actually own the cards being requested?
        // - Do they have enough quantity of that card?

        return result;
    } */

}
