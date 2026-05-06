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


    public void saveUser(User user){
        String sql = "Insert into users (username,email,password,userrole) values (?,?,?,?)";

        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getUserRole()
        );
    }

    public void deleteUser(int userID){
        String sql = "Delete from users where id=?";

        jdbcTemplate.update(sql, userID);
    }

    public void updateUserInformation(User user){
        String sql = "Update users set username = ?, email = ? where user_id = ?";

        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getEmail(),
                user.getUserID()
        );
    }

    public List<User> findAllUsers(){
        String sql = "Select * from users";

        return jdbcTemplate.query(sql, userRowMapper);
    }

    public Optional<User> findUserByEmail(String email){
        String sql = "Select * from users where email = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<User> findUserByID(int userID){
        String sql = "Select * from users where user_id = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, userID);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }



}
