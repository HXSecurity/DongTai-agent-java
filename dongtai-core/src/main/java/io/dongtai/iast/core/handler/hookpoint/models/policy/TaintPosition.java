package io.dongtai.iast.core.handler.hookpoint.models.policy;

import java.util.HashSet;
import java.util.Set;

public class TaintPosition {
    public static final String OBJECT = "O";
    public static final String RETURN = "R";
    public static final String PARAM_PREFIX = "P";

    public static final String OR = "\\|";

    public static final TaintPosition POS_OBJECT = new TaintPosition(OBJECT);
    public static final TaintPosition POS_RETURN = new TaintPosition(RETURN);

    public static final String ERR_POSITION_EMPTY = "taint position can not empty";
    public static final String ERR_POSITION_INVALID = "taint position invalid";
    public static final String ERR_POSITION_PARAMETER_INDEX_INVALID = "taint position parameter index invalid";

    private final String value;
    private final int parameterIndex;

    /**
     * @param value taint position value
     */
    public TaintPosition(String value) {
        if (value == null) {
            throw new IllegalArgumentException(ERR_POSITION_EMPTY);
        }
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(ERR_POSITION_EMPTY);
        }

        int index = -1;
        boolean isParameter = value.startsWith(PARAM_PREFIX);
        if (isParameter) {
            String idx = value.substring(1).trim();
            try {
                index = Integer.parseInt(idx) - 1;
                if (index < 0) {
                    throw new NumberFormatException("position index can not be negative: " + index);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(ERR_POSITION_PARAMETER_INDEX_INVALID + ": " + value + ", " + e.getMessage());
            }
            this.value = PARAM_PREFIX + idx;
        } else {
            if (!OBJECT.equals(value) && !RETURN.equals(value)) {
                throw new IllegalArgumentException(ERR_POSITION_INVALID + ": " + value);
            }
            this.value = value;
        }

        this.parameterIndex = index;
    }

    public boolean isObject() {
        return this.equals(POS_OBJECT);
    }

    /* renamed from: b */
    public boolean isReturn() {
        return this.equals(POS_RETURN);
    }

    public boolean isParameter() {
        return this.parameterIndex >= 0;
    }

    public int getParameterIndex() {
        return this.parameterIndex;
    }

    public static Set<TaintPosition> parse(String position) throws TaintPositionException {
        if (position == null || position.isEmpty()) {
            throw new TaintPositionException("taint position can not be empty");
        }
        return parse(position.split(OR));
    }

    private static Set<TaintPosition> parse(String[] positions) throws TaintPositionException {
        if (positions == null || positions.length == 0) {
            throw new TaintPositionException("taint positions can not be empty");
        }
        Set<TaintPosition> tps = new HashSet<TaintPosition>();
        for (String position : positions) {
            position = position.trim();
            if (position.startsWith(PARAM_PREFIX)) {
                Set<TaintPosition> paramPos = parseParameter(position.substring(1));
                if (!paramPos.isEmpty()) {
                    tps.addAll(paramPos);
                }
            } else {
                if (position.isEmpty()) {
                    continue;
                }
                tps.add(parseOne(position));
            }
        }

        return tps;
    }

    private static Set<TaintPosition> parseParameter(String indiesStr) throws TaintPositionException {
        String[] indies = indiesStr.split(",");
        Set<TaintPosition> tps = new HashSet<TaintPosition>();
        for (String index : indies) {
            tps.add(parseOne(PARAM_PREFIX + index));
        }
        return tps;
    }

    private static TaintPosition parseOne(String position) throws TaintPositionException {
        try {
            return new TaintPosition(position);
        } catch (IllegalArgumentException e) {
            throw new TaintPositionException(e.getMessage(), e.getCause());
        }
    }

    public static boolean hasObject(Set<TaintPosition> positions) {
        if (positions == null) {
            return false;
        }
        return positions.contains(POS_OBJECT);
    }

    public static boolean hasReturn(Set<TaintPosition> positions) {
        if (positions == null) {
            return false;
        }
        return positions.contains(POS_RETURN);

    }

    public static boolean hasParameter(Set<TaintPosition> positions) {
        if (positions == null) {
            return false;
        }
        for (TaintPosition position : positions) {
            if (position.isParameter()) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasParameterIndex(Set<TaintPosition> positions, int index) {
        if (positions == null || index < 0) {
            return false;
        }
        for (TaintPosition position : positions) {
            if (position.getParameterIndex() == index) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof TaintPosition)) {
            return false;
        }

        final TaintPosition position = (TaintPosition) obj;
        return this.value.equals(position.value);

    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
}
