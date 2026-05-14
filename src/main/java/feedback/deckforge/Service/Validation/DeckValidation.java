package feedback.deckforge.Service.Validation;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.DeckItem;
import feedback.deckforge.Model.Format;
import feedback.deckforge.Model.Deck;
import org.springframework.stereotype.Component;

import feedback.deckforge.Model.Enum.CardType;

@Component
public class DeckValidation {

    // --- NY METODE: Validerer om kortet må være en Commander ---
    public ValidationResult validateCommander(Card commanderCard) {
        ValidationResult result = new ValidationResult();

        if (commanderCard == null) {
            result.addError("Du har ikke valgt noget kort.");
            return result;
        }

        // Tjekker om kortet er lovligt som Commander (Creature eller Planeswalker)
        CardType type = commanderCard.getCardType();
        if (type != CardType.CREATURE && type != CardType.PLANESWALKER) {
            result.addError("Regelbrud: En Commander skal være en Creature eller Planeswalker. Du valgte en " +
                    (type != null ? type.name() : "ukendt type") + ".");
        }

        return result;
    }

    // --- DIN OPRINDELIGE METODE MED TILFØJELSER ---
    public ValidationResult validateAddCard(Deck currentDeck, Card newCard, int quantityToAdd) {
        ValidationResult result = new ValidationResult();
        Format format = currentDeck.getFormat();

        if (format == null) {
            result.addError("Decket mangler et format.");
            return result;
        }

        // --- 1. TJEK: RARITY ---
        String allowed = format.getAllowedRarities();
        if (allowed != null && !allowed.equalsIgnoreCase("ALL")) {
            if (!allowed.toUpperCase().contains(newCard.getCardRarity().name().toUpperCase())) {
                result.addError("Kortet '" + newCard.getCardName() + "' har en rarity (" + newCard.getCardRarity() + "), der ikke er tilladt i " + format.getFormatName() + ".");
                return result;
            }
        }

        // --- 2. TJEK: COMMANDER REGLER (COLOR IDENTITY) ---
        if (format.isRequiresCommander()) {
            // Sikkerhed: Man SKAL have en commander først
            if (currentDeck.getCommander() == null) {
                result.addError("Du skal vælge en Commander, før du kan tilføje andre kort til dit deck.");
                return result;
            }

            // Color Identity validering
            String commanderColors = currentDeck.getCommander().getColorIdentity();
            String cardColors = newCard.getColorIdentity();

            // Vi tjekker kun farver, hvis kortet ikke er "Colorless" (C)
            if (cardColors != null && !cardColors.equalsIgnoreCase("C") && !cardColors.isBlank()) {

                // Rens strengene for symboler, kommaer og mellemrum, og gør dem til store bogstaver
                String cmdClean = (commanderColors != null) ? commanderColors.toUpperCase().replaceAll("[\\s,]", "") : "";
                String cardClean = cardColors.toUpperCase().replaceAll("[\\s,]", "");

                for (char color : cardClean.toCharArray()) {
                    if (cmdClean.indexOf(color) == -1) {
                        result.addError("Kortet '" + newCard.getCardName() + "' (" + color + ") bryder din Commanders Color Identity (" + cmdClean + ").");
                        return result; // Stop her hvis farven er ulovlig
                    }
                }
            }
        }

        // --- 3. TJEK: MÆNGDER (MAX COPIES & DECK SIZE) ---
        // Denne del skal ligge UDEN FOR Commander-blokken, så den gælder alle decks!
        int totalDeckQty = 0;
        int currentCardQty = 0;

        if (currentDeck.getDeckItems() != null) {
            for (DeckItem item : currentDeck.getDeckItems()) {
                totalDeckQty += item.getQuantity();
                if (item.getCard().getCardID() == newCard.getCardID()) {
                    currentCardQty = item.getQuantity();
                }
            }
        }

        // Tjek mængden af det specifikke kort (Lande er ofte undtaget i MTG regler, her følger vi din logik)
        boolean isLand = newCard.getCardType() == CardType.LAND;
        if (!isLand && (currentCardQty + quantityToAdd > format.getMaxCopiesOfCard())) {
            result.addError("Regelbrud: Du må maksimalt have " + format.getMaxCopiesOfCard() + " kopier af '" + newCard.getCardName() + "'.");
        }

        // Tjek den samlede størrelse på decket
        if (totalDeckQty + quantityToAdd > format.getMaxDeckSize()) {
            result.addError("Decket er fyldt. Maksimum for " + format.getFormatName() + " er " + format.getMaxDeckSize() + " kort.");
        }

        return result;
    }
}
