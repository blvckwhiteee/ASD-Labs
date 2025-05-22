import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class GraphVisualization extends JPanel {
    private static final int N = 10;
    private static final long VARIANT = 4312L;
    private final int[][] Adir;
    private final List<Point> positions;
    private boolean showDirected = true;
    private final int scaleFactor = 2;
    private List<Integer> bfsOrder;
    private int[] bfsParent;
    private List<Integer> dfsOrder;
    private int[] dfsParent;
    private int currentStep = 0;
    private String traversalType = null;
    private JTextArea protocolArea;

    private static final Color NODE_FILL = new Color(83, 83, 83);
    private static final Color NODE_BORDER = Color.BLACK;
    private static final Color DIRECTED_EDGE = new Color(70, 70, 70);
    private static final Color ARROW_HEAD = new Color(70, 70, 70);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color HIGHLIGHT_NODE = Color.RED;
    private static final Color HIGHLIGHT_EDGE = Color.BLUE;

    public GraphVisualization(int[][] Adir, List<Point> positions, JTextArea protocolArea) {
        this.Adir = Adir;
        this.positions = positions;
        this.protocolArea = protocolArea;

        for (Point p : positions) {
            p.x *= scaleFactor;
            p.y *= scaleFactor;
        }

        setPreferredSize(new Dimension(1200, 800));
        setBackground(Color.WHITE);

        int start = findStartingVertex();
        if (start != -1) {
            System.out.println("Starting vertex: " + (start + 1));
            System.out.println("=== BFS steps ===");
            computeBFS(start);
            System.out.println("\n=== DFS steps ===");
            computeDFS(start);
        } else {
            bfsOrder = new ArrayList<>();
            dfsOrder = new ArrayList<>();
            bfsParent = new int[N];
            dfsParent = new int[N];
            Arrays.fill(bfsParent, -1);
            Arrays.fill(dfsParent, -1);
            System.out.println("No edges in the graph.");
        }
    }

    private int findStartingVertex() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (Adir[i][j] == 1)
                    return i;
            }
        }
        return -1;
    }

    private void computeBFS(int start) {
        boolean[] visited = new boolean[N];
        bfsOrder = new ArrayList<>();
        bfsParent = new int[N];
        Arrays.fill(bfsParent, -1);
        Queue<Integer> queue = new LinkedList<>();
        queue.add(start);
        visited[start] = true;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            bfsOrder.add(u);
            if (u != 0)
                System.out.println((bfsParent[u] + 1) + " -> " + (u + 1));

            for (int v = 0; v < N; v++) {
                if (Adir[u][v] == 1 && !visited[v]) {
                    queue.add(v);
                    visited[v] = true;
                    bfsParent[v] = u;
                }
            }
        }
        System.out.println("\nBFS Traversal Tree Adjacency Matrix:");
        printMatrix(buildTraversalTreeMatrix(bfsParent));
    }

    private void computeDFS(int start) {
        boolean[] visited = new boolean[N];
        dfsOrder = new ArrayList<>();
        dfsParent = new int[N];
        Arrays.fill(dfsParent, -1);
        dfs(start, visited);
        System.out.println("\nDFS Traversal Tree Adjacency Matrix:");
        printMatrix(buildTraversalTreeMatrix(dfsParent));
    }

    private void dfs(int u, boolean[] visited) {
        visited[u] = true;
        dfsOrder.add(u);
        for (int v = 0; v < N; v++) {
            if (Adir[u][v] == 1) {
                if (!visited[v]) {
                    System.out.println((u + 1) + " → " + (v + 1));
                    dfsParent[v] = u;
                    dfs(v, visited);
                }
            }
        }
    }

    private int[][] buildTraversalTreeMatrix(int[] parent) {
        int[][] traversalTreeMatrix = new int[N][N];
        for (int v = 0; v < N; v++) {
            if (parent[v] != -1) {
                int u = parent[v];
                traversalTreeMatrix[u][v] = 1; // Ребро от родителя к вершине (ориентированное)
            }
        }
        return traversalTreeMatrix;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Set<Integer> highlightedNodes = new HashSet<>();
        Set<String> highlightedEdges = new HashSet<>();

        if (showDirected && traversalType != null) {
            List<Integer> order = traversalType.equals("BFS") ? bfsOrder : dfsOrder;
            int[] parent = traversalType.equals("BFS") ? bfsParent : dfsParent;
            if (currentStep > 0) {
                for (int i = 0; i < currentStep; i++) {
                    highlightedNodes.add(order.get(i));
                }
                for (int i = 1; i < currentStep; i++) {
                    int v = order.get(i);
                    if (parent[v] != -1) {
                        highlightedEdges.add(parent[v] + "->" + v);
                    }
                }
            }
        }

        if (showDirected) {
            drawGraph(g2d, Adir, true, 0, highlightedNodes, highlightedEdges);
            drawTitle(g2d, "Directed Graph", 350, 40);
        }
    }

    private void drawTitle(Graphics2D g2d, String text, int x, int y) {
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString(text, x, y);
    }

    private void drawGraph(Graphics2D g2d, int[][] matrix, boolean directed, int yOffset, Set<Integer> highlightedNodes,
            Set<String> highlightedEdges) {
        drawEdges(g2d, matrix, directed, yOffset, highlightedEdges);
        drawNodes(g2d, yOffset, highlightedNodes);
    }

    private void drawNodes(Graphics2D g2d, int yOffset, Set<Integer> highlightedNodes) {
        g2d.setStroke(new BasicStroke(3));
        for (int i = 0; i < positions.size(); i++) {
            Point p = positions.get(i);
            int sz = 40 * scaleFactor;
            int x = p.x - sz / 2, y = p.y + yOffset - sz / 2;
            g2d.setColor(highlightedNodes.contains(i) ? HIGHLIGHT_NODE : NODE_FILL);
            g2d.fillOval(x, y, sz, sz);
            g2d.setColor(NODE_BORDER);
            g2d.drawOval(x, y, sz, sz);
            drawNodeLabel(g2d, i + 1, p.x, p.y + yOffset);
        }
    }

    private void drawNodeLabel(Graphics2D g2d, int label, int cx, int cy) {
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 16 * scaleFactor));
        String s = String.valueOf(label);
        FontMetrics fm = g2d.getFontMetrics();
        int w = fm.stringWidth(s), h = fm.getAscent();
        g2d.drawString(s, cx - w / 2, cy + h / 4);
    }

    private void drawEdges(Graphics2D g2d, int[][] m, boolean dir, int yOffset, Set<String> highlightedEdges) {
        boolean[][] bidir = new boolean[N][N];
        if (dir) {
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++)
                    if (m[i][j] == 1 && m[j][i] == 1 && i != j)
                        bidir[i][j] = bidir[j][i] = true;
        }

        for (int i = 0; i < N; i++) {
            for (int j = dir ? 0 : i; j < N; j++) {
                if (m[i][j] != 1)
                    continue;
                Point p1 = translate(positions.get(i), yOffset);
                Point p2 = translate(positions.get(j), yOffset);

                if (i == j) {
                    drawSelfLoop(g2d, positions.get(i), dir, yOffset);
                    continue;
                }

                boolean intersects = false;
                for (int k = 0; k < positions.size(); k++) {
                    if (k == i || k == j)
                        continue;
                    Point pk = translate(positions.get(k), yOffset);
                    if (lineIntersectsCircle(p1, p2, pk, 20 * scaleFactor)) {
                        intersects = true;
                        break;
                    }
                }

                String edgeKey = i + "->" + j;
                if (highlightedEdges.contains(edgeKey)) {
                    g2d.setColor(HIGHLIGHT_EDGE);
                    g2d.setStroke(new BasicStroke(5));
                } else {
                    g2d.setColor(DIRECTED_EDGE);
                    g2d.setStroke(new BasicStroke(3));
                }

                if (dir && bidir[i][j] && i < j) {
                    drawBidirectionalEdge(g2d, p1, p2);
                } else if (dir) {
                    if (intersects)
                        drawCurvedArrow(g2d, p1, p2);
                    else if (!bidir[j][i])
                        drawArrow(g2d, p1, p2);
                } else if (i < j) {
                    if (intersects)
                        drawCurvedLine(g2d, p1, p2);
                    else
                        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                }

                if (highlightedEdges.contains(edgeKey)) {
                    g2d.setStroke(new BasicStroke(3));
                }
            }
        }
    }

    private Point translate(Point p, int yOff) {
        return new Point(p.x, p.y + yOff);
    }

    private void drawCurvedLine(Graphics2D g2d, Point p1, Point p2) {
        QuadCurve2D qc = createCurve(p1, p2);
        g2d.draw(qc);
    }

    private void drawCurvedArrow(Graphics2D g2d, Point p1, Point p2) {
        QuadCurve2D qc = createCurve(p1, p2);
        g2d.draw(qc);
        drawArrowHeadOnCurve(g2d, qc, 0.9);
    }

    private QuadCurve2D createCurve(Point p1, Point p2) {
        double midX = (p1.x + p2.x) / 2.0;
        double midY = (p1.y + p2.y) / 2.0;
        double dx = p2.x - p1.x, dy = p2.y - p1.y;
        double len = Math.hypot(dx, dy);
        double nx = -dy / len, ny = dx / len;
        double offset = len * 0.2;
        double cx = midX + nx * offset;
        double cy = midY + ny * offset;
        return new QuadCurve2D.Double(p1.x, p1.y, cx, cy, p2.x, p2.y);
    }

    private void drawBidirectionalEdge(Graphics2D g2d, Point p1, Point p2) {
        double midX = (p1.x + p2.x) / 2.0;
        double midY = (p1.y + p2.y) / 2.0;
        double dx = p2.x - p1.x, dy = p2.y - p1.y;
        double length = Math.sqrt(dx * dx + dy * dy);
        double nx = -dy / length, ny = dx / length;
        double curveHeight = length * 0.3;

        QuadCurve2D curve1 = new QuadCurve2D.Double(p1.x, p1.y, midX + nx * curveHeight, midY + ny * curveHeight, p2.x,
                p2.y);
        g2d.draw(curve1);
        drawArrowHeadOnCurve(g2d, curve1, 0.9);

        QuadCurve2D curve2 = new QuadCurve2D.Double(p2.x, p2.y, midX - nx * curveHeight, midY - ny * curveHeight, p1.x,
                p1.y);
        g2d.draw(curve2);
        drawArrowHeadOnCurve(g2d, curve2, 0.9);
    }

    private void drawArrow(Graphics2D g2d, Point p1, Point p2) {
        double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);
        int nodeRadius = 20 * scaleFactor;
        int endX = (int) (p2.x - nodeRadius * Math.cos(angle));
        int endY = (int) (p2.y - nodeRadius * Math.sin(angle));
        g2d.drawLine(p1.x, p1.y, endX, endY);
        drawArrowHead(g2d, new Point(endX, endY), angle);
    }

    private void drawArrowHeadOnCurve(Graphics2D g2d, QuadCurve2D curve, double t) {
        Point2D arrowPoint = getPointOnCurve(curve, t);
        Point2D beforePoint = getPointOnCurve(curve, t - 0.05);
        double angle = Math.atan2(arrowPoint.getY() - beforePoint.getY(), arrowPoint.getX() - beforePoint.getX());
        drawArrowHead(g2d, new Point((int) arrowPoint.getX(), (int) arrowPoint.getY()), angle);
    }

    private Point2D getPointOnCurve(QuadCurve2D curve, double t) {
        double x = Math.pow(1 - t, 2) * curve.getX1() + 2 * (1 - t) * t * curve.getCtrlX() + t * t * curve.getX2();
        double y = Math.pow(1 - t, 2) * curve.getY1() + 2 * (1 - t) * t * curve.getCtrlY() + t * t * curve.getY2();
        return new Point2D.Double(x, y);
    }

    private void drawArrowHead(Graphics2D g2d, Point tip, double angle) {
        int arrowSize = 20;
        double angle1 = angle + Math.toRadians(150);
        double angle2 = angle - Math.toRadians(150);
        int[] xPoints = { tip.x, (int) (tip.x + arrowSize * Math.cos(angle1)),
                (int) (tip.x + arrowSize * Math.cos(angle2)) };
        int[] yPoints = { tip.y, (int) (tip.y + arrowSize * Math.sin(angle1)),
                (int) (tip.y + arrowSize * Math.sin(angle2)) };
        g2d.setColor(ARROW_HEAD);
        g2d.fillPolygon(xPoints, yPoints, 3);
    }

    private void drawSelfLoop(Graphics2D g2d, Point center, boolean directed, int yOffset) {
        int nodeRadius = 30 * scaleFactor;
        int loopRadius = 30 * scaleFactor;
        int loopX = center.x - loopRadius / 2;
        int loopY = center.y + yOffset - nodeRadius - loopRadius + 40;
        g2d.drawOval(loopX, loopY, loopRadius, loopRadius);
        if (directed) {
            double angle = Math.toRadians(65);
            int arrowX = loopX + 5;
            int arrowY = center.y - 33;
            drawArrowHead(g2d, new Point(arrowX, arrowY), angle);
        }
    }

    private boolean lineIntersectsCircle(Point p1, Point p2, Point center, int radius) {
        Point2D.Double a = new Point2D.Double(p1.x, p1.y);
        Point2D.Double b = new Point2D.Double(p2.x, p2.y);
        Point2D.Double c = new Point2D.Double(center.x, center.y);
        Point2D.Double d = new Point2D.Double(b.x - a.x, b.y - a.y);
        Point2D.Double f = new Point2D.Double(a.x - c.x, a.y - c.y);
        double A = d.x * d.x + d.y * d.y;
        double B = 2 * (f.x * d.x + f.y * d.y);
        double C = f.x * f.x + f.y * f.y - radius * radius;
        double disc = B * B - 4 * A * C;
        if (disc < 0)
            return false;
        disc = Math.sqrt(disc);
        double t1 = (-B - disc) / (2 * A);
        double t2 = (-B + disc) / (2 * A);
        return (t1 >= 0 && t1 <= 1) || (t2 >= 0 && t2 <= 1);
    }

    public static int[][] generateDirectedMatrix() {
        int n3 = (int) (VARIANT / 10) % 10;
        int n4 = (int) VARIANT % 10;
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

    private static float calculateCoefficient(int n3, int n4) {
        return (1.0f - n3 * 0.01f - n4 * 0.005f - 0.15f);
    }

    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix)
            System.out.println(Arrays.toString(row));
    }

    private static List<Point> calculatePositions(int n) {
        int rows = 2, cols = 5;
        int padX = 100, padY = 150;
        List<Point> positions = new ArrayList<>(Math.min(n, rows * cols));
        for (int idx = 0; idx < n && idx < rows * cols; idx++) {
            int row = idx / cols;
            int col = idx % cols;
            positions.add(new Point(padX * (col + 1), padY * (row + 1)));
        }
        return positions;
    }

    public static void main(String[] args) {
        int[][] Adir = generateDirectedMatrix();
        List<Point> positions = calculatePositions(N);

        System.out.println("Directed Adjacency Matrix (Adir):");
        printMatrix(Adir);

        JFrame frame = new JFrame("Graph Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextArea protocolArea = new JTextArea(10, 50);
        protocolArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(protocolArea);

        GraphVisualization graphPanel = new GraphVisualization(Adir, positions, protocolArea);

        JPanel controlPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton directedButton = new JButton("Hide Directed Graph");
        JButton nextBFSButton = new JButton("Next BFS Step");
        JButton nextDFSButton = new JButton("Next DFS Step");
        JButton resetTraversalButton = new JButton("Reset Traversal");

        directedButton.addActionListener(e -> {
            if (!graphPanel.showDirected) {
                graphPanel.showDirected = true;
                directedButton.setText("Hide Directed Graph");
            } else {
                graphPanel.showDirected = false;
                directedButton.setText("Show Directed Graph");
            }
            graphPanel.repaint();
        });

        nextBFSButton.addActionListener(e -> {
            if (graphPanel.showDirected) {
                if (!"BFS".equals(graphPanel.traversalType)) {
                    graphPanel.traversalType = "BFS";
                    graphPanel.currentStep = 0;
                    protocolArea.setText("BFS Traversal Started\n");
                }
                if (graphPanel.currentStep < graphPanel.bfsOrder.size()) {
                    int vertex = graphPanel.bfsOrder.get(graphPanel.currentStep);
                    protocolArea.append(
                            "Step " + (graphPanel.currentStep + 1) + ": Visiting vertex " + (vertex + 1) + "\n");
                    graphPanel.currentStep++;
                    graphPanel.repaint();
                }
            }
        });

        nextDFSButton.addActionListener(e -> {
            if (graphPanel.showDirected) {
                if (!"DFS".equals(graphPanel.traversalType)) {
                    graphPanel.traversalType = "DFS";
                    graphPanel.currentStep = 0;
                    protocolArea.setText("DFS Traversal Started\n");
                }
                if (graphPanel.currentStep < graphPanel.dfsOrder.size()) {
                    int vertex = graphPanel.dfsOrder.get(graphPanel.currentStep);
                    protocolArea.append(
                            "Step " + (graphPanel.currentStep + 1) + ": Visiting vertex " + (vertex + 1) + "\n");
                    graphPanel.currentStep++;
                    graphPanel.repaint();
                }
            }
        });

        resetTraversalButton.addActionListener(e -> {
            graphPanel.currentStep = 0;
            graphPanel.traversalType = null;
            protocolArea.setText("");
            graphPanel.repaint();
        });

        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        directedButton.setFont(buttonFont);
        nextBFSButton.setFont(buttonFont);
        nextDFSButton.setFont(buttonFont);
        resetTraversalButton.setFont(buttonFont);

        directedButton.setBackground(new Color(220, 220, 255));
        nextBFSButton.setBackground(new Color(200, 255, 200));
        nextDFSButton.setBackground(new Color(200, 255, 200));
        resetTraversalButton.setBackground(new Color(255, 200, 200));

        controlPanel.add(directedButton);
        controlPanel.add(resetTraversalButton);
        controlPanel.add(nextBFSButton);
        controlPanel.add(nextDFSButton);
        

        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(graphPanel, BorderLayout.CENTER);
        frame.add(scrollPane, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}