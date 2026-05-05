package feedback.deckforge.Model;

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
        this.email = email;
        this.username = username;
        this.userRole = userRole;
        this.password = password;
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
