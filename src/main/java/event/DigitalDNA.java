package event;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;


public class DigitalDNA extends JPanel implements ActionListener, MouseMotionListener, KeyListener, MouseListener {
    private ArrayList<DNAStrand> strands;
    private Timer timer;
    private Point mousePos;
    private float rotation = 0;
    private static final Color ADENINE = new Color(255, 102, 102);
    private static final Color THYMINE = new Color(102, 178, 255);
    private static final Color CYTOSINE = new Color(102, 255, 102);
    private static final Color GUANINE = new Color(255, 178, 102);
    private static final Color BACKBONE = new Color(220, 220, 220);

    private boolean isRotating = true;
    private float rotationSpeed = 1.0f;
    private boolean pulseMode = false;
    private boolean rainbowMode = false;
    private boolean glowMode = true;
    private int zoomLevel = 100;
    private float splitLevel = 0;

    public DigitalDNA() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        strands = new ArrayList<>();
        strands.add(new DNAStrand(getPreferredSize().width / 2, 50));

        addMouseMotionListener(this);
        addMouseListener(this);
        addKeyListener(this);

        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Enhanced background effect
        GradientPaint bgGradient = new GradientPaint(
                0, 0, new Color(0, 0, 20),
                getWidth(), getHeight(), new Color(20, 0, 20)
        );
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Applying zoom transform
        g2d.translate(getWidth()/2, getHeight()/2);
        g2d.scale(zoomLevel/100.0, zoomLevel/100.0);
        g2d.translate(-getWidth()/2, -getHeight()/2);

        // Drawing strands
        for (DNAStrand strand : strands) {
            strand.draw(g2d);
        }

