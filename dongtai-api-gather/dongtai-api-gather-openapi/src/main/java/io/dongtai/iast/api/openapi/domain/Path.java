package io.dongtai.iast.api.openapi.domain;

import java.util.List;

/**
 * 表示Open Api中的一个路径对象
 *
 * @author CC11001100
 * @since v1.12.0
 */
public class Path extends Reference {

    // 路径对应的各种方法
    private Operation get;
    private Operation put;
    private Operation post;
    private Operation delete;
    private Operation options;
    private Operation head;
    private Operation patch;
    private Operation trace;

    // 洞态对OpenAPI规范的扩展的自定义的字段，这一部分是不属于open api规范中的
    private Operation dubbo;

    // 所有方法的公共参数
    private List<Parameter> parameters;

    /**
     * 把另一个映射合并到当前的映射中，这个合并只会进行方法级别的合并
     */
    public void merge(Path other) {

        if (other == null) {
            return;
        }

        if (other.get != null && this.get == null) {
            this.get = other.get;
        }

        if (other.put != null && this.put == null) {
            this.put = other.put;
        }

        if (other.post != null && this.post == null) {
            this.post = other.post;
        }

        if (other.delete != null && this.delete == null) {
            this.delete = other.delete;
        }

        if (other.options != null && this.options == null) {
            this.options = other.options;
        }

        if (other.head != null && this.head == null) {
            this.head = other.head;
        }

        if (other.patch != null && this.patch == null) {
            this.patch = other.patch;
        }

        if (other.trace != null && this.trace == null) {
            this.trace = other.trace;
        }

        // TODO 2023-6-19 14:20:14 是否需要合并参数呢？如果要合并的需要考虑去重

    }

    public Operation getGet() {
        return get;
    }

    public void setGet(Operation get) {
        this.get = get;
    }

    public Operation getPut() {
        return put;
    }

    public void setPut(Operation put) {
        this.put = put;
    }

    public Operation getPost() {
        return post;
    }

    public void setPost(Operation post) {
        this.post = post;
    }

    public Operation getDelete() {
        return delete;
    }

    public void setDelete(Operation delete) {
        this.delete = delete;
    }

    public Operation getOptions() {
        return options;
    }

    public void setOptions(Operation options) {
        this.options = options;
    }

    public Operation getHead() {
        return head;
    }

    public void setHead(Operation head) {
        this.head = head;
    }

    public Operation getPatch() {
        return patch;
    }

    public void setPatch(Operation patch) {
        this.patch = patch;
    }

    public Operation getTrace() {
        return trace;
    }

    public void setTrace(Operation trace) {
        this.trace = trace;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public Operation getDubbo() {
        return dubbo;
    }

    public void setDubbo(Operation dubbo) {
        this.dubbo = dubbo;
    }
}
