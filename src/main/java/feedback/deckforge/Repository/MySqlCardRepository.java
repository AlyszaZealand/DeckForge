package feedback.deckforge.Repository;

import feedback.deckforge.Model.Card;
import feedback.deckforge.Model.Enum.CardRarity;
import feedback.deckforge.Model.Enum.CardType;
import feedback.deckforge.Service.RepoInterfaces.ICardRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MySqlCardRepository implements ICardRepository {

    private final JdbcTemplate jdbcTemplate;

    public MySqlCardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Card> cardRowMapper = (rs, rowNum) -> {
        Card card = new Card();

        card.setCardId(rs.getInt("card_id"));
        card.setCardName(rs.getString("card_name"));
        card.setCardSet(rs.getString("card_set"));

        // Safely mapping the CardType Enum
        String typeStr = rs.getString("card_type");
        if (typeStr != null) {
            card.setCardType(CardType.valueOf(typeStr.toUpperCase()));
        }

        // Safely mapping the CardRarity Enum
        String rarityStr = rs.getString("card_rarity");
        if (rarityStr != null) {
            card.setCardRarity(CardRarity.valueOf(rarityStr.toUpperCase()));
        }

        card.setManaCost(rs.getString("mana_cost"));
        card.setColorIdentity(rs.getString("color_identity"));

        // Stats and descriptions
        card.setPower(rs.getInt("power"));
        card.setHealth(rs.getInt("health"));
        card.setDescription(rs.getString("description"));

        return card;
    };

    @Override
    public void saveCard(Card card) {
        // Tjek at dette matcher de rigtige kolonnenavne i jeres schema.sql
        String sql = "INSERT INTO cards (card_name, card_type, card_rarity, card_set, mana_cost, color_identity, power, health, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                card.getCardName(),
                card.getCardType().name(),
                card.getCardRarity().name(),
                card.getCardSet(),
                card.getManaCost(),
                card.getColorIdentity(),
                card.getPower(),
                card.getHealth(),
                card.getDescription()
        );
    }

    @Override
    public Optional<Card> findCardById(int cardID) {
        String sql = "select * form cards where card_id = ?";

        try{
            Card card =jdbcTemplate.queryForObject(sql, cardRowMapper,cardID);
            return Optional.of(card);
        } catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    @Override
    public List<Card> findAllCards() {
        String sql = "Select * from cards";
        return jdbcTemplate.query(sql,cardRowMapper);
    }
}
