package centralworks.factionsutils.database;

import centralworks.factionsutils.Main;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class DatabaseQueries<T> {

    private final Class<T> clazz;
    private static Connection connection;
    private String id;
    private String table;
    private Boolean autoIncrement;

    public DatabaseQueries(Class<T> clazz) {
        this.clazz = clazz;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public static void init() {
        if (connection == null) connection = ConnectionFactory.make();
    }

    @SneakyThrows
    public void createTable() {
        try {
            final Field id = getClazz().getDeclaredField(id());
            final boolean autoIncrement = id.getAnnotation(Key.class).autoIncrement();
            final String dataType = id.getAnnotation(DataType.class).dataType();
            final String query = "CREATE TABLE IF NOT EXISTS " + table() +
                    " (" + id() + " " + dataType + " " + (autoIncrement ? "AUTO_INCREMENT " : "") + ", value LONGTEXT, PRIMARY KEY(" + id() + "))";
            final PreparedStatement st = connection.prepareStatement(query);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void save(T object) {
        try {
            final Field field = getClazz().getDeclaredField(id());
            field.setAccessible(true);
            final String value = Main.getGson().toJson(object);
            final String query = "INSERT INTO " + table() + " VALUES(?,?) ON DUPLICATE KEY UPDATE " + id() + " = ?, value = ?";
            final PreparedStatement st = connection.prepareStatement(query);
            if (field.getType() == Integer.class) {
                st.setInt(1, field.getInt(object));
                st.setInt(3, field.getInt(object));
            } else {
                st.setString(1, String.valueOf(field.get(object)));
                st.setString(3, String.valueOf(field.get(object)));
            }
            st.setString(2, value);
            st.setString(4, value);
            st.executeUpdate();
            if (autoIncrement()) {
                final ResultSet rs = st.getGeneratedKeys();
                final int anInt = rs.getInt(1);
                field.set(object, anInt);
            }
        } catch (SQLException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void saveAll() {
        new CommonQueries<>(getClazz()).findAllFiles().forEach(object -> {
            try {
                final Field field = getClazz().getDeclaredField(id());
                field.setAccessible(true);
                final String value = Main.getGson().toJson(object);
                final String query = "INSERT INTO " + table() + " VALUES(?,?) ON DUPLICATE KEY UPDATE " + id() + " = ?, value = ?";
                final PreparedStatement st = connection.prepareStatement(query);
                if (field.getType() == Integer.class) {
                    st.setInt(1, field.getInt(object));
                    st.setInt(3, field.getInt(object));
                } else {
                    st.setString(1, String.valueOf(field.get(object)));
                    st.setString(3, String.valueOf(field.get(object)));
                }
                st.setString(2, value);
                st.setString(4, value);
                st.executeUpdate();
                if (autoIncrement()) {
                    final ResultSet rs = st.getGeneratedKeys();
                    final int anInt = rs.getInt(1);
                    field.set(object, anInt);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public T load(String id) {
        try {
            final String query = "SELECT * FROM " + table() + " WHERE " + id() + " = ?";
            final PreparedStatement st = connection.prepareStatement(query);
            st.setString(1, id);
            final ResultSet rs = st.executeQuery();
            rs.next();
            return Main.getGson().fromJson(rs.getString("value"), getClazz());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public T load(Integer id) {
        try {
            final String query = "SELECT * FROM " + table() + " WHERE " + id() + " = ?";
            final PreparedStatement st = connection.prepareStatement(query);
            st.setInt(1, id);
            final ResultSet rs = st.executeQuery();
            rs.next();
            return Main.getGson().fromJson(rs.getString("value"), getClazz());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<T> loadAll() {
        final List<T> list = Lists.newArrayList();
        try {
            final String query = "SELECT * FROM " + table();
            final PreparedStatement st = connection.prepareStatement(query);
            final ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(Main.getGson().fromJson(rs.getString("value"), getClazz()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean exists(String id) {
        try {
            final String query = "SELECT * FROM " + table() + " WHERE " + id() + " = ?";
            final PreparedStatement st = connection.prepareStatement(query);
            st.setString(1, id);
            final ResultSet rs = st.executeQuery();
            return rs.next();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean exists(Integer id) {
        try{
            final String query = "SELECT * FROM " + table() + " WHERE " + id() + " = ?";
            final PreparedStatement st = connection.prepareStatement(query);
            st.setInt(1, id);
            final ResultSet rs = st.executeQuery();
            return rs.next();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    public void delete(String id) {
        try{
            final String query = "DELETE FROM " + table() + " WHERE " + id() + " = ?";
            final PreparedStatement st = connection.prepareStatement(query);
            st.setString(1, id);
            st.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void delete(Integer id) {
        try {
            final String query = "DELETE FROM " + table() + " WHERE " + id() + " = ?";
            final PreparedStatement st = connection.prepareStatement(query);
            st.setInt(1, id);
            st.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void delete() {
        try {
            final String query = "DELETE FROM " + table();
            final PreparedStatement st = connection.prepareStatement(query);
            st.executeUpdate();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public List<T> findAll() {
        final List<T> list = Lists.newArrayList();
        try {
            final String query = "SELECT * FROM " + table();
            final PreparedStatement st = connection.prepareStatement(query);
            final ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(Main.getGson().fromJson(rs.getString("value"), getClazz()));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return list;
    }

    public String table() {
        if (table == null) table = getClazz().getDeclaredAnnotation(Table.class).name();
        return table;
    }

    public String id() {
        if (id == null)
            id = Arrays.stream(getClazz().getDeclaredFields()).filter(f -> f.isAnnotationPresent(Key.class)).findFirst().get().getName();
        return id;
    }

    public Boolean autoIncrement() {
        try {
            if (autoIncrement == null)
                autoIncrement = getClazz().getDeclaredField(id()).getAnnotation(Key.class).autoIncrement();
            return autoIncrement;
        }catch (Exception ignored){
        }
        return false;
    }

}
