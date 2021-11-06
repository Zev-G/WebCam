import com.github.sarxos.webcam.Webcam;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.util.List;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        final Webcam webcam = Webcam.getDefault();
        webcam.open();
        final Pane pane = new StackPane();
        Thread thread = new Thread(() -> {
            while (true) {
                final BufferedImage image = webcam.getImage();
                Pixel[] allPixels = PixelValidator.loadPixelsFromImage(image);
                Pixel[] validPixels = PixelValidator.validate(
                        allPixels,
                        pixel -> {
                            Color color = pixel.getColor();
                            double averageShade = averageShade(color);

                            double total = color.getRed() + color.getGreen() + color.getBlue();
                            double percentageRed = color.getRed() / total;
                            double percentageGreen = color.getGreen() / total;
                            double percentageBlue = color.getBlue() / total;

                            return percentageRed > 0.3 &&
                                    percentageBlue < 0.25 &&
                                    percentageGreen > 0.2 && percentageGreen < 0.4 &&
                                    averageShade > 0.25;
                        }
                );
                List<PixelGroup> pixelGroups = PixelGrouper.findPixelGroups(validPixels, 5);

                Platform.runLater(() -> {
                    Canvas canvas = new Canvas(300, 200);
                    canvas.setScaleX(6);
                    canvas.setScaleY(6);
                    GraphicsContext context = canvas.getGraphicsContext2D();
                    pane.getChildren().setAll(canvas);

                    for (Pixel pixel : allPixels) {
                        context.setFill(pixel.getColor());
                        context.fillRect(pixel.getX(), pixel.getY(), 1, 1);
                    }

                    for (PixelGroup group : pixelGroups) {
                        List<Pixel> pixels = group.getPixels();
                        if (pixels.size() > 10) {
                            System.out.println(pixels.size());
                            for (Pixel pixel : pixels) {
                                double x = pixel.getX();
                                double y = pixel.getY();
                            }
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
