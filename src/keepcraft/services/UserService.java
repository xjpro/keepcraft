package keepcraft.services;

import keepcraft.data.UserDataManager;
import keepcraft.data.models.*;

import java.util.ArrayList;
import java.util.Collection;

public class UserService {

    private UserDataManager userDataManager = new UserDataManager();
    private Collection<User> onlineUsers = new ArrayList<>();

    UserService() {
    }

    public Collection<User> getOnlineUsers() {
        return onlineUsers;
    }

    public boolean userIsRegistered(String name) {
        return userDataManager.exists(name);
    }

    public User getOnlineUser(String name) {
        return onlineUsers.stream()
                .filter(plot -> plot.getName().equals(name))
                .findFirst()
                .get();
    }

    public User getOnlineUser(Integer id) {
        return onlineUsers.stream()
                .filter(plot -> plot.getId() == id)
                .findFirst()
                .get();
    }

    public User loadOfflineUser(String name) {
        User user = userDataManager.getData(name);
        onlineUsers.add(user);
        return user;
    }

    public void setUserOffline(User user) {
        userDataManager.updateData(user);
        onlineUsers.remove(user);
    }

    public void updateUser(User user) {
        userDataManager.updateData(user);
    }
}
