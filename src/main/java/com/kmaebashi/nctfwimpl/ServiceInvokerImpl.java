package com.kmaebashi.nctfwimpl;

import com.kmaebashi.nctfw.BadRequestException;
import com.kmaebashi.nctfw.InternalException;
import com.kmaebashi.nctfw.InvokerOption;
import com.kmaebashi.nctfw.NotFoundException;
import com.kmaebashi.nctfw.ServiceContext;
import com.kmaebashi.nctfw.ServiceInvoker;
import com.kmaebashi.nctfw.ThrowableFunction;
import com.kmaebashi.simplelogger.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class ServiceInvokerImpl implements ServiceInvoker {
    private ServiceContext context;

    public ServiceInvokerImpl(ServiceContext context) {
        this.context = context;
    }

    @Override
    public <R> R invoke(ThrowableFunction<ServiceContext, R> logic, InvokerOption... options) {
        Logger logger = this.context.getLogger();
        R ret = null;
        if (Util.containsOption(options, InvokerOption.TRANSACTIONAL)) {
            Connection conn = ((DbAccessInvokerImpl)this.context.getDbAccessInvoker()).getContext().getConnection();
            try {
                conn.setAutoCommit(false);
                ret = doLogic(logic, logger);
                conn.commit();
            } catch (Exception ex) {
                try {
                    logger.error("Serviceでエラーが発生しました。\n" + ex.toString());
                    conn.rollback();
                    if (ex instanceof BadRequestException ex2) {
                        logger.info("Service ex2.." + ex2);
                        throw ex2;
                    } if (ex instanceof NotFoundException ex2) {
                        logger.info("Service ex2.." + ex2);
                        throw ex2;
                    } else {
                        throw new InternalException("Serviceでエラーが発生しました。", ex);
                    }
                } catch (SQLException ex2) {
                    throw new InternalException("rollback時にエラーが発生しました。", ex2);
                }
            }
        } else {
            try {
                ret = doLogic(logic, logger);
            } catch (BadRequestException | NotFoundException ex) {
                throw ex;
            } catch (Exception ex) {
                logger.error("Serviceでエラーが発生しました。\n" + ex.toString());
                throw new InternalException("Serviceでエラーが発生しました。", ex);
            }
        }
        return ret;
    }

    private <R> R doLogic(ThrowableFunction<ServiceContext, R> logic, Logger logger)
            throws Exception {
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        String methodName = ste[3].getClassName() + "." +ste[3].getMethodName();
        logger.info(methodName + " start.");
        R ret = logic.apply(this.context);
        logger.info(methodName + " end.");
        return ret;
    }
}
