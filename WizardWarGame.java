import java.util.*;

abstract class GameObject {
    protected int x, y;

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

abstract class Player extends GameObject {
    protected String name;
    protected char symbol;
    protected List<Weapon> weapons = new ArrayList<>();

    public Player(String name, char symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public char getSymbol() {
        return symbol;
    }

    public abstract void takeTurn(Board board);

    public void addWeapon(Weapon weapon) {
        weapons.add(weapon);
    }

    public Weapon chooseWeapon() {
        if (weapons.isEmpty()) {
            System.out.println("No weapons available.");
            return null;
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose a weapon to use:");
        for (int i = 0; i < weapons.size(); i++) {
            System.out.println((i + 1) + ". " + weapons.get(i).getType());
        }
        int choice;
        do {
            System.out.print("Enter weapon number: ");
            choice = scanner.nextInt();
        } while (choice < 1 || choice > weapons.size());
        return weapons.remove(choice - 1);
    }

    public String getCurrentWeapons() {
        if (weapons.isEmpty()) return "No weapons";
        StringBuilder weaponList = new StringBuilder();
        for (Weapon weapon : weapons) {
            weaponList.append(weapon.getType()).append(", ");
        }
        return weaponList.substring(0, weaponList.length() - 2);
    }
}

class HumanPlayer extends Player {
    public HumanPlayer(String name, char symbol) {
        super(name, symbol);
    }

    @Override
    public void takeTurn(Board board) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(name + ", enter move (U, D, L, R): ");
        char move = scanner.next().charAt(0);
        int newX = x, newY = y;

        switch (move) {
            case 'U': newX--; break;
            case 'D': newX++; break;
            case 'L': newY--; break;
            case 'R': newY++; break;
        }

        if (board.isValidMove(newX, newY)) {
            board.movePlayer(this, newX, newY);
        } else {
            System.out.println("Invalid move!");
        }
    }
}

class Weapon extends GameObject {
    private String type;

    public Weapon(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public boolean stronger(Weapon other) {
        if (this.type.equals("Fireball") && other.type.equals("Sword")) return true;
        if (this.type.equals("Sword") && other.type.equals("Magic Ring")) return true;
        if (this.type.equals("Magic Ring") && other.type.equals("Fireball")) return true;
        return false;
    }
}

class Tree extends GameObject {
    public Tree(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

class Board {
    private final int size = 10;
    private GameObject[][] grid = new GameObject[size][size];

    public void placeObject(GameObject obj, int x, int y) {
        if (grid[x][y] == null) {
            grid[x][y] = obj;
            obj.setPosition(x, y);
        }
    }

    public GameObject getObjectAt(int x, int y) {
        return grid[x][y];
    }

    public void removeObject(int x, int y) {
        grid[x][y] = null;
    }

    public boolean isValidMove(int x, int y) {
        return x >= 0 && x < size && y >= 0 && y < size && !(grid[x][y] instanceof Tree);
    }

    public void movePlayer(Player player, int newX, int newY) {
        grid[player.getX()][player.getY()] = null;
        grid[newX][newY] = player;
        player.setPosition(newX, newY);
        checkInteraction(player);
    }

    public void resolveBattle(Player player1, Player player2) {
        Weapon weapon1 = player1.chooseWeapon();
        Weapon weapon2 = player2.chooseWeapon();

        System.out.println(player1.getName() + " used " + (weapon1 != null ? weapon1.getType() : "no weapon"));
        System.out.println(player2.getName() + " used " + (weapon2 != null ? weapon2.getType() : "no weapon"));

        if (weapon1 == null && weapon2 == null) {
            System.out.println("Both players have no weapons. No winner.");
            return;
        }

        if (weapon1 == null) {
            System.out.println(player1.getName() + " loses (no weapon)." );
            return;
        }

        if (weapon2 == null) {
            System.out.println(player2.getName() + " loses (no weapon)." );
            return;
        }

        if (weapon1.stronger(weapon2)) {
            System.out.println(player1.getName() + " wins the battle!");
            player1.addWeapon(weapon2);
        } else if (weapon2.stronger(weapon1)) {
            System.out.println(player2.getName() + " wins the battle!");
            player2.addWeapon(weapon1);
        } else {
            System.out.println("It's a draw. " + player2.getName() + " wins.");
        }
    }

    public void checkInteraction(Player player) {
        GameObject obj = getObjectAt(player.getX(), player.getY());
        if (obj instanceof Weapon) {
            Weapon weapon = (Weapon) obj;
            Scanner scanner = new Scanner(System.in);
            System.out.println("You found a " + weapon.getType() + ". Do you want to pick it up? (yes/no): ");
            String response = scanner.next();
            if (response.equalsIgnoreCase("yes")) {
                player.addWeapon(weapon);
                removeObject(player.getX(), player.getY());
                System.out.println(player.getName() + " picked up a " + weapon.getType());
            } else {
                System.out.println(player.getName() + " left the " + weapon.getType() + " on the ground.");
            }
        } else if (obj instanceof Player) {
            Player opponent = (Player) obj;
            resolveBattle(player, opponent);
        }
    }

    public void displayBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] instanceof Player) {
                    System.out.print(((Player) grid[i][j]).getSymbol() + " ");
                } else if (grid[i][j] instanceof Weapon) {
                    System.out.print("W ");
                } else if (grid[i][j] instanceof Tree) {
                    System.out.print("T ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println();
        }
    }
}

public class WizardWarGame {
    public static void main(String[] args) {
        Board board = new Board();
        List<Player> players = new ArrayList<>();

        Random rand = new Random();
        for (int i = 0; i < 3; i++) {
            int x, y;
            do {
                x = rand.nextInt(10);
                y = rand.nextInt(10);
            } while (board.getObjectAt(x, y) != null);
            board.placeObject(new Tree(x, y), x, y);
        }

        String[] weaponTypes = {"Sword", "Fireball", "Magic Ring"};
        for (String type : weaponTypes) {
            for (int i = 0; i < 2; i++) {
                int x, y;
                do {
                    x = rand.nextInt(10);
                    y = rand.nextInt(10);
                } while (board.getObjectAt(x, y) != null);
                board.placeObject(new Weapon(type), x, y);
            }
        }

        players.add(new HumanPlayer("Player1", '1'));
        players.add(new HumanPlayer("Player2", '2'));


        for (Player player : players) {
            int x, y;
            do {
                x = rand.nextInt(10);
                y = rand.nextInt(10);
            } while (board.getObjectAt(x, y) != null);
            board.placeObject(player, x, y);
        }

        while (players.size() > 1) {
            for (Iterator<Player> it = players.iterator(); it.hasNext(); ) {
                Player player = it.next();
                board.displayBoard();
                player.takeTurn(board);

                GameObject obj = board.getObjectAt(player.getX(), player.getY());
                if (obj instanceof Player && obj != player) {
                    Player opponent = (Player) obj;
                    board.resolveBattle(player, opponent);
                    if (!players.contains(opponent)) {
                        it.remove();
                    }
                }
            }
        }

        System.out.println("Game over! Winner: " + players.get(0).getName());
    }
}
