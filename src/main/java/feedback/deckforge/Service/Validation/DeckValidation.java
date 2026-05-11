package feedback.deckforge.Service.Validation;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.DeckItem;
import feedback.deckforge.Model.Format;
import feedback.deckforge.Model.Deck;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Deck;
import feedback.deckforge.Model.DeckItem;
import feedback.deckforge.Model.Enum.CardType;
import feedback.deckforge.Model.Format;
import org.springframework.stereotype.Component;

@Component
public class DeckValidation {

    public ValidationResult validateAddCard(Deck currentDeck, Card newCard, int quantityToAdd) {
        ValidationResult result = new ValidationResult();
        Format format = currentDeck.getFormat();

        if (format == null) {
            result.addError("Decket mangler et format.");
            return result;
        }

        // 1. Tjek: Er kortets rarity tilladt i dette format?
        String allowed = format.getAllowedRarities();
        if (allowed != null && !allowed.equalsIgnoreCase("ALL")) {
            if (!allowed.contains(newCard.getCardRarity().name())) {
                result.addError("Kortet '" + newCard.getCardName() + "' har en rarity, der ikke er tilladt i " + format.getFormatName() + ".");
                return result;
            }
        }


        if (format.isRequiresCommander() && currentDeck.getCommander() != null) {
            String commanderColors = currentDeck.getCommander().getColorIdentity();
            String cardColors = newCard.getColorIdentity();

            // Hvis kortet IKKE er farveløst ("C") og det faktisk har en farve registreret
            if (cardColors != null && !cardColors.trim().isEmpty() && !cardColors.equalsIgnoreCase("C")) {

                // Hvis Commanderen er farveløs, må der slet ikke tilføjes farvede kort eller farvede lande
                if (commanderColors == null || commanderColors.trim().isEmpty() || commanderColors.equalsIgnoreCase("C")) {
                    result.addError("Din Commander er farveløs. Du kan ikke tilføje kort, der bruger farvet mana.");
                } else {
                    // Tjek om ALLE kortets farver findes i Commanderens farver
                    for (char color : cardColors.toCharArray()) {
                        if (commanderColors.indexOf(color) == -1) {
                            result.addError("Sikkerhedsfejl: Kortet '" + newCard.getCardName() + "' (" + cardColors + ") bryder reglen om Color Identity. Det matcher ikke din Commander (" + commanderColors + ").");
                            break;
                        }
                    }
                }
            }
        }


        int totalDeckQty = 0;
        int currentCardQty = 0;

        for (DeckItem item : currentDeck.getDeckItems()) {
            totalDeckQty += item.getQuantity();
            if (item.getCard().getCardId() == newCard.getCardId()) {
                currentCardQty = item.getQuantity();
            }
        }


        boolean isLand = newCard.getCardType() == CardType.LAND;


        if (!isLand && (currentCardQty + quantityToAdd > format.getMaxCopiesOfCard())) {
            result.addError("Regelbrud: Du må maksimalt have " + format.getMaxCopiesOfCard() + " kopier af '" + newCard.getCardName() + "' (Lande er undtaget).");
        }

        if (totalDeckQty + quantityToAdd > format.getMaxDeckSize()) {
            result.addError("Decket er fyldt. Maksimum for dette format er " + format.getMaxDeckSize() + " kort.");
        }

        return result;
    }
}
