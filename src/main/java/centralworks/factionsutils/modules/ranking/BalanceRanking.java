package centralworks.factionsutils.modules.ranking;

import centralworks.factionsutils.Main;
import centralworks.factionsutils.lib.InventoryBuilder;
import centralworks.factionsutils.lib.Item;
import centralworks.factionsutils.modules.commons.banners.Banners;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.entity.MPlayerColl;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class BalanceRanking implements Ranking{

    public List<Map.Entry<String, BalanceInfo>> orderlyList;

    public Item getItem(Faction faction) {
        final Economy eco = Main.getEconomy();
        final BalanceInfo supplier = getSupplier(faction);
        return new Item(Banners.getAlphabet(faction.getTag().substring(0, 1), DyeColor.BLACK, DyeColor.WHITE))
                .hideAttributes()
                .name("§f" + getPosition(faction) + "º. §8[" + faction.getTag() + "] " + faction.getName())
                .lore(
                        "§fCoins totais: §2R$ §7" + eco.format(supplier.getTotal()),
                        ""
                ).addLines(supplier.getLore());
    }

    public void remove(Faction faction) {
        if (has(faction)) {
            final ArrayList<Map.Entry<String, BalanceInfo>> map = Lists.newArrayList(getOrderlyList());
            map.remove(get(faction.getTag()));
            setOrderlyList(map);
        }
    }

    @Override
    public void update() {
        final Economy eco = Main.getEconomy();
        final List<Faction> sorted = FactionColl.get().getAll(faction -> faction.getMPlayers().size() > 0 && !Arrays.asList("ZNP", "ZNL", "ZNG", "BPD").contains(faction.getTag()));
        final HashMap<String, BalanceInfo> map = Maps.newHashMap();
        for (Faction faction : sorted) {
            final BalanceInfo balanceInfo = new BalanceInfo((HashMap<String, Double>) faction.getMPlayers().stream().collect(Collectors.toMap(MPlayer::getName, mPlayer -> eco.getBalance(mPlayer.getName()))));
            map.put(faction.getTag(), balanceInfo);
        }
        final List<Map.Entry<String, BalanceInfo>> entries = Lists.newArrayList(map.entrySet());
        entries.sort((o1, o2) -> o2.getValue().getTotal().compareTo(o1.getValue().getTotal()));
        setOrderlyList(entries);
    }

    public boolean has(Faction faction) {
        return getOrderlyList().stream().anyMatch(faction1 -> faction1.getKey().equalsIgnoreCase(faction.getTag()));
    }

    public BalanceInfo getSupplier(Faction faction) {
        return get(faction.getTag()).getValue();
    }

    @Override
    public void openInventory(Player p) {
        final MPlayer mPlayer = MPlayerColl.get().get(p);
        final InventoryBuilder builder = new InventoryBuilder(Main.get(), 6, "§8Ranking Geral - Coins");
        final Faction faction = mPlayer.getFaction();
        builder.clear();
        builder.setCancellable(true);
        if (mPlayer.hasFaction() && has(faction)) builder.setItem(7, getItem(faction));
        else
            builder.setItem(7, new Item(Material.PAPER).name("§cSem facção.").lore("§7Ranking da sua facção não foi encontrado."));
        builder.setItem(1, Item.gray().name("§fValor").lore("§7Clique para acessar o ranking por valor.").onClick(event -> Main.getWorthRanking().openInventory(p)));
        builder.setItem(2, Item.gray().name("§fKDR").lore("§7Clique para acessar o ranking por KDR.").onClick(event -> Main.getKdrRanking().openInventory(p)));
        builder.setItem(3, Item.green().name("§fCoins").lore("§7Você está vendo esse ranking.").onClick(event -> Main.getBalanceRanking().openInventory(p)));
        builder.setItem(4, Item.gray().name("§fGeradores").lore("§7Clique para acessar o ranking por geradores.").onClick(event -> Main.getSpawnerRanking().openInventory(p)));
        builder.setItem(5, Item.gray().name("§fPoder").lore("§7Clique para acessar o ranking por poder.").onClick(event -> Main.getPowerRanking().openInventory(p)));
        if (getOrderlyList().isEmpty())
            builder.setItem(31, new Item(Material.WEB).name("§cSem resultados").lore("§7Não foi encontrado nenhuma facção.").onClick(event -> p.closeInventory()));
        else {
            final List<Integer> slots = Arrays.asList(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
            int count = 0;
            final int tamanho = Math.min(21, getOrderlyList().size());
            while (count < tamanho) {
                final Faction faction1 = FactionColl.get().getByTag(getOrderlyList().get(count).getKey());
                builder.setItem(slots.get(count), getItem(faction1));
                count++;
            }
        }
        builder.open(p);
    }

    public Map.Entry<String, BalanceInfo> get(String factionTag) {
        return getOrderlyList().stream().filter(entr -> entr.getKey().equalsIgnoreCase(factionTag)).findFirst().get();
    }

    @Override
    public Integer getPosition(Faction faction) {
        return getOrderlyList().indexOf(get(faction.getTag())) + 1;
    }

    @Data
    @RequiredArgsConstructor
    public static class BalanceInfo implements Comparable<BalanceInfo> {

        private HashMap<String, Double> playersAccounts;

        public BalanceInfo(HashMap<String, Double> playersAccounts) {
            this.playersAccounts = playersAccounts;
        }

        public List<String> getLore() {
            final Economy eco = Main.getEconomy();
            return getPlayersAccounts().keySet().stream().map(s -> "§f" + s + ": §7" + eco.format(getPlayersAccounts().get(s))).collect(Collectors.toList());
        }

        public Double getTotal() {
            return getPlayersAccounts().values().stream().reduce(Double::sum).orElse(0.0);
        }

        @Override
        public int compareTo(BalanceInfo o) {
            return o.getTotal().compareTo(getTotal());
        }
    }


}
