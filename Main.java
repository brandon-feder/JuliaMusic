import processing.core.PApplet;

import java.awt.*;

public class Main extends PApplet {

    private int firstFrameStart = -1;
    private int frameEnd = 0;
    private int currentFrame = 1;
    private int c1 = color(0, 0, 0);
    private int c2 = color(0, 0, 0);

    private float a = 0;

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
        Audio.play();
        firstFrameStart = millis();
    }

    public void draw() {
        background(255, 255, 255);
//
        int currentDataFrame = getDataFrame();

        getNewColorRange(currentDataFrame);

        a = Julia.getA(Audio.data_frames[currentDataFrame]);
        float Cr = (float) (0.7*Math.cos((a) % (2 * Math.PI)));
        float Ci = (float) (0.7*Math.sin((a) % (2 * Math.PI)));

        for(int x = 0; x < Settings.canvas_dims; x++) {
            for(int y = 0; y < Settings.canvas_dims; y++) {
//                System.out.println(Julia.isInSet((x - Settings.canvas_dims/2)/500, (y - Settings.canvas_dims/2)/500, Cr, Ci));
                int res = Julia.isInSet((float)(x - Settings.canvas_dims/2)/400,(float)(y - Settings.canvas_dims/2)/400,  Cr, Ci);
                if(res < 0) {
                    set(x, y, new Color(0, 0, 0).getRGB());
                } else {
                    set(x, y, getPointColor(res));
                }
            }
        }

        // drawData(Audio.DFT_frames[currentDataFrame], 3*Settings.canvas_dims/4, -1);

        fill(255, 255, 255);
        text(1000/(millis() - frameEnd), 10, 30);
        currentFrame += 1;
        frameEnd = millis();
    }

    private void getNewColorRange(int frame) {
        // Shader 1:
            long avg_temp = 0;
            int n = 0;
            for(int i = 0; i < Audio.DFT_frames[frame].length; i++) {
                avg_temp += Audio.DFT_frames[frame][i] * i;
                n +=  Audio.DFT_frames[frame][i];
            }
            int avg = (n==0) ? 0 : (int) (avg_temp / n);
            int r = 10000;
            c1 = avg*16777215/Audio.DFT_frames[frame].length + r/2;

        // Shader 2
            // long avg_temp = 0;
            // int n = 0;
            // for(int i = 0; i < Audio.DFT_frames[frame].length; i++) {
            //     avg_temp += Audio.DFT_frames[frame][i] * i;
            //     n +=  Audio.DFT_frames[frame][i];
            // }
            // int avg = (n==0) ? 0 : (int) (avg_temp / n);
            // int r = 100000;
            // c1 = avg*(16777215 - r)/Audio.DFT_frames[frame].length;
            // c2 = avg*(16777215 - r)/Audio.DFT_frames[frame].length + r/2;
            // int test = 0;
        
    }

    public int getPointColor(int depth) {
        // Shader 1:
            return lerpColor(c1, c2, (float)depth/25f);

        // Shader 2
            // int w1 = (c1 >> 32)&0x0ff;
            // int x1 = (c1 >> 16)&0x0ff;
            // int y1 =(c1 >> 8) &0x0ff;
            // int z1 = (c1)&0x0ff;
            // int w2 = (c2 >> 32)&0x0ff;
            // int x2 = (c2 >> 16)&0x0ff;
            // int y2 =(c2 >> 8) &0x0ff;
            // int z2 = (c2)&0x0ff;

            // return lerpColor(color(y1, z1, w1, x1), color(y2, z2, w2, x2), (float)depth/25f);
    }

    // private int getAbsMax(int[] arr) {
    //     int max = 0;
    //     for(int i = 0; i < arr.length; i++) {
    //         if(arr[i] > max) {
    //             max = arr[i];
    //         } else if(-arr[i] > max) {
    //             max = -arr[i];
    //         }
    //     }

    //     return max;
    // }

    public void keyPressed() {
        if(keyCode == UP) {
            Settings.amplitude_min_change += 0.1;
        } else if(keyCode == DOWN) {
            Settings.amplitude_min_change -= 0.1;
        }
    }

    private int getDataFrame() {
        return Settings.FPS * (millis() - firstFrameStart)/ 1000;
    }

    private void drawData(int[] data, int basePos, int scaleFactor) {
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