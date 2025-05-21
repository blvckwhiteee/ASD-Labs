import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class GraphVisualization extends JPanel {
    private static final int N = 10;
    private static final long VARIANT = 4312L;
    private final int[][] Aundir;
    private final int[][] Adir;
    private final int[][] Adir2;
    private final List<Point> positions;
    private boolean showFirstDirected = false;
    private boolean showUndirected = false;
    private boolean showSecondDirected = false;
    private final int scaleFactor = 2;
    private static int[][] condensationMatrix;
    private static List<List<Integer>> sccs;
    private boolean showCondensation = false;

    private static final Color NODE_FILL = new Color(70, 70, 70);
    private static final Color NODE_BORDER = new Color(0, 0, 0);
    private static final Color EDGE_COLOR = new Color(70, 70, 70);
    private static final Color ARROW_HEAD = new Color(70, 70, 70);
    private static final Color BLUE_BUTTON_COLOR = new Color(220, 220, 255);
    private static final Color GREEN_BUTTON_COLOR = new Color(220, 255, 220);
    private static final Color RED_BUTTON_COLOR = new Color(255, 200, 200);
    private static final Color TEXT_COLOR = Color.WHITE;

    public GraphVisualization(int[][] Aundir, int[][] Adir, int[][] Adir2, List<Point> positions) {
        this.Aundir = Aundir;
        this.Adir = Adir;
        this.positions = positions;
        this.Adir2 = Adir2;

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

        if (showUndirected) {
            drawGraph(g2d, Aundir, false, 0);
            drawTitle(g2d, "Undirected Graph", 350, 40);
        }

        if (showFirstDirected) {
            drawGraph(g2d, Adir, true, showUndirected ? 400 : 0);
            drawTitle(g2d, "Directed Graph", 350, showUndirected ? 440 : 40);
        }

        if (showSecondDirected) {
            drawGraph(g2d, Adir2, true, 0);
        }

        if (showCondensation) {
            drawCondensationGraph(g2d, 0);
        }
    }

    private void drawTitle(Graphics2D g2d, String text, int x, int y) {
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString(text, x, y);
    }

    private void drawGraph(Graphics2D g2d, int[][] matrix, boolean directed, int yOffset) {
        drawEdges(g2d, matrix, directed, yOffset);
        drawNodes(g2d, yOffset);
    }

    private void drawNodes(Graphics2D g2d, int yOffset) {
        g2d.setStroke(new BasicStroke(3));

        for (int i = 0; i < positions.size(); i++) {
            Point p = positions.get(i);
            int sz = 40 * scaleFactor;
            int x = p.x - sz / 2, y = p.y + yOffset - sz / 2;
            g2d.setColor(NODE_FILL);
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

    private void drawEdges(Graphics2D g2d, int[][] m, boolean dir, int yOffset) {
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
                    int r = 20 * scaleFactor;
                    if (lineIntersectsCircle(p1, p2, pk, r)) {
                        intersects = true;
                        break;
                    }
                }

                if (dir && bidir[i][j] && i < j) {
                    g2d.setColor(EDGE_COLOR);
                    drawBidirectionalEdge(g2d, p1, p2);

                } else if (dir) {
                    g2d.setColor(EDGE_COLOR);
                    if (intersects)
                        drawCurvedArrow(g2d, p1, p2);
                    else if (!bidir[j][i])
                        drawArrow(g2d, p1, p2);

                } else if (i < j) {
                    g2d.setColor(EDGE_COLOR);
                    if (intersects)
                        drawCurvedLine(g2d, p1, p2);
                    else
                        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
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

        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double length = Math.sqrt(dx * dx + dy * dy);

        double nx = -dy / length;
        double ny = dx / length;

        double curveHeight = length * 0.3;

        double ctrl1X = midX + nx * curveHeight;
        double ctrl1Y = midY + ny * curveHeight;
        QuadCurve2D curve1 = new QuadCurve2D.Double(
                p1.x, p1.y,
                ctrl1X, ctrl1Y,
                p2.x, p2.y);
        g2d.draw(curve1);
        drawArrowHeadOnCurve(g2d, curve1, 0.9);

        double ctrl2X = midX - nx * curveHeight;
        double ctrl2Y = midY - ny * curveHeight;
        QuadCurve2D curve2 = new QuadCurve2D.Double(
                p2.x, p2.y,
                ctrl2X, ctrl2Y,
                p1.x, p1.y);
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

        double angle = Math.atan2(
                arrowPoint.getY() - beforePoint.getY(),
                arrowPoint.getX() - beforePoint.getX());

        drawArrowHead(g2d,
                new Point((int) arrowPoint.getX(), (int) arrowPoint.getY()),
                angle);
    }

    private Point2D getPointOnCurve(QuadCurve2D curve, double t) {
        double x = Math.pow(1 - t, 2) * curve.getX1() +
                2 * (1 - t) * t * curve.getCtrlX() +
                t * t * curve.getX2();
        double y = Math.pow(1 - t, 2) * curve.getY1() +
                2 * (1 - t) * t * curve.getCtrlY() +
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

    public static int[][] generateDirectedMatrix(boolean isFirst) {
        int n3 = (int) (VARIANT / 10) % 10;
        int n4 = (int) VARIANT % 10;
        float K = calculateCoefficient(n3, n4, isFirst);

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

    private static float calculateCoefficient(int n3, int n4, boolean isFirst) {
        return isFirst ? (1.0f - n3 * 0.01f - n4 * 0.01f - 0.3f) : (1.0f - n3 * 0.005f - n4 * 0.005f - 0.27f);
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

    public static void analyzeGraphs(int[][] firstDirectedMatrix, int[][] undirectedMatrix,
            int[][] secondDirectedMatrix) {
        System.out.println("=== First Directed Graph Properties ===");
        analyzeDirectedGraph(firstDirectedMatrix);
        System.out.println("\n=== Undirected Graph Properties ===");
        analyzeUndirectedGraph(undirectedMatrix);
        System.out.println("\n=== Second Directed Graph Properties ===");
        analyzeDirectedGraph(secondDirectedMatrix);
    }

    private static void analyzeDirectedGraph(int[][] matrix) {
        int n = matrix.length;
        int[] outDegrees = new int[n];
        int[] inDegrees = new int[n];
        int[] totalDegrees = new int[n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                outDegrees[i] += matrix[i][j];
                inDegrees[i] += matrix[j][i];
            }
            totalDegrees[i] = inDegrees[i] + outDegrees[i];
        }

        System.out.println("Vertex Degrees (in + out):");
        for (int i = 0; i < n; i++) {
            System.out.printf("Vertex %d: in=%d, out=%d, total=%d%n",
                    i + 1, inDegrees[i], outDegrees[i], totalDegrees[i]);
        }

        checkRegular(inDegrees, outDegrees, true);
        findSpecialVertices(totalDegrees);
    }

    private static void analyzeUndirectedGraph(int[][] matrix) {
        int n = matrix.length;
        int[] degrees = new int[n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                degrees[i] += matrix[i][j];
            }
        }

        System.out.println("Vertex Degrees:");
        for (int i = 0; i < n; i++) {
            System.out.printf("Vertex %d: degree=%d%n", i + 1, degrees[i]);
        }

        checkRegular(degrees, degrees, false);
        findSpecialVertices(degrees);
    }

    private static void checkRegular(int[] degrees1, int[] degrees2, boolean isDirected) {
        boolean isRegular = true;
        int firstDegree = degrees1[0] + (isDirected ? degrees2[0] : 0);

        for (int i = 1; i < degrees1.length; i++) {
            int currentDegree = degrees1[i] + (isDirected ? degrees2[i] : 0);
            if (currentDegree != firstDegree) {
                isRegular = false;
                break;
            }
        }

        if (isRegular) {
            if (isDirected) {
                System.out.printf("The graph is regular with in-degree=%d and out-degree=%d%n",
                        degrees1[0], degrees2[0]);
            } else {
                System.out.printf("The graph is regular with degree=%d%n", degrees1[0]);
            }
        } else {
            System.out.println("The graph is not regular");
        }
    }

    private static void findSpecialVertices(int[] degrees) {
        List<Integer> hangingVertices = new ArrayList<>();
        List<Integer> isolatedVertices = new ArrayList<>();

        for (int i = 0; i < degrees.length; i++) {
            if (degrees[i] == 1) {
                hangingVertices.add(i);
            } else if (degrees[i] == 0) {
                isolatedVertices.add(i);
            }
        }

        System.out.println("Hanging vertices (degree=1): " + hangingVertices);
        System.out.println("Isolated vertices (degree=0): " + isolatedVertices);
    }

    public static int[][] multiplyMatrices(int[][] a, int[][] b) {
        int n = a.length;
        int[][] result = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }

    public static int[][] matrixPower(int[][] matrix, int power) {
        int n = matrix.length;
        int[][] result = new int[n][n];

        for (int i = 0; i < n; i++) {
            result[i][i] = 1;
        }

        for (int p = 0; p < power; p++) {
            result = multiplyMatrices(result, matrix);
        }

        return result;
    }

    public static List<String> findAllPaths(int[][] adjMatrix, int length) {
        List<String> paths = new ArrayList<>();
        int n = adjMatrix.length;

        if (length == 2) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < n; k++) {
                        if (adjMatrix[i][k] == 1 && adjMatrix[k][j] == 1) {
                            paths.add((i + 1) + " → " + (k + 1) + " → " + (j + 1));
                        }
                    }
                }
            }
        } else if (length == 3) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < n; k++) {
                        for (int m = 0; m < n; m++) {
                            if (adjMatrix[i][k] == 1 && adjMatrix[k][m] == 1 && adjMatrix[m][j] == 1) {
                                paths.add((i + 1) + " → " + (k + 1) + " → " + (m + 1) + " → " + (j + 1));
                            }
                        }
                    }
                }
            }
        }

        return paths;
    }

    public static void printAllPaths(int[][] adjMatrix) {
        System.out.println("\n=== Paths Analysis ===");

        int[][] A2 = matrixPower(adjMatrix, 2);
        int[][] A3 = matrixPower(adjMatrix, 3);

        System.out.println("\nMatrix A² (paths of length 2 counts):");
        printMatrix(A2);

        System.out.println("\nMatrix A³ (paths of length 3 counts):");
        printMatrix(A3);

        System.out.println("\nAll paths of length 2:");
        List<String> paths2 = findAllPaths(adjMatrix, 2);
        for (String path : paths2) {
            System.out.print(path + "\t");
        }

        System.out.println("\nAll paths of length 3:");
        List<String> paths3 = findAllPaths(adjMatrix, 3);
        for (String path : paths3) {
            System.out.print(path + "\t");
        }
    }

    public static int[][] computeReachabilityMatrix(int[][] adjMatrix) {
        int n = adjMatrix.length;
        int[][] reachability = new int[n][n];

        for (int i = 0; i < n; i++) {
            System.arraycopy(adjMatrix[i], 0, reachability[i], 0, n);
        }

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (reachability[i][j] == 0) {
                        reachability[i][j] = (reachability[i][k] == 1 && reachability[k][j] == 1) ? 1 : 0;
                    }
                }
            }
        }

        return reachability;
    }

    public static int[][] computeStrongConnectivityMatrix(int[][] adjMatrix) {
        int n = adjMatrix.length;
        int[][] reachability = computeReachabilityMatrix(adjMatrix);
        int[][] strongConnectivity = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                strongConnectivity[i][j] = (reachability[i][j] == 1 && reachability[j][i] == 1) ? 1 : 0;
            }
        }

        return strongConnectivity;
    }

    public static List<List<Integer>> findStronglyConnectedComponents(int[][] adjMatrix) {
        int n = adjMatrix.length;
        boolean[] visited = new boolean[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                dfs(adjMatrix, i, visited, stack);
            }
        }

        int[][] transposed = transposeMatrix(adjMatrix);

        Arrays.fill(visited, false);
        List<List<Integer>> sccs = new ArrayList<>();

        while (!stack.isEmpty()) {
            int v = stack.pop();
            if (!visited[v]) {
                List<Integer> component = new ArrayList<>();
                dfs(transposed, v, visited, component);
                Collections.sort(component);
                sccs.add(component);
            }
        }

        return sccs;
    }

    private static void dfs(int[][] matrix, int v, boolean[] visited, Stack<Integer> stack) {
        visited[v] = true;
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[v][i] == 1 && !visited[i]) {
                dfs(matrix, i, visited, stack);
            }
        }
        stack.push(v);
    }

    private static void dfs(int[][] matrix, int v, boolean[] visited, List<Integer> component) {
        visited[v] = true;
        component.add(v + 1);
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[v][i] == 1 && !visited[i]) {
                dfs(matrix, i, visited, component);
            }
        }
    }

    private static int[][] transposeMatrix(int[][] matrix) {
        int n = matrix.length;
        int[][] transposed = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                transposed[j][i] = matrix[i][j];
            }
        }
        return transposed;
    }

    public static void printConnectivityAnalysis(int[][] adjMatrix) {
        System.out.println("\n=== Graph Connectivity Analysis ===");

        int[][] reachability = computeReachabilityMatrix(adjMatrix);
        System.out.println("\nReachability Matrix (Transitive Closure):");
        printMatrix(reachability);

        int[][] strongConnectivity = computeStrongConnectivityMatrix(adjMatrix);
        System.out.println("\nStrong Connectivity Matrix:");
        printMatrix(strongConnectivity);

        List<List<Integer>> sccs = findStronglyConnectedComponents(adjMatrix);
        System.out.println("\nStrongly Connected Components:");
        for (int i = 0; i < sccs.size(); i++) {
            System.out.println("Component " + (i + 1) + ": " + sccs.get(i));
        }
    }

    public static void buildCondensationGraph(int[][] adjMatrix) {
        sccs = findStronglyConnectedComponents(adjMatrix);
        condensationMatrix = new int[sccs.size()][sccs.size()];

        Map<Integer, Integer> vertexToComponent = new HashMap<>();
        for (int i = 0; i < sccs.size(); i++) {
            for (int vertex : sccs.get(i)) {
                vertexToComponent.put(vertex, i);
            }
        }

        for (int i = 0; i < sccs.size(); i++) {
            for (int srcVertex : sccs.get(i)) {
                for (int j = 0; j < adjMatrix[srcVertex - 1].length; j++) {
                    if (adjMatrix[srcVertex - 1][j] == 1) {
                        int destVertex = j + 1;
                        int destComponent = vertexToComponent.get(destVertex);
                        if (i != destComponent) {
                            condensationMatrix[i][destComponent] = 1;
                        }
                    }
                }
            }
        }
    }

    private void drawCondensationGraph(Graphics2D g2d, int yOffset) {
        if (condensationMatrix == null || sccs == null)
            return;

        int centerX = 600;
        int centerY = yOffset + 300;
        int radius = 150;
        List<Point> componentPositions = new ArrayList<>();

        for (int i = 0; i < sccs.size(); i++) {
            double angle = 2 * Math.PI * i / sccs.size();
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));
            componentPositions.add(new Point(x, y));
        }

        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(2));

        for (int i = 0; i < condensationMatrix.length; i++) {
            for (int j = 0; j < condensationMatrix[i].length; j++) {
                if (condensationMatrix[i][j] == 1 && i != j) {
                    Point p1 = componentPositions.get(i);
                    Point p2 = componentPositions.get(j);
                    drawArrow(g2d, p1, p2);
                }
            }
        }

        int nodeSize = 80;
        for (int i = 0; i < componentPositions.size(); i++) {
            Point p = componentPositions.get(i);

            g2d.setColor(NODE_FILL);
            g2d.fillOval(p.x - nodeSize / 2, p.y - nodeSize / 2, nodeSize, nodeSize);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(p.x - nodeSize / 2, p.y - nodeSize / 2, nodeSize, nodeSize);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16 * scaleFactor));
            String label = String.valueOf(i + 1);
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2d.drawString(label, p.x - labelWidth / 2, p.y + fm.getAscent() / 3);
        }

        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Condensation Graph", 350, yOffset + 40);
    }

    public static void main(String[] args) {
        int[][] Adir = generateDirectedMatrix(true);
        int[][] Adir2 = generateDirectedMatrix(false);
        int[][] Aundir = makeUndirectedMatrix(Adir);
        List<Point> positions = calculatePositions(N);

        System.out.println("First Directed Adjacency Matrix (Adir):");
        printMatrix(Adir);

        System.out.println("\nUndirected Adjacency Matrix (Aundir):");
        printMatrix(Aundir);

        System.out.println("\nModificated Directed Adjacency Matrix (Adir):");
        printMatrix(Adir2);

        analyzeGraphs(Adir, Aundir, Adir2);

        System.out.println("\n=== Path Analysis for Modificated Directed Graph ===");
        printAllPaths(Adir2);

        System.out.println("\n=== Connectivity Analysis for Directed Graph ===");
        printConnectivityAnalysis(Adir2);

        buildCondensationGraph(Adir2);

        JFrame frame = new JFrame("Graph Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GraphVisualization graphPanel = new GraphVisualization(Aundir, Adir, Adir2, positions);

        JPanel controlPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton firstDirectedButton = new JButton("Show Directed Graph");
        JButton undirectedButton = new JButton("Show Undirected Graph");
        JButton secondDirectedButton = new JButton("Show Second Directed Graph");
        JButton condensationButton = new JButton("Show Condensation");
        JButton clearButton = new JButton("Clear All");

        firstDirectedButton.addActionListener(e -> {
            if (!graphPanel.showFirstDirected) {
                graphPanel.showFirstDirected = true;
                graphPanel.showUndirected = false;
                graphPanel.showSecondDirected = false;
                graphPanel.showCondensation = false;
                firstDirectedButton.setText("Hide Directed Graph");
                undirectedButton.setText("Show Undirected Graph");
                secondDirectedButton.setText("Show Second Directed Graph");
                condensationButton.setText("Show Condensation");
            } else {
                graphPanel.showFirstDirected = false;
                firstDirectedButton.setText("Show Directed Graph");
            }
            graphPanel.repaint();
        });

        undirectedButton.addActionListener(e -> {
            if (!graphPanel.showUndirected) {
                graphPanel.showUndirected = true;
                graphPanel.showFirstDirected = false;
                graphPanel.showSecondDirected = false;
                graphPanel.showCondensation = false;
                undirectedButton.setText("Hide Undirected Graph");
                firstDirectedButton.setText("Show Directed Graph");
                secondDirectedButton.setText("Show Second Directed Graph");
                condensationButton.setText("Show Condensation");
            } else {
                graphPanel.showUndirected = false;
                undirectedButton.setText("Show Undirected Graph");
            }
            graphPanel.repaint();
        });

        secondDirectedButton.addActionListener(e -> {
            if (!graphPanel.showSecondDirected) {
                graphPanel.showSecondDirected = true;
                graphPanel.showFirstDirected = false;
                graphPanel.showUndirected = false;
                graphPanel.showCondensation = false;
                secondDirectedButton.setText("Hide Second Directed Graph");
                firstDirectedButton.setText("Show Directed Graph");
                undirectedButton.setText("Show Undirected Graph");
                condensationButton.setText("Show Condensation");
            } else {
                graphPanel.showSecondDirected = false;
                secondDirectedButton.setText("Show Second Directed Graph");
            }
            graphPanel.repaint();
        });

        condensationButton.addActionListener(e -> {
            if (!graphPanel.showCondensation) {
                graphPanel.showCondensation = true;
                graphPanel.showFirstDirected = false;
                graphPanel.showUndirected = false;
                graphPanel.showSecondDirected = false;
                condensationButton.setText("Hide Condensation");
                undirectedButton.setText("Show Undirected Graph");
                firstDirectedButton.setText("Show Directed Graph");
                secondDirectedButton.setText("Show Second Directed Graph");
            } else {
                graphPanel.showCondensation = false;
                condensationButton.setText("Show Condensation");
            }
            graphPanel.repaint();
        });

        clearButton.addActionListener(e -> {
            graphPanel.showFirstDirected = false;
            graphPanel.showUndirected = false;
            graphPanel.showSecondDirected = false;
            graphPanel.showCondensation = false;
            firstDirectedButton.setText("Show Directed Graph");
            undirectedButton.setText("Show Undirected Graph");
            secondDirectedButton.setText("Show Second Directed Graph");
            condensationButton.setText("Show Condensation");
            graphPanel.repaint();
        });

        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        firstDirectedButton.setFont(buttonFont);
        undirectedButton.setFont(buttonFont);
        secondDirectedButton.setFont(buttonFont);
        clearButton.setFont(buttonFont);
        condensationButton.setFont(buttonFont);

        firstDirectedButton.setBackground(BLUE_BUTTON_COLOR);
        undirectedButton.setBackground(BLUE_BUTTON_COLOR);
        secondDirectedButton.setBackground(GREEN_BUTTON_COLOR);
        condensationButton.setBackground(GREEN_BUTTON_COLOR);
        clearButton.setBackground(RED_BUTTON_COLOR);

        controlPanel.add(firstDirectedButton);
        controlPanel.add(undirectedButton);
        controlPanel.add(secondDirectedButton);
        controlPanel.add(condensationButton);
        controlPanel.add(clearButton);

        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(graphPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}