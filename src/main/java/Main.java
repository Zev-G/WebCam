import com.github.sarxos.webcam.Webcam;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        final Webcam webcam = Webcam.getDefault();
        webcam.open();
        final Pane pane = new Pane();
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    final BufferedImage image = webcam.getImage();
                    Platform.runLater(new Runnable() {
                        public void run() {
                            Image jfxImage = SwingFXUtils.toFXImage(image, null);
                            Canvas canvas = new Canvas(1000, 1000);
                            GraphicsContext context = canvas.getGraphicsContext2D();
                            pane.getChildren().setAll(canvas);
                            PixelReader reader = jfxImage.getPixelReader();
                            double averageX = 0;
                            double averageY = 0;
                            int acceptedPoints = 0;
                            for (int x = 1; x < jfxImage.getWidth(); x++) {
                                for (int y = 0; y < jfxImage.getHeight(); y++) {
                                    Color color = reader.getColor(x, y);

                                    double averageShade = averageShade(color);

                                    double total = color.getRed() + color.getGreen() + color.getBlue();
                                    double percentageRed = color.getRed() / total;
                                    double percentageGreen = color.getGreen() / total;
                                    double percentageBlue = color.getBlue() / total;

                                if (
                                        percentageRed > 0.3 &&
                                        percentageBlue < 0.25 &&
                                        percentageGreen > 0.2 && percentageGreen < 0.4 &&
                                        averageShade > 0.25
                                ) {
                                    System.out.println(averageShade);
                                        averageX += x;
                                        averageY += y;
                                        acceptedPoints++;

                                    }
                                    context.setFill(reader.getColor(x, y));
                                    context.fillRect(x, y, 1, 1);
                                }
                            }
                            if (acceptedPoints > 100) {
                                averageX = averageX / acceptedPoints;
                                averageY = averageY / acceptedPoints;
                                context.setFill(Color.rgb(0, 0, 0, 0.5));
                                context.fillOval(averageX - 10, averageY - 10, 20, 20);
                            }

//                            ImageView imageView = new ImageView(jfxImage);
//                            imageView.setPreserveRatio(true);
//                            imageView.setFitHeight(500);
                        }
                    });
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
        primaryStage.setScene(new Scene(pane));
        primaryStage.show();
    }

    private double averageShade(Color color) {
        return (color.getRed() + color.getGreen() + color.getBlue()) / 3D;
    }

}
