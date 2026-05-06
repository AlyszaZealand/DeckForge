package feedback.deckforge.Model;

public class CollectionItem {
    private Card card;
    private int quantity;

    public CollectionItem() {}
    public CollectionItem(Card card, int quantity) {
        this.card = card;
        this.quantity = quantity;
    }

    public Card getCard() { return card; }
    public void setCard(Card card) { this.card = card; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
