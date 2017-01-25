package keepcraft.services;

import keepcraft.data.UserDataManager;
import keepcraft.data.models.*;

import java.util.Collection;
import java.util.HashMap;

public class UserService {

    private final UserDataManager userDataManager;
    private HashMap<String, User> onlineUsers = new HashMap<>();

    public UserService(UserDataManager userDataManager) {
        this.userDataManager = userDataManager;
    }

    public void refreshCache() {
        onlineUsers.clear();
    }

    public Collection<User> getOnlineUsers() {
        return onlineUsers.values();
    }

    public boolean userIsRegistered(String name) {
        return userDataManager.exists(name);
    }

    public User getOnlineUser(String name) {
        return onlineUsers.get(name);
    }

    public User loadOfflineUser(String name) {
        User user = userDataManager.getData(name);
        onlineUsers.put(user.getName(), user);
        return user;
    }

    public void setUserOffline(User user) {
        userDataManager.updateData(user);
        onlineUsers.remove(user.getName());
    }

    public void updateUser(User user) {
        userDataManager.updateData(user);
    }

    public boolean removeUser(User user) {
        if (!userDataManager.exists(user.getName())) {
            return false;
        }
        userDataManager.deleteData(user);
        return true;
    }
}
