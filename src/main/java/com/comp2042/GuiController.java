package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
public class GuiController implements Initializable {

    private static final int BRICK_SIZE = 20;

    @FXML
    private GridPane gamePanel;

    @FXML
    private Group groupNotification;

    @FXML
    private GridPane brickPanel;

    @FXML
    private Label scoreLabel;

    @FXML
    private Label lineLabel;

    //add the label in the FXML file
    @FXML
    private Label levelLabel;

    @FXML
    private Button pauseButton;

    @FXML
    private GameOverPanel gameOverPanel;

    private Rectangle[][] displayMatrix;

    private InputEventListener eventListener;

    private Rectangle[][] nextBrickRectangles;

    @FXML
    private GridPane nextBrickPanel;

    private Rectangle[][] rectangles;
    // hold panel
    @FXML
    private GridPane holdBrickPanel;
    private Rectangle[][] holdBrickRectangles;



    //    ghost piece and rectangles
    @FXML
    private GridPane ghostPanel;
    private Rectangle[][] ghostRectangles;

    @FXML
    private StackPane pauseOverlay;

    private Timeline timeLine;

    private final BooleanProperty isPause = new SimpleBooleanProperty();

    private final BooleanProperty isGameOver = new SimpleBooleanProperty();

    private MediaPlayer bgmPlayer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Font.loadFont(getClass().getClassLoader().getResource("digital.ttf").toExternalForm(), 38);
//        initialize background music
        try {
            Media bgm = new Media(getClass().getResource("/bgm.mp3").toString());
            bgmPlayer = new MediaPlayer(bgm);
            bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
//set volume
            bgmPlayer.setVolume(1.0);
            bgmPlayer.play();
        } catch (Exception e) {
            System.err.println("Error loading background music: " + e.getMessage());
            e.printStackTrace();
        }
        initNextBrickPanel();
        initHoldBrickPanel();
        initPauseOverlay();

        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();
        gamePanel.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE) {
                    if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
                        refreshBrick(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
                        refreshBrick(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
                        refreshBrick(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
                        moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
                        keyEvent.consume();
                    }
                }
//hold with C or shift key
                if (keyEvent.getCode() == KeyCode.C || keyEvent.getCode() == KeyCode.SHIFT) {
                    ViewData holdResult = eventListener.onHoldEvent();
                    if (holdResult != null) {
                        refreshBrick(holdResult);
                    }
                    keyEvent.consume();
                }

//hard drop with space
                if (keyEvent.getCode() == KeyCode.SPACE) {
                    DownData downData = eventListener.onHardDropEvent(new MoveEvent(EventType.HARD_DROP, EventSource.USER));
                    if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                        NotificationPanel notificationPanel = new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                        groupNotification.getChildren().add(notificationPanel);
                        notificationPanel.showScore(groupNotification.getChildren());
                    }
                    refreshBrick(downData.getViewData());
                    keyEvent.consume();
                }
                if (keyEvent.getCode() == KeyCode.N) {
                    newGame(null);
                }

                if (keyEvent.getCode() == KeyCode.P || keyEvent.getCode() == KeyCode.ESCAPE) {
                    pauseGame(null);
                }
            }
        });
        gameOverPanel.setVisible(false);

        final Reflection reflection = new Reflection();
        reflection.setFraction(0.8);
        reflection.setTopOpacity(0.9);
        reflection.setTopOffset(-12);
    }

    private void initPauseOverlay() {
        if (pauseOverlay == null) {
            System.err.println("pauseOverlay not injected");
            return;
        }

        try {
            InputStream is = getClass().getResourceAsStream("/pause.png");
            if (is != null) {
                Image pauseImage = new Image(is);
                ImageView pauseImageView = new ImageView(pauseImage);
                pauseImageView.setFitWidth(150);
                pauseImageView.setFitHeight(150);
                pauseImageView.setPreserveRatio(true);
                pauseOverlay.getChildren().add(pauseImageView);

            } else {
                System.err.println("pause.png not found");
            }
            pauseOverlay.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

            public void initGameView(int[][] boardMatrix, ViewData brick) {
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int i = 2; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i - 2);
            }
        }

        rectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(getFillColor(brick.getBrickData()[i][j]));
                rectangles[i][j] = rectangle;
                brickPanel.add(rectangle, j, i);
            }
        }
        double initialX = gamePanel.getLayoutX() + brick.getxPosition() * (BRICK_SIZE + brickPanel.getHgap());
        double initialY = -42 + gamePanel.getLayoutY() + brick.getyPosition() * (BRICK_SIZE + brickPanel.getVgap());

        brickPanel.setLayoutX(initialX);
        brickPanel.setLayoutY(initialY);

        initGhostPanel(brick);


        timeLine = new Timeline(new KeyFrame(
                Duration.millis(400),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
    }

    private void initGhostPanel(ViewData brick) {
        ghostPanel = new GridPane();
        ghostPanel.setHgap(brickPanel.getHgap());
        ghostPanel.setVgap(brickPanel.getVgap());

        ghostRectangles = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                rectangle.setArcHeight(9);
                rectangle.setArcWidth(9);

                ghostRectangles[i][j] = rectangle;
                ghostPanel.add(rectangle, j, i);
            }
        }

