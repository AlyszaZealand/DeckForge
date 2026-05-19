package feedback.deckforge.Model.DTO;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Enum.TradeStatus;
import java.time.LocalDateTime;
import java.util.List;

public class TradeViewDTO {
    private int tradeId;
    private String partnerUsername;
    private TradeStatus tradeStatus;
    private LocalDateTime tradeDate;
    private List<TradeCardDTO> cardsYouGive;
    private List<TradeCardDTO> cardsYouGet;
    private boolean requiresYourConfirmation;
    private boolean waitingForPartnerConfirmation;

    // --- Getters & Setters ---
    public int getTradeId() { return tradeId; }
    public void setTradeId(int tradeId) { this.tradeId = tradeId; }

    public String getPartnerUsername() { return partnerUsername; }
    public void setPartnerUsername(String partnerUsername) { this.partnerUsername = partnerUsername; }

    public TradeStatus getTradeStatus() { return tradeStatus; }
    public void setTradeStatus(TradeStatus tradeStatus) { this.tradeStatus = tradeStatus; }

    public LocalDateTime getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDateTime tradeDate) { this.tradeDate = tradeDate; }

    public List<TradeCardDTO> getCardsYouGive() { return cardsYouGive; }
    public void setCardsYouGive(List<TradeCardDTO> cardsYouGive) { this.cardsYouGive = cardsYouGive; }

    public List<TradeCardDTO> getCardsYouGet() { return cardsYouGet; }
    public void setCardsYouGet(List<TradeCardDTO> cardsYouGet) { this.cardsYouGet = cardsYouGet; }

    public boolean isRequiresYourConfirmation() { return requiresYourConfirmation; }
    public void setRequiresYourConfirmation(boolean requiresYourConfirmation) { this.requiresYourConfirmation = requiresYourConfirmation; }

    public boolean isWaitingForPartnerConfirmation() { return waitingForPartnerConfirmation; }
    public void setWaitingForPartnerConfirmation(boolean waitingForPartnerConfirmation) { this.waitingForPartnerConfirmation = waitingForPartnerConfirmation; }
}