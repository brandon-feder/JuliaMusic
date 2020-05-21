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

    public static float getA(int[] data) {
        int sum = 0;

        for(int point : data) {
            sum += point;
        }

        frames_since_change += 1;

        if( Math.abs((float)sum/data.length) <= 0.000001) {
            last_sum = sum;
            a_rate = 0;
        }

        // System.out.println(Settings.amplitude_min_change);

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
