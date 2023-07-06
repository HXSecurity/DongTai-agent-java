package io.dongtai.iast.core.handler.hookpoint.models.taint.range;

import io.dongtai.iast.core.handler.hookpoint.models.policy.PropagatorNode;
import io.dongtai.log.DongTaiLog;
import io.dongtai.log.ErrorCode;

import java.util.ArrayList;
import java.util.List;

public class TaintCommandRunner {
    private String signature;

    private TaintRangesBuilder builder;

    private TaintCommand command;

    private final List<RunnerParam> params = new ArrayList<RunnerParam>();

    private final List<String> origParams = new ArrayList<String>();

    private int paramsCount = 0;

    static class RunnerParam {
        private final int position;
        private boolean isLiteral = false;

        public RunnerParam(String param) {
            if (param.startsWith("P")) {
                this.position = Integer.parseInt(param.substring(1)) - 1;
            } else {
                this.position = Integer.parseInt(param);
                this.isLiteral = true;
            }
        }

        public int getParam(Object[] params) {
            if (this.isLiteral) {
                return this.position;
            }
            if (params == null) {
                return 0;
            }

            return (Integer) params[this.position];
        }
    }

    public TaintCommand getCommand() {
        return command;
    }

    public static TaintCommandRunner create(String signature, TaintCommand command) {
        return create(signature, command, null);
    }

    public static TaintCommandRunner create(String signature, TaintCommand command, String[] params) {
        try {
            TaintCommandRunner r = new TaintCommandRunner();
            r.signature = signature;
            r.builder = new TaintRangesBuilder();
            r.command = command;
            if (params != null && params.length > 0) {
                r.paramsCount = params.length;
                for (String param : params) {
                    r.params.add(new RunnerParam(param));
                    r.origParams.add(param);
                }
            }
            return r;
        } catch (Throwable e) {
            return null;
        }
    }

    public TaintRangesBuilder getTaintRangesBuilder() {
        return this.builder;
    }

    public List<String> getOrigParams() {
        return origParams;
    }

    public TaintRanges run(PropagatorNode propagatorNode, Object source, Object target, Object[] params,
                           TaintRanges oldTaintRanges, TaintRanges srcTaintRanges) {
        int p1 = 0;
        int p2 = 0;
        int p3 = 0;
        TaintRanges tr = new TaintRanges();

        try {
            if (this.paramsCount > 0) {
                p1 = this.params.get(0).getParam(params);
            }
            if (this.paramsCount > 1) {
                p2 = this.params.get(1).getParam(params);
            }
            if (this.paramsCount > 2) {
                p3 = this.params.get(2).getParam(params);
            }
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("TAINT_COMMAND_GET_PARAMETERS_FAILED"), this.signature, e.getMessage());
            return tr;
        }

        if (propagatorNode.hasTags()) {
            String[] tags = propagatorNode.getTags();
            int len = TaintRangesBuilder.getLength(target);
            for (String tag : tags) {
                tr.add(new TaintRange(tag, 0, len));
            }
        }

        try {
            switch (this.command) {
                case KEEP:
                    this.builder.keep(tr, target, this.paramsCount, srcTaintRanges);
                    break;
                case APPEND:
                    this.builder.append(tr, target, oldTaintRanges, source, srcTaintRanges, p1, p2, this.paramsCount);
                    break;
                case SUBSET:
                    this.builder.subset(tr, oldTaintRanges, source, srcTaintRanges, p1, p2, p3, this.paramsCount);
                    break;
                case INSERT:
                    this.builder.insert(tr, oldTaintRanges, source, srcTaintRanges, p1, p2, p3, this.paramsCount);
                    break;
                case REPLACE:
                    this.builder.replace(tr, target, oldTaintRanges, source, srcTaintRanges, p1, p2, this.paramsCount);
                    break;
                case REMOVE:
                    this.builder.remove(tr, source, srcTaintRanges, p1, p2, this.paramsCount);
                    break;
                case CONCAT:
                    this.builder.concat(tr, target, oldTaintRanges, source, srcTaintRanges, params);
                    break;
                case OVERWRITE:
                    this.builder.overwrite(tr, oldTaintRanges, source, srcTaintRanges, p1, this.paramsCount);
                case TRIM:
                case TRIM_LEFT:
                case TRIM_RIGHT:
                    this.builder.trim(this.command, tr, source, srcTaintRanges, this.paramsCount);
                    break;
                default:
                    break;
            }
        } catch (Throwable e) {
            DongTaiLog.warn(ErrorCode.get("TAINT_COMMAND_RANGE_PROCESS_FAILED"), e);
        }

        tr.untag(propagatorNode.getUntags());

        return tr;
    }
}
