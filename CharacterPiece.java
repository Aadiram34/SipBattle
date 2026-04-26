public class CharacterPiece {
    String name;
    String emoji;
    String symbol;
    AttackType type;
    Player owner;
    int row = -1;
    int col = -1;
    boolean deployed = false;
    boolean eliminated = false;

    public CharacterPiece(String name, String emoji, String symbol, AttackType type) {
        this.name = name;
        this.emoji = emoji;
        this.symbol = symbol;
        this.type = type;
    }
}