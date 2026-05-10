package org.example.dice3d;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;

public class Dice extends Group {

    private final Affine rotation = new Affine();

    private boolean aligning = false;
    private boolean dragging = false;

    private Point3D alignNormal;

    public Dice() {
        getTransforms().add(rotation);
        createDice();
    }

    private void createDice() {
        double size = 200;
        double h = size / 2;

        Box cube = new Box(size, size, size);
        cube.setMaterial(new PhongMaterial(Color.WHITE));

        getChildren().add(cube);

        addDot(0, 0, -h);

        addDot(-40, -40, h);
        addDot(40, 40, h);

        addDot(h, -45, -45);
        addDot(h, 0, 0);
        addDot(h, 45, 45);

        addDot(-h, -45, -45);
        addDot(-h, 45, -45);
        addDot(-h, -45, 45);
        addDot(-h, 45, 45);

        addDot(-45, -h, -45);
        addDot(45, -h, -45);
        addDot(0, -h, 0);
        addDot(-45, -h, 45);
        addDot(45, -h, 45);

        addDot(-45, h, -60);
        addDot(45, h, -60);
        addDot(-45, h, 0);
        addDot(45, h, 0);
        addDot(-45, h, 60);
        addDot(45, h, 60);
    }

    private void addDot(double x, double y, double z) {
        Sphere dot = new Sphere(10);

        dot.setTranslateX(x);
        dot.setTranslateY(y);
        dot.setTranslateZ(z);

        dot.setMaterial(new PhongMaterial(Color.BLACK));

        getChildren().add(dot);
    }

    public void addRotation(double dx, double dy, Point3D cameraForward, Point3D cameraRight) {
        Point3D horizontalAxis = cameraRight.crossProduct(cameraForward).normalize();

        appendWorldRotation(dy, cameraRight);
        appendWorldRotation(dx, horizontalAxis);
    }

    public void rotateAroundAxis(Point3D axis, double angle) {
        appendWorldRotation(angle, axis);
    }

    private void appendWorldRotation(double angle, Point3D axis) {
        Rotate rotate = new Rotate(angle, axis);
        rotation.prepend(rotate);
    }

    private Point3D localToSceneDirection(Point3D dir) {
        Point3D origin = localToScene(0, 0, 0);
        Point3D target = localToScene(dir);

        return target.subtract(origin);
    }

    public double getLowestPointY() {
        double h = 100;

        Point3D[] points = {
                new Point3D(-h, -h, -h),
                new Point3D(h, -h, -h),
                new Point3D(-h, h, -h),
                new Point3D(h, h, -h),
                new Point3D(-h, -h, h),
                new Point3D(h, -h, h),
                new Point3D(-h, h, h),
                new Point3D(h, h, h)
        };

        double maxY = Double.NEGATIVE_INFINITY;

        for (Point3D p : points) {
            Point3D world = localToScene(p);

            if (world.getY() > maxY)
                maxY = world.getY();
        }

        return maxY;
    }

    public void startAlignment() {
        if (aligning)
            return;

        Point3D down = new Point3D(0, 1, 0);

        Point3D[] normals = {
                localToSceneDirection(new Point3D(1, 0, 0)),
                localToSceneDirection(new Point3D(-1, 0, 0)),
                localToSceneDirection(new Point3D(0, 1, 0)),
                localToSceneDirection(new Point3D(0, -1, 0)),
                localToSceneDirection(new Point3D(0, 0, 1)),
                localToSceneDirection(new Point3D(0, 0, -1))
        };

        alignNormal = normals[0];
        double bestDot = alignNormal.normalize().dotProduct(down);

        for (Point3D n : normals) {
            double dot = n.normalize().dotProduct(down);

            if (dot > bestDot) {
                bestDot = dot;
                alignNormal = n;
            }
        }

        alignNormal = alignNormal.normalize();

        aligning = true;
    }

    public void updateAlignment() {
        if (!aligning)
            return;

        Point3D down = new Point3D(0, 1, 0);

        Point3D current = findMatchingNormal(alignNormal);

        double dot = Math.max(-1, Math.min(1, current.normalize().dotProduct(down)));
        double angle = Math.toDegrees(Math.acos(dot));

        if (angle < 0.3) {
            aligning = false;
            return;
        }

        Point3D axis = current.crossProduct(down);

        if (axis.magnitude() < 0.00001) {
            aligning = false;
            return;
        }

        axis = axis.normalize();

        appendWorldRotation(Math.min(angle * 0.12, 1.0), axis);
    }

    private Point3D findMatchingNormal(Point3D target) {
        Point3D[] normals = {
                localToSceneDirection(new Point3D(1, 0, 0)),
                localToSceneDirection(new Point3D(-1, 0, 0)),
                localToSceneDirection(new Point3D(0, 1, 0)),
                localToSceneDirection(new Point3D(0, -1, 0)),
                localToSceneDirection(new Point3D(0, 0, 1)),
                localToSceneDirection(new Point3D(0, 0, -1))
        };

        Point3D best = normals[0];
        double bestDot = best.normalize().dotProduct(target);

        for (Point3D n : normals) {
            double dot = n.normalize().dotProduct(target);

            if (dot > bestDot) {
                bestDot = dot;
                best = n;
            }
        }

        return best.normalize();
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }
}