package centralworks.factionsutils.modules.spawnerstorage;

import centralworks.factionsutils.database.ConnectionFactory;
import centralworks.factionsutils.database.QueriesSync;
import com.google.common.collect.Maps;
import com.massivecraft.factions.entity.FactionColl;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

public class Convert {

    public static Boolean run() {
        if (Bukkit.getPluginManager().isPluginEnabled("twFactionAddons")) {
            final Connection con = ConnectionFactory.make();
            try {
                assert con != null : "Ocorreu um erro, não foi possível se conectar com o banco de dados.";
                final PreparedStatement st = con.prepareStatement("SELECT * FROM Geradores;");
                final ResultSet rs = st.executeQuery();
                while (rs.next()) {
                    final String factionName = rs.getString("FactionName");
                    final String tag = FactionColl.get().getByName(factionName).getTag();
                    final HashMap<EntityType, Integer> map = convert(rs.getString("Spawners"));
                    QueriesSync.supply(SpawnerStorage.class, tag).queue(storage -> {
                        map.forEach(storage::addSpawnerData);
                        storage.deepSave();
                    }, exception -> {
                        final SpawnerStorage storage = new SpawnerStorage(tag);
                        map.forEach(storage::addSpawnerData);
                        storage.deepSave();
                    });
                }
                Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("twFactionAddons"));
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static HashMap<EntityType, Integer> convert(String json) {
        final String newValues = json.replace("{", "").replace("}", "").replace(" ", "").replace("\"", "");
        final HashMap<EntityType, Integer> map = Maps.newHashMap();
        if (newValues.equalsIgnoreCase("")) return map;
        Arrays.stream(newValues.split(",")).forEach(s -> map.put(EntityType.valueOf(s.split(":")[0]), Integer.valueOf(s.split(":")[1])));
        return map;
    }

}
