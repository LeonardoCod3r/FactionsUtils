package centralworks.factionsutils.modules.listeners;

import centralworks.factionsutils.Main;
import centralworks.factionsutils.database.QueriesSync;
import centralworks.factionsutils.lib.EntityName;
import centralworks.factionsutils.lib.Movement;
import centralworks.factionsutils.modules.commons.InventoryMethods;
import centralworks.factionsutils.modules.spawnerstorage.SpawnerStorage;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.entity.MPlayerColl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import rush.sistemas.spawners.MobSpawner;

public class SpawnerStorageListeners implements Listener {

    @EventHandler
    public void command(PlayerCommandPreprocessEvent e) {
        final Player p = e.getPlayer();
        final MPlayer mPlayer = MPlayerColl.get().get(p);
        final String command = e.getMessage();
        if (command.equalsIgnoreCase("/f geradores") || command.equalsIgnoreCase("/f spawners")) {
            e.setCancelled(true);
            if (!mPlayer.hasFaction()) {
                p.sendMessage("§cVocê não possui uma facção no momento.");
                return;
            }
            final Faction faction = mPlayer.getFaction();
            QueriesSync.supply(SpawnerStorage.class, faction.getTag()).getObject().openInventory(p);
        } else if (command.equalsIgnoreCase("/f top") || command.equalsIgnoreCase("/f ranking")) {
            e.setCancelled(true);
            Main.getWorthRanking().openInventory(p);
        }
    }

    @EventHandler
    public void chat(AsyncPlayerChatEvent e) {
        final Player p = e.getPlayer();
        final MPlayer mPlayer = MPlayerColl.get().get(p);
        final String message = e.getMessage();
        final SpawnerStorage.EditGeradoresC cache = SpawnerStorage.EditGeradoresC.getInstance();
        if (!cache.hasPlayer(p)) return;
        e.setCancelled(true);
        if (message.equalsIgnoreCase("cancelar")) {
            cache.removePlayer(p);
            p.sendMessage("§cOperação cancelada com sucesso.");
            return;
        }
        final Object[] objects = cache.get(p);
        QueriesSync.supply(((SpawnerStorage) objects[1])).queue(spawnerStorage -> {
            if (spawnerStorage.next(mPlayer, Rel.OFFICER)) {
                final EntityType entityType = EntityType.valueOf(String.valueOf(objects[0]));
                try {
                    final int i = Integer.parseInt(message);
                    if (i <= 0) {
                        p.sendMessage("§cO número escrito não é válido.");
                        cache.removePlayer(p);
                        return;
                    }
                    if (spawnerStorage.getStorageAmount(entityType) < i) {
                        p.sendMessage("§cA sua facção não possui geradores de " + EntityName.valueOf(entityType).getName() + " suficientes.");
                        cache.removePlayer(p);
                        return;
                    }
                    if (!new InventoryMethods(p, MobSpawner.get(entityType.name(), 1), i).haveSpace()) {
                        p.sendMessage("§cO seu inventário não possui espaço suficiente.");
                        cache.removePlayer(p);
                        return;
                    }
                    if (spawnerStorage.run(Movement.REMOVE, p.getName(), entityType, i)) {
                        p.sendMessage("§aSpawners coletados com sucesso!");
                    }
                    cache.removePlayer(p);
                } catch (Exception ex) {
                    p.sendMessage("§cO número escrito não é válido.");
                }
            }
        });
    }

}
