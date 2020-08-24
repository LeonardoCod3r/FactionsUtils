package centralworks.factionsutils.lib;

import centralworks.factionsutils.Main;
import centralworks.factionsutils.modules.ranking.WorthRanking;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.collect.Lists;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.LookClose;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Map;

public class NPCManager implements Listener {

    private Location first;
    private Location second;
    private Location third;

    private static NPCManager me;

    public static NPCManager get() {
        if (me == null) {
            me = new NPCManager();
            try {
                me.first = Main.getConfiguration().getLocationInConfig("npc1");
                me.second = Main.getConfiguration().getLocationInConfig("npc2");
                me.third = Main.getConfiguration().getLocationInConfig("npc3");
            } catch (Exception ignored) {
            }
        }
        return me;
    }

    public void update(WorthRanking ranking) {
        final List<Map.Entry<String, WorthRanking.Worth>> ol = ranking.getOrderlyList();
        final Map.Entry<String, WorthRanking.Worth> first = ol.size() >= 1 ? ol.get(0) : null;
        final Map.Entry<String, WorthRanking.Worth> second = ol.size() >= 2 ? ol.get(1) : null;
        final Map.Entry<String, WorthRanking.Worth> third = ol.size() >= 3 ? ol.get(2) : null;
        removeAll();
        final Economy eco = Main.getEconomy();
        if (first != null && this.first != null) {
            final Faction f = FactionColl.get().getByTag(first.getKey());
            spawn(this.first, f.getLeader().getName(), "§b1º Lugar", f.getColor() + "[" + f.getTag() + "] §e" + f.getName(), "", eco.format(first.getValue().getTotal()));
        }
        if (second != null && this.second != null) {
            final Faction f = FactionColl.get().getByTag(second.getKey());
            spawn(this.second, f.getLeader().getName(), "§b2º Lugar", f.getColor() + "[" + f.getTag() + "] §e" + f.getName(), "", eco.format(second.getValue().getTotal()));
        }
        if (third != null && this.third != null) {
            final Faction f = FactionColl.get().getByTag(third.getKey());
            spawn(this.third, f.getLeader().getName(), "§b3º Lugar", f.getColor() + "[" + f.getTag() + "] §e" + f.getName(), "", eco.format(third.getValue().getTotal()));
        }
    }

    public List<Integer> entities = Lists.newArrayList();

    public void removeAll() {
        entities.forEach(id -> CitizensAPI.getNPCRegistry().getById(id).destroy());
        entities.clear();
        HologramsAPI.getHolograms(Main.get()).forEach(Hologram::delete);
    }

    public NPC spawn(Location location, String npcName, String... lines) {
        final NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
        final NPC npc = npcRegistry.createNPC(EntityType.PLAYER, npcName);
        npc.setFlyable(true);
        npc.setProtected(true);
        npc.data().setPersistent("player-skin-name", npcName);
        npc.spawn(location);
        npc.setName("");
        npc.getTrait(LookClose.class).toggle();
        final Hologram hologram = HologramsAPI.createHologram(Main.get(), location.clone().add(0.0, 3.05, 0.0));
        Lists.newArrayList(lines).forEach(hologram::appendTextLine);
        this.entities.add(npc.getId());
        return npc;
    }

    public Location getThird() {
        return third;
    }

    public Location getSecond() {
        return second;
    }

    public Location getFirst() {
        return first;
    }

    public void setFirst(Location first) {
        this.first = first;
    }

    public void setSecond(Location second) {
        this.second = second;
    }

    public void setThird(Location third) {
        this.third = third;
    }
}
