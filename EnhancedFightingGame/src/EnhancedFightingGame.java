import java.util.Scanner;
import java.util.Random;

public class EnhancedFightingGame {
    private static final int INITIAL_BLOOD = 10;
    private static final int DAMAGE_REDUCTION = 2;

    private static Player humanPlayer;
    private static Machine computerPlayer;
    private static boolean gameRunning = true;
    static Scanner scanner;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        initializeGame();

        while (gameRunning && humanPlayer.blood > 0 && computerPlayer.blood > 0) {
            clearScreen();
            displayGameStatus();

            if (computerPlayer.hand == 1) { // Player's turn to attack
                playerAttackPhase();
            } else { // Player's turn to defend
                computerAttackPhase();
            }

            computerPlayer.pass_hand();
        }

        displayFinalResult();
        scanner.close();
    }

    private static void initializeGame() {
        humanPlayer = new Player();
        computerPlayer = new Machine();
        humanPlayer.init_blood(INITIAL_BLOOD);
        computerPlayer.init_blood(INITIAL_BLOOD);

        System.out.println("\n=== WELCOME TO THE FIGHTING GAME ===");
        System.out.println("Prepare for battle! First to attack is randomly decided...");
        waitForEnter();

        computerPlayer.hand = new Random().nextInt(2);
        if (computerPlayer.hand == 0) {
            System.out.println("Computer starts first!");
        } else {
            System.out.println("You start first!");
        }
        waitForEnter();
    }

    private static void playerAttackPhase() {
        System.out.println("\n=== YOUR ATTACK TURN ===");
        displayFighters("attack");

        int attackChoice = humanPlayer.p_attack();
        if (attackChoice == 4) {
            gameRunning = false;
            return;
        }

        int defenseChoice = computerPlayer.defend();
        animateAttack(true, attackChoice);

        if (attackChoice != defenseChoice) {
            computerPlayer.update_blood(DAMAGE_REDUCTION);
            System.out.println("Your attack landed!");
        } else {
            System.out.println("Computer blocked your attack!");
        }

        waitForEnter();
    }

    private static void computerAttackPhase() {
        System.out.println("\n=== COMPUTER'S ATTACK TURN ===");
        displayFighters("defend");

        int attackChoice = computerPlayer.attack();
        int defenseChoice = humanPlayer.p_defend();
        if (defenseChoice == 4) {
            gameRunning = false;
            return;
        }

        animateAttack(false, attackChoice);

        if (attackChoice != defenseChoice) {
            humanPlayer.update_blood(DAMAGE_REDUCTION);
            System.out.println("Computer's attack hit you!");
        } else {
            System.out.println("You blocked the computer's attack!");
        }

        waitForEnter();
    }

    private static void displayGameStatus() {
        System.out.println("+---------------------------------------+");
        System.out.printf("| YOUR HEALTH: %-3d  |  COMPUTER HEALTH: %-3d |\n",
                humanPlayer.blood, computerPlayer.blood);
        System.out.println("+---------------------------------------+");

        System.out.print("YOU: ");
        displayHealthBar(humanPlayer.blood);
        System.out.print("CPU: ");
        displayHealthBar(computerPlayer.blood);
        System.out.println();
    }

    private static void displayHealthBar(int current) {
        int bars = (int) ((double) current / EnhancedFightingGame.INITIAL_BLOOD * 20);
        System.out.print("[");
        for (int i = 0; i < 20; i++) {
            if (i < bars) {
                System.out.print("▓");
            } else {
                System.out.print("░");
            }
        }
        System.out.println("] " + current + "/" + EnhancedFightingGame.INITIAL_BLOOD);
    }

    private static void displayFighters(String action) {
        if (action.equals("attack")) {
            System.out.println("  YOU: (ง •̀_•́)ง    vs    CPU: ヽ(•́o•̀)ﾉ");
        } else {
            System.out.println("  YOU: ヽ(•́o•̀)ﾉ    vs    CPU: (ง •̀_•́)ง");
        }
    }

    private static void animateAttack(boolean isPlayerAttacking, int move) {
        String[] playerAttacks = {
                " (ง •̀_•́)ง=====> KICK!",
                " (ง •̀_•́)ง=====> PUNCH!",
                " (ง •̀_•́)ง=====> SLAP!"
        };

        String[] computerAttacks = {
                "<=====ヽ(•́o•̀)ﾉ KICK!",
                "<=====ヽ(•́o•̀)ﾉ PUNCH!",
                "<=====ヽ(•́o•̀)ﾉ SLAP!"
        };

        String[] defenses = {
                " ヽ(•́o•̀)ﾉ ↑↑↑ JUMP!",
                " ヽ(•́o•̀)ﾉ ||| BLOCK!",
                " ヽ(•́o•̀)ﾉ ↓↓↓ BEND!"
        };

        try {
            System.out.println("\nAction:");
            if (isPlayerAttacking) {
                System.out.println(playerAttacks[move - 1]);
                Thread.sleep(500);
                System.out.println("          " + defenses[computerPlayer.defend() - 1]);
            } else {
                System.out.println("          " + computerAttacks[move - 1]);
                Thread.sleep(500);
                System.out.println(defenses[humanPlayer.p_defend() - 1]);
            }
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void displayFinalResult() {
        clearScreen();
        System.out.println("\n=== GAME OVER ===");
        System.out.println("Final Health:");
        System.out.printf("YOU: %d  |  COMPUTER: %d\n", humanPlayer.blood, computerPlayer.blood);

        if (humanPlayer.blood <= 0 && computerPlayer.blood <= 0) {
            System.out.println("It's a draw! Both fighters collapsed!");
            System.out.println(" (╯°□°)╯︵ ┻━┻");
        } else if (humanPlayer.blood > computerPlayer.blood) {
            System.out.println("CONGRATULATIONS! YOU WON!");
            System.out.println(" ヽ(^o^)ノ");
        } else {
            System.out.println("COMPUTER WINS! Better luck next time!");
            System.out.println(" (⌣_⌣”)");
        }
    }

    private static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println("\n\n\n\n\n\n\n\n\n\n");
        }
    }

    private static void waitForEnter() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}

