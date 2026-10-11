package net.itsthesky.upfall.content.chronospire.puzzle;

public final class WheelRing {
    private WheelRing() {}

    public static final int STATES = 4;

    public static void press(int[] wheels, int index) {
        int n = wheels.length;
        for (int d = -1; d <= 1; d++) {
            if (n < 3 && d != 0) continue;
            int i = Math.floorMod(index + d, n);
            wheels[i] = (wheels[i] + 1) % STATES;
        }
    }

    public static boolean solved(int[] wheels, int target) {
        for (int wheel : wheels) {
            if (wheel != target) return false;
        }
        return true;
    }

    public static int[] solution(int[] wheels, int target) {
        int n = wheels.length;
        int combos = 1 << (2 * n);
        for (int code = 0; code < combos; code++) {
            int[] state = wheels.clone();
            int[] presses = new int[n];
            for (int i = 0; i < n; i++) {
                presses[i] = (code >> (2 * i)) & 3;
                for (int p = 0; p < presses[i]; p++) {
                    press(state, i);
                }
            }
            if (solved(state, target)) {
                return presses;
            }
        }
        return null;
    }
}
