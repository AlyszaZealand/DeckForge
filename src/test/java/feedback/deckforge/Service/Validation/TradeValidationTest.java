package feedback.deckforge.Service.Validation;

import feedback.deckforge.Model.Trade;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.CollectionService;
import feedback.deckforge.Service.RepoInterfaces.ITradeRepository;
import feedback.deckforge.Service.TradeCollectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TradeValidationTest {


    @Mock private TradeCollectionService tradeCollectionService;
    @Mock private CollectionService collectionService;
    @Mock private ITradeRepository tradeRepository;

    @InjectMocks
    private TradeValidation tradeValidation;

    // 1. tester at man ikke kan bytte med sig selv
    @Test
    void validateProposal_ShouldReturnError_WhenTradingWithYourself() {
        // Arrange
        User user = new User();
        user.setUserID(1); // Samme bruger-ID

        Trade trade = new Trade();
        trade.setInitiator(user);
        trade.setReceiver(user);

        // Act
        ValidationResult result = tradeValidation.validateProposal(trade);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().contains("Du kan ikke bytte kort med dig selv."));
    }

    // 2. tester at et bytte skal indeholde kort fra begge parter
    @Test
    void validateProposal_ShouldReturnError_WhenNoCardsOfferedOrRequested() {
        // Arrange
        User user1 = new User(); user1.setUserID(1);
        User user2 = new User(); user2.setUserID(2);

        Trade trade = new Trade();
        trade.setInitiator(user1);
        trade.setReceiver(user2);
        trade.setOfferedCards(new ArrayList<>()); // Tom liste - tilbyder intet
        trade.setRequestedCards(new ArrayList<>()); // Tom liste - anmoder om intet

        // Act
        ValidationResult result = tradeValidation.validateProposal(trade);

        // Assert
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().contains("Du skal tilbyde mindst ét kort fra din egen bytteliste."));
        assertTrue(result.getErrors().contains("Du skal anmode om mindst ét kort fra modtagerens bytteliste."));
    }
}