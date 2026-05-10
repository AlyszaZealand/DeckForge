package feedback.deckforge.Repository;

import feedback.deckforge.Model.Enum.EventStatus;
import feedback.deckforge.Model.Event;
import feedback.deckforge.Service.RepoInterfaces.IEventRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MySqlEventRepository implements IEventRepository {

    private final JdbcTemplate jdbcTemplate;

    public MySqlEventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Event> eventRowMapper = (rs,rowNum) -> {
        return new Event(
                rs.getString("event_format"),
                EventStatus.valueOf(rs.getString("event_status")),
                rs.getInt("event_size"),
                rs.getObject("event_date", LocalDateTime.class),
                rs.getString("event_description"),
                rs.getString("event_name")
        );
    };

    @Override
    public void createEvent(Event event){
        String sql = "Insert into events (event_format, event_status, event_size, event_date, event_description, event_name) values (?,?,?,?,?,?)";

        jdbcTemplate.update(sql,
                event.getEventFormat(),
                event.getEventStatus(),
                event.getEventSize(),
                event.getEventDate(),
                event.getEventDescription(),
                event.getEventName()
        );
    }

    @Override
    public void updateEvent(Event event){
        String sql = "update events set event_format = ?, event_status = ?, event_size = ?, event_date = ?, event_description = ? where event_id = ?";

        jdbcTemplate.update(sql,
                event.getEventFormat(),
                event.getEventStatus(),
                event.getEventSize(),
                event.getEventDate(),
                event.getEventDescription(),
                event.getEventId()
                );
    }

    @Override
    public void deleteEvent(int eventID){
        String sql = "delete from events where event_id = ?";

        jdbcTemplate.update(sql, eventID);
    }

    @Override
    public Optional<Event> findEventByID(int eventID){
        String sql = "select * from events where event_id = ?";

        try{
            Event event = jdbcTemplate.queryForObject(sql, eventRowMapper, eventID);
            return Optional.of(event);
        } catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    @Override
    public List<Event> findAllEvents(){
        String sql = "select * from events";

        return jdbcTemplate.query(sql, eventRowMapper);
    }

    @Override
    public List<Integer> findSignedUpUsersByEventID(int eventID){
        String sql = "select user_id from event_registrations where event_id = ?";

        return jdbcTemplate.queryForList(sql, Integer.class, eventID);
    }

    @Override
    public void signUp(int eventID, int userID){
        String sql = "insert into event_registrations (event_id, user_id) values (?,?)";

        jdbcTemplate.update(sql, eventID, userID);
    }

    @Override
    public void removeSignUp(int eventID, int userID){
        String sql = "delete from event_registrations where event_id = ? and user_id = ?";

        jdbcTemplate.update(sql, eventID, userID);
    }


}
