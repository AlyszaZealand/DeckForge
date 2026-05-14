package feedback.deckforge.Model;

public class DeckItem {
    private Card card;
    private int quantity;
    private int ownedQuantity;

    public DeckItem() {}

    public DeckItem(Card card, int quantity) {
        this.card = card;
        this.quantity = quantity;
    }

    public Card getCard() { return card; }
    public void setCard(Card card) { this.card = card; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getOwnedQuantity() {
        return ownedQuantity;
    }
    public void setOwnedQuantity(int ownedQuantity) {
        this.ownedQuantity = ownedQuantity;
    }

    public boolean isFullyOwned() {
        return ownedQuantity >= quantity;
    }

}

