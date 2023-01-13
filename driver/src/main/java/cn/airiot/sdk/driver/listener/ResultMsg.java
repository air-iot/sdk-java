package cn.airiot.sdk.driver.listener;

public class ResultMsg {
    private String message;

    public ResultMsg() {
        // TODO Auto-generated constructor stub
    }

    public ResultMsg(String message) {
        super();
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ResultMsg [message=" + message + "]";
    }

}