//        add ghost panel into gui
        if (brickPanel.getParent() instanceof javafx.scene.layout.Pane) {
            ((javafx.scene.layout.Pane) brickPanel.getParent()).getChildren().add(0, ghostPanel);
        }
    }

    private void initNextBrickPanel() {
//        create a 4*4 grid for the next brick
        nextBrickRectangles = new Rectangle[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                rectangle.setArcHeight(9);
                rectangle.setArcWidth(9);
                nextBrickRectangles[i][j] = rectangle;
                nextBrickPanel.add(rectangle, j, i);
            }
        }
    }
//    initialize hold panel
    private void initHoldBrickPanel() {
        holdBrickRectangles = new Rectangle[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                rectangle.setArcHeight(9);
                rectangle.setArcWidth(9);
                holdBrickRectangles[i][j] = rectangle;
                holdBrickPanel.add(rectangle, j, i);
            }
        }
    }


    public void updateNextBrick(ViewData nextBrick) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                nextBrickRectangles[i][j].setFill(Color.TRANSPARENT);
            }
        }
//        display the next brick in the center of the 4*4 grid
        int[][] brickData = nextBrick.getBrickData();
        int offsetY = (4 - brickData.length) / 2;
        int offsetX = (4 - brickData[0].length) / 2;

        for (int i = 0; i < brickData.length; i++) {
            for (int j = 0; j < brickData[i].length; j++) {
                int targetI = i + offsetY;
                int targetJ = j + offsetX;
                if (targetI >= 0 && targetI < 4 && targetJ >= 0 && targetJ < 4) {
                    nextBrickRectangles[targetI][targetJ].setFill(getFillColor(brickData[i][j]));
                }
            }
        }
  }
