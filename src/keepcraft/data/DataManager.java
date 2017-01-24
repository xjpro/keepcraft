package keepcraft.data;

import java.util.Collection;
import java.util.logging.Logger;

public abstract class DataManager<T> {
    // For logging of course

    final static Logger logger = Logger.getLogger("Minecraft");

    // Database we'll be utilizing
    final Database database;

    DataManager(Database database) {
        this.database = database;
    }

    public abstract boolean exists(Object key);

    public abstract T getData(Object key);

    public abstract Collection<T> getAllData();

    public abstract void updateData(T value);

    public abstract void putData(T value);

    public abstract void deleteData(T value);

    public abstract void truncate();
}
