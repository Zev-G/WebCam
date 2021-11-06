import com.github.sarxos.webcam.Webcam;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.controlsfx.control.RangeSlider;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    double redMin = 0.3;
    double redMax = 1;
    double greenMin = 0.2;
    double greenMax = 0.4;
    double blueMin = 0;
    double blueMax = 0.25;

    double averageShadeMin = 0.25;
    double averageShadeMax = 1;

    BufferedImage image;

    long minTime = 1000 / 60;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        final Webcam webcam = Webcam.getDefault();
        webcam.open();

        Slider minRed = new Slider(0, 1, redMin);
        Slider maxRed = new Slider(0, 1, redMax);
        Slider minGreen = new Slider(0, 1, greenMin);
        Slider maxGreen = new Slider(0, 1, greenMax);
        Slider minBlue = new Slider(0, 1, blueMin);
        Slider maxBlue = new Slider(0, 1, blueMax);

        Label minRedVal = new Label();
        minRedVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(minRed.getValue() * 10000) / 10000D), minRed.valueProperty()));
        Label maxRedVal = new Label();
        maxRedVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(maxRed.getValue() * 10000) / 10000D), maxRed.valueProperty()));
        Label minGreenVal = new Label();
        minGreenVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(minGreen.getValue() * 10000) / 10000D), minGreen.valueProperty()));
        Label maxGreenVal = new Label();
        maxGreenVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(maxGreen.getValue() * 10000) / 10000D), maxGreen.valueProperty()));
        Label minBlueVal = new Label();
        minBlueVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(minBlue.getValue() * 10000) / 10000D), minBlue.valueProperty()));
        Label maxBlueVal = new Label();
        maxBlueVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(maxBlue.getValue() * 10000) / 10000D), maxBlue.valueProperty()));

        GridPane minMaxColors = new GridPane();
        minMaxColors.addRow(0, new Label("Min Red %: "), minRed, minRedVal);
        minMaxColors.addRow(1, new Label("Max Red %: "), maxRed, maxRedVal);
        minMaxColors.addRow(2, new Label("Min Green %: "), minGreen, minGreenVal);
        minMaxColors.addRow(3, new Label("Max Green %: "), maxGreen, maxGreenVal);
        minMaxColors.addRow(4, new Label("Min Blue %: "), minBlue, minBlueVal);
        minMaxColors.addRow(5, new Label("Max Blue %: "), maxBlue, maxBlueVal);

        Slider minAverageShade = new Slider(0, 1, averageShadeMin);
        Slider maxAverageShade = new Slider(0, 1, averageShadeMax);
        Label minAverageShadeVal = new Label();
        minAverageShadeVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(minAverageShade.getValue() * 10000) / 10000D), minAverageShade.valueProperty()));
        Label maxAverageShadeVal = new Label();
        maxAverageShadeVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(maxAverageShade.getValue() * 10000) / 10000D), maxAverageShade.valueProperty()));

        GridPane minMaxShade = new GridPane();
        minMaxShade.addRow(0, new Label("Min Average Shade: "), minAverageShade, minAverageShadeVal);
        minMaxShade.addRow(1, new Label("Max Average Shade: "), maxAverageShade, maxAverageShadeVal);

        Slider minBlockSize = new Slider(1, 500, 50);
        Label minBlockSizeVal = new Label();
        minBlockSizeVal.textProperty().bind(Bindings.createStringBinding(() -> String.valueOf(Math.round(minBlockSize.getValue())), minBlockSize.valueProperty()));

        CheckBox showObjects = new CheckBox("Show Objects");
        showObjects.setSelected(true);
        CheckBox draw = new CheckBox("Draw Pixels");
        draw.setSelected(true);
        CheckBox drawInvalidObjects = new CheckBox("Draw Invalid Pixels");
        drawInvalidObjects.setSelected(true);
        CheckBox monochromeInvalid = new CheckBox("Draw Invalid Pixels Monochrome");
        monochromeInvalid.setSelected(true);
        CheckBox playing = new CheckBox("Record");
        playing.setSelected(true);

        VBox sliders = new VBox(
                minMaxColors,
                new Separator(), minMaxShade,
                new Separator(), showObjects, draw, drawInvalidObjects, monochromeInvalid, playing,
                new Separator(), new HBox(new Label("Minimum Pixel Group Size:  "), minBlockSize, minBlockSizeVal)
        );
        sliders.setPadding(new Insets(20));
        sliders.setPrefWidth(450);

        final BorderPane pane = new BorderPane();
        pane.setRight(sliders);
        Thread thread = new Thread(() -> {
            while (true) {
                long start = System.currentTimeMillis();
                if (playing.isSelected()) {
                    image = webcam.getImage();
                }
                Pixel[] allPixels = PixelValidator.loadPixelsFromImage(image);
                redMin = minRed.getValue();
                redMax = maxRed.getValue();
                greenMin = minGreen.getValue();
                greenMax = maxGreen.getValue();
                blueMin = minBlue.getValue();
                blueMax = maxBlue.getValue();
                averageShadeMin = minAverageShade.getValue();
                averageShadeMax = maxAverageShade.getValue();
                Pixel[] validPixels = PixelValidator.validate(
                        allPixels,
                        pixel -> {
                            Color color = pixel.getColor();
                            double averageShade = averageShade(color);

                            double total = color.getRed() + color.getGreen() + color.getBlue();
                            double percentageRed = color.getRed() / total;
                            double percentageGreen = color.getGreen() / total;
                            double percentageBlue = color.getBlue() / total;

                            return percentageRed > redMin && percentageRed < redMax &&
                                    percentageBlue > blueMin && percentageBlue < blueMax &&
                                    percentageGreen > greenMin && percentageGreen < greenMax &&
                                    averageShade > averageShadeMin && averageShade < averageShadeMax;
                        }
                );
                final boolean objsOutline = showObjects.isSelected();
                final boolean invalidDraw = drawInvalidObjects.isSelected();
                final boolean monochromeDraw = monochromeInvalid.isSelected();
                final double blockSize = minBlockSize.getValue();
                final boolean drawPixels = draw.isSelected();
                List<PixelGroup> pixelGroups = objsOutline ? PixelGrouper.findPixelGroups(validPixels, 3) : new ArrayList<>();

                Platform.runLater(() -> {
                    Canvas canvas = new Canvas(image.getWidth(), image.getHeight());
                    canvas.setScaleX(6);
                    canvas.setScaleY(6);
                    GraphicsContext context = canvas.getGraphicsContext2D();
                    pane.setCenter(canvas);

                    if (drawPixels) {
                        for (int i = 0, allPixelsLength = allPixels.length; i < allPixelsLength; i++) {
                            Pixel pixel = allPixels[i];
                            boolean accepted = validPixels[i] != null;
                            if (accepted) {
                                context.setFill(pixel.getColor());
                                context.fillRect(pixel.getX(), pixel.getY(), 1, 1);
                            } else if (invalidDraw) {
                                if (monochromeDraw) {
                                    context.setFill(pixel.getColor().grayscale());
                                } else {
                                    context.setFill(pixel.getColor());
                                }
                                context.fillRect(pixel.getX(), pixel.getY(), 1, 1);
                            }
                        }
                    }

                    for (PixelGroup group : pixelGroups) {
                        List<Pixel> pixels = group.getPixels();
                        if (pixels.size() > blockSize) {
                            context.setFill(Color.rgb(0, 0, 0, 0.5));
                            context.fillRect(group.getMinX(), group.getMinY(), group.getMaxX() - group.getMinX(), group.getMaxY() - group.getMinY());
                        }
                    }

                });

                long time = System.currentTimeMillis() - start;
                if (time < minTime) {
                    try {
                        Thread.sleep(minTime - time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
        primaryStage.setScene(new Scene(pane));
        primaryStage.show();
    }

    private double averageShade(Color color) {
        return (color.getRed() + color.getGreen() + color.getBlue()) / 3D;
    }

}
