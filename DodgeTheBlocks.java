import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class DodgeTheBlocks extends JFrame implements KeyListener {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 400;
    private static final int PLAYER_SIZE = 20;
    private static final int BLOCK_SIZE = 20;
    private static final int[] LEVEL_SPEEDS = {3, 5, 7}; // Speeds for levels 1, 2, and 3
    private static final int[] LEVEL_BLOCK_THRESHOLDS = {60, 250, 500}; // Blocks required to win each level
    private static final int[] LEVEL_DURATIONS = {120000, 240000, 450000}; // Durations for levels 1, 2, and 3

    private JLabel statusLabel;
    private JLabel scoreLabel;
    private JLabel highScoreLabel;
    private Timer timer;
    private boolean gameStarted;
    private int playerX, playerY;
    private ArrayList<Block> blocks;
    private boolean leftPressed, rightPressed;
    private int level;
    private int blockSpeed;
    private int blockThreshold;
    private int levelDuration;
    private long startTime;
    private int score;
    private int highScore;

    public DodgeTheBlocks() {
        setTitle("Dodge The Blocks");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Color.BLACK); // Set background color to black

        statusLabel = new JLabel("Press Spacebar to Start");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setVerticalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(Color.BLUE); // Set text color to blue
        statusLabel.setFont(new Font("Arial", Font.BOLD, 20)); // Set font and size
        add(statusLabel, BorderLayout.CENTER);

        scoreLabel = new JLabel();
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setForeground(Color.WHITE); // Set text color to white
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Set font and size
        add(scoreLabel, BorderLayout.SOUTH);

        highScoreLabel = new JLabel();
        highScoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        highScoreLabel.setForeground(Color.YELLOW); // Set text color to yellow
        highScoreLabel.setFont(new Font("Arial", Font.PLAIN, 14)); // Set font and size
        add(highScoreLabel, BorderLayout.NORTH);

        addKeyListener(this);

        playerX = WIDTH / 2 - PLAYER_SIZE / 2;
        playerY = HEIGHT - PLAYER_SIZE - 10;

        blocks = new ArrayList<>();

        timer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameStarted) {
                    movePlayer();
                    moveBlocks();
                    checkCollision();
                    repaint();
                    checkLevelDuration();
                }
            }
        });

        loadHighScore(); // Load high score from file
        updateHighScoreLabel(); // Display high score

        setVisible(true);
    }

    private void movePlayer() {
        if (leftPressed && playerX > 0) {
            playerX -= 3;
        }
        if (rightPressed && playerX < WIDTH - PLAYER_SIZE) {
            playerX += 3;
        }
    }

    private void moveBlocks() {
        for (Block block : blocks) {
            block.setY(block.getY() + blockSpeed);
            if (block.getY() > HEIGHT) {
                score++; // Increment score when a block passes the player
            }
        }
        if (blocks.size() < blockThreshold) {
            java.util.Random random = new java.util.Random();
            if (random.nextInt(100) < 5) {
                int x = random.nextInt(WIDTH - BLOCK_SIZE);
                // Random RGB color for the block
                Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                blocks.add(new Block(x, 0, color));
            }
        }
    }

    private void checkCollision() {
        for (Block block : blocks) {
            Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
            Rectangle blockRect = new Rectangle(block.getX(), block.getY(), BLOCK_SIZE, BLOCK_SIZE);
            if (playerRect.intersects(blockRect)) {
                gameOver();
                return;
            }
        }
        if (blocks.size() >= blockThreshold) {
            if (level == 3) {
                gameWon();
            } else {
                level++;
                blockSpeed = LEVEL_SPEEDS[level - 1];
                blockThreshold = LEVEL_BLOCK_THRESHOLDS[level - 1];
                levelDuration = LEVEL_DURATIONS[level - 1];
                statusLabel.setText("Level " + level);
                blocks.clear();
                startTime = System.currentTimeMillis();
            }
        }
    }

    private void checkLevelDuration() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        if (elapsedTime >= levelDuration) {
            gameOver();
        }
    }

    private void gameWon() {
        statusLabel.setText("Congratulations! You won!");
        gameStarted = false;
        updateHighScore();
        displayScore();
    }

    private void gameOver() {
        statusLabel.setText("Aww, you lose!");
        gameStarted = false;
        updateHighScore();
        displayScore();
    }

    private void updateHighScore() {
        if (score > highScore) {
            highScore = score;
            saveHighScore(); // Save high score to file if it's updated
            updateHighScoreLabel(); // Update high score label
        }
    }

    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("highscore.txt"))) {
            highScore = Integer.parseInt(reader.readLine());
        } catch (IOException | NumberFormatException e) {
            highScore = 0;
        }
    }

    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("highscore.txt"))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayScore() {
        scoreLabel.setText("Score: " + score);
    }

    private void updateHighScoreLabel() {
        highScoreLabel.setText("High Score: " + highScore);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_SPACE) {
            if (!gameStarted) {
                startGame();
            }
        }
        if (keyCode == KeyEvent.VK_LEFT) {
            leftPressed = true;
        }
        if (keyCode == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if (keyCode == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private void startGame() {
        gameStarted = true;
        level = 1;
        blockSpeed = LEVEL_SPEEDS[level - 1];
        blockThreshold = LEVEL_BLOCK_THRESHOLDS[level - 1];
        levelDuration = LEVEL_DURATIONS[level - 1];
        statusLabel.setText("Level " + level);
        score = 0; // Reset score
        scoreLabel.setText(""); // Clear score label
        blocks.clear();
        startTime = System.currentTimeMillis();
        timer.start();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (gameStarted) {
            g.setColor(Color.DARK_GRAY); // Set player color to dark gray
            g.fillOval(playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
            for (Block block : blocks) {
                g.setColor(block.getColor()); // Set block color
                g.fillRect(block.getX(), block.getY(), BLOCK_SIZE, BLOCK_SIZE);
            }
        }
    }

    public static void main(String[] args) {
        new DodgeTheBlocks();
    }
}

class Block {
    private int x, y;
    private Color color;

    public Block(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Color getColor() {
        return color;
    }
}
