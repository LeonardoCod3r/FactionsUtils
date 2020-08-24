package centralworks.factionsutils.modules.listeners;

import centralworks.factionsutils.Main;
import centralworks.factionsutils.database.QueriesSync;
import centralworks.factionsutils.modules.spawnerstorage.SpawnerStorage;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.event.EventFactionsCreate;
import com.massivecraft.factions.event.EventFactionsDisband;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;

public class FactionListeners implements Listener {

    @EventHandler
    public void create(EventFactionsCreate e) {
        new SpawnerStorage(e.getFactionTag()).deepSave();
    }

    @EventHandler
    public void disband(EventFactionsDisband e) {
        final Faction faction = e.getFaction();
        QueriesSync.supply(SpawnerStorage.class, faction.getTag()).getObject().deepDelete();
        Arrays.asList(Main.getBalanceRanking(), Main.getPowerRanking(), Main.getWorthRanking(), Main.getSpawnerRanking(), Main.getKdrRanking(), Main.getSpawnerPRanking(), Main.getSpawnerSRanking()).forEach(ranking -> {
            if (ranking.has(faction)) ranking.remove(faction);
        });
    }
}
