package feedback.deckforge.Model;

import java.util.ArrayList;
import java.util.List;

public class Deck {

    private int deckId;
    private String deckName;
    private User user;
    private Format format;

    // Kortene i decket
    private Card commander;     // Vil være null, hvis format.requiresCommander er false
    private List<Card> cards = new ArrayList<>();

    public Deck() {}

    public Deck(String deckName, User user, Format format) {
        this.deckName = deckName;
        this.user = user;
        this.format = format;
    }

    // --- Getters og Setters ---
    public int getDeckId() { return deckId; }
    public void setDeckId(int deckId) { this.deckId = deckId; }

    public String getDeckName() { return deckName; }
    public void setDeckName(String deckName) { this.deckName = deckName; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Format getFormat() { return format; }
    public void setFormat(Format format) { this.format = format; }

    public Card getCommander() { return commander; }
    public void setCommander(Card commander) { this.commander = commander; }

    public List<Card> getCards() { return cards; }
    public void setCards(List<Card> cards) { this.cards = cards; }

    public void addCard(Card card) {
        this.cards.add(card);
    }
}
