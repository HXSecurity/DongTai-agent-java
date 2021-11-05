package com.secnium.iast.core.enhance.plugins.hardcoded;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.DispatchPlugin;
import com.secnium.iast.core.util.AsmUtils;
import com.secnium.iast.core.util.LogUtils;
import com.secnium.iast.core.util.commonUtils;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.slf4j.Logger;

/**
 * 检测字节码中使用硬编码的转换类
 *
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchHardcodedPlugin implements DispatchPlugin {

    private final Logger logger = LogUtils.getLogger(getClass());

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        classVisitor = new ExtractClassContent(classVisitor);
        return classVisitor;
    }

    @Override
    public String isMatch() {
        return null;
    }

    private class ExtractClassContent extends ClassVisitor {

        private String source;

        public ExtractClassContent(ClassVisitor classVisitor) {
            super(AsmUtils.api, classVisitor);
        }

        @Override
        public void visitSource(String source, String debug) {
            super.visitSource(source, debug);
            this.source = source;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            FieldVisitor fieldVisitor = super.visitField(access, name, desc, signature, value);
            if ("[B".equals(desc) && isKeysField(name)) {
                logger.trace("Source is {}" + this.source);
            } else if ("Ljava/lang/String;".equals(desc) && isStaticAndFinal(access) && isPassField(name)
                    && !isWrongPrefix(name) && value instanceof String) {
                String fieldName = (String) value;
                if (!commonUtils.isEmpty(fieldName) && !valueMatcher(fieldName)) {
                    logger.trace("Source is " + this.source);
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
            for (String array : arrays) {
                if (commonUtils.subContain(name, array)) {
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
        private final String[] passArray = {"password", "passkey", "passphrase", "secret"};
        private final String[] notPrefixes = {"date", "forgot", "form", "encode", "pattern", "prefix", "prop", "suffix",
                "url"};

    }
}
