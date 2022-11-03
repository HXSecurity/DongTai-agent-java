package io.dongtai.iast.common.scope;

public class GeneralScope {
    private int level;

    public boolean in() {
        return this.level != 0;
    }

    public boolean isFirst() {
        return this.level == 1;
    }

    public void enter() {
        this.level++;
    }

    public void leave() {
        this.level = decrement(this.level);
    }

    private int decrement(int level) {
        if (level > 0) {
            return level - 1;
        }
        return 0;
    }
}
