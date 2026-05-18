package feedback.deckforge.Service.Validation;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Deck;
import feedback.deckforge.Model.DeckItem;
import feedback.deckforge.Model.Format;
import feedback.deckforge.Model.Enum.CardType;
import org.springframework.stereotype.Component;

@Component
public class DeckValidation {

    /**
     * Validerer om et kort må bruges som Commander (skal være Creature eller Planeswalker).
     */
    public ValidationResult validateCommander(Card commanderCard) {
        ValidationResult result = new ValidationResult();

        if (commanderCard == null) {
            result.addError("Intet kort valgt.");
            return result;
        }

        String typeStr = (commanderCard.getCardType() != null) ? commanderCard.getCardType().name().toUpperCase() : "";

        if (!typeStr.contains("CREATURE") && !typeStr.contains("PLANESWALKER")) {
            result.addError("En Commander skal være en Creature eller Planeswalker. Dette kort er: " + typeStr);
        }

        return result;
    }


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
            // Man SKAL have en commander før man kan tilføje andre kort
            if (currentDeck.getCommander() == null) {
                result.addError("Du skal vælge en Commander, før du kan tilføje andre kort til dit deck.");
                return result;
            }

            String commanderColors = currentDeck.getCommander().getColorIdentity();
            String cardColors = newCard.getColorIdentity();

            // Vi tjekker kun farver, hvis kortet ikke er "Colorless" (C)
            if (cardColors != null && !cardColors.equalsIgnoreCase("C") && !cardColors.isBlank()) {
                String cmdClean = (commanderColors != null) ? commanderColors.toUpperCase().replaceAll("[\\s,]", "") : "";
                String cardClean = cardColors.toUpperCase().replaceAll("[\\s,]", "");

                for (char color : cardClean.toCharArray()) {
                    if (cmdClean.indexOf(color) == -1) {
                        result.addError("Kortet '" + newCard.getCardName() + "' (" + color + ") bryder din Commanders Color Identity (" + cmdClean + ").");
                        return result;
                    }
                }
            }
        }

        // --- 3. TJEK: MÆNGDER (MAX COPIES & DECK SIZE) ---
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

        // ROBUST LANDE-TJEK: Undtag alle typer lande fra "max kopier" reglen
        String typeStr = (newCard.getCardType() != null) ? newCard.getCardType().name().toUpperCase() : "";
        boolean isLand = typeStr.contains("LAND");

        if (!isLand) {
            if (currentCardQty + quantityToAdd > format.getMaxCopiesOfCard()) {
                result.addError("Regelbrud: Du må maksimalt have " + format.getMaxCopiesOfCard() + " kopier af '" + newCard.getCardName() + "'.");
                return result;
            }
        }

        // Tjek den samlede størrelse på decket (gælder også lande!)
        if (totalDeckQty + quantityToAdd > format.getMaxDeckSize()) {
            result.addError("Decket bliver for stort. Maksimum for " + format.getFormatName() + " er " + format.getMaxDeckSize() + " kort.");
        }

        return result;
    }

    public ValidationResult validateEntireDeck(Deck deck) {
        ValidationResult result = new ValidationResult();
        Format format = deck.getFormat();

        if (format == null) {
            result.addError("Decket mangler et gyldigt format.");
            return result;
        }

        // 1. Tjek Commander reglen
        boolean hasCommander = deck.getCommander() != null;
        if (format.isRequiresCommander() && !hasCommander) {
            result.addError("Advarsel: Formatet '" + format.getFormatName() + "' kræver nu en Commander! Dit deck er ikke længere lovligt, før du vælger en.");
        } else if (!format.isRequiresCommander() && hasCommander) {
            result.addError("Advarsel: Formatet '" + format.getFormatName() + "' bruger ikke længere Commanders. Fjern din Commander for at gøre decket lovligt.");
        }

        // Hent den (potentielt nye) commanders farver til brug i tjekket
        String cmdClean = "";
        if (format.isRequiresCommander() && hasCommander) {
            String colors = deck.getCommander().getColorIdentity();
            cmdClean = (colors != null) ? colors.toUpperCase().replaceAll("[\\s,]", "") : "";
        }

        int totalDeckQty = 0;

        // 2. Tjek alle kortene igennem
        if (deck.getDeckItems() != null) {
            for (DeckItem item : deck.getDeckItems()) {
                Card card = item.getCard();
                int qty = item.getQuantity();
                totalDeckQty += qty;

                // --- NYT: Tjek om eksisterende kort bryder den nye Commanders farver ---
                if (format.isRequiresCommander() && hasCommander) {
                    String cardColors = card.getColorIdentity();
                    if (cardColors != null && !cardColors.equalsIgnoreCase("C") && !cardColors.isBlank()) {
                        String cardClean = cardColors.toUpperCase().replaceAll("[\\s,]", "");
                        for (char color : cardClean.toCharArray()) {
                            if (cmdClean.indexOf(color) == -1) {
                                result.addError("Advarsel: Kortet '" + card.getCardName() + "' bryder den nye Commanders farveidentitet (" + cmdClean + "). Det skal fjernes.");
                                break; // Kun én besked pr. kort, så vi ikke spammer brugeren
                            }
                        }
                    }
                }

                // Rarity-tjek
                String allowed = format.getAllowedRarities();
                if (allowed != null && !allowed.equalsIgnoreCase("ALL")) {
                    if (!allowed.toUpperCase().contains(card.getCardRarity().name().toUpperCase())) {
                        result.addError("Advarsel: Kortet '" + card.getCardName() + "' (" + card.getCardRarity() + ") er ikke længere tilladt i dette format.");
                    }
                }

                // Max kopier-tjek (Undtagen lande)
                String typeStr = (card.getCardType() != null) ? card.getCardType().name().toUpperCase() : "";
                boolean isLand = typeStr.contains("LAND");

                if (!isLand && qty > format.getMaxCopiesOfCard()) {
                    result.addError("Advarsel: Du har " + qty + " kopier af '" + card.getCardName() + "', men formatet tillader nu maksimalt " + format.getMaxCopiesOfCard() + ".");
                }
            }
        }

        // 3. Tjek total Deck Size (FJERNET MINIMUMS-KRAVET SÅ DEN IKKE SPAMMER!)
        if (totalDeckQty > format.getMaxDeckSize()) {
            result.addError("Advarsel: Formatets maksimumsgrænse er faldet. Decket må nu max være " + format.getMaxDeckSize() + " kort.");
        }

        return result;
    }
}
