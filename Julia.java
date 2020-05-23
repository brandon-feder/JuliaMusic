import static java.lang.Math.random;
import static java.lang.Math.round;
import static processing.core.PApplet.println;

public class Julia {

    private static float a = 0.01f;
    private static float a_rate;
    private static float last_sum = 0;
    private static int frames_since_change;

    public static int isInSet(float r, float i, float Cr, float Ci) {
        return isInSet_Sub(Cr, Ci, r, i, 0);
    }

    // My implementation of the Julia Set recursive function
    private static int isInSet_Sub(float Cr, float Ci, float Zr, float Zi, int i) {
        if(Zr*Zr + Zi*Zi >= 4) {
            return i;
        } else if(i > 25) {
            return -1;
        } else {
            float new_Zr = Zr * Zr - Zi * Zi + Cr;
            float new_Zi = 2 * Zr * Zi + Ci;
            return isInSet_Sub(Cr, Ci, new_Zr, new_Zi, i+1);
        }
    }
     // Calculates the next value variable a in the equation here https://en.wikipedia.org/wiki/Julia_set#/media/File:JSr07885.gif 
     // based off of the the last value, the amplitude of the current data frame, etc.
    public static float getA(int[] data) {
        // I am not going to comment every line, but here is how this worls:
        // 1. The sum of all the points are found to get the total amplitude
        // 2. The # frames since a was changed is incremented
        // 3. If the amplitude is less then some constant, it is made 0.
        // 4. If the amplitude is avoce some constant, enough time has past since the last change, and the 
            // average is greater than 1 ( this is pointless. I just feel like I will break something because otherwise I dont know wht I added it )
        // 5. Otherwise. do some self explanatory stuff

        int sum = 0;
        for(int point : data) {
            sum += point;
        }

        frames_since_change += 1;

        if( Math.abs((float)sum/data.length) <= 0.000001) {
            last_sum = sum;
            a_rate = 0;
        }

        if(Math.abs((float)sum/data.length) >= 1 && frames_since_change > Settings.amplitude_min_change && Math.abs(last_sum) > Math.abs(sum)) {
            a_rate = -(a /Math.abs(a)) * Math.abs(((float)sum/data.length) / 10);
            last_sum = sum;
            frames_since_change = 0;

            return a;
        } else {
            a += a_rate;
            last_sum = sum;
            return a;
        }
    }
}
