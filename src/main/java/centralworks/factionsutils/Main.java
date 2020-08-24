package centralworks.factionsutils;

import centralworks.factionsutils.database.DatabaseQueries;
import centralworks.factionsutils.database.QueriesSync;
import centralworks.factionsutils.lib.*;
import centralworks.factionsutils.modules.cmds.CmdConvert;
import centralworks.factionsutils.modules.cmds.CmdSetNpc;
import centralworks.factionsutils.modules.listeners.FactionListeners;
import centralworks.factionsutils.modules.listeners.SimpleListeners;
import centralworks.factionsutils.modules.listeners.SpawnerStorageListeners;
import centralworks.factionsutils.modules.ranking.*;
import centralworks.factionsutils.modules.spawnerstorage.SpawnerStorage;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.massivecraft.factions.entity.FactionColl;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Main extends JavaPlugin {

    private static Main me;
    private static Gson gson;
    private static Configuration configuration;
    private static Economy economy;
    private static final HashMap<EntityType, SpawnerDetails> spawnersDetails = Maps.newHashMap();
    private static SpawnerRanking spawnerRanking;
    private static SpawnerSRanking spawnerSRanking;
    private static SpawnerPRanking spawnerPRanking;
    private static WorthRanking worthRanking;
    private static BalanceRanking balanceRanking;
    private static KdrRanking kdrRanking;
    private static PowerRanking powerRanking;

    public static SpawnerRanking getSpawnerRanking() {
        return spawnerRanking;
    }

    public static BalanceRanking getBalanceRanking() {
        return balanceRanking;
    }

    public static SpawnerPRanking getSpawnerPRanking() {
        return spawnerPRanking;
    }

    public static KdrRanking getKdrRanking() {
        return kdrRanking;
    }

    public static SpawnerSRanking getSpawnerSRanking() {
        return spawnerSRanking;
    }

    public static PowerRanking getPowerRanking() {
        return powerRanking;
    }

    public static WorthRanking getWorthRanking() {
        return worthRanking;
    }

    public static HashMap<EntityType, SpawnerDetails> getSpawnersDetails() {
        return spawnersDetails;
    }

    public static Main get() {
        return me;
    }

    public static Gson getGson() {
        return gson == null ? gson = new Gson() : gson;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static Item getHead(EntityType entityType) {
        final SpawnerDetails info = Main.getSpawnersDetails().get(entityType);
        return new Item(Material.SKULL_ITEM, 1, (short) 3).setSkullOwner(info.getHead()).name("§e" + EntityName.valueOf(entityType).getName());
    }

    @Override
    public void onEnable() {
        me = this;
        configuration = new Configuration("config");
        economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        FormatBalance.suffix = configuration.getList("Settings.format", true).stream().map(s -> s.replace(s.split(",")[1], "").replace(",", "")).toArray(String[]::new);
        configuration.getList("Settings.format", true).forEach(s -> FormatBalance.format.put(s.split(",")[0], Double.valueOf(s.split(",")[1])));
        configuration.section("Spawners").forEach(s -> {
            final String path = "Spawners." + s + ".";
            spawnersDetails.put(EntityType.valueOf(s), new SpawnerDetails(configuration.get(path + "head", false), configuration.getDouble(path + "value")));
        });
        registerListeners(NPCManager.get(), new FactionListeners(), new SimpleListeners(), new SpawnerStorageListeners(), SpawnerStorage.EditGeradoresC.getInstance());
        ((CraftServer) getServer()).getCommandMap().register("setnpctop", new CmdSetNpc());
        ((CraftServer) getServer()).getCommandMap().register("storageconvert", new CmdConvert());
        DatabaseQueries.init();
        final QueriesSync<SpawnerStorage> q = QueriesSync.supply(SpawnerStorage.class);
        q.getDao().createTable();
        final List<SpawnerStorage> all = q.getDao().loadAll();
        all.forEach(SpawnerStorage::save);
        all.stream().filter(st -> FactionColl.get().getByTag(st.getTagOwner()) == null || FactionColl.get().getByTag(st.getTagOwner()).isNone()).forEach(SpawnerStorage::deepDelete);
        FactionColl.get().getAll(faction -> !Arrays.asList("ZNP", "ZNL", "ZNG", "BPD").contains(faction.getTag()) && faction.getMPlayers() != null).forEach(faction -> {
            if (all.stream().noneMatch(spawnerStorage -> spawnerStorage.getTagOwner().equals(faction.getTag())))
                new SpawnerStorage(faction.getTag()).deepSave();
        });
        spawnerRanking = new SpawnerRanking();
        spawnerPRanking = new SpawnerPRanking();
        spawnerSRanking = new SpawnerSRanking();
        worthRanking = new WorthRanking();
        balanceRanking = new BalanceRanking();
        kdrRanking = new KdrRanking();
        powerRanking = new PowerRanking();
        Arrays.asList(getSpawnerRanking(), getSpawnerPRanking(), getSpawnerSRanking(), getWorthRanking(), getKdrRanking(),
                getBalanceRanking(), getPowerRanking()).forEach(Ranking::update);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            final long a = System.currentTimeMillis();
            Bukkit.getScheduler().runTask(this, () -> getSpawnerRanking().update());
            Arrays.asList(getSpawnerPRanking(), getSpawnerSRanking(), getWorthRanking(), getKdrRanking(),
                    getBalanceRanking(), getPowerRanking()).forEach(Ranking::update);
            Bukkit.getConsoleSender().sendMessage("§cO ranking foi atualizado, tempo de atualização: " + (System.currentTimeMillis() - a) + "ms.");
        }, 200L, 20L * 60L * 10);
    }

    @Override
    public void onDisable() {
        final QueriesSync<SpawnerStorage> q = QueriesSync.supply(SpawnerStorage.class);
        q.getDao().saveAll();
        q.getDto().delete();
        NPCManager.get().removeAll();
    }

    private void registerListeners(Listener... listeners) {
        final PluginManager pm = Bukkit.getPluginManager();
        Arrays.stream(listeners).forEach(listener -> pm.registerEvents(listener, me));
    }

    @Data
    @RequiredArgsConstructor
    public static class SpawnerDetails {

        private String head;
        private Double price;

        public SpawnerDetails(String head, Double price) {
            this.head = head;
            this.price = price;
        }
    }
}
