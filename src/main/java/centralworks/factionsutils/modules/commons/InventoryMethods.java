package centralworks.factionsutils.modules.commons;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Data
@RequiredArgsConstructor
public class InventoryMethods {

    private Player p;
    private ItemStack itemStack;
    private Integer amount;

    public InventoryMethods(Player p, ItemStack itemStack, Integer amount) {
        this.p = p;
        this.itemStack = itemStack;
        this.amount = amount;
    }

    public void andRemove() {
        for (int slot = 0; slot < 36; slot++) {
            final ItemStack content = p.getInventory().getItem(slot);
            if (amount <= 0) return;
            if (content == null) continue;
            if (content.isSimilar(itemStack)) {
                if (amount >= content.getAmount()) {
                    amount -= content.getAmount();
                    p.getInventory().setItem(slot, new ItemStack(Material.AIR));
                } else {
                    content.setAmount(content.getAmount() - amount);
                    return;
                }
            }
        }
    }

    public boolean haveSpace() {
        final int stackMax = itemStack.getMaxStackSize();
        for (ItemStack itemStack1 : p.getInventory().getContents()) {
            if (itemStack1 == null) {
                amount -= stackMax;
                continue;
            }
            if (itemStack1.isSimilar(itemStack)) {
                amount -= stackMax - itemStack1.getAmount();
            }
        }
        return amount <= 0;
    }

    public void andGive() {
        final int stackMax = itemStack.getMaxStackSize();
        for (ItemStack itemStack1 : p.getInventory().getContents()) {
            if (amount == 0) return;
            if (itemStack1 == null) continue;
            if (itemStack.isSimilar(itemStack1) && itemStack1.getAmount() < stackMax) {
                if (itemStack1.getAmount() + amount <= stackMax) {
                    itemStack1.setAmount(itemStack1.getAmount() + amount);
                    return;
                }
                amount = amount + itemStack1.getAmount() - stackMax;
                itemStack1.setAmount(stackMax);
            }
        }
        if (amount == 0) return;
        if (amount <= stackMax) {
            itemStack.setAmount(amount);
            if (p.getInventory().firstEmpty() != -1) {
                p.getInventory().addItem(itemStack);
            } else p.getWorld().dropItem(p.getLocation(), itemStack);
            return;
        }
        final int items = amount / stackMax;
        final int rest = amount % stackMax;
        final ItemStack newItemStack = itemStack.clone();
        newItemStack.setAmount(rest);
        if (p.getInventory().firstEmpty() != -1) {
            p.getInventory().addItem(newItemStack);
        } else p.getWorld().dropItem(p.getLocation(), newItemStack);
        for (int item = items; item > 0; item--) {
            final ItemStack newItem = itemStack.clone();
            newItem.setAmount(stackMax);
            if (p.getInventory().firstEmpty() != -1) {
                p.getInventory().addItem(newItem);
            } else p.getWorld().dropItem(p.getLocation(), newItem);
        }
    }

}
