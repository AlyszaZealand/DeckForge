package feedback.deckforge.Service;

import feedback.deckforge.Model.User;
import feedback.deckforge.Service.RepoInterfaces.IUserRepository;
import feedback.deckforge.Service.Validation.UserValidation;
import feedback.deckforge.Service.Validation.ValidationResult;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private IUserRepository userRepository;
    private UserValidation userValidation;

    public UserService(IUserRepository userRepository, UserValidation userValidation) {
        this.userRepository = userRepository;
        this.userValidation = userValidation;
    }

    public List<User> getAllUsers(){

        List<User> userList = userRepository.findAllUsers();

        return userList;
    }

    public Optional<User> getUserByEmail(String email){
        return userRepository.findUserByEmail(email);
    }

    public Optional<User> getUserByID(int userID){
        return userRepository.findUserByID(userID);
    }

    public void saveUser(User user){
        userRepository.saveUser(user);
    }

    public void deleteUser(User user){
        userRepository.deleteUser(user.getUserID());
    }


    public ValidationResult registerNewUser(User newUser){

        ValidationResult result = userValidation.validateRegisterUser(newUser);

        // 1. Return early if basic validation fails
        if (result.hasErrors()){
            return result;
        }

        // 2. Check if email is already taken
        Optional<User> existingUser = userRepository.findUserByEmail(newUser.getEmail());
        if (existingUser.isPresent()){
            result.addError("Denne e-mail er allerede i brug");
            return result;
        }

        // 3. Hash password and save
        String hashedPassword = BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt());
        newUser.setPassword(hashedPassword);
        userRepository.saveUser(newUser);

        return result;
    }

    public Optional<User> loginValidation(String email, String rawPassword) {
        Optional<User> userOptional = userRepository.findUserByEmail(email);

        if(userOptional.isPresent()){
            User loggedUser = userOptional.get();

            try{
                if(BCrypt.checkpw(rawPassword, loggedUser.getPassword())){
                    return Optional.of(loggedUser);
                } else {
                    return Optional.empty();
                }
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }


}
