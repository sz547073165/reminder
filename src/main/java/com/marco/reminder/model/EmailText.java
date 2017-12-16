package com.marco.reminder.model;

/**
 * 邮件内容对象
 */
public class EmailText {
    /**
     * 邮件内容对象，在Redis中存储的key
     */
    private String key;
    /**
     * 内容主体
     */
    private String text;
    /**
     * 发送状态<br>
     * 未发送：0<br>
     * 已发送：1<br>
     */
    private Integer status;

    private EmailText() {

    }

    public EmailText(String key, String text) {
        this.setKey(key);
        this.setText(text);
        this.setStatus(1);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
