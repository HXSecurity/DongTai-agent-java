package io.dongtai.iast.core.handler.hookpoint.models;


/**
 * 危险方法策略模型
 *
 * @author dongzhiyong@huoxian.cn
 */
public class IastSinkModel {

    /**
     * sink点的漏洞类型
     */
    private String type;

    /**
     * sink点污点所在的参数位置
     */
    private int[] position;

    /**
     * sink点对应的标识
     */
    private String signature;

    /**
     * 标记是否需要 track
     *
     * @param signature
     * @param type
     * @param position
     */
    private boolean track;

    IastSinkModel(String signature, String type, int[] position, String track) {
        this.signature = signature;
        this.type = type;
        this.position = position;
        this.track = "true".equals(track);
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public int[] getPos() {
        return position;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return this.signature;
    }

    public boolean isTrack() {
        return track;
    }
}
