import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameOfLife extends JFrame {
    private DrawingPanel drawingPanel;
    private boolean running = false, toroid = false;
    private Grid grid;
    private int frameCount = 0;
    private double generationsPerSec, nextGenerationFrame;

    /**
     * Getter of a boolean field.
     * @return <code>true</code> if the game is running, <code>false</code> otherwise
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Getter of a boolean field.
     * @return <code>true</code> if the grid is toroid (corresponding edges are "stitched"),
     * <code>false</code> otherwise
     */
    public boolean isToroid() {
        return toroid;
    }

    /**
     * Pauses the game.
     */
    public void pause() {
        running = false;
    }

    /**
     * Resumes the game.
     */
    public void resume() {
        running = true;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    GameOfLife frame = new GameOfLife();
                    frame.setVisible(true);
                    frame.addContents();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Initiates an instance of the game and sets default values.
     */
    public GameOfLife() {
        super("Game of Life");

        grid = new Grid();
        this.frameCount = 0;
        this.generationsPerSec = 3;
        this.nextGenerationFrame = 50.0 / this.generationsPerSec;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(0, 0, screen.width, screen.height);
        drawingPanel = new DrawingPanel(this);
        drawingPanel.setLayout(null);
        setContentPane(drawingPanel);

        Animator animator = new Animator(this);
        getContentPane().addMouseListener(animator);
        Timer timer = new Timer(20, animator);
        timer.start();
    }

    /**
     * Adds the labels and buttons to draw. Also implements the buttons' mechanics.
     */
    private void addContents() {
        JLabel text = new JLabel("Welcome to Conway's Game of Life!");
        JLabel text2 = new JLabel("The grid size is 50x50. " +
                "While the game is not running,");
        JLabel text3 = new JLabel("the grid can be edited manually by clicking the cells.");
        text.setFont(new Font("Arial", Font.PLAIN, 18));
        text.setBounds(30, 20, 2000, 30);
        this.add(text);
        text2.setFont(new Font("Arial", Font.PLAIN, 18));
        text2.setBounds(30, 50, 2000, 30);
        this.add(text2);
        text3.setFont(new Font("Arial", Font.PLAIN, 18));
        text3.setBounds(30, 80, 2000, 30);
        this.add(text3);

        JLabel pauseButtonText = new JLabel("Start/Pause/Resume the game");
        pauseButtonText.setFont(new Font("Arial", Font.PLAIN, 17));
        pauseButtonText.setBounds(90, 150, 300, 40);
        this.add(pauseButtonText);

        JButton pauseButton = new JButton("Start");
        pauseButton.setBounds(365, 150, 115, 40);
        this.add(pauseButton);
        pauseButton.setBackground(Color.green);
        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                running = !running;
                pauseButton.setText(isRunning() ? "Pause" : "Resume");
            }
        });

        JLabel clearButtonText = new JLabel("Clear the grid");
        clearButtonText.setFont(new Font("Arial", Font.PLAIN, 17));
        clearButtonText.setBounds(220, 200, 300, 40);
        this.add(clearButtonText);

        JButton clearButton = new JButton("Clear");
        clearButton.setBounds(365, 200, 115, 40);
        this.add(clearButton);
        clearButton.setBackground(Color.green);
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                running = false;
                grid.clear();
                pauseButton.setText("Start");
            }
        });

        JLabel randomButtonText = new JLabel("Random fill");
        randomButtonText.setFont(new Font("Arial", Font.PLAIN, 17));
        randomButtonText.setBounds(240, 250, 300, 40);
        this.add(randomButtonText);

        JButton randomButton = new JButton("Set Random");
        randomButton.setBounds(365, 250, 115, 40);
        this.add(randomButton);
        randomButton.setBackground(Color.green);
        randomButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                running = false;
                grid.fillRandom();
                pauseButton.setText("Start");
            }
        });

        JLabel gosperButtonText = new JLabel("Gosper's glider gun");
        gosperButtonText.setFont(new Font("Arial", Font.PLAIN, 17));
        gosperButtonText.setBounds(175, 300, 300, 40);
        this.add(gosperButtonText);

        JButton gosperButton = new JButton("Gosper gun");
        gosperButton.setBounds(365, 300, 115, 40);
        this.add(gosperButton);
        gosperButton.setBackground(Color.green);
        gosperButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                running = false;
                grid.fillGosper();
                pauseButton.setText("Start");
            }
        });

        addGenerationsPerSec();
        addToroid();
        addExit();
    }

    /**
     * Adds the label and buttons for choosing animation speed.
     */
    private void addGenerationsPerSec() {
        JLabel switchGenPerSecText = new JLabel("Choose animation speed:");
        switchGenPerSecText.setFont(new Font("Arial", Font.PLAIN, 17));
        switchGenPerSecText.setBounds(160, 370, 300, 40);
        this.add(switchGenPerSecText);
        JLabel switchGenPerSecText2 = new JLabel("( generations per second )");
        switchGenPerSecText2.setFont(new Font("Arial", Font.PLAIN, 17));
        switchGenPerSecText2.setBounds(160, 390, 300, 40);
        this.add(switchGenPerSecText2);

        JButton[] options = new JButton[5]; // 0.5, 1, 3, 10, 30
        Double[] val = {0.5, 1.0, 3.0, 10.0, 30.0};
        for (int i = 0; i < 5; i++) {
            JButton button = new JButton(val[i].toString());
            button.setBackground(Color.cyan);
            if (i == 2) {
                button.setBackground(Color.blue);
            }
            button.setBounds(39 + 90 * i, 430, 80, 40);
            double newGenerationsPerSec = val[i];
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    frameCount = 0;
                    generationsPerSec = newGenerationsPerSec;
                    nextGenerationFrame = 50.0 / generationsPerSec;
                    for (int i = 0; i < 5; i++) {
                        options[i].setBackground(Color.cyan);
                    }
                    button.setBackground(Color.blue);
                }
            });
            this.add(button);
            options[i] = button;
        }
    }

    /**
     * Adds the label and button for toroid switch.
     */
    private void addToroid() {
        JLabel toroidButtonText = new JLabel("Make the edges \"stitched\"");
        toroidButtonText.setFont(new Font("Arial", Font.PLAIN, 17));
        toroidButtonText.setBounds(120, 500, 300, 40);
        this.add(toroidButtonText);

        JButton toroidButton = new JButton("Toroidal");
        toroidButton.setBackground(Color.white);
        toroidButton.setBounds(365, 500, 115, 40);
        toroidButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toroid = !isToroid();
                toroidButton.setBackground(isToroid() ? Color.green : Color.white);
            }
        });
        this.add(toroidButton);
    }

    /**
     * Adds the exit button.
     */
    private void addExit() {
        JButton exitButton = new JButton("Exit");
        exitButton.setBackground(Color.red);
        exitButton.setBounds(365, 700, 115, 40);
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                running = false;
                System.exit(0);
            }
        });
        this.add(exitButton);
    }

    /**
     * Method called from <code>DrawingPanel</code> to draw the grid.
     * @param g the <code>Graphics</code> context in which to paint
     */
    public void drawGrid(Graphics g) {
        this.grid.draw(g);
    }

    /**
     *  Increments frame count. If needed, counts next generation (based on the speed of animation).
     */
    public void evolve() {
        incrementFrameCount();
        if (frameCount == (int)nextGenerationFrame) {
            nextGenerationFrame = nextGenerationFrame + 50.0 / generationsPerSec;
            if (nextGenerationFrame > 1001) {
                nextGenerationFrame -= 1000;
            }
            if (running) {
                grid.evolve(isToroid());
            }
        }
    }

    /**
     * Increments frame count (keeps it between 1 and 1000 to not cause overflow later on).
     */
    public void incrementFrameCount() {
        frameCount = frameCount % 1000 + 1;
    }

    /**
     * Getter for <code>frameCount</code>.
     * @return <code>frameCount</code>
     */
    public int getFrameCount() {
        return frameCount;
    }

    /**
     * Getter for <code>generationsPerSec</code>.
     * @return <code>generationsPerSec</code>
     */
    public double getGenerationsPerSec() {
        return generationsPerSec;
    }

    /**
     * Getter for <code>nextGenerationFrame</code>.
     * @return <code>nextGenerationFrame</code>
     */
    public double getNextGenerationFrame() {
        return nextGenerationFrame;
    }

    /**
     * Updates the cell on mouse click if the game is not running.
     * @param mouseDownY y-coordinate of the mouse press
     * @param mouseDownX x-coordinate of the mouse press
     * @param mouseUpY y-coordinate of the mouse release
     * @param mouseUpX x-coordinate of the mouse release
     */
    public void updateCell(int mouseDownY, int mouseDownX, int mouseUpY, int mouseUpX) {
        if (!running) {
            grid.updateMouse(mouseDownY, mouseDownX, mouseUpY, mouseUpX);
        }
    }
}
