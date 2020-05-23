import processing.core.PApplet;

import java.awt.*;

public class Main extends PApplet {

    private int firstFrameStart = -1;
    private int frameEnd = 0; // Time since the end of the previus frame
    private int currentFrame = 1;
    
    // The colors between wich the color lerp is applied
    private int c1 = color(0, 0, 0);
    private int c2 = color(0, 0, 0);


    private float a = 0; // The a in the formula here: https://en.wikipedia.org/wiki/Julia_set#/media/File:JSr07885.gif 

    public static void main(String[] args) {
        PApplet.main("Main");
    }

    public void settings() {
        size(Settings.canvas_dims, Settings.canvas_dims);
    }

    public void setup() {
        textSize(32);
        frameRate(Settings.FPS);
        Audio.getAudioData();
        Audio.play(); // Start playing the music
        firstFrameStart = millis(); // Gets at what time the first frame starts
    }

    public void draw() {
        background(255, 255, 255);

        int currentDataFrame = getDataFrame(); // The current frame of data that should be used

        getNewColorRange(currentDataFrame);

        a = Julia.getA(Audio.data_frames[currentDataFrame]); // Update the value of a. Look at comment for a definition above

         // Get the real and imaginary parts for the C variable in the julia set recursive defenition. Look at comment for varaible a defenition
        float Cr = (float) (0.7*Math.cos((a) % (2 * Math.PI)));
        float Ci = (float) (0.7*Math.sin((a) % (2 * Math.PI)));

        // For every pixel
        for(int x = 0; x < Settings.canvas_dims; x++) {
            for(int y = 0; y < Settings.canvas_dims; y++) {
                int res = Julia.isInSet((float)(x - Settings.canvas_dims/2)/400,(float)(y - Settings.canvas_dims/2)/400,  Cr, Ci); // Get how many calls of recursive function until point diverges ( -1 if it does not )
                if(res < 0) { // If (x, y) ∈ Julia Set
                    set(x, y, new Color(255, 255, 255).getRGB());
                } else {
                    set(x, y, getPointColor(res));
                }
            }
        }

        // Calculate the framerate, show on screen, etc.
        fill(255, 255, 255);
        text(1000/(millis() - frameEnd), 10, 30);
        currentFrame += 1; // Increment number of frames
        frameEnd = millis();
    }

    private void getNewColorRange(int frame) {
        // Shader 1 & 2:
            // Calculate the average of the frequencies of the FFT
            long avg_temp = 0;
            int n = 0;
            for(int i = 0; i < Audio.DFT_frames[frame].length; i++) {
                avg_temp += Audio.DFT_frames[frame][i] * i;
                n +=  Audio.DFT_frames[frame][i];
            }
            int avg = (n==0) ? 0 : (int) (avg_temp / n);

            int r = 10000; // The lerp range 
            c1 = 16777215 - avg*16777215/Audio.DFT_frames[frame].length + r/2; // I dont know why this works but it looks cool
        
    }

    public int getPointColor(int depth) {
        // Shader 1:
            return lerpColor(c1, c2, (float)depth/25f);

        // Shader 2:
            // return lerpColor(color(255, 0, 0), color(0, 0, 0), (float)depth/25f);
    }

    private int getDataFrame() {
        return Settings.FPS * (millis() - firstFrameStart)/ 1000; // Get the current frame of data
    }

    private void drawData(int[] data, int basePos, int scaleFactor) { // Gragh the data
        int dist = 1;
        for(int i = 0; i < Settings.canvas_dims/dist-1; i += dist) {
            line(i*dist, data[(int) (((float) i / Settings.canvas_dims) * data.length)]/scaleFactor + basePos, (i+1)*dist, data[(int) (((float) (i+1) / Settings.canvas_dims) * data.length)]/scaleFactor + basePos);
            line(i*dist, 0/scaleFactor + basePos, (i+1)*dist, 0/scaleFactor + basePos);
        }

        stroke(255, 0, 0);
        line(0, basePos, Settings.canvas_dims, basePos);
        stroke(0, 0, 0);

    }
}