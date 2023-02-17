package cn.airiot.sdk.client.context;


import java.util.Stack;

/**
 * 请求上下文
 */
public class RequestContext {

    private final static ThreadLocal<Stack<ContextData>> RT_CONTEXT = ThreadLocal.withInitial(() -> {
        Stack<ContextData> stack = new Stack<>();
        stack.push(new ContextData());
        return stack;
    });

    public static void push() {
        ContextData data = new ContextData(RT_CONTEXT.get().firstElement());
        RT_CONTEXT.get().push(data);
    }

    public static void pop() {
        if (RT_CONTEXT.get().size() == 1) {
            throw new IllegalStateException("cannot pop the last request context");
        }

        RT_CONTEXT.get().pop();
    }

    public static void setProjectId(String projectId) {
        ContextData data = RT_CONTEXT.get().firstElement();
        data.projectId = projectId;
    }

    public static String getProjectId() {
        return RT_CONTEXT.get().firstElement().projectId;
    }

    public static void disableAuth() {
        ContextData data = RT_CONTEXT.get().firstElement();
        data.disableAuth = true;
    }

    public static void enableAuth() {
        ContextData data = RT_CONTEXT.get().firstElement();
        data.disableAuth = false;
    }

    public static boolean isAuthEnabled() {
        return !RT_CONTEXT.get().firstElement().disableAuth;
    }

    public static void disableTakeProject() {
        ContextData data = RT_CONTEXT.get().firstElement();
        data.takeProject = false;
    }

    public static void enableTakeProject() {
        ContextData data = RT_CONTEXT.get().firstElement();
        data.takeProject = true;
    }

    public static boolean isTakeProject() {
        return RT_CONTEXT.get().firstElement().takeProject;
    }
}
