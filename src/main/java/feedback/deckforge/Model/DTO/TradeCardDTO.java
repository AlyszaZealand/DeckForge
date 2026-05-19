package feedback.deckforge.Model.DTO;

import feedback.deckforge.Model.Card;

public class TradeCardDTO {
    private Card card;
    private int totalQuantity;
    private int availableQuantity;

    public TradeCardDTO(Card card, int totalQuantity, int availableQuantity) {
        this.card = card;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
    }

    public Card getCard() { return card; }
    public int getTotalQuantity() { return totalQuantity; }
    public int getAvailableQuantity() { return availableQuantity; }
}