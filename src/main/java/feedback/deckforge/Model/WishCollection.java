package feedback.deckforge.Model;

import java.util.ArrayList;
import java.util.List;

public class WishCollection {
    private int wishCollectionID;
    private User user;
    private List<WishCollectionItem> items = new ArrayList<>();

    public WishCollection() {}

    public void addCard(Card card) {
        for (WishCollectionItem item : items) {
            if (item.getCard().getCardId() == card.getCardId()) {
                return;
            }
        }
        // Hvis vi når herned, findes kortet ikke i listen endnu
        this.items.add(new WishCollectionItem(card));
    }

    public int getWishCollectionId() { return wishCollectionID; }
    public void setWishCollectionId(int wishCollectionID) { this.wishCollectionID = wishCollectionID; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<WishCollectionItem> getWishCollectionItems() { return items; }
    public void setWishCollectionItems(List<WishCollectionItem> items) { this.items = items; }
}

