package centralworks.factionsutils.modules.ranking;

import centralworks.factionsutils.Main;
import centralworks.factionsutils.lib.FormatBalance;
import centralworks.factionsutils.lib.InventoryBuilder;
import centralworks.factionsutils.lib.Item;
import centralworks.factionsutils.modules.commons.banners.Banners;
import com.google.common.collect.Lists;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.entity.MPlayerColl;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpawnerPRanking implements Ranking{

    public List<String> orderlyList;

    public List<String> getOrderlyList() {
        return orderlyList;
    }

    public void setOrderlyList(List<String> orderlyList) {
        this.orderlyList = orderlyList;
    }

    public RankingSupplier getSupplier(Faction faction) {
        return Main.getSpawnerRanking().getSupplier(faction);
    }

    public boolean has(Faction faction) {
        return getOrderlyList().stream().anyMatch(faction1 -> faction1.equalsIgnoreCase(faction.getTag()));
    }

    public void remove(Faction faction) {
        if (has(faction)) {
            List<String> map = Lists.newArrayList(getOrderlyList());
            map.remove(faction.getTag());
            setOrderlyList(map);
        }
    }

    public Integer getPosition(Faction faction) {
        if (!has(faction)) return 15000;
        return getOrderlyList().indexOf(faction.getTag()) + 1;
    }

    public Item getItem(Faction faction) {
        final RankingSupplier rs = getSupplier(faction);
        return new Item(Banners.getAlphabet(faction.getTag().substring(0, 1), DyeColor.BLACK, DyeColor.WHITE))
                .hideAttributes()
                .name("§f" + getPosition(faction) + "º. §8[" + faction.getTag() + "] " + faction.getName())
                .lore(rs.hasSpawners() ? "§fTotal de coins do(s) " + rs.getColocadosAmount() + " gerador(es): §2R$ §f" + FormatBalance.format(rs.getColocadosPrice()) : "§cNenhum spawner foi encontrado para essa facção.", "")
                .addLines(rs.getLoreColocados());
    }

    @Override
    public void update() {
        final HashMap<String, RankingSupplier> mapSorted = (HashMap<String, RankingSupplier>) Main.getSpawnerRanking().getOrderlyList().stream().filter(e -> e.getValue().getColocadosAmount() > 0).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        final List<String> novaLista = Lists.newArrayList();
        for (Map.Entry<String, RankingSupplier> e : mapSorted.entrySet().stream().sorted((o1, o2) -> o2.getValue().getColocadosPrice().compareTo(o1.getValue().getColocadosPrice())).collect(Collectors.toList())) {
            novaLista.add(e.getKey());
        }
        setOrderlyList(novaLista);
    }

    @Override
    public void openInventory(final Player p) {
        final MPlayer mPlayer = MPlayerColl.get().get(p);
        final InventoryBuilder builder = new InventoryBuilder(Main.get(), 6, "§8Ranking Geral - Geradores");
        final Faction faction = mPlayer.getFaction();
        builder.clear();
        builder.setCancellable(true);
        if (mPlayer.hasFaction() && has(faction)) builder.setItem(7, getItem(faction));
        else {
            builder.setItem(7, new Item(Material.PAPER).name("§cSem facção.").lore("§7Ranking da sua facção não foi encontrado."));
        }
        builder.setItem(1, Item.gray().name("§fValor").lore("§7Clique para acessar o ranking por valor.").onClick(event -> Main.getWorthRanking().openInventory(p)));
        builder.setItem(2, Item.gray().name("§fKDR").lore("§7Clique para acessar o ranking por KDR.").onClick(event -> Main.getKdrRanking().openInventory(p)));
        builder.setItem(3, Item.gray().name("§fCoins").lore("§7Clique para acessar o ranking por coins.").onClick(event -> Main.getBalanceRanking().openInventory(p)));
        builder.setItem(4, Item.green().name("§fGeradores").lore("§7Você está vendo esse ranking.").onClick(event -> Main.getSpawnerRanking().openInventory(p)));
        builder.setItem(5, Item.gray().name("§fPoder").lore("§7Clique para acessar o ranking por poder.").onClick(event -> Main.getPowerRanking().openInventory(p)));
        builder.setItem(26, Item.gray().name("§fTodos").lore("§7Clique para acessar o ranking por todos geradores.").onClick(event -> Main.getSpawnerRanking().openInventory(p)));
        builder.setItem(35, Item.green().name("§fColocados").lore("§7Você está vendo esse ranking.").onClick(event -> Main.getSpawnerPRanking().openInventory(p)));
        builder.setItem(44, Item.gray().name("§fArmazenados").lore("§7Clique para acessar o ranking por geradores armazenados.").onClick(event -> Main.getSpawnerSRanking().openInventory(p)));
        if (getOrderlyList().isEmpty())
            builder.setItem(31, new Item(Material.WEB).name("§cSem resultados").lore("§7Não foi encontrado nenhuma facção.").onClick(event -> p.closeInventory()));
        else {
            final List<Integer> slots = Arrays.asList(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);
            int count = 0;
            final int tamanho = Math.min(21, getOrderlyList().size());
            while (count < tamanho) {
                builder.setItem(slots.get(count), getItem(FactionColl.get().getByTag(getOrderlyList().get(count))));
                count++;
            }
        }
        builder.open(p);
    }

}
