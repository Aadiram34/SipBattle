import java.util.ArrayList;

public class Player {
    String name;
    boolean isHuman;
    ArrayList<CharacterPiece> characters = new ArrayList<>();
    CharacterPiece active;

    public Player(String name, boolean isHuman) {
        this.name = name;
        this.isHuman = isHuman;
    }

    public void add(CharacterPiece c) {
        characters.add(c);
    }

    public CharacterPiece nextAvailable() {
        for (CharacterPiece c : characters) {
            if (!c.deployed && !c.eliminated) {
                return c;
            }
        }
        return null;
    }

    public CharacterPiece nextAvailableDifferentFrom(CharacterPiece other) {
        for (CharacterPiece c : characters) {
            if (!c.deployed && !c.eliminated) {
                if (other == null || !c.name.equals(other.name)) {
                    return c;
                }
            }
        }
        return null;
    }

    public boolean hasCharactersLeft() {
        for (CharacterPiece c : characters) {
            if (!c.eliminated) {
                return true;
            }
        }
        return false;
    }
}