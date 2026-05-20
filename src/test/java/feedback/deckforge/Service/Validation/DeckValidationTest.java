package feedback.deckforge.Service.Validation;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Deck;
import feedback.deckforge.Model.Format;
import feedback.deckforge.Model.Enum.CardType;
import feedback.deckforge.Model.Enum.CardRarity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeckValidationTest {

    private DeckValidation deckValidation;

    @BeforeEach
    void setUp() {
        deckValidation = new DeckValidation();
    }

    // 1: tester at man ikke kan tilføje kort, der bryder Commanderens Color Identity
    @Test
    void validateAddCard_ShouldReturnError_WhenColorIdentityIsBroken() {
        // Arrange
        Format format = new Format();
        format.setRequiresCommander(true);
        format.setMaxDeckSize(100);
        format.setMaxCopiesOfCard(1);
        format.setAllowedRarities("ALL");

        Card commander = new Card();
        commander.setColorIdentity("R,G"); // Rød og Grøn

        Deck deck = new Deck();
        deck.setFormat(format);
        deck.setCommander(commander);

        Card newCard = new Card();
        newCard.setCardName("Blue Spell");
        newCard.setColorIdentity("U"); // Blå
        newCard.setCardType(CardType.INSTANT);
        newCard.setCardRarity(CardRarity.COMMON);


        ValidationResult result = deckValidation.validateAddCard(deck, newCard, 1);


        assertTrue(result.hasErrors(), "Bør fejle fordi 'U' (Blå) ikke er i commanderens farver ('R,G')");
        assertTrue(result.getErrors().get(0).contains("bryder din Commanders Color Identity"));
    }

    // 2: tester at reglen for max kopier af et kort ignoreres for Land
    @Test
    void validateAddCard_ShouldAllowExceedingMaxCopies_IfCardIsLand() {
        // Arrange
        Format format = new Format();
        format.setMaxCopiesOfCard(4);
        format.setMaxDeckSize(60);
        format.setAllowedRarities("ALL");

        Deck deck = new Deck();
        deck.setFormat(format);

        Card landCard = new Card();
        landCard.setCardName("Forest");
        landCard.setCardType(CardType.LAND);
        landCard.setCardRarity(CardRarity.COMMON);


        // Vi prøver at tilføje 10 skove, selvom formatets max kopier er 4
        ValidationResult result = deckValidation.validateAddCard(deck, landCard, 10);

        assertFalse(result.hasErrors(), "Lande bør undtages fra max-kopier reglen");
    }
}