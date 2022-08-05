package com.example.inventorysheriff.data;

import android.content.Context;
import android.util.Log;
import com.example.inventorysheriff.data.model.DatabaseHelper;
import com.example.inventorysheriff.data.model.LoggedInUser;
import com.example.inventorysheriff.data.model.User;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password, Context context) {

        try {
            DatabaseHelper helper = new DatabaseHelper(context);
            try {
                Map<String, Object> values = new HashMap<>();
                values.put("userName",username);
                values.put("password",password);
                List<User> users = helper.getUserSheriffDao().queryForFieldValues(values);
                if(users.isEmpty()){
                    return new Result.Error(new IOException("Error logging in", new Throwable()));
                }else{
                    User u = users.get(0);
                    LoggedInUser loggedInUser = new LoggedInUser(String.valueOf(u.getId()),
                            u.getUserName());
                    return new Result.Success<>(loggedInUser);
                }

            }catch (Exception e){
                Log.e("ERROR",e.getMessage());
                return new Result.Error(new IOException("Error logging in", e));
            }

        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}