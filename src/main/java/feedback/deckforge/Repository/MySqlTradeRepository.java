package feedback.deckforge.Repository;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Enum.CardRarity;
import feedback.deckforge.Model.Enum.CardType;
import feedback.deckforge.Model.Enum.TradeStatus;
import feedback.deckforge.Model.Trade;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.RepoInterfaces.ITradeRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class MySqlTradeRepository implements ITradeRepository {

    private final JdbcTemplate jdbcTemplate;

    public MySqlTradeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Mapper selve Trade-objektet (uden listerne i første omgang)
    private final RowMapper<Trade> tradeRowMapper = (rs, rowNum) -> {
        Trade trade = new Trade();
        trade.setTradeId(rs.getInt("trade_id"));
        trade.setTradeStatus(TradeStatus.valueOf(rs.getString("trade_status")));

        trade.setInitiatorConfirmed(rs.getBoolean("initiator_confirmed"));
        trade.setReceiverConfirmed(rs.getBoolean("receiver_confirmed"));

        if (rs.getTimestamp("trade_date") != null) {
            trade.setTradeDate(rs.getTimestamp("trade_date").toLocalDateTime());
        }

        // Sætter dummy users ind med de rigtige ID'er
        User initiator = new User();
        initiator.setUserID(rs.getInt("initiator_user_id"));
        trade.setInitiator(initiator);

        User receiver = new User();
        receiver.setUserID(rs.getInt("receiver_user_id"));
        trade.setReceiver(receiver);

        return trade;
    };

    @Override
    public void saveTrade(Trade trade) {
        String insertTradeSql = "INSERT INTO trades (initiator_user_id, receiver_user_id, trade_status) VALUES (?, ?, ?)";

        // Vi bruger en KeyHolder for at få fat i det trade_id, som databasen automatisk genererer (AUTO_INCREMENT)
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertTradeSql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, trade.getInitiator().getUserID());
            ps.setInt(2, trade.getReceiver().getUserID());
            ps.setString(3, trade.getTradeStatus().name());
            return ps;
        }, keyHolder);

        // Hent det nye ID
        int newTradeId = keyHolder.getKey().intValue();
        trade.setTradeId(newTradeId);

        // Nu gemmer vi kortene!
        String insertItemSql = "INSERT INTO trade_items (trade_id, card_id, quantity, is_offered_by_initiator) VALUES (?, ?, 1, ?)";

        // 1. Gem alle de kort, Initiator tilbyder (is_offered_by_initiator = TRUE)
        for (Card offered : trade.getOfferedCards()) {
            jdbcTemplate.update(insertItemSql, newTradeId, offered.getCardID(), true);
        }

        // 2. Gem alle de kort, Initiator ønsker fra modtageren (is_offered_by_initiator = FALSE)
        for (Card requested : trade.getRequestedCards()) {
            jdbcTemplate.update(insertItemSql, newTradeId, requested.getCardID(), false);
        }
    }

    @Override
    public Optional<Trade> findTradeById(int tradeId) {
        String sql = "SELECT * FROM trades WHERE trade_id = ?";
        try {
            Trade trade = jdbcTemplate.queryForObject(sql, tradeRowMapper, tradeId);

            // Når vi har fundet byttehandlen, skal vi hente dens kort (se hjælpe-metoden nederst)
            loadCardsIntoTrade(trade);

            return Optional.of(trade);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    @Override
    public void deleteTrade(int tradeId) {
        // Takket være 'ON DELETE CASCADE' i jeres schema, slettes trade_items automatisk når traden slettes!
        String sql = "DELETE FROM trades WHERE trade_id = ?";
        jdbcTemplate.update(sql, tradeId);
    }

    @Override
    public List<Trade> findTradesByInitiatorId(int initiatorId) {
        String sql = "SELECT * FROM trades WHERE initiator_user_id = ?";
        List<Trade> trades = jdbcTemplate.query(sql, tradeRowMapper, initiatorId);
        // Henter kort for hver trade i listen
        for (Trade trade : trades) {
            loadCardsIntoTrade(trade);
        }
        return trades;
    }

    @Override
    public List<Trade> findTradesByReceiverId(int receiverId) {
        String sql = "SELECT * FROM trades WHERE receiver_user_id = ?";
        List<Trade> trades = jdbcTemplate.query(sql, tradeRowMapper, receiverId);
        for (Trade trade : trades) {
            loadCardsIntoTrade(trade);
        }
        return trades;
    }

    @Override
    public List<Trade> findAllTradesByUserId(int userId) {
        String sql = "SELECT * FROM trades WHERE initiator_user_id = ? OR receiver_user_id = ?";
        List<Trade> trades = jdbcTemplate.query(sql, tradeRowMapper, userId, userId);
        for (Trade trade : trades) {
            loadCardsIntoTrade(trade);
        }
        return trades;
    }

    @Override
    public void updateTradeStatus(int tradeId, TradeStatus status) {
        String sql = "UPDATE trades SET trade_status = ? WHERE trade_id = ?";

        // Her sender vi både status-navnet og ID'et ned til databasen
        jdbcTemplate.update(sql, status.name(), tradeId);
    }

    // ==========================================
    // HJÆLPE-METODE: Henter kortene til en Trade
    // ==========================================
    private void loadCardsIntoTrade(Trade trade) {
        // Vi joiner trade_items med cards for at få al info om kortet
        String sql = "SELECT c.*, ti.is_offered_by_initiator FROM trade_items ti " +
                "JOIN cards c ON ti.card_id = c.card_id " +
                "WHERE ti.trade_id = ?";

        jdbcTemplate.query(sql, rs -> {
            Card card = new Card();
            card.setCardID(rs.getInt("card_id"));
            card.setCardName(rs.getString("card_name"));
            card.setCardRarity(CardRarity.valueOf(rs.getString("card_rarity")));
            card.setCardType(CardType.valueOf(rs.getString("card_type")));
            card.setCardSet(rs.getString("card_set"));
            card.setManaCost(rs.getString("mana_cost"));
            card.setColorIdentity(rs.getString("color_identity"));
            card.setPower(rs.getInt("power"));
            card.setHealth(rs.getInt("health"));
            card.setDescription(rs.getString("description"));

            // Trafiklyset! Er det tilbudt eller anmodet?
            boolean isOffered = rs.getBoolean("is_offered_by_initiator");

            if (isOffered) {
                trade.getOfferedCards().add(card);
            } else {
                trade.getRequestedCards().add(card);
            }
        }, trade.getTradeId());
    }

    @Override
    public void updateTradeStatusAndConfirmations(int tradeId, TradeStatus status, boolean initiatorConfirmed, boolean receiverConfirmed) {
        String sql = "UPDATE trades SET trade_status = ?, initiator_confirmed = ?, receiver_confirmed = ? WHERE trade_id = ?";

        jdbcTemplate.update(sql, status.name(), initiatorConfirmed, receiverConfirmed, tradeId);
    }
}
