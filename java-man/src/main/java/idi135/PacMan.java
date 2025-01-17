package idi135;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
public class PacMan extends JPanel implements ActionListener, KeyListener {

    // Constants for board dimensions
    private final int rowCount = 21;
    private final int columnCount = 19;
    private final int tileSize = 32;
    private final int boardWidth = columnCount * tileSize;
    private final int boardHeight = rowCount * tileSize;

    // Game state variables
    private int score = 0;
    private int lives = 3;
    private int level = 0;
    private boolean gameOver = false;

    // Images for game elements
    private Image wallImage, blueGhostImage, orangeGhostImage, pinkGhostImage, redGhostImage;
    private Image cherryImage, foodImage, pacmanUpImage, pacmanDownImage, pacmanLeftImage, pacmanRightImage;

    // Collections for game elements
    private HashSet<Block> walls;
    private HashSet<Block> foods;
    private HashSet<Block> ghosts;
    private Block pacman;

    private Block powerUp;
    private boolean isPowerUpActive = false;
    private int powerUpDuration = 20000; 
    private long powerUpStartTime;


    // Directions and game loop
    private final char[] directions = {'U', 'D', 'L', 'R'};
    private final Random random = new Random();
    private Timer gameLoop;
    private String[] currentMap;

    private ScorePanel scorePanel;
    private LivesPanel livesPanel;
    private GameOverScreen gameOverScreen;


    
    private String[][] tileMaps = {
        {
            "XXXXXXXXXXXXXXXXXXX",
            "X  XXX  X X  XXX  X",
            "X   XX  X X  XX  XX",
            "X  XXX  X X  XXX  X",
            "XX  XX  X X  XX  XX",
            "X  XXXb X X pXXX  X",
            "XX  XX  X X  XX  XX",
            "X  XXX  X X  XXX  X",
            "XX  XX  X X  XX  XX",
            "X                 X",
            "XXXX     P     XXXX",
            "X                 X",
            "X   XX  X X  XX  XX",
            "X  XXX  X X  XXX  X",
            "X   XX  X X  XX  XX",
            "X  XXX  X X  XXX  X",
            "X   XXo X Xr XX  XX",
            "X  XXX  X X  XXX  X",
            "X   XX  X X  XX  XX",
            "X  XXX  X X  XXX  X",
            "XXXXXXXXXXXXXXXXXXX",
        },
        {
            "XXXXXXXXXXXXXXXXXXX",
            "X PXX    p XX   b X",
            "X  XX  XX  XX  XX X",
            "X  XX  XX  XX  XX X",
            "X  XX  XX  XX  XX X", 
            "X  XX  XX  XX  XX X",
            "X  XX  XX  XX  XX X",
            "X  XX  XX  XX  XX X",
            "X  XX  XX  XX  XX X",
            "X  XX  XX  XX  XX X",
            "X  XX  XX  XX  XX X",
            "X  XX  XX  XX  XX X",
            "X  XX  XX  XX  XX X",
            "X  XX  XX  XX  XX X",
            "X  XX  XX  XX  XX X",
            "X  XX  XX  XX  XX X",
            "X  XX  XX  XX  XX X",
            "X  XX  XX  XX  XX X",
            "X  XX  XX  XX  XX X",
            "X     rXX  Co  XX X",
            "XXXXXXXXXXXXXXXXXXX"
        },
         {
            "XXXXXXXXXXXXXXXXXXX", 
            "X                 X", 
            "X XX X X XX  XXXX X", 
            "X    X X     X    X", 
            "X XX X XXXXX XXX XX", 
            "X    X       X    X",  
            "XXXX XXXX XXXX XXXX",  
            "OOOX X       X XOOO",  
            "XXXX X XXrXX X XXXX",  
            "O  C    bpo       O",  
            "XXXX X XXXXX X XXXX",  
            "OOOX X       X XOOO",  
            "XXXX X XXXXX X XXXX",  
            "X    X      XX    X", 
            "X XX XX XXX XX XX X", 
            "X  X     P     X  X",   
            "XX X X XXXXX X X XX",   
            "X    X   X   X    X",   
            "X XXXXXX X XXXXXX X",   
            "X                 X",   
            "XXXXXXXXXXXXXXXXXXX"    
        },
        {
        "XXXXXXXXXXXXXXXXXXX", 
        "X                 X", 
        "X XX X XX X X XXXXX", 
        "X    X            X", 
        "X XX   XXXX XX X XX", 
        "X    X       X    X", 
        "XXXX XXXX XXXX XXXX", 
        "OOOX X       X XOOO", 
        "XXXX X XXrXX X XXXX", 
        "X       bpo       X", 
        "XXXX X XXXXX X XXXX", 
        "OOOX X       X XOOO", 
        "XXXX X XXXXX X XXXX", 
        "X  C   X   X      X", 
        "X XX XXX X XXX XX X", 
        "X  X     X     X  X", 
        "XX X X XXXXX X X XX", 
        "X    X   P   X    X", 
        "X XXXXXX X XXXXXX X",
        "X    X   X   X    X", 
        "XXXXXXXXXXXXXXXXXXX"
        },
        {
            "XXXXXXXXXXXXXXXXXXX", 
            "X        X        X", 
            "X XX XXX X XXX XX X",
            "X                 X", 
            "X XX X XXXXX X XX X", 
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX", 
            "OOOX X       X XOOO", 
            "XXXX X XXrXX X XXXX",
            "O       bpo       O", 
            "XXXX X XXXXX X XXXX", 
            "OOOX X       X XOOO",
            "XXXX X XXXXX X XXXX", 
            "X        X  C     X", 
            "X XX XXX X XXX XX X",
            "X  X     P     X  X", 
            "XX X X XXXXX X X XX", 
            "X    X   X   X    X",
            "X XXXXXX X XXXXXX X", 
            "X                 X", 
            "XXXXXXXXXXXXXXXXXXX"
        }
    };