class Player {
    public int blood;

    public void init_blood(int initial_blood) {
        this.blood = initial_blood;
    }

    public int p_attack() {
        System.out.println("\nChoose your attack:");
        System.out.println("1. Kick (Fast)");
        System.out.println("2. Punch (Medium)");
        System.out.println("3. Slap (Slow)");
        System.out.println("4. Quit Game");
        System.out.print("Enter choice (1-4): ");

        int choice;
        do {
            while (!EnhancedFightingGame.scanner.hasNextInt()) {
                System.out.println("Invalid input! Enter a number between 1-4");
                EnhancedFightingGame.scanner.next();
            }
            choice = EnhancedFightingGame.scanner.nextInt();
            EnhancedFightingGame.scanner.nextLine();
        } while (choice < 1 || choice > 4);

        return choice;
    }

    public int p_defend() {
        System.out.println("\nComputer is attacking! Choose your defense:");
        System.out.println("1. Jump (vs Kick)");
        System.out.println("2. Block (vs Punch)");
        System.out.println("3. Bend (vs Slap)");
        System.out.println("4. Quit Game");
        System.out.print("Enter choice (1-4): ");

        int choice;
        do {
            while (!EnhancedFightingGame.scanner.hasNextInt()) {
                System.out.println("Invalid input! Enter a number between 1-4");
                EnhancedFightingGame.scanner.next();
            }
            choice = EnhancedFightingGame.scanner.nextInt();
            EnhancedFightingGame.scanner.nextLine();
        } while (choice < 1 || choice > 4);

        return choice;
    }

    public void update_blood(int reduction) {
        this.blood = Math.max(0, this.blood - reduction);
    }
}

class Machine extends Player {
    public int hand = 0;
    private final Random random = new Random();

    public int attack() {
        return random.nextInt(3) + 1;
    }

    public int defend() {
        return random.nextInt(3) + 1;
    }

    public void pass_hand() {
        this.hand = (this.hand + 1) % 2;
    }
}

