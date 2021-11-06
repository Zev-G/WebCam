import com.github.sarxos.webcam.Webcam;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.controlsfx.control.RangeSlider;

import java.awt.image.BufferedImage;
import java.util.List;

public class Main extends Application {

    double redMin = 0.3;
    double redMax = 1;
    double greenMin = 0.2;
    double greenMax = 0.4;
    double blueMin = 0;
    double blueMax = 0.25;

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

        Label ranges = new Label();
        ranges.textProperty().bind(Bindings.createStringBinding(() ->
                "Red:   Min = " + minRed.getValue() + "  Max = " + maxRed.getValue() + "\n" +
                "Green:   Min = " + minGreen.getValue() + "  Max = " + maxGreen.getValue() + "\n" +
                "Blue:   Min = " + minBlue.getValue() + "  Max = " + maxBlue.getValue() + "\n"
                , minRed.valueProperty(), maxRed.valueProperty(), minGreen.valueProperty(), maxGreen.valueProperty(), minBlue.valueProperty(), maxBlue.valueProperty()));

        VBox sliders = new VBox(
                minRed, maxRed, minGreen, maxGreen, minBlue, maxBlue,
                ranges
        );
        sliders.setPrefWidth(450);

        final BorderPane pane = new BorderPane();
        pane.setRight(sliders);
        Thread thread = new Thread(() -> {
            while (true) {
                final BufferedImage image = webcam.getImage();
                Pixel[] allPixels = PixelValidator.loadPixelsFromImage(image);
                redMin = minRed.getValue();
                redMax = maxRed.getValue();
                greenMin = minGreen.getValue();
                greenMax = maxGreen.getValue();
                blueMin = minBlue.getValue();
                blueMax = maxBlue.getValue();
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
                                    averageShade > 0.25;
                        }
                );
                List<PixelGroup> pixelGroups = PixelGrouper.findPixelGroups(validPixels, 5);

                Platform.runLater(() -> {
                    Canvas canvas = new Canvas(300, 200);
                    canvas.setScaleX(6);
                    canvas.setScaleY(6);
                    GraphicsContext context = canvas.getGraphicsContext2D();
                    pane.setCenter(canvas);

                    for (Pixel pixel : allPixels) {
                        context.setFill(pixel.getColor());
                        context.fillRect(pixel.getX(), pixel.getY(), 1, 1);
                    }

                    for (PixelGroup group : pixelGroups) {
                        List<Pixel> pixels = group.getPixels();
                        if (pixels.size() > 10) {
                            context.setFill(Color.rgb(0, 0, 0, 0.5));
                            context.fillRect(group.getMinX(), group.getMinY(), group.getMaxX() - group.getMinX(), group.getMaxY() - group.getMinY());
                        }
                    }

                });
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
