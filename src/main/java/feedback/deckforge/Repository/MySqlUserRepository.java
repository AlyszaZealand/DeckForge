package feedback.deckforge.Repository;

import feedback.deckforge.Model.Enum.UserRole;
import feedback.deckforge.Model.User;
import feedback.deckforge.Service.RepoInterfaces.IUserRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;



import java.util.List;
import java.util.Optional;

@Repository
public class MySqlUserRepository implements IUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public MySqlUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) ->{
        return new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("email"),
                UserRole.valueOf(rs.getString("user_role")), // Converts DB String to Java Enum
                rs.getString("password_hash")
        );
    };

    @Override
    public int saveUser(User user) {
        String sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
        org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            java.sql.PreparedStatement ps = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword()); // Dette er det hashede password fra servicen
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public void deleteUser(int userID){
        String sql = "Delete from users where user_id=?";

        jdbcTemplate.update(sql, userID);
    }

    @Override
    public void updateUserInformation(User user){
        String sql = "Update users set username = ?, email = ? where user_id = ?";

        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getEmail(),
                user.getUserID()
        );
    }

    @Override
    public List<User> findAllUsers(){
        String sql = "Select * from users";

        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public Optional<User> findUserByEmail(String email){
        String sql = "Select * from users where email = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findUserByID(int userID){
        String sql = "Select * from users where user_id = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, userID);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findMembersByTradelistCard(String cardName) {
        String sql = "SELECT DISTINCT u.* FROM users u " +
                "JOIN tradecollections tc ON u.user_id = tc.user_id " +
                "JOIN tradecollection_items tci ON tc.tradecollection_id = tci.tradecollection_id " +
                "JOIN cards c ON tci.card_id = c.card_id " +
                "WHERE u.user_role = 'MEMBER' AND c.card_name LIKE ?";

        String searchParam = "%" + cardName + "%";

        return jdbcTemplate.query(sql, userRowMapper, searchParam);
    }

    @Override
    public void changeUserRole(int userId, feedback.deckforge.Model.Enum.UserRole newRole) {
        String sql = "UPDATE users SET user_role = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, newRole.name(), userId);
    }

    @Override
    public List<User> findUsersByEventID(int eventID) {
        String sql = "SELECT u.* FROM users u " +
                "INNER JOIN event_registrations er ON u.user_id = er.user_id " +
                "WHERE er.event_id = ?";

        return jdbcTemplate.query(sql, userRowMapper, eventID);
    }

    @Override
    public Optional<User> findUserByUsername(String username){
        String sql = "Select username from users where username = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, username);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }



}
