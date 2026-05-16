package feedback.deckforge.Repository;

import feedback.deckforge.Model.*;
import feedback.deckforge.Model.Enum.CardRarity;
import feedback.deckforge.Model.Enum.CardType;
import feedback.deckforge.Service.RepoInterfaces.IDeckRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MySqlDeckRepository implements IDeckRepository {

    private final JdbcTemplate jdbcTemplate;

    public MySqlDeckRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // --- 1. RowMapper til et komplet Kort (Card) ---
    private final RowMapper<Card> cardRowMapper = (rs, rowNum) -> {
        Card c = new Card();
        c.setCardID(rs.getInt("card_id"));
        c.setCardName(rs.getString("card_name"));
        // c.setCardSet(rs.getString("card_set")); // Fjern '//' hvis du har 'card_set' i DB
        c.setManaCost(rs.getString("mana_cost"));
        c.setPower(rs.getInt("power"));
        c.setHealth(rs.getInt("health"));
        c.setDescription(rs.getString("description"));
        c.setColorIdentity(rs.getString("color_identity"));

        String typeStr = rs.getString("card_type");
        if (typeStr != null) c.setCardType(CardType.valueOf(typeStr.toUpperCase()));

        String rarityStr = rs.getString("card_rarity");
        if (rarityStr != null) c.setCardRarity(CardRarity.valueOf(rarityStr.toUpperCase()));

        return c;
    };

    // --- 2. RowMapper til DeckItem (Henter mængde og genbruger cardRowMapper) ---
    private final RowMapper<DeckItem> deckItemRowMapper = (rs, rowNum) -> {
        DeckItem item = new DeckItem();
        item.setQuantity(rs.getInt("quantity"));
        item.setCard(cardRowMapper.mapRow(rs, rowNum)); // Her mapper vi selve kortet
        return item;
    };

    // --- 3. RowMapper til selve Decket ---
    private final RowMapper<Deck> deckRowMapper = (rs, rowNum) -> {
        Format format = new Format();
        format.setFormatID(rs.getInt("format_id"));
        format.setFormatName(rs.getString("format_name"));
        format.setMinDeckSize(rs.getInt("min_deck_size"));
        format.setMaxDeckSize(rs.getInt("max_deck_size"));
        format.setMaxCopiesOfCard(rs.getInt("max_copies_of_card"));
        format.setRequiresCommander(rs.getBoolean("requires_commander"));
        format.setAllowedRarities(rs.getString("allowed_rarities"));

        User owner = new User();
        owner.setUserID(rs.getInt("user_id"));

        Deck deck = new Deck();
        deck.setDeckID(rs.getInt("deck_id"));
        deck.setDeckName(rs.getString("deck_name"));
        deck.setUser(owner);
        deck.setFormat(format);

        int commanderId = rs.getInt("commander_card_id");
        if (!rs.wasNull()) {
            Card commander = new Card();
            commander.setCardID(commanderId);
            deck.setCommander(commander);
        }

        return deck;
    };


    @Override
    public void saveDeck(Deck deck){
        String sql = "INSERT INTO decks (user_id, format_id, deck_name, deck_format, commander_card_id) VALUES (?, ?, ?, ?, ?)";
        Integer commanderID = (deck.getCommander() != null) ? deck.getCommander().getCardID() : null;
        jdbcTemplate.update(sql, deck.getUser().getUserID(), deck.getFormat().getFormatID(), deck.getDeckName(), deck.getFormat().getFormatName(), commanderID);
    }

    @Override
    public void updateDeck(Deck deck) {
        String sql = "UPDATE decks SET deck_name = ?, format_id = ?, commander_card_id = ? WHERE deck_id = ?";
        Integer commanderId = (deck.getCommander() != null) ? deck.getCommander().getCardID() : null;
        jdbcTemplate.update(sql, deck.getDeckName(), deck.getFormat().getFormatID(), commanderId, deck.getDeckID());
    }

    @Override
    public void deleteDeck(int deckID) {
        jdbcTemplate.update("DELETE FROM deck_items WHERE deck_id = ?", deckID);
        jdbcTemplate.update("DELETE FROM decks WHERE deck_id = ?", deckID);
    }

    @Override
    public void addCardToDeck(int deckID, int cardID, int quantity) {
        String sql = "INSERT INTO deck_items (deck_id, card_id, quantity) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
        jdbcTemplate.update(sql, deckID, cardID, quantity);
    }

    @Override
    public void removeCardFromDeck(int deckID, int cardID) {
        jdbcTemplate.update("DELETE FROM deck_items WHERE deck_id = ? AND card_id = ?", deckID, cardID);
    }

    @Override
    public Optional<Deck> findDeckByID(int deckID) {
        String sql = "SELECT d.*, f.* FROM decks d " +
                "JOIN formats f ON d.format_id = f.format_id " +
                "WHERE d.deck_id = ?";
        try {
            // Hent basis-decket via deckRowMapper
            Deck deck = jdbcTemplate.queryForObject(sql, deckRowMapper, deckID);

            if (deck != null) {
                // Hent commander (hvis der er valgt en) via cardRowMapper
                if (deck.getCommander() != null && deck.getCommander().getCardID() > 0) {
                    String cmdSql = "SELECT * FROM cards WHERE card_id = ?";
                    Card commander = jdbcTemplate.queryForObject(cmdSql, cardRowMapper, deck.getCommander().getCardID());
                    deck.setCommander(commander);
                }

                // Hent alle kort (DeckItems) via deckItemRowMapper
                String itemsSql = "SELECT di.quantity, c.* FROM deck_items di " +
                        "JOIN cards c ON di.card_id = c.card_id " +
                        "WHERE di.deck_id = ?";
                List<DeckItem> items = jdbcTemplate.query(itemsSql, deckItemRowMapper, deckID);
                deck.setDeckItems(items);
            }
            return Optional.ofNullable(deck);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Deck> findAllDecksByUserID(int userID) {
        String sql = "SELECT d.*, f.* FROM decks d " +
                "JOIN formats f ON d.format_id = f.format_id " +
                "WHERE d.user_id = ?";

        List<Deck> decks = jdbcTemplate.query(sql, deckRowMapper, userID);

        for (Deck deck : decks) {
            String itemsSql = "SELECT di.quantity, c.* FROM deck_items di " +
                    "JOIN cards c ON di.card_id = c.card_id " +
                    "WHERE di.deck_id = ?";

            // Genbruger vores deckItemRowMapper præcis som i findDeckByID
            List<DeckItem> items = jdbcTemplate.query(itemsSql, deckItemRowMapper, deck.getDeckID());
            deck.setDeckItems(items);
        }

        return decks;
    }

    @Override
    public int getCardQuantity(int deckID, int cardID) {
        String sql = "SELECT quantity FROM deck_items WHERE deck_id = ? AND card_id = ?";
        try {
            Integer quantity = jdbcTemplate.queryForObject(sql, Integer.class, deckID, cardID);
            return quantity != null ? quantity : 0;
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    @Override
    public void updateCardQuantity(int deckID, int cardID, int quantity) {
        String sql = "UPDATE deck_items SET quantity = ? WHERE deck_id = ? AND card_id = ?";
        jdbcTemplate.update(sql, quantity, deckID, cardID);
    }
}