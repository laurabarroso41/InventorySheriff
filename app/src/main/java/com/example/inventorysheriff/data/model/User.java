package com.example.inventorysheriff.data.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "user_sherif")
public class User {
    @DatabaseField(generatedId = true, columnName = "user_id")
    private Integer id ;
    @DatabaseField(columnName = "userName")
    private String userName;
    @DatabaseField(columnName = "password")
    private String password;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
