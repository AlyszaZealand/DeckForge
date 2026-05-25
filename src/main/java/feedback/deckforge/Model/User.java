package feedback.deckforge.Model;

import feedback.deckforge.Exceptions.UserEmailNotValid;
import feedback.deckforge.Exceptions.UserNameNotValid;
import feedback.deckforge.Exceptions.UserPasswordNotValid;
import feedback.deckforge.Model.Enum.UserRole;

public class User {

    private int userID;
    private String username;
    private String email;
    private UserRole userRole;
    private String password;

    public User() {}

    public User (int userID,String username, String email, UserRole userRole, String password){
        this.userID = userID;
        validateEmail(email);
        this.email = email;
        validateUsername(username);
        this.username = username;
        this.userRole = userRole;
        validatePassword(password);
        this.password = password;
    }

    public void validateUsername(String username){
        if (username == null || username.isEmpty()){
            throw new UserNameNotValid("Brugernavn må ikke være tom");
        }
    }

    public void validateEmail(String email){
        if (email == null || email.isEmpty()){
            throw new UserEmailNotValid("Email'en må ikke være tom");
        }
    }

    public void validatePassword(String password){
        if (password == null || password.isEmpty()){
            throw new UserPasswordNotValid("Password må ikke være tom");
        }
    }

    public String getUsername(){
        return this.username;
    }
    public void setUsername (String username){
        this.username = username;
    }

    public String getEmail(){
        return this.email;
    }
    public void setEmail(String email){
        this.email = email;
    }

    public UserRole getUserRole(){
        return this.userRole;
    }
    public void setUserRole (UserRole userRole){
        this.userRole = userRole;
    }

    public String getPassword(){
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public int getUserID(){
        return this.userID;
    }
    public void setUserID(int userID){
        this.userID = userID;
    }



}
