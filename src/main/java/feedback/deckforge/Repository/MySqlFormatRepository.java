package feedback.deckforge.Repository;

import feedback.deckforge.Model.Format;
import feedback.deckforge.Service.RepoInterfaces.IFormatRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MySqlFormatRepository implements IFormatRepository {

    private JdbcTemplate jdbcTemplate;

    public MySqlFormatRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Format> formatRowMapper = (rs, rowNum) -> {
        Format format = new Format();

        format.setFormatID(rs.getInt("format_id"));
        format.setFormatName(rs.getString("format_name"));
        format.setMinDeckSize(rs.getInt("min_deck_size"));
        format.setMaxDeckSize(rs.getInt("max_deck_size"));
        format.setMaxCopiesOfCard(rs.getInt("max_copies_of_card"));
        format.setRequiresCommander(rs.getBoolean("requires_commander"));
        format.setAllowedRarities(rs.getString("allowed_rarities"));

        return format;
    };

    @Override
    public void saveFormat(Format format){
        String sql = "Insert into formats (format_name, min_deck_size, max_deck_size, max_copies_of_card, requires_commander, allowed_rarities) values (?,?,?,?,?,?);";

        jdbcTemplate.update(sql,
                format.getFormatName(),
                format.getMinDeckSize(),
                format.getMaxDeckSize(),
                format.getMaxCopiesOfCard(),
                format.isRequiresCommander(),
                format.getAllowedRarities()
        );
    }

    @Override
    public void deleteFormat(int formatID){
        String sql = "Delete from formats where format_id = ?";

        jdbcTemplate.update(sql, formatID);
    }

    @Override
    public void updateFormat(Format format){
        String sql = "UPDATE formats SET format_name = ?, min_deck_size = ?, max_deck_size = ?, max_copies_of_card = ?, requires_commander = ?, allowed_rarities = ? WHERE format_id = ?";

        jdbcTemplate.update(sql,
                format.getFormatName(),
                // Fjernet den ekstra getFormatName() herfra
                format.getMinDeckSize(),
                format.getMaxDeckSize(),
                format.getMaxCopiesOfCard(),
                format.isRequiresCommander(),
                format.getAllowedRarities(),
                format.getFormatID() // Format ID til WHERE cluse
        );
    }

    @Override
    public List<Format> findAllFormats(){
        String sql = "Select * from formats";
        return jdbcTemplate.query(sql, formatRowMapper);
    }

    @Override
    public Optional<Format> findFormatByID(int formatID) {
        // SQL-forespørgslen der leder efter det specifikke id
        String sql = "SELECT * FROM formats WHERE format_id = ?";

        try {
            // Her bruger vi jeres eksisterende formatRowMapper!
            Format format = jdbcTemplate.queryForObject(sql, formatRowMapper, formatID);
            return Optional.of(format);
        } catch (EmptyResultDataAccessException e) {
            // Hvis formatet ikke findes (f.eks. ved et ugyldigt ID), returnerer vi empty
            return Optional.empty();
        }
    }


}