    // Block class to represent walls, ghosts, foods, and PacMan
    class Block {
        int x, y, width, height;
        int startX, startY;
        int velocityX = 0, velocityY = 0;
        char direction = 'U';
        Image image;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            // Keeps PacMan moving in direction after hitting wall
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize / 4;
            } else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize / 4;
            } else if (this.direction == 'L') {
                this.velocityX = -tileSize / 4;
                this.velocityY = 0;
            } else if (this.direction == 'R') {
                this.velocityX = tileSize / 4;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    // Constructor to initialize the game
    public PacMan(ScorePanel scorePanel, LivesPanel livesPanel) {

        this.scorePanel = scorePanel;
        this.livesPanel = livesPanel;
        scorePanel.updateLevel(level);
        // livesPanel.updateLives(lives);
        scorePanel.updateScore(score);
        gameOverScreen = new GameOverScreen();

        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        currentMap = tileMaps[level];

        loadImages();
        loadMap();
        initializeGhosts();
        gameLoop = new Timer(50, this); // 20fps (1000/50)
        gameLoop.start();
        SoundManager.playSound("java-man/src/main/resources/pacman_beginning.wav");
        
    }

    //  game element images
    private void loadImages() {
        wallImage = new ImageIcon(getClass().getResource("/wall2.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("/blueGhost1.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("/orangeGhost1.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("/pinkGhost1.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("/redGhost1.png")).getImage();
        cherryImage = new ImageIcon(getClass().getResource("/cherry.png")).getImage();
        foodImage = new ImageIcon(getClass().getResource("/powerFood.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("/pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("/pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("/pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("/pacmanRight.png")).getImage();
    }

 
    private void loadMap() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                char tileChar = currentMap[r].charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;

                switch (tileChar) {
                    case 'X': // Wall
                        walls.add(new Block(wallImage, x, y, tileSize, tileSize));
                        break;
                    case 'b': // Blue Ghost
                        ghosts.add(new Block(blueGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'o': // Orange Ghost
                        ghosts.add(new Block(orangeGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'p': // Pink Ghost
                        ghosts.add(new Block(pinkGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'r': // Red Ghost
                        ghosts.add(new Block(redGhostImage, x, y, tileSize, tileSize));
                        break;
                    case 'P': // PacMan
                        pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                        break;
                    case ' ': // Food
                        foods.add(new Block(foodImage, x + 14, y + 14, 4, 4));
                        break;
                    case 'C': // Cherry Power-Up
                        powerUp = new Block(cherryImage, x, y, tileSize, tileSize); // Adjust position/size for visibility
                        break;
                }
            }
        }
    }

    private void initializeGhosts() {
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);

        if (gameOver) {
            gameOverScreen.paintComponent(g); 
        }
    }


    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        if (isPowerUpActive) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(255, 255, 0, 50)); 
            g2d.fillRect(0, 0, boardWidth, boardHeight); 
        }

        if (powerUp != null) {
            g.drawImage(powerUp.image, powerUp.x, powerUp.y, powerUp.width, powerUp.height, null);
        }

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        for (Block food : foods) {
            g.drawImage(food.image, food.x, food.y, food.width, food.height, null);
        }
    }


    public void move() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        if (pacman.x < 0) pacman.x = boardWidth - tileSize;
        if (pacman.x + pacman.width > boardWidth) pacman.x = 0;

        if (powerUp != null && collision(pacman, powerUp)) {
            powerUp = null; 
            isPowerUpActive = true;
            powerUpStartTime = System.currentTimeMillis();
            SoundManager.playSound("java-man/src/main/resources/pacman_eatfruit.wav");
        }

        if (isPowerUpActive) {
            long elapsedTime = System.currentTimeMillis() - powerUpStartTime;
        
      
            for (Block ghost : ghosts) {
                if (collision(pacman, ghost)) {
                    ghost.reset(); 
                }
            }
        
         
            if (elapsedTime > powerUpDuration) {
                isPowerUpActive = false;
            }
        }
        

        for (Block ghost : ghosts) {
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            
            if (collision(pacman, ghost)) {
                if (isPowerUpActive) {
                    ghost.reset(); 
                    score += 50; 
                    scorePanel.updateScore(score);
                } else {
                    lives--;
                    livesPanel.updateLives(lives);
                    if (lives == 0) {
                        gameOver();
                        return;
                    }
                }
                SoundManager.playSound("java-man/src/main/resources/pacman_death.wav");
                resetPositions();
            }
            
            if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
                    
    
            for (Block wall : walls) {
                if (collision(pacman, wall)) {
                    pacman.x -= pacman.velocityX;
                    pacman.y -= pacman.velocityY;
                    break;
                }
            }

            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }

    
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                int points = isPowerUpActive ? 20 : 10; // Double points if power-up is active
                score += points;
                scorePanel.updateScore(score);
                SoundManager.playSound("java-man/src/main/resources/pacman_chomp.wav");
            }
        }
        foods.remove(foodEaten);

        if (foods.isEmpty()) {
            nextLevel();
        }
    }

    
    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y;
    }

    
    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = pacman.velocityY = 0;

        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    public void gameOver() {
        gameOver = true;
        gameLoop.stop();
    
        
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.setBackground(Color.BLACK);
        inputPanel.setBorder(BorderFactory.createLineBorder(Color.MAGENTA, 5));
    
        JLabel label = new JLabel("Enter your username:", SwingConstants.CENTER);
        label.setFont(new Font("Courier New", Font.BOLD, 18)); 
        label.setForeground(Color.CYAN);
    
        JTextField textField = new JTextField();
        textField.setFont(new Font("Courier New", Font.PLAIN, 14));
        textField.setBackground(Color.BLACK);
        textField.setForeground(Color.YELLOW);
        textField.setCaretColor(Color.YELLOW);
        
    
        inputPanel.add(label, BorderLayout.NORTH);
        inputPanel.add(textField, BorderLayout.CENTER);
    
        int result = JOptionPane.showConfirmDialog(
            PacMan.this,
            inputPanel,
            "Game Over",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
    
        String playerName = null;
        if (result == JOptionPane.OK_OPTION) {
            playerName = textField.getText();
            if (playerName == null || playerName.trim().isEmpty()) {
                playerName = "Player" + new Random().nextInt(1000);
            }
        }
    
        if (playerName != null && !playerName.trim().isEmpty()) {
            FirebaseService.writeLeaderboard(playerName, score);
        }
    
        showLeaderboard();
      
    }

    public void nextLevel() {
        if (level < tileMaps.length - 1) {
            level++;  
            scorePanel.updateLevel(level);
            currentMap = tileMaps[level];  
            loadMap();  
            resetPositions();  
            score += 100;  
            scorePanel.updateScore(score);
        } else {
            System.out.println("You have completed all levels!");
            gameOver();  
        }
    }

    // Show leaderboard
    private void showLeaderboard() {
        FirebaseService.getLeaderboard(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<PlayerScore> leaderboard = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PlayerScore playerScore = snapshot.getValue(PlayerScore.class);
                    leaderboard.add(playerScore);
                }
    
               
                leaderboard.sort((p1, p2) -> Integer.compare(p2.score, p1.score));
    
                
                JPanel leaderboardPanel = new JPanel();
                leaderboardPanel.setLayout(new GridLayout(6, 1)); 
                leaderboardPanel.setBackground(Color.BLACK);
                leaderboardPanel.setBorder(BorderFactory.createLineBorder(Color.MAGENTA, 5));
    
                JLabel header = new JLabel("Leaderboard", SwingConstants.CENTER);
                header.setForeground(Color.CYAN);
                header.setFont(new Font("Press Start 2P", Font.BOLD, 24)); 
                leaderboardPanel.add(header);
    
                for (int i = 0; i < Math.min(5, leaderboard.size()); i++) {
                    PlayerScore playerScore = leaderboard.get(i);
                    JLabel entry = new JLabel((i + 1) + ". " + playerScore.name + " - " + playerScore.score + " points", SwingConstants.CENTER);
                    entry.setForeground(Color.YELLOW);
                    entry.setFont(new Font("Press Start 2P", Font.PLAIN, 18)); 
                    leaderboardPanel.add(entry);
                }
    
                JOptionPane.showMessageDialog(
                    PacMan.this,
                    leaderboardPanel,
                    "Cyberpunk PacMan",
                    JOptionPane.PLAIN_MESSAGE
                );
            }
    
            @Override
            public void onCancelled(DatabaseError databaseError) {
                JOptionPane.showMessageDialog(PacMan.this, "Error loading leaderboard.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            currentMap = tileMaps[0];
            loadMap();
            resetPositions();
            lives = 3;
            livesPanel.updateLives(lives);
            score = 0;
            scorePanel.updateScore(score);
            level = 0;
            scorePanel.updateLevel(level);
            gameOver = false;
            gameLoop.start();
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
        }

        // Update PacMan's image based on direction
        switch (pacman.direction) {
            case 'U': pacman.image = pacmanUpImage; break;
            case 'D': pacman.image = pacmanDownImage; break;
            case 'L': pacman.image = pacmanLeftImage; break;
            case 'R': pacman.image = pacmanRightImage; break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}
