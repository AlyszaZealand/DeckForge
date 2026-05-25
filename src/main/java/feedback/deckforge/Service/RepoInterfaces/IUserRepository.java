package feedback.deckforge.Service.RepoInterfaces;

import feedback.deckforge.Model.User;

import java.util.List;
import java.util.Optional;

public interface IUserRepository {

    int saveUser(User user);
    void deleteUser(int userID);
    void updateUserInformation(User user);
    List<User> findAllUsers();
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByID(int userID);
    List<User> findMembersByTradelistCard(String cardName);
    void changeUserRole(int userId, feedback.deckforge.Model.Enum.UserRole newRole);
    List<User> findUsersByEventID(int eventID);
    Optional<User> findUserByUsername(String username);


}
