package io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

public class TaintRangesBuilder {
    public void keep(TaintRanges taintRanges, Object target, int argC, TaintRanges srcTaintRanges) {
        if (argC == 0) {
            int length = getLength(target);
            if (length > 0) {
                for (TaintRange taintRange : srcTaintRanges.getTaintRanges()) {
                    if (taintRange.getStart() < length && taintRange.getStop() > length) {
                        taintRange.setStop(length);
                    }
                    taintRanges.add(taintRange);
                }
            } else {
                taintRanges.addAll(srcTaintRanges);
            }

            taintRanges.merge();
        }
    }

    public void append(TaintRanges taintRanges, Object target, TaintRanges oldTaintRanges,
                       Object source, TaintRanges srcTaintRanges, int p1, int p2, int argC) {
        int length = getLength(target);
        switch (argC) {
            case 0:
                // src.append(tgt)
                srcTaintRanges.shift(length - getLength(source));
                taintRanges.addAll(oldTaintRanges);
                taintRanges.addAll(srcTaintRanges);
                break;
            case 2:
                srcTaintRanges.trim(p1, p2);
                srcTaintRanges.shift(length - (p2 - p1));
                taintRanges.addAll(oldTaintRanges);
                taintRanges.addAll(srcTaintRanges);
                break;
            case 3:
                srcTaintRanges.trim(p1, p1 + p2);
                srcTaintRanges.shift(length - p2);
                taintRanges.addAll(oldTaintRanges);
                taintRanges.addAll(srcTaintRanges);
                break;
            default:
                return;
        }
        taintRanges.merge();
    }

    public void subset(TaintRanges taintRanges, TaintRanges oldTaintRanges, Object source, TaintRanges srcTaintRanges, int p1, int p2, int p3, int argC) {
        int length = getLength(source);
        switch (argC) {
            case 1:
                srcTaintRanges.trim(p1, length);
                taintRanges.addAll(srcTaintRanges);
                break;
            case 2:
                srcTaintRanges.trim(p1, p2);
                taintRanges.addAll(srcTaintRanges);
                break;
            case 3:
                oldTaintRanges.clear(p3, (p3 + p2) - p1);
                srcTaintRanges.trim(p1, p2);
                srcTaintRanges.shift(p3);
                taintRanges.addAll(oldTaintRanges);
                taintRanges.addAll(srcTaintRanges);
                break;
            default:
                return;
        }
        taintRanges.merge();
    }

    public void insert(TaintRanges taintRanges, TaintRanges srcTaintRanges, Object source, TaintRanges tgtTaintRanges, int p1, int p2, int p3, int argC) {
        int length = getLength(source);
        switch (argC) {
            case 1:
                tgtTaintRanges.shift(p1);
                srcTaintRanges.split(p1, length + p1);
                taintRanges.addAll(srcTaintRanges);
                taintRanges.addAll(tgtTaintRanges);
                break;
            case 3:
                length = p3 - p2;
                tgtTaintRanges.subRange(p2, p3);
                tgtTaintRanges.shift(p1 - p2);
                srcTaintRanges.split(p1, length + p1);
                taintRanges.addAll(srcTaintRanges);
                taintRanges.addAll(tgtTaintRanges);
                break;
            default:
                return;
        }
        taintRanges.merge();
    }

    public void remove(TaintRanges taintRanges, Object source, TaintRanges tgtTaintRanges, int p1, int p2, int argC) {
        switch (argC) {
            case 0:
                tgtTaintRanges.remove(0, getLength(source));
                break;
            case 1:
                tgtTaintRanges.remove(p1, p1 + 1);
                taintRanges.addAll(tgtTaintRanges);
                break;
            case 2:
                tgtTaintRanges.remove(p1, p2);
                taintRanges.addAll(tgtTaintRanges);
                break;
            default:
                return;
        }
        taintRanges.merge();
    }

    public void replace(TaintRanges taintRanges, Object target, TaintRanges srcTaintRanges, TaintRanges tgtTaintRanges) {
        // @TODO
        taintRanges.add(new TaintRange(0, getLength(target)));
    }

    public void concat(TaintRanges taintRanges, Object target, TaintRanges srcTaintRanges, Object source, TaintRanges tgtTaintRanges, Object[] params) {
        if (params != null && params.length == 1 && source.equals(params[0])) {
            tgtTaintRanges.shift(getLength(target) - getLength(source));
        }
        taintRanges.addAll(srcTaintRanges);
        taintRanges.addAll(tgtTaintRanges);
        taintRanges.merge();
    }

    public void trim(TaintCommand command, TaintRanges taintRanges, Object source, TaintRanges tgtTaintRanges, int argC) {
        if (argC > 0) {
            return;
        }
        if (!tgtTaintRanges.isEmpty()) {
            if (!(source instanceof CharSequence)) {
                taintRanges.addAll(tgtTaintRanges.getTaintRanges());
                return;
            }
            int left = 0;
            CharSequence charSequence = (CharSequence) source;
            int length = charSequence.length();
            if (command.equals(TaintCommand.TRIM) || command.equals(TaintCommand.TRIM_LEFT)) {
                while (left < length && Character.isWhitespace(charSequence.charAt(left))) {
                    left++;
                }
            }
            if (command.equals(TaintCommand.TRIM) || command.equals(TaintCommand.TRIM_RIGHT)) {
                int right = length;
                while (right > 0 && Character.isWhitespace(charSequence.charAt(right - 1))) {
                    right--;
                }
                length = right;
            }
            for (TaintRange taintRange : tgtTaintRanges.getTaintRanges()) {
                int max = Math.max(0, taintRange.getStart() - left);
                int min = Math.min(length, taintRange.getStop()) - left;
                if (min > max) {
                    taintRanges.add(new TaintRange(taintRange.getName(), max, min));
                }
            }
        }
    }

    public static String obj2String(Object obj) {
        if (obj == null) {
            return "";
        }

        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).toString();
        } else if (obj instanceof StringWriter) {
            return ((StringWriter) obj).getBuffer().toString();
        } else if (obj instanceof ByteArrayOutputStream) {
            return ((ByteArrayOutputStream) obj).toString();
        } else if (obj instanceof Character) {
            return ((Character) obj).toString();
        } else if (obj instanceof byte[]) {
            return new String((byte[]) obj);
        } else if (obj instanceof char[]) {
            return new String((char[]) obj);
        } else {
            return (obj.getClass().getName() + "@" + Integer.toHexString(obj.hashCode()));
        }
    }

    public static int getLength(Object obj) {
        if (obj == null) {
            return 0;
        }

        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length();
        } else if (obj instanceof StringWriter) {
            return ((StringWriter) obj).getBuffer().length();
        } else if (obj instanceof ByteArrayOutputStream) {
            return ((ByteArrayOutputStream) obj).size();
        } else if (obj instanceof Character) {
            return 1;
        } else if (obj instanceof boolean[]) {
            return ((boolean[]) obj).length;
        } else if (obj instanceof byte[]) {
            return ((byte[]) obj).length;
        } else if (obj instanceof char[]) {
            return ((char[]) obj).length;
        } else if (obj instanceof short[]) {
            return ((short[]) obj).length;
        } else if (obj instanceof int[]) {
            return ((int[]) obj).length;
        } else if (obj instanceof float[]) {
            return ((float[]) obj).length;
        } else if (obj instanceof long[]) {
            return ((long[]) obj).length;
        } else if (obj instanceof double[]) {
            return ((double[]) obj).length;
        } else {
            return (obj.getClass().getName() + "@" + Integer.toHexString(obj.hashCode())).length();
        }
    }
}
