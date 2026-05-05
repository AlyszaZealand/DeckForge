package feedback.deckforge.Service.RepoInterfaces;

import feedback.deckforge.Model.User;

import java.util.List;
import java.util.Optional;

public interface IUserRepository {

    void saveUser(User user);
    void deleteUser(int userID);
    void updateUserInformation(User user);
    List<User> findAllUsers();
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByID(int userID);


}
