/* *****************************************************************************
 *  Name:
 *  Date:
 *  Description:
 **************************************************************************** */

import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdDraw;

public class KdTree {
    private int size;
    private Node node;

    // construct an empty set of points
    public KdTree() {
        size = 0;
    }

    // is the set empty?
    public boolean isEmpty() {
        return size == 0;
    }

    // number of points in the set
    public int size() {
        return size;
    }

    // add the point to the set (if it is not already in the set)
    public void insert(Point2D p) {
        if (p == null) {
            throw new IllegalArgumentException();
        }

        node = insert(node, p, 0.0, 0.0, 1.0, 1.0, 0);
    }

    private Node insert(Node n, Point2D point, double xmin, double ymin, double xmax, double ymax,
                        int level) {
        if (n == null) {
            size++;
            return new Node(point, new RectHV(xmin, ymin, xmax, ymax));
        }

        Point2D p = n.p;
        if (p.equals(point)) {
            return n;
        }

        int value = compare(point, p, level);
        if (value < 0) {
            if (level % 2 == 0) {
                n.left = insert(n.left, point, xmin, ymin, p.x(), ymax, level++);
            }
            else {
                n.left = insert(n.left, point, xmin, ymin, xmax, p.y(), level++);
            }
        }
        else if (value > 0) {
            if (level % 2 == 0) {
                n.right = insert(n.right, point, p.x(), ymin, xmax, ymax, level++);
            }
            else {
                n.right = insert(n.right, point, xmin, p.y(), xmax, ymax, level++);
            }
        }
        else {
            n.right = insert(n.right, point, xmin, ymin, xmax, ymax, level++);
        }
        return n;
    }

    // does the set contain point p?
    public boolean contains(Point2D p) {
        if (p == null) {
            throw new IllegalArgumentException();
        }
        return contains(node, p, 0);
    }

    // draw all points to standard draw
    public void draw() {
        drawLine(node, 0);
    }

    private void drawLine(Node n, int level) {
        if (n == null) {
            return;
        }

        StdDraw.setPenRadius();
        Point2D p = n.p;
        RectHV rect = n.rect;
        if (level % 2 == 0) {
            StdDraw.setPenColor(StdDraw.RED);
            double x = p.x();
            StdDraw.line(x, rect.ymin(), x, rect.ymax());
        }
        else {
            StdDraw.setPenColor(StdDraw.BLUE);
            double y = p.y();
            StdDraw.line(rect.xmin(), y, rect.xmax(), y);
        }

        StdDraw.setPenColor(StdDraw.BLACK);
        p.draw();

        drawLine(n.left, level++);
        drawLine(n.right, level++);
    }

    // all points that are inside the rectangle (or on the boundary)
    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null) {
            throw new IllegalArgumentException();
        }

        Stack<Point2D> points = new Stack<>();
        range(node, rect, points);
        return points;
    }

    // a nearest neighbor in the set to point p; null if the set is empty
    public Point2D nearest(Point2D p) {
        if (p == null) {
            throw new IllegalArgumentException();
        }
        return nearest(node, p, null);
    }

    private int compare(Point2D a, Point2D b, int level) {
        if (level % 2 == 0) {
            int value = Double.compare(a.x(), b.x());

            if (value == 0) {
                value = Double.compare(a.y(), b.y());
            }
            return value;
        }
        else {
            int value = Double.compare(a.y(), b.y());

            if (value == 0) {
                value = Double.compare(a.x(), b.x());
            }
            return value;
        }
    }

    private boolean contains(Node n, Point2D point, int level) {
        if (n == null) {
            return false;
        }

        int value = compare(point, n.p, level);
        if (value == 0) {
            return true;
        }
        else if (value < 0) {
            return contains(n.left, point, level++);
        }
        return contains(n.right, point, level++);
    }

    private void range(Node n, RectHV rect, Stack<Point2D> stack) {
        if (n == null || !rect.intersects(n.rect)) {
            return;
        }

        if (rect.contains(n.p)) {
            stack.push(n.p);
        }

        range(n.left, rect, stack);
        range(n.right, rect, stack);
    }

    private Point2D nearest(Node n, Point2D point, Point2D min) {
        if (n == null) {
            return min;
        }

        Point2D p = n.p;
        if (min == null) {
            min = p;
        }

        double minToPoint = min.distanceSquaredTo(point);
        if (minToPoint <= n.rect.distanceSquaredTo(point)) {
            return min;
        }

        if (p.distanceSquaredTo(point) < minToPoint) {
            min = p;
        }
        Node right = n.right;
        Node left = n.left;
        if (right != null && right.rect.contains(point)) {
            min = nearest(right, point, min);
            min = nearest(left, point, min);
        }
        else {
            min = nearest(left, point, min);
            min = nearest(right, point, min);
        }
        return min;
    }

    private class Node {
        private final Point2D p;
        private final RectHV rect;
        private Node left = null;
        private Node right = null;

        private Node(Point2D point, RectHV rectHv) {
            p = point;
            rect = rectHv;
        }
    }
}