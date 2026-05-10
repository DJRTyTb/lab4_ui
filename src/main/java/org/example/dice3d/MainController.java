package org.example.dice3d;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.transform.Rotate;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MainController {

    @FXML
    private Pane scenePane;

    @FXML
    private Slider ambientSlider;

    @FXML
    private Slider attenuationSlider;

    private final Group root3D = new Group();

    private final PerspectiveCamera camera =
            new PerspectiveCamera(true);

    private final Dice dice = new Dice();

    private final PointLight pointLight =
            new PointLight(Color.WHITE);

    private final AmbientLight ambientLight =
            new AmbientLight();

    private final Random random =
            new Random();

    private double angularVelocityX;
    private double angularVelocityY;

    private double velocityY = 0;

    private final double gravity = 0.5;

    private boolean rolling = false;
    private boolean grounded = false;

    // движение камеры
    private final Set<KeyCode> keys =
            new HashSet<>();

    private double mouseOldX;
    private double mouseOldY;

    private double cameraYaw = 0;
    private double cameraPitch = 0;

    private final Rotate cameraRotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate cameraRotateX = new Rotate(0, Rotate.X_AXIS);

    @FXML
    public void initialize() {

        createSubScene();

        setupCamera();

        setupFloor();

        setupLights();

        setupDice();

        startPhysics();
    }

    private void createSubScene() {

        SubScene subScene =
                new SubScene(
                        root3D,
                        1000,
                        800,
                        true,
                        SceneAntialiasing.BALANCED);

        subScene.setFill(
                Color.rgb(30, 30, 30));

        subScene.setCamera(camera);

        scenePane.getChildren().add(subScene);

        subScene.widthProperty().bind(
                scenePane.widthProperty());

        subScene.heightProperty().bind(
                scenePane.heightProperty());

        // Фокус клавиатуры
        subScene.setFocusTraversable(true);

        subScene.setOnKeyPressed(
                e -> keys.add(e.getCode()));

        subScene.setOnKeyReleased(
                e -> keys.remove(e.getCode()));

        subScene.setOnMousePressed(event -> {
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();

            dice.setDragging(event.isPrimaryButtonDown());
        });

        subScene.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {

                double dx = event.getSceneX() - mouseOldX;
                double dy = event.getSceneY() - mouseOldY;

                dx *= 0.7;
                dy *= 0.7;

                double yawRad = Math.toRadians(cameraYaw);
                double pitchRad = Math.toRadians(cameraPitch);

                Point3D cameraForward = new Point3D(
                        Math.sin(yawRad) * Math.cos(pitchRad),
                        -Math.sin(pitchRad),
                        Math.cos(yawRad) * Math.cos(pitchRad)
                ).normalize();

                Point3D cameraRight = new Point3D(
                        Math.cos(yawRad),
                        0,
                        -Math.sin(yawRad)
                ).normalize();

                dice.addRotation(dx, dy, cameraForward, cameraRight);

                dice.setDragging(true);
            }
            else dice.setDragging(false);

            if (event.isSecondaryButtonDown()) {

                double dx = event.getSceneX() - mouseOldX;
                double dy = event.getSceneY() - mouseOldY;

                cameraYaw += dx * 0.2;
                cameraPitch -= dy * 0.2;

                // ограничение вертикального угла
                cameraPitch = Math.max(-80, Math.min(80, cameraPitch));

                cameraRotateY.setAngle(cameraYaw);
                cameraRotateX.setAngle(cameraPitch);

                mouseOldX = event.getSceneX();
                mouseOldY = event.getSceneY();
            }

            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        subScene.setOnMouseReleased(event -> dice.setDragging(event.isPrimaryButtonDown()));
    }

    private void setupCamera() {
        camera.setNearClip(0.1);
        camera.setFarClip(10000);

        // позиция камеры
        camera.setTranslateX(0);
        camera.setTranslateY(-250);
        camera.setTranslateZ(-1200);

        // стартовый угол
        cameraYaw = 0;
        cameraPitch = -16;

        cameraRotateY.setAngle(cameraYaw);
        cameraRotateX.setAngle(cameraPitch);

        camera.getTransforms().addAll(
                cameraRotateY,
                cameraRotateX);
    }

    private void setupFloor() {

        Box floor =
                new Box(5000, 20, 5000);

        floor.setTranslateY(250);

        PhongMaterial material =
                new PhongMaterial(
                        Color.TURQUOISE);

        floor.setMaterial(material);

        root3D.getChildren().add(floor);
    }

    private void setupLights() {

        pointLight.setTranslateX(-300);
        pointLight.setTranslateY(-400);
        pointLight.setTranslateZ(-500);

        ambientLight.setColor(
                Color.color(
                        0.3,
                        0.3,
                        0.3));

        ambientSlider.valueProperty().addListener(
                (obs, oldV, newV) -> ambientLight.setColor(
                        Color.color(
                                newV.doubleValue(),
                                newV.doubleValue(),
                                newV.doubleValue())));

        attenuationSlider.valueProperty().addListener(
                (obs, oldV, newV) -> {

                    double power =
                            1.0 -
                                    newV.doubleValue() * 15;

                    power = Math.max(0.05, power);
                    power = Math.min(1.0, power);

                    pointLight.setColor(
                            Color.color(
                                    power,
                                    power,
                                    power));
                });

        root3D.getChildren().addAll(
                ambientLight,
                pointLight);
    }

    private void setupDice() {

        dice.setTranslateY(100);

        root3D.getChildren().add(dice);
    }

    @FXML
    private void rollDice() {

        angularVelocityX =
                random.nextDouble() * 25;

        angularVelocityY =
                random.nextDouble() * 25;

        velocityY = -15;

        rolling = true;
    }

    private void updateCamera() {
        double speed = 10;
        double yawRad = Math.toRadians(cameraYaw);

        double forwardX = Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);
        double rightX = Math.cos(yawRad);
        double rightZ = -Math.sin(yawRad);

        if (keys.contains(KeyCode.W)) {
            camera.setTranslateX(camera.getTranslateX() + forwardX * speed);
            camera.setTranslateZ(camera.getTranslateZ() + forwardZ * speed);
        }

        if (keys.contains(KeyCode.S)) {
            camera.setTranslateX(camera.getTranslateX() - forwardX * speed);
            camera.setTranslateZ(camera.getTranslateZ() - forwardZ * speed);
        }

        if (keys.contains(KeyCode.A)) {
            camera.setTranslateX(camera.getTranslateX() - rightX * speed);
            camera.setTranslateZ(camera.getTranslateZ() - rightZ * speed);
        }

        if (keys.contains(KeyCode.D)) {
            camera.setTranslateX(camera.getTranslateX() + rightX * speed);
            camera.setTranslateZ(camera.getTranslateZ() + rightZ * speed);
        }

        if (keys.contains(KeyCode.Q))
            camera.setTranslateY(camera.getTranslateY() - speed);

        if (keys.contains(KeyCode.E))
            camera.setTranslateY(camera.getTranslateY() + speed);
    }

    private void startPhysics() {

        AnimationTimer timer =
                new AnimationTimer() {

                    @Override
                    public void handle(long now) {

                        updateCamera();
                        dice.updateAlignment();

                        // === ГРАВИТАЦИЯ ===

                        if (!grounded || velocityY != 0) {
                            velocityY += gravity;
                            dice.setTranslateY(dice.getTranslateY() + velocityY);
                        }

                        // === СТОЛКНОВЕНИЕ С ПОЛОМ ===

                        double cubeBottom = dice.getLowestPointY();
                        double floorTop = 240;

                        if (cubeBottom >= floorTop) {

                            double penetration = cubeBottom - floorTop;

                            dice.setTranslateY(dice.getTranslateY() - penetration);

                            if (Math.abs(velocityY) > 4) {

                                velocityY *= -0.18;
                                grounded = false;

                            } else {

                                velocityY = 0;
                                grounded = true;

                                if (!dice.isDragging())
                                    dice.startAlignment();
                            }

                        } else {
                            grounded = false;
                        }

                        // === ВРАЩЕНИЕ ===

                        if (rolling) {
                            dice.rotateAroundAxis(new Point3D(1, 0, 0), angularVelocityX);
                            dice.rotateAroundAxis(new Point3D(0, 1, 0), angularVelocityY);

                            angularVelocityX *= 0.98;
                            angularVelocityY *= 0.98;

                            if (Math.abs(angularVelocityX) < 0.1 &&
                                    Math.abs(angularVelocityY) < 0.1 &&
                                    velocityY == 0) {
                                rolling = false;
                            }
                        }
                    }
                };

        timer.start();
    }
}