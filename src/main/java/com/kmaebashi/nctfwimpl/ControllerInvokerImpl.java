package com.kmaebashi.nctfwimpl;
import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.ControllerInvoker;
import com.kmaebashi.nctfw.InternalException;
import com.kmaebashi.nctfw.NotFoundException;
import com.kmaebashi.nctfw.RequestContext;
import com.kmaebashi.nctfw.RoutingResult;
import com.kmaebashi.nctfw.ThrowableFunction;

public class ControllerInvokerImpl implements ControllerInvoker {
    private RequestContext context;

    public ControllerInvokerImpl(RequestContext context) {
        this.context = context;
    }

    public RoutingResult invoke(ThrowableFunction<RequestContext, RoutingResult> logic) {
        RoutingResult ret;
        try {
            ret = logic.apply(this.context);
        } catch (BadRequestException | NotFoundException ex) {
            this.context.getLogger().info("Controller ex.." + ex);
            throw ex;
        } catch (Exception ex) {
            this.context.getLogger().error("コントローラーの呼び出しでエラーが発生しました。\n" + ex.toString());
            throw new InternalException("コントローラーの呼び出しでエラーが発生しました。", ex);
        }
        return ret;
    }
}
