package com.secnium.iast.core.handler.vulscan;

import com.secnium.iast.core.EngineManager;
import com.secnium.iast.core.handler.models.IASTSinkModel;
import com.secnium.iast.core.handler.models.MethodEvent;
import com.secnium.iast.core.handler.vulscan.dynamic.DynamicPropagatorScanner;
import com.secnium.iast.core.handler.vulscan.normal.CookieFlagsMissingVulScan;
import com.secnium.iast.core.handler.vulscan.normal.CryptoBacCiphersVulScan;
import com.secnium.iast.core.handler.vulscan.normal.CryptoBadMacVulScan;
import com.secnium.iast.core.handler.vulscan.normal.CryptoWeakRandomnessVulScan;
import com.secnium.iast.core.handler.vulscan.overpower.AuthInfoManager;
import com.secnium.iast.core.handler.vulscan.overpower.IJdbc;
import com.secnium.iast.core.handler.vulscan.overpower.JDBCImpl.MySqlImpl;
import com.secnium.iast.core.handler.vulscan.overpower.JDBCImpl.OracleImpl;
import com.secnium.iast.core.handler.vulscan.overpower.JDBCImpl.PostgresImpl;
import com.secnium.iast.core.handler.vulscan.overpower.JDBCImpl.SqlServerImpl;
import com.secnium.iast.core.handler.vulscan.overpower.LoginLogicRecognize;
import com.secnium.iast.core.handler.vulscan.overpower.OverPowerScanner;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class ScannerFactory {
    private static ScannerFactory INSTANCE = null;
    private static final ArrayList<IJdbc> JDBC_IMPLS = new ArrayList<IJdbc>();

    public static ScannerFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ScannerFactory();
        }
        return INSTANCE;
    }

    private IVulScan getDynamicVulScanner() {
        return new DynamicPropagatorScanner();
    }

    private IVulScan getOverPowerVulScanner() {
        return new OverPowerScanner();
    }

    private IVulScan getStaticVulScanner(String sinkType) {
        if (VulnType.CRYPTO_WEEK_RANDOMNESS.equals(sinkType)) {
            return new CryptoWeakRandomnessVulScan();
        } else if (VulnType.CRYPTO_BAD_MAC.equals(sinkType)) {
            return new CryptoBadMacVulScan();
        } else if (VulnType.CRYPTO_BAC_CIPHERS.equals(sinkType)) {
            return new CryptoBacCiphersVulScan();
        } else if (VulnType.COOKIE_FLAGS_MISSING.equals(sinkType)) {
            return new CookieFlagsMissingVulScan();
        }
        return null;
    }

    /**
     * 根据配置扫描
     *
     * @param invokeIdSequencer
     * @param sink
     * @param jdbcImpl
     * @param event
     */
    public static void scan(AtomicInteger invokeIdSequencer, IASTSinkModel sink, IJdbc jdbcImpl, MethodEvent event) {
        ScannerFactory factory = ScannerFactory.getInstance();
        IVulScan scanner = factory.getStaticVulScanner(sink.getType());
        if (scanner != null) {
            scanner.scan(sink, event, invokeIdSequencer);
        } else if (EngineManager.TAINT_POOL.isNotEmpty()) {
            if (jdbcImpl != null && EngineManager.isLogined()) {
                scanner = factory.getOverPowerVulScanner();
                scanner.scan(jdbcImpl.readSql(), jdbcImpl.readParams());
            } else {
                scanner = factory.getDynamicVulScanner();
                scanner.scan(sink, event, invokeIdSequencer);
            }
        }
    }

    /**
     * 对sink点数据进行预处理
     *
     * @param sink
     * @param event
     * @param jdbcImpl
     * @return
     */
    public static boolean preScan(IASTSinkModel sink, MethodEvent event, IJdbc jdbcImpl) {
        boolean setJdbcImpl = false;
        if (DynamicPropagatorScanner.isRedirectVuln(sink.getType(), event.signature)) {
            AuthInfoManager.handleSetCookieAction(event.argumentArray[0], event.argumentArray[1]);
        } else if (isSqlOverPower(sink.getType())) {
            String signature = sink.getSignature();
            for (IJdbc jdbc : JDBC_IMPLS) {
                if (jdbc.matchJdbc(signature)) {
                    jdbcImpl = jdbc;
                    setJdbcImpl = true;
                    break;
                }
            }
            if (setJdbcImpl) {
                jdbcImpl.setEvent(event);
                LoginLogicRecognize.handleLoginLogicRecognize(event.javaClassName, jdbcImpl.readSql());
            }
        }
        return setJdbcImpl;
    }

    /**
     * sql-over-power
     *
     * @param vulType 漏洞类型
     * @return 是否为sql越权查询语句
     */
    private static boolean isSqlOverPower(String vulType) {
        return VulnType.SQL_OVER_POWER.equals(vulType);
    }

    static {
        JDBC_IMPLS.add(new MySqlImpl());
        JDBC_IMPLS.add(new SqlServerImpl());
        JDBC_IMPLS.add(new PostgresImpl());
        JDBC_IMPLS.add(new OracleImpl());
    }
}
