package me.tyler.raytrace;

public class FastMath {

    private static double[] a = new double[65536];

    public static final float sin(float f) {
        return (float) a[(int) (f * 10430.378F) & '\uffff'];
    }

    public static final float cos(float f) {
        return (float) a[(int) (f * 10430.378F + 16384.0F) & '\uffff'];
    }

    public static final float fract(float f){
        return (float) (f - Math.floor(f));
    }

    static {
        for (int i = 0; i < 65536; ++i) {
            a[i] = Math.sin((double) i * 3.141592653589793D * 2.0D / 65536.0D);
        }
    }

}
