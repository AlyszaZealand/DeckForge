package feedback.deckforge.Model;

import feedback.deckforge.Model.Enum.TradeStatus;
import java.time.LocalDateTime;

public class Trade {

    private int tradeId;
    private User initiator;
    private User receiver;
    private TradeStatus tradeStatus;
    private LocalDateTime tradeDate;
    private LocalDateTime completedDate;

    public Trade() {}

    public Trade(User initiator, User receiver, TradeStatus tradeStatus, LocalDateTime tradeDate) {
        this.initiator = initiator;
        this.receiver = receiver;
        this.tradeStatus = tradeStatus;
        this.tradeDate = tradeDate;
    }

    // Getters og Setters
    public int getTradeId() { return tradeId; }
    public void setTradeId(int tradeId) { this.tradeId = tradeId; }

    public User getInitiator() { return initiator; }
    public void setInitiator(User initiator) { this.initiator = initiator; }

    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }

    public TradeStatus getTradeStatus() { return tradeStatus; }
    public void setTradeStatus(TradeStatus tradeStatus) { this.tradeStatus = tradeStatus; }

    public LocalDateTime getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDateTime tradeDate) { this.tradeDate = tradeDate; }

    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }
}
