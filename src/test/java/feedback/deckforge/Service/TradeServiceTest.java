package feedback.deckforge.Service;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Enum.TradeStatus;
import feedback.deckforge.Model.Trade;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.RepoInterfaces.ITradeRepository;
import feedback.deckforge.Service.Validation.TradeValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock private ITradeRepository tradeRepository;
    @Mock private TradeCollectionService tradeCollectionService;
    @Mock private TradeValidation tradeValidation;

    @InjectMocks
    private TradeService tradeService;

    // 1. tester at trade status sættes til pending og gemmes
    @Test
    void proposeTrade_ShouldSetStatusToPendingAndSave_WhenValidationPasses() {
        // Arrange
        Trade trade = new Trade();

        // Vi instruerer vores mock-validering i, at der IKKE er nogen fejl (tom ValidationResult)
        when(tradeValidation.validateProposal(trade)).thenReturn(new ValidationResult());

        // Act
        ValidationResult result = tradeService.proposeTrade(trade);

        // Assert
        assertFalse(result.hasErrors(), "Der bør ikke være nogen valideringsfejl");
        assertEquals(TradeStatus.PENDING, trade.getTradeStatus(), "Status skal ændres til PENDING");

        verify(tradeRepository, times(1)).saveTrade(trade);
    }

    // 2. tester at når en accepteret handel annulleres, skal kortene frigives og gives tilbage
    @Test
    void cancelTrade_ShouldReturnCardsToTradeCollection_WhenTradeWasAccepted() {
        // Arrange
        User initiator = new User(); initiator.setUserID(1);
        User receiver = new User(); receiver.setUserID(2);

        Card offeredCard = new Card(); offeredCard.setCardID(10);
        Card requestedCard = new Card(); requestedCard.setCardID(20);

        Trade trade = new Trade();
        trade.setTradeId(100);
        trade.setTradeStatus(TradeStatus.ACCEPTED); // Handlen er allerede låst/accepteret
        trade.setInitiator(initiator);
        trade.setReceiver(receiver);
        trade.setOfferedCards(List.of(offeredCard));
        trade.setRequestedCards(List.of(requestedCard));

        // Fortæl den falske database, at den skal finde dette bytte, når der søges på ID 100
        when(tradeRepository.findTradeById(100)).thenReturn(Optional.of(trade));

        // Act - Vi annullerer handlen
        tradeService.cancelTrade(100,initiator.getUserID());

        // Assert
        // Tjek at status på selve objektet blev ændret
        assertEquals(TradeStatus.CANCELLED, trade.getTradeStatus());

        // Tjek at den falske database blev bedt om at opdatere statusen
        verify(tradeRepository, times(1)).updateTradeStatus(100, TradeStatus.CANCELLED);


        // Initiator (ID 1) skal have sit kort (ID 10) tilbage
        verify(tradeCollectionService, times(1)).addCardsToTradeCollection(1, 10, 1);
        // Receiver (ID 2) skal have sit kort (ID 20) tilbage
        verify(tradeCollectionService, times(1)).addCardsToTradeCollection(2, 20, 1);
    }
}