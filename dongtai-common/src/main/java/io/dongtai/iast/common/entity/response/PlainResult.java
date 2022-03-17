package io.dongtai.iast.common.entity.response;

/**
 * 简单返回结果
 *
 * @author chenyi
 * @date 2022/3/17
 */
public class PlainResult<T> extends BaseResult {
    private static final long serialVersionUID = 6831478245821138990L;

    /**
     * 调用返回的数据
     */
    private T data;

    /**
     * @return the data
     */
    public T getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(T data) {
        this.data = data;
    }
}
