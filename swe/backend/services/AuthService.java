package services;

import models.User;
import storage.FileStorage;
import utils.InputUtil;
import utils.UIFormatter;

import java.util.Map;
import java.util.Scanner;

public class AuthService {
    private Map<String, User> users;

    public AuthService() {
        this.users = FileStorage.loadUsers();
    }

    public void saveUsers() {
        FileStorage.saveUsers(users);
    }

    public User register(Scanner sc) {
        UIFormatter.printHeader("Registration");
        String username = InputUtil.getStringInput(sc, "Enter new username: ");
        if (users.containsKey(username)) {
            UIFormatter.printError("Username already exists!");
            return null;
        }
        String password = InputUtil.getStringInput(sc, "Enter new password: ");
        User newUser = new User(username, password);
        users.put(username, newUser);
        saveUsers();
        UIFormatter.printSuccess("Registration successful! You can now log in.");
        return null;
    }

    public User login(Scanner sc) {
        UIFormatter.printHeader("Login");
        String username = InputUtil.getStringInput(sc, "Enter username: ");
        if (!users.containsKey(username)) {
            UIFormatter.printError("User not found!");
            return null;
        }
        String password = InputUtil.getStringInput(sc, "Enter password: ");
        User user = users.get(username);
        if (user.checkPassword(password)) {
            UIFormatter.printSuccess("Login successful! Welcome, " + username + ".");
            return user;
        } else {
            UIFormatter.printError("Incorrect password.");
            return null;
        }
    }
}
