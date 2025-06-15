class Player {
    private final String name;
    private int health;
    private final int attackPower;

    // Constructor
    public Player(String name, int health, int attackPower) {
        this.name = name;
        this.health = health;
        this.attackPower = attackPower;
    }

    // Method to display player info
    public void displayInfo() {
        System.out.println("Player: " + name + ", Health: " + health + ", Attack Power: " + attackPower);
    }

    // Method to simulate attack
    public int attack() {
        return attackPower;
    }

    // Method to take damage
    public void takeDamage(int damage) {
        health -= damage;
    }

    // Method to check if the player is alive
    public boolean isAlive() {
        return health > 0;
    }
}

class Machine {
    private int health;
    private final int attackPower;

    // Constructor
    public Machine(int health, int attackPower) {
        this.health = health;
        this.attackPower = attackPower;
    }

    // Method to display machine info
    public void displayInfo() {
        System.out.println("Machine, Health: " + health + ", Attack Power: " + attackPower);
    }

    // Method to simulate attack
    public int attack() {
        return attackPower;
    }

    // Method to take damage
    public void takeDamage(int damage) {
        health -= damage;
    }

    // Method to check if the machine is alive
    public boolean isAlive() {
        return health > 0;
    }
}

class FightingGame {
    public static void main(String[] args) {
        Player player1 = new Player("Hero", 100, 20);
        Machine machine1 = new Machine(80, 15);

        // Combat loop
        while (player1.isAlive() && machine1.isAlive()) {
            // Player attacks the machine
            int playerDamage = player1.attack();
            machine1.takeDamage(playerDamage);

            // Machine attacks the player
            int machineDamage = machine1.attack();
            player1.takeDamage(machineDamage);
        }

        // Display final results
        if (!machine1.isAlive() && !player1.isAlive()) {
            System.out.println("It's a draw! Both the player and the machine are defeated.");
        } else if (!machine1.isAlive()) {
            System.out.println("Machine is defeated! Player wins!");
        } else {
            System.out.println("Player is defeated! Machine wins!");
        }

        // Display final stats
        player1.displayInfo();
        machine1.displayInfo();
    }
}