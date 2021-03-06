package centralworks.factionsutils.database;

import centralworks.factionsutils.Main;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class JSONRead<T> {

    private Class<T> result;
    private T object;
    private String fileName;

    public JSONRead(Class<T> result, String fileName) {
        this.result = result;
        this.fileName = fileName;
        read();
    }

    private void read() {
        try {
            final File database = new File(Main.get().getDataFolder(), "database");
            final File dir = new File(database, result.getSimpleName());
            final File file = new File(dir, fileName + ".json");
            final FileReader reader = new FileReader(file);
            object = Main.getGson().fromJson(reader, result);
            reader.close();
        } catch (IOException ignored) {
        }
    }

    public T getObject() {
        return object;
    }

    public Class<T> getResult() {
        return result;
    }
}
