package feedback.deckforge.Model;

import java.util.ArrayList;
import java.util.List;

public class Collection {
    private int collectionID;
    private User user;
    private List<CollectionItem> items = new ArrayList<>();

    public Collection() {}

    // Hjælpemetode til at håndtere mængder logisk
    public void addCard(Card card, int quantity) {
        for (CollectionItem item : items) {
            if (item.getCard().getCardID() == card.getCardID()) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        this.items.add(new CollectionItem(card, quantity));
    }

    public int getCollectionId() { return collectionID; }
    public void setCollectionId(int collectionId) { this.collectionID = collectionId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<CollectionItem> getCollectionItems() { return items; }
    public void setCollectionItems(List<CollectionItem> items) { this.items = items; }
}