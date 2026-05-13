package feedback.deckforge.Model;

import java.util.ArrayList;
import java.util.List;

public class TradeCollection {
    private int tradeCollectionID;
    private User user;
    private List<TradeCollectionItem> items = new ArrayList<>();

    public TradeCollection() {}

    public void addCard(Card card, int quantity) {
        for (TradeCollectionItem item : items) {
            if (item.getCard().getCardID() == card.getCardID()) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        this.items.add(new TradeCollectionItem(card, quantity));
    }

    public int getTradeCollectionId() { return tradeCollectionID; }
    public void setTradeCollectionId(int tradeCollectionID) { this.tradeCollectionID = tradeCollectionID; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<TradeCollectionItem> getTradeCollectionItems() { return items; }
    public void setTradeCollectionItems(List<TradeCollectionItem> items) { this.items = items; }
}
