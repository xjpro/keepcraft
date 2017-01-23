package keepcraft.services;

import keepcraft.data.UserDataManager;
import keepcraft.data.models.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class UserService {

    private UserDataManager userDataManager = new UserDataManager();
    private Collection<User> onlineUsers = new ArrayList<>();

    UserService() {
    }

    void refreshCache() {
        onlineUsers.clear();
    }

    public Collection<User> getOnlineUsers() {
        return onlineUsers;
    }

    public boolean userIsRegistered(String name) {
        return userDataManager.exists(name);
    }

    public User getOnlineUser(String name) {
        Optional<User> userOrNull = onlineUsers.stream()
                .filter(plot -> plot.getName().equals(name))
                .findFirst();
        return userOrNull.isPresent() ? userOrNull.get() : null;
    }

    public User getOnlineUser(Integer id) {
        Optional<User> userOrNull = onlineUsers.stream()
                .filter(plot -> plot.getId() == id)
                .findFirst();
        return userOrNull.isPresent() ? userOrNull.get() : null;
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

    public boolean removeUser(User user) {
        if (!userDataManager.exists(user.getName())) {
            return false;
        }
        userDataManager.deleteData(user);
        return true;
    }
}
