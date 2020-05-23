import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.sound.sampled.*;
import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static processing.core.PApplet.println;

public class Audio {

    public static int[] raw_data;
    public static int[][] data_frames;
    public static int[][] DFT_frames;
    public static int dft_max_avg;

    private static int duration;
    private static int numBytes;

    // Get audio file info, process it/read save/create save, etc.
    public static void getAudioData() {
        System.out.println("Getting Audio Data");
        try {
            getAudioFileInfo();
            File f = new File(Settings.media_save);
            getRawAudioData();
            if (f.exists()) {
                readAudioSave();
            } else {
                getAudioDataFrames();
                saveAudioData();
            }
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Play the file.
    public static void play() {
        try {
            File file = new File(Settings.audio_file_path);

            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
            clip.start();
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }

        for(int[] frame : DFT_frames) {
            int avg = 0;
            for(int point : frame) {
                avg += point;
            }
            avg /= frame.length;

            if(avg > dft_max_avg) {
                dft_max_avg = avg;
            }
        }
    }

    // Read the raw data from the audio file
    private static void getRawAudioData() throws IOException, UnsupportedAudioFileException {
        System.out.println(" === Getting Raw Audio Data");
        AudioInputStream stream = AudioSystem.getAudioInputStream(new File(Settings.audio_file_path));

        raw_data = new int[numBytes / 2];

        byte[] buff = new byte[numBytes];
        stream.read(buff);

        for (int i = 0; i < numBytes / 2; i++) {
            raw_data[i] = buff[i * 2 + 1];
        }

        numBytes /= 2;
    }

    // Get info about the wav file
    private static void getAudioFileInfo() throws IOException, UnsupportedAudioFileException {
        System.out.println(" === Getting Audio File Info");
        File file = new File(Settings.audio_file_path);
        AudioInputStream stream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = stream.getFormat();
        duration = (int) (1000 * stream.getFrameLength() / format.getFrameRate());
        numBytes = (int) stream.getFrameLength() * stream.getFormat().getFrameSize();
    }

    // Split the raw data from the WAV file into overlapping frames
    private static void getAudioDataFrames() throws Exception {
        System.out.println(" === Getting Audio Data Seperate Frames");
        data_frames = new int[Settings.FPS * duration / 1000][Settings.frame_range];
        DFT_frames = new int[data_frames.length][Settings.frame_range / Settings.DFT_range_factor];
        for (int i = 0; i < data_frames.length; i++) {
            int start = i * numBytes / data_frames.length;
            data_frames[i] = getDataRange(raw_data, Settings.frame_range, i * (numBytes / data_frames.length));
            DFT_frames[i] = getDFT(data_frames[i]);

            if (i % 10 == 0) {
                System.out.println(Math.round(10000 * (float) i / (float) data_frames.length) / 100f);
            }
        }
    }

    // Get some part of a set of data
    private static int[] getDataRange(int[] data, int range, int start) {
        int[] data_range = new int[range];

        for (int i = 0; i < range; i++) {
            data_range[i] = (start + i > 0 && start + i < data.length) ? data[start + i] : 0;
        }

        return data_range;
    }

    // Calculate the DFT. ( not all of it, but some of it because know one cars about the very high frequencies )
    private static int[] getDFT(int[] data) throws Exception {
        int N = data.length;
        int[] k_list = new int[N / Settings.DFT_range_factor];
        int[] X = new int[N / Settings.DFT_range_factor];

        for (int i = 0; i < N / Settings.DFT_range_factor; i++) {
            k_list[i] = N - N / Settings.DFT_range_factor + i;
        }

        int numThreads = 8;

        DFT.min_k = k_list[0];
        DFT.next_k = k_list[0];
        DFT.max_k = k_list[k_list.length - 1];
        DFT.data = data;
        DFT.coefs = new int[k_list.length];

        DFT[] threads = new DFT[numThreads];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new DFT();
            threads[i].start();
        }

        for (DFT thread : threads) {
            thread.join();
        }

        return DFT.coefs;
    }
    
    // A class that uses threading to calculate the DFT faster
    public static class DFT extends Thread {
        public static int min_k;
        public static int next_k;
        public static int max_k;
        public static int[] coefs;
        public static int[] data;

        private int X(int k) {
            int real = 0;
            int imag = 0;

            for (int n = 0; n < data.length; n++) {
                real += data[n] * Math.cos(Math.PI * 2d * (double) k * (double) n / (double) data.length);
                imag += data[n] * Math.cos(Math.PI * 2d * (double) k * (double) n / (double) data.length);
            }

            real /= 1000;
            imag /= 1000;

            return (real * real) + (imag * imag);
        }

        public void run() {
            int k;
            while (next_k <= max_k) {
                k = getNextK();
                coefs[k - min_k] = X(k);
            }
        }

        public int getNextK() {
            next_k += 1;

            return next_k - 1;
        }
    }

    // Save the audio data in a json file so it does not beed to be reprocessed every time
    public static void saveAudioData() {
        JSONObject jsonObject = new JSONObject();

        JSONArray frames = new JSONArray();

        for (int[] frame : data_frames) {
            JSONArray new_frame = new JSONArray();
            for (int point : frame) {
                new_frame.add(point);
            }
            frames.add(new_frame);
        }

        JSONArray dft_frames = new JSONArray();

        for (int[] dft_frame : DFT_frames) {
            JSONArray new_dft_frame = new JSONArray();
            for (int point : dft_frame) {
                new_dft_frame.add(point);
            }
            dft_frames.add(new_dft_frame);
        }

        jsonObject.put("frames", frames);
        jsonObject.put("dft_frames", dft_frames);

        try (FileWriter file = new FileWriter(Settings.media_save)) {
            file.write(jsonObject.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Read the audio save JSON file and parse it, get its values, and put those values in there respective arrays
    public static void readAudioSave() {
        data_frames = new int[Settings.FPS * duration / 1000][Settings.frame_range];
        DFT_frames = new int[data_frames.length][Settings.frame_range / Settings.DFT_range_factor];

        try {
            JSONParser parser = new JSONParser();
            File jsonFile = new File(Settings.media_save);
            JSONObject jsonObject;
            jsonObject = (JSONObject) parser.parse(new FileReader(jsonFile));
            JSONArray frames = (JSONArray) jsonObject.get("frames");
            Iterator<JSONArray> iterator = frames.iterator();

            int i = 0;
            while (iterator.hasNext()) {
                JSONArray frame = iterator.next();
                Iterator<Long> iterator2 = frame.iterator();

                int j = 0;
                while (iterator2.hasNext()) {
                    long point = iterator2.next();
                    int test = 0;
                    data_frames[i][j] = (int) point;
                    j++;
                }

                i++;
            }

            frames = (JSONArray) jsonObject.get("dft_frames");
            iterator = frames.iterator();
            i = 0;
            while (iterator.hasNext()) {
                JSONArray dft = iterator.next();
                Iterator<Long> iterator2 = dft.iterator();

                int j = 0;
                while (iterator2.hasNext()) {
                    long point = iterator2.next();
                    DFT_frames[i][j] = (int) point;
                    j++;
                }

                i++;
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
