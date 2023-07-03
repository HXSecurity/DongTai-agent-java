package io.dongtai.iast.common.constants;

public class ReportType {
    public static final int HEART_BEAT = 0x01;
    public static final int SCA = 0x11;
    public static final int SCA_BATCH = 0x12;
    public static final int VULN_NORMAL = 0x21;
    public static final int VULN_SAAS_POOL = 0x24;
    public static final int VUL_HARDCODE = 0x25;
    public static final int LIMIT_HOOK_POINT_RATE = 0x41;
    public static final int LIMIT_HEAVY_TRAFFIC_RATE = 0x42;
    public static final int LIMIT_PERFORMANCE_FALLBACK = 0x43;
    public static final int ERROR_THREAD = 0x45;
    public static final int SECOND_FALLBACK = 0x46;
    public static final int ERROR_LOG = 0x51;
    public static final int API = 0x61;
    public static final int API_V2 = 0x62;
    public static final int SERVICE = 0x81;
    public static final int SERVICE_DIR = 0x82;
}
