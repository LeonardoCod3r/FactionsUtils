package centralworks.factionsutils.modules.commons.banners;

import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;

public class Banners {
    public static ItemStack getAlphabet(String name, final DyeColor b, final DyeColor c) {
        final String s = name.substring(0, 1).toLowerCase();
        final String s2;
        final String s3;
        switch (s3 = (s2 = s)) {
            case "0": {
                return new Letter0().getBanner(b, c, true);
            }
            case "1": {
                return new Letter1().getBanner(b, c, true);
            }
            case "2": {
                return new Letter2().getBanner(b, c, true);
            }
            case "3": {
                return new Letter3().getBanner(b, c, true);
            }
            case "4": {
                return new Letter4().getBanner(b, c, true);
            }
            case "5": {
                return new Letter5().getBanner(b, c, true);
            }
            case "6": {
                return new Letter6().getBanner(b, c, true);
            }
            case "7": {
                return new Letter7().getBanner(b, c, true);
            }
            case "8": {
                return new Letter8().getBanner(b, c, true);
            }
            case "9": {
                return new Letter9().getBanner(b, c, true);
            }
            case "a": {
                return new LetterA().getBanner(b, c, true);
            }
            case "b": {
                return new LetterB().getBanner(b, c, true);
            }
            case "c": {
                return new LetterC().getBanner(b, c, true);
            }
            case "d": {
                return new LetterD().getBanner(b, c, true);
            }
            case "e": {
                return new LetterE().getBanner(b, c, true);
            }
            case "f": {
                return new LetterF().getBanner(b, c, true);
            }
            case "g": {
                return new LetterG().getBanner(b, c, true);
            }
            case "h": {
                return new LetterH().getBanner(b, c, true);
            }
            case "i": {
                return new LetterI().getBanner(b, c, true);
            }
            case "j": {
                return new LetterJ().getBanner(b, c, true);
            }
            case "k": {
                return new LetterK().getBanner(b, c, true);
            }
            case "l": {
                return new LetterL().getBanner(b, c, true);
            }
            case "m": {
                return new LetterM().getBanner(b, c, true);
            }
            case "n": {
                return new LetterN().getBanner(b, c, true);
            }
            case "o": {
                return new LetterO().getBanner(b, c, true);
            }
            case "p": {
                return new LetterP().getBanner(b, c, true);
            }
            case "q": {
                return new LetterQ().getBanner(b, c, true);
            }
            case "r": {
                return new LetterR().getBanner(b, c, true);
            }
            case "s": {
                return new LetterS().getBanner(b, c, true);
            }
            case "t": {
                return new LetterT().getBanner(b, c, true);
            }
            case "u": {
                return new LetterU().getBanner(b, c, true);
            }
            case "v": {
                return new LetterV().getBanner(b, c, true);
            }
            case "w": {
                return new LetterW().getBanner(b, c, true);
            }
            case "x": {
                return new LetterX().getBanner(b, c, true);
            }
            case "y": {
                return new LetterY().getBanner(b, c, true);
            }
            case "z": {
                return new LetterZ().getBanner(b, c, true);
            }
            default:
                break;
        }
        return null;
    }
}
