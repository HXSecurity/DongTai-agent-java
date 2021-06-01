package com.secnium.iast.core.enhance.plugins.framework.tomcat;

import com.secnium.iast.core.enhance.IastContext;
import com.secnium.iast.core.enhance.plugins.AbstractClassVisitor;
import com.secnium.iast.core.enhance.plugins.DispatchPlugin;
import com.secnium.iast.core.enhance.plugins.core.adapter.PropagateAdviceAdapter;
import com.secnium.iast.core.util.AsmUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;
import com.secnium.iast.core.util.LogUtils;

/**
 * @author dongzhiyong@huoxian.cn
 */
public class DispatchRecyclePlugin implements DispatchPlugin {

    private final String outputBuffer = " org/apache/catalina/connector/OutputBuffer".substring(1);
    private final String byteChunkClass = " org/apache/tomcat/util/buf/ByteChunk".substring(1);
    private final String charChunkClass = " org/apache/tomcat/util/buf/CharChunk".substring(1);
    private final String requestClass = " org/apache/coyote/Request".substring(1);
    private String className;

    @Override
    public ClassVisitor dispatch(ClassVisitor classVisitor, IastContext context) {
        //
        if (isCharChunk(context)) {
            classVisitor = new CharChunkVisitor(classVisitor, context);
        } else if (isByteChunk(context)) {
            classVisitor = new ByteChunkVisitor(classVisitor, context);
        } else if (isRequest(context)) {
            classVisitor = new RequestVisitor(classVisitor, context);
        }
        className = context.getClassName();
        String matchClassname = isMatch();
        if (null != matchClassname) {
            context.setMatchClassname(matchClassname);
            classVisitor = new OutputStreamAdapter(classVisitor, context);
        }
        return classVisitor;
    }

    @Override
    public String isMatch() {
        if (outputBuffer.equals(className)) {
            return outputBuffer;
        }
        return null;
    }

    private boolean isCharChunk(IastContext iastContext) {
        return isMatch(charChunkClass, iastContext);
    }


    private boolean isByteChunk(IastContext iastContext) {
        return isMatch(byteChunkClass, iastContext);
    }


    private boolean isRequest(IastContext iastContext) {
        return isMatch(requestClass, iastContext);
    }


    private boolean isMatch(String paramString, IastContext iastContext) {
        return paramString.equals(iastContext.getClassName());
    }


    public static class OutputStreamAdapter extends AbstractClassVisitor {
        private final Logger logger = LogUtils.getLogger(getClass());

        public OutputStreamAdapter(ClassVisitor classVisitor, IastContext context) {
            super(classVisitor, context);
        }

        @Override
        public boolean hasTransformed() {
            return false;
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

            if (match(name)) {
                String iastMethodSignature = AsmUtils.buildSignature(context.getMatchClassname(), name, desc);
                String framework = "outputclose";
                mv = new PropagateAdviceAdapter(mv, access, name, desc, context, framework, iastMethodSignature);
                if (logger.isDebugEnabled()) {
                    logger.debug("rewrite method {} for listener[id={},class={}]", iastMethodSignature, context.getListenId(), context.getClassName());
                }
                transformed = true;
            }
            return mv;
        }

        protected boolean match(String name) {

            return "close".equals(name);
        }
    }
}
