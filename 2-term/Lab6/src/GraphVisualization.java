import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class GraphVisualization extends JPanel {
    private static final int N = 10;
    private static final long VARIANT = 4312L;
    private final int[][] Aundir;
    private final int[][] W;
    private final List<Point> positions;
    private final List<Edge> mstEdges;
    private int currentStep;
    private final int scaleFactor = 2;

    private static final Color NODE_FILL = new Color(83, 83, 83);
    private static final Color NODE_BORDER = Color.BLACK;
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Font FONT = new Font("Arial", Font.BOLD, 16);

    private GraphVisualization(int[][] Aundir, int[][] W, List<Point> positions, List<Edge> mstEdges) {
        this.Aundir = Aundir;
        this.W = W;
        this.positions = positions;
        this.mstEdges = mstEdges;
        this.currentStep = 0;

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

        Set<String> mstSet = new HashSet<>();
        int totalWeight = 0;
        for (int k = 0; k < currentStep && k < mstEdges.size(); k++) {
            Edge e = mstEdges.get(k);
            int u = Math.min(e.u, e.v);
            int v = Math.max(e.u, e.v);
            mstSet.add(u + "-" + v);
            totalWeight += e.weight;
        }

        drawEdges(g2d, mstSet);
        drawNodes(g2d);

        g2d.setColor(Color.BLACK);
        g2d.setFont(FONT);
        g2d.drawString("Total weight: " + totalWeight, 50, 50);
    }

    private void drawEdges(Graphics2D g2d, Set<String> mstSet) {
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < N; i++) {
            if (Aundir[i][i] == 1) {
                g2d.setColor(Color.GRAY);
                // drawSelfLoop(g2d, positions.get(i));
            }
            for (int j = i + 1; j < N; j++) {
                if (Aundir[i][j] == 1) {
                    Point p1 = positions.get(i);
                    Point p2 = positions.get(j);
                    String edgeKey = i + "-" + j;
                    boolean isMst = mstSet.contains(edgeKey);
                    g2d.setColor(isMst ? Color.ORANGE : Color.GRAY);

                    boolean intersects = false;
                    for (int k = 0; k < N; k++) {
                        if (k == i || k == j)
                            continue;
                        Point pk = positions.get(k);
                        int r = 20 * scaleFactor;
                        if (lineIntersectsCircle(p1, p2, pk, r)) {
                            intersects = true;
                            break;
                        }
                    }

                    int weight = W[i][j];
                    double t = (i + j) % 2 == 0 ? 0.4 : 0.6;
                    if (isMst) {
                        if (intersects) {
                            QuadCurve2D curve = createCurve(p1, p2);
                            g2d.draw(curve);
                            Point2D labelPoint = getPointOnCurve(curve, t);
                            drawWeightLabel(g2d, weight, (int) labelPoint.getX(), (int) labelPoint.getY() + 15);
                        } else {
                            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                            double mx = p1.x + t * (p2.x - p1.x);
                            double my = p1.y + t * (p2.y - p1.y);
                            drawWeightLabel(g2d, weight, (int) mx, (int) my);
                        }
                    }
                }
            }
        }
    }

    private void drawWeightLabel(Graphics2D g2d, int weight, int x, int y) {
        String s = String.valueOf(weight);
        FontMetrics fm = g2d.getFontMetrics();
        int w = fm.stringWidth(s);
        int h = fm.getAscent();
        int pad = 2;
        g2d.setFont(FONT);
        g2d.setColor(new Color(255, 255, 255));
        g2d.fillRect(x - w / 2 - pad, y - h / 2 - pad, w + 4 * pad, h + 4 * pad);
        g2d.setColor(Color.BLACK);
        g2d.drawString(s, x - w / 2, y + h / 2);
    }

    private void drawNodes(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(3));
        for (int i = 0; i < N; i++) {
            Point p = positions.get(i);
            int sz = 40 * scaleFactor;
            int x = p.x - sz / 2, y = p.y - sz / 2;
            g2d.setColor(NODE_FILL);
            g2d.fillOval(x, y, sz, sz);
            g2d.setColor(NODE_BORDER);
            g2d.drawOval(x, y, sz, sz);
            drawNodeLabel(g2d, i + 1, p.x, p.y);
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

    private Point2D getPointOnCurve(QuadCurve2D curve, double t) {
        double x = Math.pow(1 - t, 2) * curve.getX1() + 2 * (1 - t) * t * curve.getCtrlX() + t * t * curve.getX2();
        double y = Math.pow(1 - t, 2) * curve.getY1() + 2 * (1 - t) * t * curve.getCtrlY() + t * t * curve.getY2();
        return new Point2D.Double(x, y);
    }

    private void drawSelfLoop(Graphics2D g2d, Point center) {
        int nodeRadius = 30 * scaleFactor;
        int loopRadius = 30 * scaleFactor;
        int loopX = center.x - loopRadius / 2;
        int loopY = center.y - nodeRadius - loopRadius + 40;
        g2d.drawOval(loopX, loopY, loopRadius, loopRadius);
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
        int n3 = 1, n4 = 2;
        float k = 1.0f - n3 * 0.01f - n4 * 0.005f - 0.05f;
        Random rnd = new Random(VARIANT);
        int[][] Adir = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                float val = rnd.nextFloat() * 2.0f * k;
                Adir[i][j] = (val >= 1.0f) ? 1 : 0;
            }
        }
        return Adir;
    }

    public static int[][] makeUndirectedMatrix(int[][] Adir) {
        int[][] Aundir = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = i; j < N; j++) {
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

    public static int[][] generateWeightMatrix(int[][] Aundir) {
        Random rnd = new Random(VARIANT);
        int[][] W = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (i == j) {
                    W[i][j] = 0;
                } else if (Aundir[i][j] == 1) {
                    double b = rnd.nextDouble() * 2.0;
                    int c = (int) Math.ceil(100 * b);
                    W[i][j] = c;
                    W[j][i] = c;
                } else {
                    W[i][j] = Integer.MAX_VALUE;
                    W[j][i] = Integer.MAX_VALUE;
                }
            }
        }
        return W;
    }

    public static List<Point> calculatePositions() {
        int cols = 5;
        int padX = 100, padY = 150;
        List<Point> positions = new ArrayList<>();
        for (int idx = 0; idx < N; idx++) {
            int row = idx / cols;
            int col = idx % cols;
            int x = padX * (col + 1);
            int y = padY * (row + 1);
            positions.add(new Point(x, y));
        }
        return positions;
    }

    public static void printMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            System.out.print("[");
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] == Integer.MAX_VALUE) {
                    System.out.print("∞");
                } else {
                    System.out.print(matrix[i][j]);
                }
                if (j < matrix[i].length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println("]");
        }
    }

    public static void main(String[] args) {
        int[][] Adir = generateDirectedMatrix();
        int[][] Aundir = makeUndirectedMatrix(Adir);
        int[][] W = generateWeightMatrix(Aundir);
        List<Point> positions = calculatePositions();

        Graph graph = new Graph();
        for (int i = 0; i < N; i++) {
            graph.addVertex();
        }
        for (int i = 0; i < N; i++) {
            for (int j = i + 1; j < N; j++) {
                if (Aundir[i][j] == 1) {
                    graph.addEdge(i, j, W[i][j]);
                }
            }
        }

        List<Edge> mstEdges = kruskal(graph, N);

        System.out.println("Undirected Adjacency Matrix (Aundir):");
        printMatrix(Aundir);
        System.out.println("\nWeight Matrix (W):");
        printMatrix(W);
        System.out.println("\nMST Edges:");
        for (Edge e : mstEdges) {
            System.out.println("(" + e.u + ", " + e.v + ", " + e.weight + ")");
        }

        JFrame frame = new JFrame("MST Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GraphVisualization panel = new GraphVisualization(Aundir, W, positions, mstEdges);

        JPanel controlPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton nextButton = new JButton("Next Step");
        JButton resetButton = new JButton("Reset");

        nextButton.addActionListener(e -> {
            if (panel.currentStep < panel.mstEdges.size()) {
                panel.currentStep++;
                panel.repaint();
            }
        });

        resetButton.addActionListener(e -> {
            panel.currentStep = 0;
            panel.repaint();
        });

        nextButton.setFont(FONT);
        resetButton.setFont(FONT);
        nextButton.setBackground(new Color(220, 220, 255));
        resetButton.setBackground(new Color(255, 200, 200));

        controlPanel.add(nextButton);
        controlPanel.add(resetButton);

        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static class Graph {
        private final List<List<int[]>> adjacencyList;
        private int numVertices;

        public Graph() {
            adjacencyList = new ArrayList<>();
            numVertices = 0;
        }

        public void addVertex() {
            adjacencyList.add(new ArrayList<>());
            numVertices++;
        }

        public void removeVertex(int vertex) {
            if (vertex < 0 || vertex >= numVertices)
                return;
            for (int i = 0; i < numVertices; i++) {
                if (i != vertex) {
                    adjacencyList.get(i).removeIf(edge -> edge[0] == vertex);
                }
            }
            adjacencyList.remove(vertex);
            numVertices--;
            for (List<int[]> list : adjacencyList) {
                for (int[] edge : list) {
                    if (edge[0] > vertex)
                        edge[0]--;
                }
            }
        }

        public void addEdge(int u, int v, int weight) {
            if (u < 0 || u >= numVertices || v < 0 || v >= numVertices || u == v)
                return;
            for (int[] edge : adjacencyList.get(u)) {
                if (edge[0] == v)
                    return;
            }
            adjacencyList.get(u).add(new int[] { v, weight });
            adjacencyList.get(v).add(new int[] { u, weight });
        }

        public void removeEdge(int u, int v) {
            if (u < 0 || u >= numVertices || v < 0 || v >= numVertices)
                return;
            adjacencyList.get(u).removeIf(edge -> edge[0] == v);
            adjacencyList.get(v).removeIf(edge -> edge[0] == u);
        }
    }

    private static class Edge implements Comparable<Edge> {
        int u, v, weight;

        public Edge(int u, int v, int weight) {
            this.u = u;
            this.v = v;
            this.weight = weight;
        }

        @Override
        public int compareTo(Edge other) {
            return Integer.compare(this.weight, other.weight);
        }
    }

    private static class UnionFind {
        private final int[] parent;

        public UnionFind(int size) {
            parent = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
            }
        }

        public int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }

        public boolean union(int x, int y) {
            int rootX = find(x);
            int rootY = find(y);
            if (rootX == rootY)
                return false;
            parent[rootX] = rootY;
            return true;
        }
    }

    private static List<Edge> kruskal(Graph graph, int numVertices) {
        List<Edge> allEdges = new ArrayList<>();
        for (int u = 0; u < numVertices; u++) {
            for (int[] neighbor : graph.adjacencyList.get(u)) {
                int v = neighbor[0];
                int weight = neighbor[1];
                if (u < v) {
                    allEdges.add(new Edge(u, v, weight));
                }
            }
        }
        Collections.sort(allEdges);
        UnionFind uf = new UnionFind(numVertices);
        List<Edge> mst = new ArrayList<>();
        for (Edge edge : allEdges) {
            if (uf.union(edge.u, edge.v)) {
                mst.add(edge);
                if (mst.size() == numVertices - 1)
                    break;
            }
        }
        return mst;
    }
}