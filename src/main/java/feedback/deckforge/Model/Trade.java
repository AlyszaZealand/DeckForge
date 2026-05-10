package feedback.deckforge.Model;

import feedback.deckforge.Model.Enum.TradeStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Trade {

    private int tradeID;
    private User initiator;
    private User receiver;
    private TradeStatus tradeStatus;
    private boolean initiatorConfirmed = false;
    private boolean receiverConfirmed = false;
    private LocalDateTime tradeDate;
    private LocalDateTime completedDate;

    private List<Card> offeredCards = new ArrayList<>();   // Kort som initiator vil afgive
    private List<Card> requestedCards = new ArrayList<>(); // Kort som initiator vil have

    public Trade() {}

    public Trade(User initiator, User receiver, TradeStatus tradeStatus, LocalDateTime tradeDate) {
        this.initiator = initiator;
        this.receiver = receiver;
        this.tradeStatus = tradeStatus;
        this.tradeDate = tradeDate;
    }

    // Getters og Setters
    public int getTradeId() { return tradeID; }
    public void setTradeId(int tradeId) { this.tradeID = tradeId; }

    public User getInitiator() { return initiator; }
    public void setInitiator(User initiator) { this.initiator = initiator; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public TradeStatus getTradeStatus() { return tradeStatus; }
    public void setTradeStatus(TradeStatus tradeStatus) { this.tradeStatus = tradeStatus; }

    public boolean isInitiatorConfirmed() { return initiatorConfirmed; }
    public void setInitiatorConfirmed(boolean initiatorConfirmed) { this.initiatorConfirmed = initiatorConfirmed; }

    public boolean isReceiverConfirmed() { return receiverConfirmed; }
    public void setReceiverConfirmed(boolean receiverConfirmed) { this.receiverConfirmed = receiverConfirmed; }

    public LocalDateTime getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDateTime tradeDate) { this.tradeDate = tradeDate; }

    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }

    public List<Card> getOfferedCards() { return offeredCards; }
    public void setOfferedCards(List<Card> offeredCards) { this.offeredCards = offeredCards; }

    public List<Card> getRequestedCards() { return requestedCards; }
    public void setRequestedCards(List<Card> requestedCards) { this.requestedCards = requestedCards; }
}
