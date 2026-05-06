package feedback.deckforge.Model;

public class WishCollectionItem {
    private Card card;

    public WishCollectionItem() {}
    public WishCollectionItem(Card card) {
        this.card = card;

    }

    public Card getCard() { return card; }

    public void setCard(Card card) { this.card = card; }
}