//  update hold panel
    public void updateHoldBrick(ViewData holdBrick) {
        // Clear the hold panel first
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                holdBrickRectangles[i][j].setFill(Color.TRANSPARENT);
            }
        }
        if (holdBrick != null) {
            int[][] brickData = holdBrick.getBrickData();
            int offsetY = (4 - brickData.length) / 2;
            int offsetX = (4 - brickData[0].length) / 2;

            for (int i = 0; i < brickData.length; i++) {
                for (int j = 0; j < brickData[i].length; j++) {
                    int targetI = i + offsetY;
                    int targetJ = j + offsetX;
                    if (targetI >= 0 && targetI < 4 && targetJ >= 0 && targetJ < 4) {
                        holdBrickRectangles[targetI][targetJ].setFill(getFillColor(brickData[i][j]));
                    }
                }
            }
        }
    }

    private Paint getFillColor(int i) {
        Paint returnPaint;
        switch (i) {
            case 0:
                returnPaint = Color.TRANSPARENT;
                break;
            case 1:
                returnPaint = Color.web("bde0fe");
                break;
            case 2:
                returnPaint = Color.web("ffc8dd");
                break;
            case 3:
                returnPaint = Color.web("d9ed92");
                break;
            case 4:
                returnPaint = Color.web("fff3b0");
                break;
            case 5:
                returnPaint = Color.web("ffadad");
                break;
            case 6:
                returnPaint = Color.web("ffc6ff");
                break;
            case 7:
                returnPaint = Color.web("ffd6a5");
                break;
            default:
                returnPaint = Color.WHITE;
                break;
        }
        return returnPaint;
    }

    private Paint getGhostColor(int i) {
        if (i == 0) {
            return Color.TRANSPARENT;
        }
        return Color.rgb(255, 255, 255, 0.3);
    }

    private void refreshBrick(ViewData brick) {
        if (isPause.getValue() == Boolean.FALSE) {
            double brickX = gamePanel.getLayoutX() + brick.getxPosition() * (BRICK_SIZE + brickPanel.getHgap());
            double brickY = -42 + gamePanel.getLayoutY() + brick.getyPosition() * (BRICK_SIZE + brickPanel.getVgap());
            brickPanel.setLayoutX(brickX);
            brickPanel.setLayoutY(brickY);

            for (int i = 0; i < brick.getBrickData().length; i++) {
                for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                    setRectangleData(brick.getBrickData()[i][j], rectangles[i][j]);
                }
            }
//            Update ghost piece
            updateGhostPiece(brick);
        }
    }

    //ghost position from GameController
    public void updateGhostPiece(ViewData currentBrick) {
        if (ghostPanel == null || ghostRectangles == null) {
            return;
        }

        ViewData ghostData = eventListener.getGhostPosition(currentBrick);

        if (ghostData != null) {
            ghostPanel.setLayoutX(gamePanel.getLayoutX() + ghostData.getxPosition() * ghostPanel.getVgap() + ghostData.getxPosition() * BRICK_SIZE);
            ghostPanel.setLayoutY(-42 + gamePanel.getLayoutY() + ghostData.getyPosition() * ghostPanel.getHgap() + ghostData.getyPosition() * BRICK_SIZE);

            for (int i = 0; i < ghostData.getBrickData().length; i++) {
                for (int j = 0; j < ghostData.getBrickData()[i].length; j++) {
                    ghostRectangles[i][j].setFill(getGhostColor(ghostData.getBrickData()[i][j]));
                    ghostRectangles[i][j].setArcHeight(9);
                    ghostRectangles[i][j].setArcWidth(9);
                    // Add stroke to make ghost more visible
                    if (ghostData.getBrickData()[i][j] != 0) {
                        ghostRectangles[i][j].setStroke(Color.rgb(200, 200, 200, 0.5));
                        ghostRectangles[i][j].setStrokeWidth(1);
                    } else {
                        ghostRectangles[i][j].setStroke(null);
                    }
                }
            }
            ghostPanel.setVisible(true);
        } else {
            ghostPanel.setVisible(false);
        }
    }

    public void refreshGameBackground(int[][] board) {
        for (int i = 2; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                setRectangleData(board[i][j], displayMatrix[i][j]);
            }
        }
    }

    private void setRectangleData(int color, Rectangle rectangle) {
        rectangle.setFill(getFillColor(color));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }

    //    show score notification
    private void showScoreNotification(int scoreBonus, int linesCleared) {
        String message;
        if (linesCleared == 1) {
            message = "Single! +" + scoreBonus;
        } else if (linesCleared == 2) {
            message = "Double! +" + scoreBonus;
        } else if (linesCleared == 3) {
            message = "Triple! +" + scoreBonus;
        } else if (linesCleared >= 4) {
            message = "TETRIS! +" + scoreBonus;
        } else {
            message = "+" + scoreBonus;
        }

        NotificationPanel notificationPanel = new NotificationPanel(message);
        groupNotification.getChildren().add(notificationPanel);
        notificationPanel.showScore(groupNotification.getChildren());
    }

    private void moveDown(MoveEvent event) {
        if (isPause.getValue() == Boolean.FALSE) {
            DownData downData = eventListener.onDownEvent(event);
            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                NotificationPanel notificationPanel = new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                groupNotification.getChildren().add(notificationPanel);
                notificationPanel.showScore(groupNotification.getChildren());
            }
            refreshBrick(downData.getViewData());
        }
        gamePanel.requestFocus();
    }

    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void bindScore(IntegerProperty integerProperty) {
        scoreLabel.textProperty().bind(integerProperty.asString());
    }

    public void bindLine(IntegerProperty integerProperty) {
        lineLabel.textProperty().bind(integerProperty.asString());
    }

    //bind level label
    public void bindLevel(IntegerProperty levelProperty) {
        levelLabel.textProperty().bind(levelProperty.asString());
    }

    public void updateGameSpeed(double newDurationMillis) {
        if (timeLine != null) {
            timeLine.stop();

            KeyFrame newKeyFrame = new KeyFrame(
                    Duration.millis(newDurationMillis),
                    ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
            );
            timeLine = new Timeline(newKeyFrame);
            timeLine.setCycleCount(Timeline.INDEFINITE);
            // Only play if game is active
            if (isPause.getValue() == Boolean.FALSE && isGameOver.getValue() == Boolean.FALSE) {
                timeLine.play();
            }
        }
    }

    public void gameOver() {
        timeLine.stop();
        gameOverPanel.setVisible(true);
        isGameOver.setValue(Boolean.TRUE);
    }

    public void newGame(ActionEvent actionEvent) {
        timeLine.stop();
        gameOverPanel.setVisible(false);
        eventListener.createNewGame();
        gamePanel.requestFocus();
        timeLine.play();
        isPause.setValue(Boolean.FALSE);
        isGameOver.setValue(Boolean.FALSE);

        // ensure music is playing when a new game starts
        if (bgmPlayer != null && bgmPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            bgmPlayer.play();
        }
    }

    public void pauseGame(ActionEvent actionEvent) {
        gamePanel.requestFocus();

        if (isPause.getValue()) {
            isPause.setValue(Boolean.FALSE);
            if(pauseOverlay !=null){
                pauseOverlay.setVisible(false);
            }
            if (timeLine != null) {
                timeLine.play();
            }
//            resume bgm
            if (bgmPlayer != null) {
                bgmPlayer.play();
            }
        } else {
//pause the falling animation
            isPause.setValue(Boolean.TRUE);
            if(pauseOverlay !=null){
                pauseOverlay.setVisible(true);
            }
            if (timeLine != null) {
                timeLine.pause();
            }
            if (bgmPlayer != null) {
                bgmPlayer.pause();
            }
        }
    }
}