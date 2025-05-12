import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.Random;

public class GraphVisualization extends JPanel {
    private static final int N = 10;
    private static final long VARIANT = 4312L;
    private final int[][] Aundir;
    private final int[][] Adir;
    private final List<Point> positions;
    private boolean showDirected = false;
    private boolean showUndirected = false;
    private final int scaleFactor = 2;

    // Color constants
    private static final Color NODE_FILL = new Color(83, 83, 83);
    private static final Color NODE_BORDER = new Color(0, 0, 0);
    private static final Color DIRECTED_EDGE = new Color(70, 70, 70);
    private static final Color UNDIRECTED_EDGE = new Color(70, 70, 70);
    private static final Color ARROW_HEAD = new Color(70, 70, 70);
    private static final Color TEXT_COLOR = Color.WHITE;

    public GraphVisualization(int[][] Aundir, int[][] Adir, List<Point> positions) {
        this.Aundir = Aundir;
        this.Adir = Adir;
        this.positions = positions;

        // Scale up positions
        for (Point p : positions) {
            p.x *= scaleFactor;
            p.y *= scaleFactor;
        }

        setPreferredSize(new Dimension(1200, 800));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw undirected graph if enabled
        if (showUndirected) {
            drawGraph(g2d, Aundir, false, 0);
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("Undirected Graph", 350, 40);
        }

        // Draw directed graph if enabled
        if (showDirected) {
            drawGraph(g2d, Adir, true, showUndirected ? 400 : 0);
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("Directed Graph", 350, showUndirected ? 440 : 40);
        }
    }

    private void drawGraph(Graphics2D g2d, int[][] matrix, boolean directed, int yOffset) {
        // Draw edges first (so they appear behind nodes)
        drawEdges(g2d, matrix, directed, yOffset);

        // Draw nodes
        drawNodes(g2d, yOffset);
    }

