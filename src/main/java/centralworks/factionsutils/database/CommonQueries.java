package centralworks.factionsutils.database;

import centralworks.factionsutils.Main;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommonQueries<T> {

    private final Class<T> clazz;

    public Class<T> getClazz() {
        return clazz;
    }

    public CommonQueries(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void write(T object) {
        final String id = ((Identifier) object).getIdentifier();
        new JSONWrite<>(object, id);
    }

    public T read(String id) {
        return new JSONRead<>(getClazz(), id).getObject();
    }

    public T read(Integer id) {
        return read(id.toString());
    }

    public boolean exists(String id) {
        return JsonFiles.getInstance().exists(id);
    }

    public boolean exists(Integer id) {
        return exists(id.toString());
    }

    public List<T> loadAllFiles() {
        final List<T> ts = new DatabaseQueries<>(clazz).loadAll();
        ts.forEach(t -> {
            final String id = ((Identifier) t).getIdentifier();
            Bukkit.getScheduler().runTask(Main.get(), () -> read(id));
        });
        return ts;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void delete(String id) {
        final File database = new File(Main.get().getDataFolder(), "database");
        final File dir = new File(database, getClazz().getSimpleName());
        new File(dir, id + ".json").delete();
        JsonFiles.getInstance().remove(id);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void delete(File file) {
        JsonFiles.getInstance().remove(file.getName().split(".json")[0]);
        file.delete();
    }

    public void delete(Integer id) {
        delete(id.toString());
    }

    public void delete() {
        try {
            final File database = new File(Main.get().getDataFolder(), "database");
            final File dir = new File(database, getClazz().getSimpleName());
            Arrays.stream(Objects.requireNonNull(dir.listFiles())).forEach(this::delete);
        } catch (NullPointerException ignored) {
        }
    }

    public List<T> findAllFiles() {
        final File database = new File(Main.get().getDataFolder(), "database");
        final File dir = new File(database, getClazz().getSimpleName());
        return Arrays.stream(Objects.requireNonNull(dir.listFiles())).map(file -> new JSONRead<>(getClazz(), file.getName().split(".json")[0]).getObject()).collect(Collectors.toList());
    }

}