        // Drawing controls info
        drawControls(g2d);
    }

    private void drawControls(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        int y = 20;
        int lineHeight = 20;

        g2d.drawString("Controls:", 10, y);
        g2d.drawString("SPACE - Toggle Rotation: " + (isRotating ? "ON" : "OFF"), 10, y += lineHeight);
        g2d.drawString("P - Pulse Mode: " + (pulseMode ? "ON" : "OFF"), 10, y += lineHeight);
        g2d.drawString("R - Rainbow Mode: " + (rainbowMode ? "ON" : "OFF"), 10, y += lineHeight);
        g2d.drawString("G - Glow Effect: " + (glowMode ? "ON" : "OFF"), 10, y += lineHeight);
        g2d.drawString("UP/DOWN - Zoom: " + zoomLevel + "%", 10, y += lineHeight);
        g2d.drawString("+/- - Speed: " + String.format("%.1f", rotationSpeed), 10, y += lineHeight);
        g2d.drawString("S - Split DNA: " + String.format("%.0f%%", splitLevel * 100), 10, y += lineHeight);

        // Drawing base pair legend
        drawLegend(g2d);
    }

    private void drawLegend(Graphics2D g2d) {
        int x = getWidth() - 150;
        int y = 20;
        int size = 10;

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));

        Color[] colors = {ADENINE, THYMINE, CYTOSINE, GUANINE};
        String[] names = {"Adenine", "Thymine", "Cytosine", "Guanine"};

        for (int i = 0; i < colors.length; i++) {
            g2d.setColor(colors[i]);
            g2d.fillOval(x, y + i*20, size, size);
            g2d.setColor(Color.WHITE);
            g2d.drawString(names[i], x + 15, y + i*20 + 10);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
                isRotating = !isRotating;
                break;
            case KeyEvent.VK_P:
                pulseMode = !pulseMode;
                break;
            case KeyEvent.VK_R:
                rainbowMode = !rainbowMode;
                break;
            case KeyEvent.VK_G:
                glowMode = !glowMode;
                break;
            case KeyEvent.VK_UP:
                zoomLevel = Math.min(200, zoomLevel + 10);
                break;
            case KeyEvent.VK_DOWN:
                zoomLevel = Math.max(50, zoomLevel - 10);
                break;
            case KeyEvent.VK_EQUALS:
                rotationSpeed = Math.min(3.0f, rotationSpeed + 0.1f);
                break;
            case KeyEvent.VK_MINUS:
                rotationSpeed = Math.max(0.1f, rotationSpeed - 0.1f);
                break;
            case KeyEvent.VK_S:
                splitLevel = (splitLevel > 0) ? 0 : 1.0f;
                break;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isRotating) {
            rotation += 0.02 * rotationSpeed;
        }

        if (splitLevel > 0) {
            splitLevel = Math.max(0, splitLevel - 0.02f);
        }

        for (DNAStrand strand : strands) {
            strand.update(mousePos, rotation);
        }
        repaint();
    }

    private class DNAStrand {
        private ArrayList<BasePair> basePairs;
        private int centerX;
        private double startY;
        private static final int PAIRS = 30;
        private static final int HELIX_RADIUS = 40;

        public DNAStrand(int x, int y) {
            centerX = x;
            startY = y;
            basePairs = new ArrayList<>();

            for (int i = 0; i < PAIRS; i++) {
                basePairs.add(new BasePair(i));
            }
        }

        public void update(Point mousePos, float rotation) {
            for (BasePair pair : basePairs) {
                pair.update(rotation);
            }
        }

        public void draw(Graphics2D g2d) {
            g2d.setStroke(new BasicStroke(4));

            // Drawing left and right backbones
            for (int i = 0; i < basePairs.size() - 1; i++) {
                BasePair current = basePairs.get(i);
                BasePair next = basePairs.get(i + 1);

                Color backboneColor = rainbowMode ?
                        Color.getHSBColor((float)i/basePairs.size(), 0.7f, 1.0f) :
                        BACKBONE;
                g2d.setColor(backboneColor);

                // Applying split effect
                double splitOffset = HELIX_RADIUS * splitLevel;

                // Left backbone
                g2d.drawLine(
                        (int)(current.leftX - splitOffset), (int)current.y,
                        (int)(next.leftX - splitOffset), (int)next.y
                );

                // Right backbone
                g2d.drawLine(
                        (int)(current.rightX + splitOffset), (int)current.y,
                        (int)(next.rightX + splitOffset), (int)next.y
                );
            }

            // Drawing base pairs
            g2d.setStroke(new BasicStroke(2));
            for (BasePair pair : basePairs) {
                pair.draw(g2d);
            }
        }

        private class BasePair {
            private double leftX, rightX, y;
            private Color leftColor, rightColor;
            private int index;
            private double pulsePhase = 0;

            public BasePair(int index) {
                this.index = index;
                // Assigning base pair colors based on index
                int baseType = index % 4;
                switch (baseType) {
                    case 0:
                        leftColor = ADENINE;
                        rightColor = THYMINE;
                        break;
                    case 1:
                        leftColor = THYMINE;
                        rightColor = ADENINE;
                        break;
                    case 2:
                        leftColor = CYTOSINE;
                        rightColor = GUANINE;
                        break;
                    case 3:
                        leftColor = GUANINE;
                        rightColor = CYTOSINE;
                        break;
                }
            }

            public void update(float rotation) {
                double angle = rotation + (index * Math.PI / 5.0);
                y = startY + (index * 20);

                if (pulseMode) {
                    pulsePhase += 0.1;
                    double pulseFactor = 1.0 + 0.2 * Math.sin(pulsePhase);
                    leftX = centerX + Math.cos(angle) * HELIX_RADIUS * pulseFactor;
                    rightX = centerX + Math.cos(angle + Math.PI) * HELIX_RADIUS * pulseFactor;
                } else {
                    leftX = centerX + Math.cos(angle) * HELIX_RADIUS;
                    rightX = centerX + Math.cos(angle + Math.PI) * HELIX_RADIUS;
                }

                // Applying split effect
                double splitOffset = HELIX_RADIUS * splitLevel;
                leftX -= splitOffset;
                rightX += splitOffset;
            }

            public void draw(Graphics2D g2d) {
                int baseSize = 8;

                // Drawing base pair connection if not fully split
                if (splitLevel < 1.0f) {
                    g2d.setColor(new Color(200, 200, 200, 150));
                    g2d.drawLine((int)leftX, (int)y, (int)rightX, (int)y);
                }

                // Drawing bases with optional effects
                Color leftBaseColor = rainbowMode ?
                        Color.getHSBColor((float)index/PAIRS, 0.8f, 1.0f) :
                        leftColor;
                Color rightBaseColor = rainbowMode ?
                        Color.getHSBColor((float)index/PAIRS + 0.5f, 0.8f, 1.0f) :
                        rightColor;

                // Drawing base pairs with glow effect
                if (glowMode) {
                    drawGlowingBase(g2d, leftX, y, baseSize, leftBaseColor);
                    drawGlowingBase(g2d, rightX, y, baseSize, rightBaseColor);
                } else {
                    g2d.setColor(leftBaseColor);
                    g2d.fillOval((int)leftX - baseSize/2, (int)y - baseSize/2, baseSize, baseSize);
                    g2d.setColor(rightBaseColor);
                    g2d.fillOval((int)rightX - baseSize/2, (int)y - baseSize/2, baseSize, baseSize);
                }
            }

            private void drawGlowingBase(Graphics2D g2d, double x, double y, int baseSize, Color color) {
                Graphics2D g2dCopy = (Graphics2D) g2d.create();
                g2dCopy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2dCopy.setColor(color);
                g2dCopy.fillOval((int)x - baseSize, (int)y - baseSize, baseSize*2, baseSize*2);
                g2dCopy.dispose();

                g2d.setColor(color);
                g2d.fillOval((int)x - baseSize/2, (int)y - baseSize/2, baseSize, baseSize);
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) { mousePos = e.getPoint(); }
    @Override
    public void mouseDragged(MouseEvent e) { mousePos = e.getPoint(); }
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
}
