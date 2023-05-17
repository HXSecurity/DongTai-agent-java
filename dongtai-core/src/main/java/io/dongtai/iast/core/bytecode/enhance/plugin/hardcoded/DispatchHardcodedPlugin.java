package io.dongtai.iast.core.bytecode.enhance.plugin.hardcoded;

import com.alibaba.fastjson2.JSONObject;
import io.dongtai.iast.common.constants.*;
import io.dongtai.iast.common.utils.base64.Base64Encoder;
import io.dongtai.iast.core.EngineManager;
import io.dongtai.iast.core.bytecode.enhance.ClassContext;
import io.dongtai.iast.core.bytecode.enhance.plugin.AbstractClassVisitor;
import io.dongtai.iast.core.bytecode.enhance.plugin.DispatchPlugin;
import io.dongtai.iast.core.handler.hookpoint.models.policy.Policy;
import io.dongtai.iast.core.service.ThreadPools;
import io.dongtai.iast.core.utils.commonUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

/**
 * 检测字节码中使用硬编码的转换类
 *
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchHardcodedPlugin implements DispatchPlugin {


    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, ClassContext context, Policy policy) {
        if (!context.isBootstrapClassLoader()) {
            classVisitor = new ExtractClassContent(classVisitor, context);
            return classVisitor;
        }
        return classVisitor;
    }

    @Override
    public String getName() {
        return "hardcode";
    }

    private class ExtractClassContent extends AbstractClassVisitor {

        private String source;

        public ExtractClassContent(ClassVisitor classVisitor, ClassContext context) {
            super(classVisitor, context);
        }

        @Override
        public void visitSource(String source, String debug) {
            super.visitSource(source, debug);
            this.source = source;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            FieldVisitor fieldVisitor = super.visitField(access, name, desc, signature, value);
            if (null != value) {
                if ("[B".equals(desc) && isKeysField(name)) {
                    sendVulReport(source, context.getClassName(), context.isBootstrapClassLoader(), name,
                            Base64Encoder.encodeBase64String((byte[]) value));
                } else if ("Ljava/lang/String;".equals(desc) && isStaticAndFinal(access) && isPassField(name)
                        && !isWrongPrefix(name) && value instanceof String) {
                    String fieldValue = (String) value;
                    if (!commonUtils.isEmpty(fieldValue) && !valueMatcher(fieldValue)) {
                        sendVulReport(source, context.getClassName(), context.isBootstrapClassLoader(), name,
                                fieldValue);
                    }
                }
            }
            return fieldVisitor;
        }

        private boolean isStaticAndFinal(int access) {
            return (Modifier.isStatic(access) && Modifier.isFinal(access));
        }

        private boolean isPassField(String name) {
            return containArrayItem(name, passArray);
        }

        private boolean isWrongPrefix(String name) {
            return containArrayItem(name, notPrefixes);
        }

        private boolean isKeysField(String name) {
            return containArrayItem(name, keyArray);
        }

        private boolean containArrayItem(String name, String[] arrays) {
            name = name.toUpperCase();
            for (String item : arrays) {
                if (name.equals(item)) {
                    return true;
                }
            }
            return false;
        }

        private boolean valueMatcher(String value) {
            return e.matcher(value).find() || f.matcher(value).find();
        }

        private final Pattern e = Pattern.compile("^[a-zA-Z]+\\.[\\.a-zA-Z]*[a-zA-Z]+$");

        private final Pattern f = Pattern.compile("^[a-zA-Z]+\\_[\\_a-zA-Z]*[a-zA-Z]+$");

        private final String[] keyArray = {"key", "aes", "des", "iv", "secret", "blowfish"};
        private final String[] passArray = {"PASSWORD", "PASSKEY", "PASSPHRASE", "SECRET", "ACCESS_TOKEN",
                "AWS_ACCESS_KEY_ID", "AWS_SECRET_ACCESS_KEY"};
        private final String[] notPrefixes = {"date", "forgot", "form", "encode", "pattern", "prefix", "prop", "suffix",
                "url"};

        private void sendVulReport(String fileName, String className, boolean isJDKClass, String fieldName,
                                   String value) {
            JSONObject report = new JSONObject();
            JSONObject detail = new JSONObject();
            report.put(ReportKey.TYPE, ReportType.VUL_HARDCODE);
            report.put(ReportKey.DETAIL, detail);
            detail.put(ReportKey.AGENT_ID, EngineManager.getAgentId());
            detail.put("file", fileName);
            detail.put("class", className);
            detail.put("isJdk", isJDKClass);
            detail.put("field", fieldName);
            detail.put("value", value);
            ThreadPools.sendPriorityReport(ApiPath.REPORT_UPLOAD, report.toString());
        }
    }
}
