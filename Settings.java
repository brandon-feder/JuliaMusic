import java.io.File;
import java.io.FileInputStream;

public class Settings {
//    public static String audio_file_path = "./Media/believer_im.wav";
   public static String audio_file_path = "./Media/stiches.wav";
//    public static String audio_file_path = "./Media/500Hz.wav";
//    public static String audio_file_path = "./Media/sweep.wav";

    public static int FPS = 30; // More than 60 is pointless

    public static String media_save = "./Saves/" + audio_file_path.split("/")[2].split("\\.")[0]+ "_" + FPS + ".json";
    public static int frame_range = 3000;

    public static int canvas_dims = 1000;
    public static int DFT_range_factor = 32; // Only process that last 1/DFT_range_factor of the array

    public static float amplitude_min_change = 6f;

}
