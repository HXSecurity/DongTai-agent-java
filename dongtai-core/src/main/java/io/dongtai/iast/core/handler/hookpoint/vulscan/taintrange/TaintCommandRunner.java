package io.dongtai.iast.core.handler.hookpoint.vulscan.taintrange;

import io.dongtai.log.DongTaiLog;

import java.util.ArrayList;
import java.util.List;

public class TaintCommandRunner {
    private String signature;

    private TaintRangesBuilder builder;

    private TaintRangesBuilder.Command command;

    private List<RunnerParam> params = new ArrayList<RunnerParam>();

    private int paramsCount = 0;

    static class RunnerParam {
        private int position;
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

    public static TaintCommandRunner getInstance(String signature, TaintRangesBuilder.Command command) {
        return getInstance(signature, command, null);
    }

    public static TaintCommandRunner getInstance(String signature, TaintRangesBuilder.Command command, List<String> params) {
        try {
            TaintCommandRunner r = new TaintCommandRunner();
            r.signature = signature;
            r.builder = new TaintRangesBuilder();
            r.command = command;
            if (params != null) {
                r.paramsCount = params.size();
                for (String param : params) {
                    r.params.add(new RunnerParam(param));
                }
            }
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    public TaintRangesBuilder getTaintRangesBuilder() {
        return this.builder;
    }

    public TaintRanges run(Object source, Object target, Object[]params, TaintRanges oldTaintRanges, TaintRanges newTaintRanges) {
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
        } catch (Exception e) {
            DongTaiLog.error(this.signature + " taint command parameters fetch failed: " + e.getMessage());
            return tr;
        }

        switch (this.command) {
            case KEEP:
                this.builder.keep(tr, target, this.paramsCount, newTaintRanges);
                break;
            case APPEND:
                this.builder.append(tr, target, oldTaintRanges, source, newTaintRanges, p1, p2, this.paramsCount);
            default:
                break;
        }

        return tr;
    }
}
