package feedback.deckforge.Repository;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Enum.CardRarity;
import feedback.deckforge.Model.Enum.CardType;
import feedback.deckforge.Model.TradeCollection;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.RepoInterfaces.ITradeCollectionRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MySqlTradeCollectionRepository implements ITradeCollectionRepository {

    private final JdbcTemplate jdbcTemplate;

    public MySqlTradeCollectionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<TradeCollection> findTradeCollectionByUserId(int userID){
        try {
            String sql = "SELECT trade_collection_id FROM tradecollections WHERE user_id = ?";
            Integer tradeCollectionId = jdbcTemplate.queryForObject(sql, Integer.class, userID);

            TradeCollection tc = new TradeCollection();
            tc.setTradeCollectionId(tradeCollectionId);

            User user = new User();
            user.setUserID(userID);
            tc.setUser(user);

            // TRIN 2: Hent kort og mængder via JOIN
            String itemsSql = "SELECT tci.quantity, c.* FROM tradecollection_items tci " +
                    "JOIN cards c ON tci.card_id = c.card_id " +
                    "WHERE tci.trade_collection_id = ?";

            jdbcTemplate.query(itemsSql, rs -> {
                Card card = new Card();
                card.setCardId(rs.getInt("card_id"));
                card.setCardName(rs.getString("card_name"));
                card.setCardRarity(CardRarity.valueOf(rs.getString("card_rarity")));
                card.setCardType(CardType.valueOf(rs.getString("card_type")));
                card.setCardSet(rs.getString("card_set"));
                card.setManaCost(rs.getString("mana_cost"));
                card.setColorIdentity(rs.getString("color_identity"));
                card.setPower(rs.getInt("power"));
                card.setHealth(rs.getInt("health"));
                card.setDescription(rs.getString("description"));

                tc.addCard(card, rs.getInt("quantity"));
            }, tradeCollectionId);

            return Optional.of(tc);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addCardToTradeCollection(int tradeCollectionID, int cardID, int quantity) {
        String sql = "INSERT INTO tradelist_items (tradelist_id, card_id, quantity) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
        jdbcTemplate.update(sql, tradeCollectionID, cardID, quantity);
    }

    @Override
    public void removeCardFromTradeCollection(int tradeCollectionID, int cardID) {
        String sql = "DELETE FROM tradelist_items WHERE tradelist_id = ? AND card_id = ?";
        jdbcTemplate.update(sql, tradeCollectionID, cardID);
    }

    @Override
    public void setCardQuantity(int tradeCollectionID, int cardID, int newQuantity) {
        String sql = "UPDATE tradelist_items SET quantity = ? WHERE tradelist_id = ? AND card_id = ?";
        jdbcTemplate.update(sql, newQuantity, tradeCollectionID, cardID);
    }


}
