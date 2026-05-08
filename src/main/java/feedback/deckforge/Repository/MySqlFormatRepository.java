package feedback.deckforge.Repository;

import feedback.deckforge.Model.Format;
import feedback.deckforge.Service.RepoInterfaces.IFormatRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MySqlFormatRepository implements IFormatRepository {

    private JdbcTemplate jdbcTemplate;

    public MySqlFormatRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Format> formatRowMapper = (rs, rowNum) -> {
        Format format = new Format();

        format.setFormatId(rs.getInt("format_id"));
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
        String sql = "update formats = set format_name = ?, min_deck_size = ?, max_deck_size = ?, max_copies_of_card = ?, requires_commader = ?, allowed_rarities =? where format_id = ? ";

        jdbcTemplate.update(sql,
                format.getFormatName(),
                format.getFormatName(),
                format.getMinDeckSize(),
                format.getMaxDeckSize(),
                format.getMaxCopiesOfCard(),
                format.isRequiresCommander(),
                format.getAllowedRarities(),
                format.getFormatId()
                );
    }

    @Override
    public List<Format> findAllFormats(){
        String sql = "Select * from formats";
        return jdbcTemplate.query(sql, formatRowMapper);
    }

}
