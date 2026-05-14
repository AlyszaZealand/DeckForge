package feedback.deckforge.Repository;

import feedback.deckforge.Model.*;
import feedback.deckforge.Model.Enum.CardRarity;
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

    private final RowMapper<Deck> deckRowMapper = (rs, rowNum) -> {
        Format format = new Format();
        format.setFormatID(rs.getInt("format_id"));
        format.setFormatName(rs.getString("format_name"));
        format.setMinDeckSize(rs.getInt("min_deck_size"));
        format.setMaxDeckSize(rs.getInt("max_deck_size"));
        format.setMaxCopiesOfCard(rs.getInt("max_copies_of_card"));
        format.setRequiresCommander(rs.getBoolean("requires_commander"));
        format.setAllowedRarities(rs.getString("allowed_rarities"));

        // opret user
        User owner = new User();
        owner.setUserID(rs.getInt("user_id"));

        // Opret Decket
        Deck deck = new Deck();
        deck.setDeckID(rs.getInt("deck_id"));
        deck.setDeckName(rs.getString("deck_name"));
        deck.setUser(owner);
        deck.setFormat(format);

        // Håndter Commander hvis den findes
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

        jdbcTemplate.update(sql,
                deck.getUser().getUserID(),
                deck.getFormat().getFormatID(),
                deck.getDeckName(),
                deck.getFormat().getFormatName(),
                commanderID
        );
    }

    @Override
    public void updateDeck(Deck deck) {
        String sql = "UPDATE decks SET deck_name = ?, format_id = ?, commander_card_id = ? WHERE deck_id = ?";

        Integer commanderId = (deck.getCommander() != null) ? deck.getCommander().getCardID() : null;

        jdbcTemplate.update(sql,
                deck.getDeckName(),
                deck.getFormat().getFormatID(),
                commanderId,
                deck.getDeckID()
        );
    }

    @Override
    public void deleteDeck(int deckID) {
        String deleteItemsSql = "DELETE FROM deck_items WHERE deck_id = ?";
        jdbcTemplate.update(deleteItemsSql, deckID);

        String deleteDeckSql = "DELETE FROM decks WHERE deck_id = ?";
        jdbcTemplate.update(deleteDeckSql, deckID);
    }

    @Override
    public void addCardToDeck(int deckID, int cardID, int quantity) {
        String sql = "INSERT INTO deck_items (deck_id, card_id, quantity) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
        jdbcTemplate.update(sql, deckID, cardID, quantity);
    }

    @Override
    public void removeCardFromDeck(int deckID, int cardID) {
        String sql = "DELETE FROM deck_items WHERE deck_id = ? AND card_id = ?";
        jdbcTemplate.update(sql, deckID, cardID);
    }

    @Override
    public Optional<Deck> findDeckByID(int deckID) {
        // Vi joiner nu også med cards tabellen (som 'cmd') for at få Commander-detaljer
        String sql = "SELECT d.*, f.*, " +
                "cmd.card_name AS cmd_name, cmd.color_identity AS cmd_identity, cmd.card_rarity AS cmd_rarity, cmd.card_type AS cmd_type " +
                "FROM decks d " +
                "JOIN formats f ON d.format_id = f.format_id " +
                "LEFT JOIN cards cmd ON d.commander_card_id = cmd.card_id " + // LEFT JOIN så vi stadig finder decks uden commander
                "WHERE d.deck_id = ?";
        try {
            Deck deck = jdbcTemplate.queryForObject(sql, deckRowMapper, deckID);

            if (deck != null) {
                // --- NYT: Udfyld Commander-objektet hvis der findes et ID ---
                // (Tjek om jeres ResultSet har et navn i 'cmd_name')
                String cmdName = jdbcTemplate.queryForObject("SELECT commander_card_id FROM decks WHERE deck_id = ?", Integer.class, deckID) != null ? "exists" : null;

                // Hvis der er en commander, så byg kort-objektet manuelt fra de kolonner vi hentede
                // Alternativt kan du køre et hurtigt cardRepository.findCardByID() kald her.

                // --- Hent deck items (som før) ---
                String itemsSql = "SELECT di.quantity, c.* FROM deck_items di " +
                        "JOIN cards c ON di.card_id = c.card_id " +
                        "WHERE di.deck_id = ?";

                List<DeckItem> items = jdbcTemplate.query(itemsSql, (rs, rowNum) -> {
                    DeckItem item = new DeckItem();
                    item.setQuantity(rs.getInt("quantity"));
                    Card card = new Card();
                    card.setCardID(rs.getInt("card_id"));
                    card.setCardName(rs.getString("card_name"));
                    card.setColorIdentity(rs.getString("color_identity"));
                    String rarityStr = rs.getString("card_rarity");
                    if (rarityStr != null) card.setCardRarity(CardRarity.valueOf(rarityStr.toUpperCase()));
                    item.setCard(card);
                    return item;
                }, deckID);

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
        return jdbcTemplate.query(sql, deckRowMapper, userID);
    }

    @Override
    public int getCardQuantity(int deckID, int cardID) {
        String sql = "SELECT quantity FROM deck_items WHERE deck_id = ? AND card_id = ?";
        try {
            // Vi gemmer resultatet som et Integer objekt først
            Integer quantity = jdbcTemplate.queryForObject(sql, Integer.class, deckID, cardID);

            // Hvis quantity ikke er null, returnerer vi værdien. Ellers returnerer vi 0.
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