    private void drawNodes(Graphics2D g2d, int yOffset) {
        g2d.setStroke(new BasicStroke(3));

        for (int i = 0; i < positions.size(); i++) {
            Point p = positions.get(i);
            int nodeSize = 40 * scaleFactor;

            // Draw node circle
            g2d.setColor(NODE_FILL);
            g2d.fillOval(p.x - nodeSize/2, p.y + yOffset - nodeSize/2, nodeSize, nodeSize);
            g2d.setColor(NODE_BORDER);
            g2d.drawOval(p.x - nodeSize/2, p.y + yOffset - nodeSize/2, nodeSize, nodeSize);

            // Draw node number
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("Arial", Font.BOLD, 16 * scaleFactor));
            String label = String.valueOf(i + 1);
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            int labelHeight = fm.getAscent();
            g2d.drawString(label, p.x - labelWidth/2, p.y + yOffset + labelHeight/4);
        }
    }

    private void drawEdges(Graphics2D g2d, int[][] matrix, boolean directed, int yOffset) {
        // Matrix to track which edges are bidirectional
        boolean[][] isBidirectional = new boolean[N][N];

        // First pass: identify bidirectional edges
        if (directed) {
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    if (matrix[i][j] == 1 && matrix[j][i] == 1 && i != j) {
                        isBidirectional[i][j] = true;
                        isBidirectional[j][i] = true;
                    }
                }
            }
        }

        // Second pass: draw all edges
        for (int i = 0; i < matrix.length; i++) {
            for (int j = directed ? 0 : i; j < matrix[i].length; j++) {
                if (matrix[i][j] == 1) {
                    if (i == j) {
                        // Self-loop
                        Point p = positions.get(i);
                        drawSelfLoop(g2d, p, directed, yOffset);
                    } else {
                        Point p1 = positions.get(i);
                        Point p2 = positions.get(j);

                        Point offsetP1 = new Point(p1.x, p1.y + yOffset);
                        Point offsetP2 = new Point(p2.x, p2.y + yOffset);

                        if (directed && isBidirectional[i][j] && i < j) {
                            g2d.setColor(DIRECTED_EDGE);
                            drawBidirectionalEdge(g2d, offsetP1, offsetP2);
                        } else if (directed) {
                            if (!isBidirectional[i][j]) {
                                g2d.setColor(DIRECTED_EDGE);
                                drawArrow(g2d, offsetP1, offsetP2);
                            }
                        } else if (i < j) {
                            g2d.setColor(UNDIRECTED_EDGE);
                            g2d.drawLine(offsetP1.x, offsetP1.y, offsetP2.x, offsetP2.y);
                        }
                    }
                }
            }
        }

    }

    private void drawBidirectionalEdge(Graphics2D g2d, Point p1, Point p2) {
        // Calculate midpoint and perpendicular direction
        double midX = (p1.x + p2.x) / 2.0;
        double midY = (p1.y + p2.y) / 2.0;

        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double length = Math.sqrt(dx*dx + dy*dy);

        // Normalize and rotate 90 degrees for perpendicular direction
        double nx = -dy/length;
        double ny = dx/length;

        // Calculate control points for both curves
        double curveHeight = length * 0.3; // Adjust curve height based on edge length

        // Draw first curve (A->B)
        double ctrl1X = midX + nx * curveHeight;
        double ctrl1Y = midY + ny * curveHeight;
        QuadCurve2D curve1 = new QuadCurve2D.Double(
                p1.x, p1.y,
                ctrl1X, ctrl1Y,
                p2.x, p2.y
        );
        g2d.draw(curve1);
        drawArrowHeadOnCurve(g2d, curve1, 0.9);

        // Draw second curve (B->A) with opposite curvature
        double ctrl2X = midX - nx * curveHeight;
        double ctrl2Y = midY - ny * curveHeight;
        QuadCurve2D curve2 = new QuadCurve2D.Double(
                p2.x, p2.y,
                ctrl2X, ctrl2Y,
                p1.x, p1.y
        );
        g2d.draw(curve2);
        drawArrowHeadOnCurve(g2d, curve2, 0.9);
    }

    private void drawArrow(Graphics2D g2d, Point p1, Point p2) {
        // Calculate the point where the arrow should end (before the node circle)
        double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);
        int nodeRadius = 20 * scaleFactor;
        int endX = (int)(p2.x - nodeRadius * Math.cos(angle));
        int endY = (int)(p2.y - nodeRadius * Math.sin(angle));

        // Draw line
        g2d.drawLine(p1.x, p1.y, endX, endY);

        // Draw arrow head
        drawArrowHead(g2d, new Point(endX, endY), angle);
    }

    private void drawArrowHeadOnCurve(Graphics2D g2d, QuadCurve2D curve, double t) {
        // Get point near the end of the curve for arrow placement
        Point2D arrowPoint = getPointOnCurve(curve, t);
        Point2D beforePoint = getPointOnCurve(curve, t - 0.05);

        // Calculate angle at this point
        double angle = Math.atan2(
                arrowPoint.getY() - beforePoint.getY(),
                arrowPoint.getX() - beforePoint.getX()
        );

        // Draw arrow head
        drawArrowHead(g2d,
                new Point((int)arrowPoint.getX(), (int)arrowPoint.getY()),
                angle
        );
    }

    private Point2D getPointOnCurve(QuadCurve2D curve, double t) {
        double x = Math.pow(1-t, 2) * curve.getX1() +
                2 * (1-t) * t * curve.getCtrlX() +
                t * t * curve.getX2();
        double y = Math.pow(1-t, 2) * curve.getY1() +
                2 * (1-t) * t * curve.getCtrlY() +
                t * t * curve.getY2();
        return new Point2D.Double(x, y);
    }

    private void drawArrowHead(Graphics2D g2d, Point tip, double angle) {
        int arrowSize = 20;
        double angle1 = angle + Math.toRadians(150);
        double angle2 = angle - Math.toRadians(150);

        int xPoints[] = {
                tip.x,
                (int) (tip.x + arrowSize * Math.cos(angle1)),
                (int) (tip.x + arrowSize * Math.cos(angle2))
        };
        int yPoints[] = {
                tip.y,
                (int) (tip.y + arrowSize * Math.sin(angle1)),
                (int) (tip.y + arrowSize * Math.sin(angle2))
        };

        g2d.setColor(ARROW_HEAD);
        g2d.fillPolygon(xPoints, yPoints, 3);
    }

    private void drawSelfLoop(Graphics2D g2d, Point center, boolean directed, int yOffset) {
        int nodeRadius = 30 * scaleFactor;
        int loopRadius = 30 * scaleFactor;
        int loopX = center.x - loopRadius / 2;
        int loopY = center.y + yOffset - nodeRadius - loopRadius + 40;

        // Draw the loop (circle/oval)
        g2d.drawOval(loopX, loopY, loopRadius, loopRadius);

        // Draw arrowhead for directed self-loop
        if (directed) {
            double angle = Math.toRadians(65);
            int arrowX = loopX + 5;
            int arrowY = center.y - 33;
            drawArrowHead(g2d, new Point(arrowX, arrowY), angle);
        }
    }


    private boolean lineIntersectsCircle(Point p1, Point p2, Point center, int radius) {
        Point2D.Double lineStart = new Point2D.Double(p1.x, p1.y);
        Point2D.Double lineEnd = new Point2D.Double(p2.x, p2.y);
        Point2D.Double circleCenter = new Point2D.Double(center.x, center.y);

        Point2D.Double d = new Point2D.Double(lineEnd.x - lineStart.x, lineEnd.y - lineStart.y);
        Point2D.Double f = new Point2D.Double(lineStart.x - circleCenter.x, lineStart.y - circleCenter.y);

        double a = d.x*d.x + d.y*d.y;
        double b = 2 * (f.x*d.x + f.y*d.y);
        double c = (f.x*f.x + f.y*f.y) - radius*radius;

        double discriminant = b*b - 4*a*c;

        if (discriminant < 0) {
            return false;
        }

        discriminant = Math.sqrt(discriminant);
        double t1 = (-b - discriminant) / (2*a);
        double t2 = (-b + discriminant) / (2*a);

        return (t1 >= 0 && t1 <= 1) || (t2 >= 0 && t2 <= 1);
    }

    public static void main(String[] args) {
        int[][] Adir = generateDirectedMatrix();
        int[][] Aundir = makeUndirectedMatrix(Adir);
        List<Point> positions = calculatePositions(N);

        System.out.println("Directed Adjacency Matrix (Adir):");
        printMatrix(Adir);

        System.out.println("\nUndirected Adjacency Matrix (Aundir):");
        printMatrix(Aundir);

        JFrame frame = new JFrame("Graph Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GraphVisualization graphPanel = new GraphVisualization(Aundir, Adir, positions);

        // Create control panel with buttons
        JPanel controlPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton directedButton = new JButton("Show Directed Graph");
        JButton undirectedButton = new JButton("Show Undirected Graph");
        JButton clearButton = new JButton("Clear All");

        directedButton.addActionListener(e -> {
            if (!graphPanel.showDirected) {
                graphPanel.showDirected = true;
                graphPanel.showUndirected = false;
                directedButton.setText("Hide Directed Graph");
                undirectedButton.setText("Show Undirected Graph");
            } else {
                graphPanel.showDirected = false;
                directedButton.setText("Show Directed Graph");
            }
            graphPanel.repaint();
        });

        undirectedButton.addActionListener(e -> {
            if (!graphPanel.showUndirected) {
                graphPanel.showUndirected = true;
                graphPanel.showDirected = false;
                undirectedButton.setText("Hide Undirected Graph");
                directedButton.setText("Show Directed Graph");
            } else {
                graphPanel.showUndirected = false;
                undirectedButton.setText("Show Undirected Graph");
            }
            graphPanel.repaint();
        });

        clearButton.addActionListener(e -> {
            graphPanel.showDirected = false;
            graphPanel.showUndirected = false;
            directedButton.setText("Show Directed Graph");
            undirectedButton.setText("Show Undirected Graph");
            graphPanel.repaint();
        });

        // Style buttons
        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        directedButton.setFont(buttonFont);
        undirectedButton.setFont(buttonFont);
        clearButton.setFont(buttonFont);

        directedButton.setBackground(new Color(220, 220, 255));
        undirectedButton.setBackground(new Color(220, 220, 255));
        clearButton.setBackground(new Color(255, 200, 200));

        controlPanel.add(directedButton);
        controlPanel.add(undirectedButton);
        controlPanel.add(clearButton);

        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(graphPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static int[][] generateDirectedMatrix() {
        int n3 = (int)(VARIANT / 10) % 10;
        int n4 = (int)VARIANT % 10;
        float K = calculateCoefficient(n3, n4);

        Random rnd = new Random(VARIANT);
        int[][] Adir = new int[N][N];

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                float randomValue = rnd.nextFloat() * 2.0f;
                Adir[i][j] = (randomValue * K < 1.0f) ? 0 : 1;
            }
        }

        return Adir;
    }

    public static int[][] makeUndirectedMatrix(int[][] Adir) {
        int n = Adir.length;
        int[][] Aundir = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                if (i == j) {
                    Aundir[i][j] = Adir[i][j];
                } else {
                    int edge = (Adir[i][j] == 1 || Adir[j][i] == 1) ? 1 : 0;
                    Aundir[i][j] = edge;
                    Aundir[j][i] = edge;
                }
            }
        }

        return Aundir;
    }

    private static float calculateCoefficient(int n3, int n4) {
        return 1.0f - n3 * 0.02f - n4 * 0.005f - 0.25f;
    }

    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }

    private static List<Point> calculatePositions(int n) {
        int rows = 2;
        int cols = 5;
        int padX = 100;
        int padY = 150;

        List<Point> positions = new ArrayList<>(Math.min(n, rows * cols));

        for (int idx = 0; idx < n && idx < rows * cols; idx++) {
            int row = idx / cols;
            int col = idx % cols;
            int x = padX * (col + 1);
            int y = padY * (row + 1);
            positions.add(new Point(x, y));
        }

        return positions;
    }
}