package net.undeadriders.util;

/** Keeps the external part of a shared fire timer while sunlight extends it. */
public final class SunlightFireState {
    private boolean active;
    private boolean igniting;
    private int externalTicks;

    public void beginSunlight(int currentTicks) {
        if (!active) externalTicks = Math.max(0, currentTicks);
        active = true;
        igniting = true;
    }

    public void endSunlight() {
        igniting = false;
    }

    public void onExternalIgnition(int ticks) {
        if (active && !igniting) externalTicks = Math.max(externalTicks, ticks);
    }

    public void onTimerChange(int previous, int next) {
        if (!active || igniting) return;
        if (next <= 0) {
            clear();
        } else if (next > previous) {
            externalTicks = Math.max(externalTicks, next);
        } else {
            externalTicks = Math.max(0, externalTicks - (previous - next));
        }
    }

    public boolean active() { return active; }
    public int externalTicks() { return externalTicks; }

    public void restore(boolean owned, int external, int current) {
        active = owned && current > 0;
        externalTicks = active ? Math.clamp(external, 0, current) : 0;
        igniting = false;
    }

    public void clear() {
        active = false;
        igniting = false;
        externalTicks = 0;
    }
}
